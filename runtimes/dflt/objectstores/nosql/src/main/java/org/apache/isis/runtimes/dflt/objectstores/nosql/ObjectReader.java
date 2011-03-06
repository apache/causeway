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


package org.apache.isis.runtimes.dflt.objecstores.nosql;

import java.util.List;

import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationContainer;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManager;

class ObjectReader {

    public ObjectAdapter load(StateReader reader, KeyCreator keyCreator, VersionCreator versionCreator) {
        String className = reader.readObjectType();
        ObjectSpecification specification = IsisContext.getSpecificationLoader().loadSpecification(className);
        String id = reader.readId();
        Oid oid = keyCreator.oid(id);
        
        ObjectAdapter object = getAdapter(specification, oid);
        if (object.getResolveState().isResolved()) {
            Version version = null;
            String versionString = reader.readVersion();
            if (!versionString.equals("")) {
                String user = reader.readUser();
                String time = reader.readTime();
                version = versionCreator.version(versionString, user, time);
            }
            if (version.different(object.getVersion())) {
                // TODO - do we need to CHECK version and update
                throw new UnexpectedCallException();
            } else { 
                return object;
            }
        }
        
        // TODO move lock to common method
//        object.setOptimisticLock(version);
        loadState(reader, keyCreator, versionCreator, object);
        return object;
    }
    
    public void update(StateReader reader, KeyCreator keyCreator, VersionCreator versionCreator, ObjectAdapter object) {
        loadState(reader, keyCreator, versionCreator, object);
    }

    private void loadState(StateReader reader, KeyCreator keyCreator, VersionCreator versionCreator, ObjectAdapter object) {
        ResolveState resolveState = ResolveState.RESOLVING;
        object.changeState(resolveState);
        Version version = null;
        String versionString = reader.readVersion();
        if (!versionString.equals("")) {
            String user = reader.readUser();
            String time = reader.readTime();
            version = versionCreator.version(versionString, user, time);
        }
        readFields(reader, object, keyCreator);
        object.setOptimisticLock(version);
        object.changeState(resolveState.getEndState());
    }

    private void readFields(StateReader reader, ObjectAdapter object, KeyCreator keyCreator) {
        ObjectAssociationContainer specification = object.getSpecification();
        List<ObjectAssociation> associations = specification.getAssociations();
        for (ObjectAssociation association : associations) {
            if (association.isNotPersisted()) {
                continue;
            }
            if (association.isOneToManyAssociation()) {
               readCollection(reader, keyCreator, (OneToManyAssociation) association, object);
            } else if (association.getSpecification().isValue()) {
                readValue(reader, (OneToOneAssociation) association, object);
            } else if (association.getSpecification().isAggregated()) {
                readAggregate(reader, keyCreator, (OneToOneAssociation) association, object);
            } else {
                readReference(reader, keyCreator, (OneToOneAssociation) association, object);
            }
        }
    }
    
    private void readAggregate(StateReader reader, KeyCreator keyCreator, OneToOneAssociation association, ObjectAdapter object) {
       String id = association.getId();
       StateReader aggregateReader = reader.readAggregate(id);
       if (aggregateReader != null) {
           AggregatedOid oid = new AggregatedOid(object.getOid(), id);
           ObjectAdapter fieldObject = restoreAggregatedObject(aggregateReader, oid, keyCreator);
           association.initAssociation(object, fieldObject);
       } else {
           association.initAssociation(object, null);
       }
    }

    private ObjectAdapter restoreAggregatedObject(StateReader aggregateReader, Oid oid, KeyCreator keyCreator) {
           String objectType = aggregateReader.readObjectType();
           ObjectSpecification specification = IsisContext.getSpecificationLoader().loadSpecification(objectType);
           ObjectAdapter fieldObject = getAdapter(specification, oid);
           ResolveState resolveState = ResolveState.RESOLVING;
           fieldObject.changeState(resolveState);
           readFields(aggregateReader, fieldObject, keyCreator);
           fieldObject.changeState(resolveState.getEndState());
        return fieldObject;
    }

    private void readValue(StateReader reader, OneToOneAssociation association, ObjectAdapter object) {
        String fieldData = reader.readField(association.getId());
        if (fieldData != null) { 
            if (fieldData.equals("null")) { 
                association.initAssociation(object, null);
            } else { 
                EncodableFacet encodeableFacet = association.getSpecification().getFacet(EncodableFacet.class); 
                ObjectAdapter value = encodeableFacet.fromEncodedString(fieldData); 
                association.initAssociation(object, value); 
            } 
        }
    }

    private void readReference(StateReader reader, KeyCreator keyCreator, OneToOneAssociation association, ObjectAdapter object) {
        ObjectAdapter fieldObject;
        String ref = reader.readField(association.getId());
        if (ref == null || ref.equals("null")) {
            fieldObject = null;
        } else {
            Oid oid = keyCreator.oidFromReference(ref);
            ObjectSpecification specification = keyCreator.specificationFromReference(ref);
            fieldObject = getAdapter(specification, oid);
        }
        association.initAssociation(object, fieldObject);
    }
    
    private void readCollection(StateReader reader, KeyCreator keyCreator, OneToManyAssociation association, ObjectAdapter object) {
        ObjectAdapter collection = association.get(object);
        CollectionFacet facet = collection.getSpecification().getFacet(CollectionFacet.class);
        if (association.getSpecification().isAggregated()) {
            List<StateReader> readers = reader.readCollection(association.getId());
            String id = association.getId();
            ObjectAdapter[] elements = new ObjectAdapter[readers.size()];
            int i = 0;
            for (StateReader elementReader : readers) {
                AggregatedOid oid = new AggregatedOid(object.getOid(), id, i);
                elements[i++] = restoreAggregatedObject(elementReader, oid, keyCreator);
            }
            facet.init(collection, elements);
        } else {
            String referencesList = reader.readField(association.getId());
            if (referencesList == null || referencesList.length() == 0) {
                facet.init(collection, new ObjectAdapter[0]);
            } else {
                ObjectAdapter[] elements = restoreElements(referencesList, keyCreator);
                facet.init(collection, elements);
            }
        }
    }

    private ObjectAdapter[] restoreElements(String referencesList, KeyCreator keyCreator) {
        String[] references = referencesList.split("\\|");
        ObjectAdapter[] elements = new ObjectAdapter[references.length];
        for (int i = 0; i < references.length; i++) {
            ObjectSpecification specification = keyCreator.specificationFromReference(references[i]);
            Oid oid = keyCreator.oidFromReference(references[i]);
            elements[i] = getAdapter(specification, oid);
        }
        return elements;
    }        

    protected ObjectAdapter getAdapter(final ObjectSpecification specification, final Oid oid) {
        AdapterManager objectLoader = IsisContext.getPersistenceSession().getAdapterManager();
        ObjectAdapter adapter = objectLoader.getAdapterFor(oid);
        if (adapter != null) {
            return adapter;
        } else {
            return IsisContext.getPersistenceSession().recreateAdapter(oid, specification);
        }
    }
}


