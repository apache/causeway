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

package org.apache.isis.runtime.memento;

import java.io.Serializable;
import java.util.List;

import org.apache.isis.commons.exceptions.UnknownTypeException;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Holds the state for the specified object in serializable form.
 *
 * <p>
 * This object is {@link Serializable} and can be passed over the network
 * easily. Also for a persistent objects only the reference's {@link Oid}s are
 * held, avoiding the need for serializing the whole object graph.
 */
@Log4j2
public class Memento implements Serializable {

    private final static long serialVersionUID = 1L;

    private final List<Oid> oids = _Lists.newArrayList();

    @Getter private Data data;

    public Memento(ManagedObject adapter) {
        data = (adapter == null) ? null : createData(adapter);
        log.debug("created memento for {}", this);
    }
    
    public ObjectAdapter recreateObject(
            SpecificationLoader specLoader, 
            MementoStore mementoStore) {
        
        if (data == null) {
            return null;
        }
        val spec = specLoader.lookupBySpecIdElseLoad(ObjectSpecId.of(data.getClassName()));
        val oid = data.getOid();
        return mementoStore.adapterOfMemento(spec, oid, data);
    }

    @Override
    public String toString() {
        return "[" + (data == null ? null : data.getClassName() + "/" + data.getOid() + data) + "]";
    }
    
    // -- HELPER

    private Data createData(ManagedObject adapter) {
        if (adapter.getSpecification().isParentedOrFreeCollection() && 
                !adapter.getSpecification().isEncodeable()) {
            return createCollectionData(adapter);
        } else {
            return createObjectData(adapter);
        }
    }

    private Data createCollectionData(ManagedObject adapter) {

        final Data[] collData = CollectionFacet.Utils.streamAdapters(adapter)
                .map(this::createReferencedData)
                .collect(_Arrays.toArray(Data.class, CollectionFacet.Utils.size(adapter)));

        val elementOid = ManagedObject._identify(adapter);
        val elementSpec = adapter.getSpecification();
        
        return new CollectionData(elementOid, elementSpec.getFullIdentifier(), collData);
    }

    private ObjectData createObjectData(ManagedObject adapter) {
        val oid = ManagedObject._identify(adapter);
        oids.add(oid);
        val spec = adapter.getSpecification();
        val data = new ObjectData(oid, spec.getFullIdentifier());

        spec.streamAssociations(Contributed.EXCLUDED)
        .filter(association->{
            if (association.isNotPersisted()) {
                if (association.isOneToManyAssociation()) {
                    return false;
                }
                if (association.containsFacet(PropertyOrCollectionAccessorFacet.class) && 
                        !association.containsFacet(PropertySetterFacet.class)) {
                    log.debug("ignoring not-settable field {}", association.getName());
                    return false;
                }
            }
            return true;
        })
        .forEach(association->{
            createAssociationData(adapter, data, association);
        });

        return data;
    }

    private void createAssociationData(ManagedObject adapter, ObjectData data, ObjectAssociation objectAssoc) {
        Object assocData;
        if (objectAssoc.isOneToManyAssociation()) {
            val collAdapter = objectAssoc.get(adapter, InteractionInitiatedBy.FRAMEWORK);
            assocData = createCollectionData(collAdapter);
        } else if (objectAssoc.getSpecification().isEncodeable()) {
            val encodableFacet = objectAssoc.getSpecification().getFacet(EncodableFacet.class);
            val value = objectAssoc.get(adapter, InteractionInitiatedBy.FRAMEWORK);
            assocData = encodableFacet.toEncodedString(value);
        } else if (objectAssoc.isOneToOneAssociation()) {
            val referencedAdapter = objectAssoc.get(adapter, InteractionInitiatedBy.FRAMEWORK);
            assocData = createReferencedData(referencedAdapter);
        } else {
            throw new UnknownTypeException(objectAssoc);
        }
        data.addField(objectAssoc.getId(), assocData);
    }

    private Data createReferencedData(ManagedObject referencedAdapter) {
        if (referencedAdapter == null) {
            return null;
        }

        val refOid = ManagedObject._identify(referencedAdapter);

        if (refOid == null || refOid.isValue()) {
            return createStandaloneData(referencedAdapter);
        }

        val refSpec = referencedAdapter.getSpecification();
        
        if (refSpec.isParented() || refOid.isTransient()) {
            
            if(!oids.contains(refOid)) {
                oids.add(refOid);
                return createObjectData(referencedAdapter);    
            }
        }

        return new Data(refOid, refSpec.getFullIdentifier());
    }

    private Data createStandaloneData(ManagedObject adapter) {
        return new StandaloneData(adapter);
    }



}
