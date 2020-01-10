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

package org.apache.isis.viewer.wicket.viewer.services.mementos;

import java.util.Set;

import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.commons.internal.collections._Arrays;
import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * converts a {@link ManagedObject} to serializable {@link Data} 
 * 
 * @since 2.0
 */
@Log4j2
final class ObjectMarshaller {

    Data toData(ManagedObject adapter) {
        val data = (adapter == null) ? null : createData(adapter);
        log.debug("created memento for {}", this);
        return data;
    }
    
    // -- HELPER
    
    private final transient Set<Oid> oids = _Sets.newHashSet();

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
        
        return new CollectionData(elementOid, collData);
    }

    private ObjectData createObjectData(ManagedObject adapter) {
        val oid = ManagedObject._identify(adapter);
        oids.add(oid);
        val spec = adapter.getSpecification();
        val data = new ObjectData(oid);

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

        if (refOid == null) {
            throw _Exceptions.unexpectedCodeReach();
        }
        
        if (refOid.isValue()) {
            return new StandaloneData(refOid, referencedAdapter);
        }

        val refSpec = referencedAdapter.getSpecification();
        
        if (refSpec.isParented() || refOid.isTransient()) {
            
            if(!oids.contains(refOid)) {
                oids.add(refOid);
                return createObjectData(referencedAdapter);    
            }
        }

        return new Data(refOid);
    }


}
