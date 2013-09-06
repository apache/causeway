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

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.DomainModelException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.nosql.db.StateWriter;
import org.apache.isis.objectstore.nosql.encryption.DataEncryption;
import org.apache.isis.objectstore.nosql.keys.KeyCreatorDefault;
import org.apache.isis.objectstore.nosql.versions.VersionCreator;

class WriteObjectCommand implements PersistenceCommand {
    
    public enum Mode {
        UPDATE,
        NON_UPDATE;
        
        public static Mode modeFor(boolean isUpdate) {
            return isUpdate?UPDATE:NON_UPDATE;
        }

        public boolean isUpdate() {
            return this == UPDATE;
        }
    }
    
    private final KeyCreatorDefault keyCreator = new KeyCreatorDefault();
    private final ObjectAdapter adapter;
    private final VersionCreator versionCreator;
    private final DataEncryption dataEncrypter;
    private final Mode mode;

    WriteObjectCommand(final Mode mode, final VersionCreator versionCreator, final DataEncryption dataEncrypter, final ObjectAdapter adapter) {
        this.mode = mode;
        this.versionCreator = versionCreator;
        this.dataEncrypter = dataEncrypter;
        this.adapter = adapter;
    }

    @Override
    public void execute(final PersistenceCommandContext context) {
        final NoSqlCommandContext noSqlCommandContext = (NoSqlCommandContext) context;
        
        final ObjectSpecification objectSpec = adapter.getSpecification();
        //final String specName = objectSpec.getFullIdentifier();
        final StateWriter writer = noSqlCommandContext.createStateWriter(objectSpec.getSpecId());
        
        //final String key = keyCreator.key(adapter.getOid());
        //writer.writeId(key);
        final TypedOid typedOid = (TypedOid) adapter.getOid();
        writer.writeOid(typedOid);
        
        writeFields(writer, adapter);
        final String user = getAuthenticationSession().getUserName();

        final Version currentVersion = adapter.getVersion();
        
        final Version newVersion = mode.isUpdate() ? versionCreator.nextVersion(currentVersion, user) : versionCreator.newVersion(user);
        adapter.setVersion(newVersion);
        if (newVersion != null) {
            final String version = currentVersion == null ? null : versionCreator.versionString(currentVersion);
            writer.writeVersion(version, versionCreator.versionString(newVersion));
            writer.writeUser(newVersion.getUser());
            writer.writeTime(versionCreator.timeString(newVersion));
            writer.writeEncryptionType(dataEncrypter.getType());
        }

        if (mode.isUpdate()) {
            noSqlCommandContext.update(writer);
        } else {
            noSqlCommandContext.insert(writer);
        }
    }

    private void writeFields(final StateWriter writer, final ObjectAdapter adapter) {
        
        final List<ObjectAssociation> associations = adapter.getSpecification().getAssociations(Contributed.EXCLUDED);
        
//        final String specName = adapter.getSpecification().getFullIdentifier();
//        writer.writeObjectType(specName);
        
        writer.writeOid((TypedOid) adapter.getOid());
        
        for (final ObjectAssociation association : associations) {
            if (association.isNotPersisted()) {
                continue;
            }
            final ObjectAdapter fieldAdapter = association.get(adapter);
            if (association.isOneToManyAssociation()) {
                final OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) association;
                final ObjectAdapter collectionAdapter = fieldAdapter; // to explain
                writeCollection(writer, oneToManyAssociation, collectionAdapter);
            } else { 
                final OneToOneAssociation oneToOneAssociation = (OneToOneAssociation) association;
                final ObjectAdapter propertyAdapter = fieldAdapter; // to explain
                writeProperty(writer, oneToOneAssociation, propertyAdapter);
            }
        }
    }

    private void writeProperty(final StateWriter writer, final OneToOneAssociation oneToOneAssociation, final ObjectAdapter propertyAdapter) {
        if (oneToOneAssociation.getSpecification().isValue()) {
            final ObjectAdapter valueAdapter = propertyAdapter; // to explain
            writeValueProperty(writer, oneToOneAssociation, valueAdapter);
        } else { 
            final ObjectAdapter referencedAdapter = propertyAdapter; // to explain 
            writeReferenceProperty(writer, oneToOneAssociation, referencedAdapter);
        }
    }

    private void writeValueProperty(final StateWriter writer, final OneToOneAssociation otoa, final ObjectAdapter valueAdapter) {
        String data;
        if (valueAdapter == null) {
            data = null;
        } else {
            final EncodableFacet encodeableFacet = valueAdapter.getSpecification().getFacet(EncodableFacet.class);
            data = encodeableFacet.toEncodedString(valueAdapter);
            data = dataEncrypter.encrypt(data);
        }
        writer.writeField(otoa.getId(), data);
    }

    private void writeReferenceProperty(final StateWriter writer, final OneToOneAssociation otoa, final ObjectAdapter referencedAdapter) {
        if (otoa.getSpecification().isParented()) {
            writeReferencedAsAggregated(writer, otoa, referencedAdapter);
        } else {
            writeReference(writer, otoa, referencedAdapter);
        }
    }

    private void writeReferencedAsAggregated(final StateWriter writer, final OneToOneAssociation otoa, final ObjectAdapter referencedAdapter) {
        if (referencedAdapter == null) {
            writer.writeField(otoa.getId(), null);
            return;
        } 
        final Oid referencedOid = referencedAdapter.getOid();
        if (!(referencedOid instanceof AggregatedOid)) {
            throw new NoSqlStoreException("Object type is inconsistent with it OID - it should have an AggregatedOid: " + referencedAdapter);
        } 
        final AggregatedOid aggregatedOid = (AggregatedOid) referencedOid;
        
        final String associationId = otoa.getId();
        final StateWriter aggregateWriter = writer.addAggregate(associationId);
        //aggregateWriter.writeId(aggregatedOid.getLocalId());
        aggregateWriter.writeOid(aggregatedOid);
        
        writeFields(aggregateWriter, referencedAdapter);
    }

    private void writeReference(final StateWriter writer, final ObjectAssociation association, final ObjectAdapter referencedAdapter) {
        final String key = keyCreator.oidStrFor(referencedAdapter);
        writer.writeField(association.getId(), key);
    }

    private void writeCollection(final StateWriter writer, final OneToManyAssociation association, final ObjectAdapter collectionAdapter) {
        if (association.getSpecification().isParented()) {
            writeCollectionOfAggregated(writer, association, collectionAdapter);
        } else {
            writeCollectionOfReferences(writer, association, collectionAdapter);
        }
    }

    private void writeCollectionOfAggregated(final StateWriter writer, final ObjectAssociation association, final ObjectAdapter collectionAdapter) {
        final List<StateWriter> elementWriters = Lists.newArrayList();
        final CollectionFacet collectionFacet = collectionAdapter.getSpecification().getFacet(CollectionFacet.class);
        for (final ObjectAdapter referencedAdapter : collectionFacet.iterable(collectionAdapter)) {
            final AggregatedOid elementOid = (AggregatedOid) referencedAdapter.getOid();
            final StateWriter elementWriter = writer.createElementWriter();
            
            //elementWriter.writeId(elementOid.getLocalId());
            elementWriter.writeOid(elementOid);
            
            writeFields(elementWriter, referencedAdapter);
            elementWriters.add(elementWriter);
        }
        writer.writeCollection(association.getId(), elementWriters);
    }

    private void writeCollectionOfReferences(final StateWriter writer, final ObjectAssociation association, final ObjectAdapter collectionAdapter) {
        final CollectionFacet collectionFacet = collectionAdapter.getSpecification().getFacet(CollectionFacet.class);
        
        final StringBuilder buf = new StringBuilder();
        for (final ObjectAdapter elementAdapter : collectionFacet.iterable(collectionAdapter)) {
            if (elementAdapter.isParented()) {
                throw new DomainModelException("Can't store an aggregated object within a collection that is not expected aggregates: " + elementAdapter + " (" + collectionAdapter + ")");
            }
            buf.append(keyCreator.oidStrFor(elementAdapter)).append("|");
        }
        if (buf.length() > 0) {
            writer.writeField(association.getId(), buf.toString());
        }
    }


    @Override
    public ObjectAdapter onAdapter() {
        return adapter;
    }

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        toString.append("spec", adapter.getSpecification().getFullIdentifier());
        toString.append("oid", adapter.getOid());
        return toString.toString();
    }
    
    
    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

}
