/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.objectstore.nosql;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationContainer;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.Persistor;
import org.apache.isis.objectstore.nosql.db.StateReader;
import org.apache.isis.objectstore.nosql.encryption.DataEncryption;
import org.apache.isis.objectstore.nosql.keys.KeyCreatorDefault;
import org.apache.isis.objectstore.nosql.versions.VersionCreator;

public class ObjectReader {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectReader.class);
    
    private final KeyCreatorDefault keyCreator = new KeyCreatorDefault();

    public ObjectAdapter load(final StateReader reader, final VersionCreator versionCreator, final Map<String, DataEncryption> dataEncrypters) {
        
        final String oidStr = reader.readOid();
        final RootOid rootOid = getOidMarshaller().unmarshal(oidStr, RootOid.class);
        
        final ObjectAdapter adapter = getAdapter(rootOid);
        if (adapter.isResolved()) {
            Version version = null;
            final String versionString = reader.readVersion();
            if (!versionString.equals("")) {
                final String user = reader.readUser();
                final String time = reader.readTime();
                version = versionCreator.version(versionString, user, time);
            }
            if (version.different(adapter.getVersion())) {
                // TODO - do we need to CHECK version and update
                LOG.warn("while reading data into " + oidStr + " version was " + version + " when existing adapter was already " + adapter.getVersion());
            }
            
        } else {
            
            // TODO move lock to common method
            // object.setOptimisticLock(version);
            loadState(reader, versionCreator, dataEncrypters, adapter);
        }

        return adapter;
    }

    public void update(final StateReader reader, final VersionCreator versionCreator, final Map<String, DataEncryption> dataEncrypters, final ObjectAdapter object) {
        loadState(reader, versionCreator, dataEncrypters, object);
    }

    private void loadState(final StateReader reader, final VersionCreator versionCreator, final Map<String, DataEncryption> dataEncrypters, final ObjectAdapter object) {
        final ResolveState resolveState = ResolveState.RESOLVING;
        object.changeState(resolveState);
        Version version = null;
        final String versionString = reader.readVersion();
        if (!versionString.equals("")) {
            final String user = reader.readUser();
            final String time = reader.readTime();
            version = versionCreator.version(versionString, user, time);
        }
        final String encryptionType = reader.readEncrytionType();
        readFields(reader, object, dataEncrypters.get(encryptionType));
        object.setVersion(version);
        object.changeState(resolveState.getEndState());
    }

    private void readFields(final StateReader reader, final ObjectAdapter object, final DataEncryption dataEncrypter) {
        final ObjectAssociationContainer specification = object.getSpecification();
        final List<ObjectAssociation> associations = specification.getAssociations(Contributed.EXCLUDED);
        for (final ObjectAssociation association : associations) {
            if (association.isNotPersisted()) {
                continue;
            }
            if (association.isOneToManyAssociation()) {
                readCollection(reader, dataEncrypter, (OneToManyAssociation) association, object);
            } else if (association.getSpecification().isValue()) {
                readValue(reader, dataEncrypter, (OneToOneAssociation) association, object);
            } else if (association.getSpecification().isParented()) {
                readAggregate(reader, dataEncrypter, (OneToOneAssociation) association, object);
            } else {
                readReference(reader, (OneToOneAssociation) association, object);
            }
        }
    }

    private void readAggregate(final StateReader reader, final DataEncryption dataEncrypter, final OneToOneAssociation association, final ObjectAdapter parentAdapter) {
        final String id = association.getId();
        final StateReader aggregateReader = reader.readAggregate(id);
        
        final ObjectAdapter fieldObject;
        if (aggregateReader != null) {
            final String oidStr = aggregateReader.readOid();
            final AggregatedOid aggregatedOid = getOidMarshaller().unmarshal(oidStr, AggregatedOid.class);
            fieldObject = restoreAggregatedObject(aggregateReader, aggregatedOid, dataEncrypter);
        } else {
            fieldObject = null;
        }
        
        association.initAssociation(parentAdapter, fieldObject);
    }

    private ObjectAdapter restoreAggregatedObject(final StateReader aggregateReader, final AggregatedOid aggregatedOid, final DataEncryption dataEncrypter) {
        final ObjectAdapter fieldObject = getAdapter(aggregatedOid);
        final ResolveState resolveState = ResolveState.RESOLVING;
        fieldObject.changeState(resolveState);
        readFields(aggregateReader, fieldObject, dataEncrypter);
        fieldObject.changeState(resolveState.getEndState());

        return fieldObject;
    }

    private void readValue(final StateReader reader, final DataEncryption dataEncrypter, final OneToOneAssociation association, final ObjectAdapter object) {
        final String fieldData = reader.readField(association.getId());
        if (fieldData != null) {
            if (fieldData.equals("null")) {
                association.initAssociation(object, null);
            } else {
                final EncodableFacet encodeableFacet = association.getSpecification().getFacet(EncodableFacet.class);
                final String decryptedData = dataEncrypter.decrypt(fieldData);
                final ObjectAdapter value = encodeableFacet.fromEncodedString(decryptedData);
                association.initAssociation(object, value);
            }
        }
    }

    private void readReference(final StateReader reader, final OneToOneAssociation association, final ObjectAdapter object) {
        ObjectAdapter fieldObject;
        final String ref = reader.readField(association.getId());
        if (ref == null || ref.equals("null")) {
            fieldObject = null;
        } else {
            if (ref.equals("")) {
                throw new NoSqlStoreException("Invalid reference field (an empty string) in data for " + association.getName() + "  in " + object);
            }
            final RootOid oid = keyCreator.unmarshal(ref);
            fieldObject = getAdapter(oid);
        }
        try {
            association.initAssociation(object, fieldObject);
        } catch (IllegalArgumentException e) {
            throw new NoSqlStoreException("Failed to process field data for " + association.getName() + "  in " + object + ": " + ref);
        }
    }

    private void readCollection(final StateReader reader, final DataEncryption dataEncrypter, final OneToManyAssociation association, final ObjectAdapter parentAdapter) {
        final ObjectAdapter collectionAdapter = association.get(parentAdapter);
        
        final CollectionFacet facet = collectionAdapter.getSpecification().getFacet(CollectionFacet.class);
        if (association.getSpecification().isParented()) {
            // were persisted inline, so read back inline
            final List<StateReader> readers = reader.readCollection(association.getId());
            final ObjectAdapter[] elementAdapters = new ObjectAdapter[readers.size()];
            int i = 0;
            for (final StateReader elementReader : readers) {
                
                final String oidStr = elementReader.readOid();
                final AggregatedOid aggregatedOid = getOidMarshaller().unmarshal(oidStr, AggregatedOid.class);
                
                elementAdapters[i++] = restoreAggregatedObject(elementReader, aggregatedOid, dataEncrypter);
            }
            facet.init(collectionAdapter, elementAdapters);
        } else {
            // were persisted as references, so read back as references
            final String referencesList = reader.readField(association.getId());
            if (referencesList == null || referencesList.length() == 0) {
                facet.init(collectionAdapter, new ObjectAdapter[0]);
            } else {
                final ObjectAdapter[] elements = restoreElements(referencesList);
                facet.init(collectionAdapter, elements);
            }
        }
    }

    private ObjectAdapter[] restoreElements(final String referencesList) {
        final String[] references = referencesList.split("\\|");
        final ObjectAdapter[] elements = new ObjectAdapter[references.length];
        for (int i = 0; i < references.length; i++) {
            
            // no longer used
            //final ObjectSpecification specification = keyCreator.specificationFromOidStr(references[i]);
            
            final RootOid oid = keyCreator.unmarshal(references[i]);
            elements[i] = getAdapter(oid);
        }
        return elements;
    }

    protected ObjectAdapter getAdapter(final TypedOid oid) {
        return getAdapterManager().adapterFor(oid);
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // dependencies (from context)
    ////////////////////////////////////////////////////////////////////////////
    
    protected Persistor getPersistenceSession() {
    	return IsisContext.getPersistenceSession();
    }
    
    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }

}
