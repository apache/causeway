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
import java.util.stream.Stream;

import org.apache.isis.commons.exceptions.UnknownTypeException;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;

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

    private final List<Oid> transientObjects = _Lists.newArrayList();


    private Data data;


    ////////////////////////////////////////////////
    // constructor, Encodeable
    ////////////////////////////////////////////////

    public Memento(final ObjectAdapter adapter) {
        data = adapter == null ? null : createData(adapter);
        log.debug("created memento for {}", this);
    }


    ////////////////////////////////////////////////
    // createData
    ////////////////////////////////////////////////

    private Data createData(final ObjectAdapter adapter) {
        if (adapter.getSpecification().isParentedOrFreeCollection() && !adapter.getSpecification().isEncodeable()) {
            return createCollectionData(adapter);
        } else {
            return createObjectData(adapter);
        }
    }

    private Data createCollectionData(final ObjectAdapter adapter) {
        
        final Data[] collData = CollectionFacet.Utils.streamAdapters(adapter)
            .map(ref->createReferenceData(ref))
            .collect(_Arrays.toArray(Data.class, CollectionFacet.Utils.size(adapter)));
        
        final String elementTypeSpecName = adapter.getSpecification().getFullIdentifier();
        return new CollectionData(clone(adapter.getOid()), elementTypeSpecName, collData);
    }

    private ObjectData createObjectData(final ObjectAdapter adapter) {
        final Oid adapterOid = clone(adapter.getOid());
        transientObjects.add(adapterOid);
        final ObjectSpecification cls = adapter.getSpecification();
        final ObjectData data = new ObjectData(adapterOid, cls.getFullIdentifier());
        
        final Stream<ObjectAssociation> associations = cls.streamAssociations(Contributed.EXCLUDED);
        
        associations
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

    private void createAssociationData(final ObjectAdapter adapter, final ObjectData data, final ObjectAssociation objectAssoc) {
        Object assocData;
        if (objectAssoc.isOneToManyAssociation()) {
            final ObjectAdapter collAdapter = objectAssoc.get(adapter, InteractionInitiatedBy.FRAMEWORK);
            assocData = createCollectionData(collAdapter);
        } else if (objectAssoc.getSpecification().isEncodeable()) {
            final EncodableFacet facet = objectAssoc.getSpecification().getFacet(EncodableFacet.class);
            final ObjectAdapter value = objectAssoc.get(adapter, InteractionInitiatedBy.FRAMEWORK);
            assocData = facet.toEncodedString(value);
        } else if (objectAssoc.isOneToOneAssociation()) {
            final ObjectAdapter referencedAdapter = objectAssoc.get(adapter, InteractionInitiatedBy.FRAMEWORK);
            assocData = createReferenceData(referencedAdapter);
        } else {
            throw new UnknownTypeException(objectAssoc);
        }
        data.addField(objectAssoc.getId(), assocData);
    }

    private Data createReferenceData(final ObjectAdapter referencedAdapter) {
        if (referencedAdapter == null) {
            return null;
        }

        final Oid refOid = clone(referencedAdapter.getOid());

        if (refOid == null || refOid.isValue()) {
            return createStandaloneData(referencedAdapter);
        }


        if (    (referencedAdapter.getSpecification().isParented() || refOid.isTransient()) &&
                !transientObjects.contains(refOid)) {
            transientObjects.add(refOid);
            return createObjectData(referencedAdapter);
        }

        final String specification = referencedAdapter.getSpecification().getFullIdentifier();
        return new Data(refOid, specification);
    }

    private static <T extends Oid> T clone(final T oid) {
        if(oid == null) { return null; }
        return _Casts.uncheckedCast(oid.copy()); 
    }

    private Data createStandaloneData(final ObjectAdapter adapter) {
        return new StandaloneData(adapter);
    }

    ////////////////////////////////////////////////
    // properties
    ////////////////////////////////////////////////

    public Oid getOid() {
        return data.getOid();
    }

    protected Data getData() {
        return data;
    }

    ////////////////////////////////////////////////
    // recreateObject
    ////////////////////////////////////////////////

    public ObjectAdapter recreateObject() {
        if (data == null) {
            return null;
        }
        final ObjectSpecification spec =
                getSpecificationLoader().lookupBySpecIdElseLoad(ObjectSpecId.of(data.getClassName()));
        
        final Oid oid = getOid();

        return IsisContext.getPersistenceSession().get().
                adapterOfMemento(spec, oid, data);
        //return IsisSession.currentOrElseNull().adapterOfMemento(spec, oid, data);
                
    }


    // ///////////////////////////////////////////////////////////////
    // toString, debug
    // ///////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "[" + (data == null ? null : data.getClassName() + "/" + data.getOid() + data) + "]";
    }

    // ///////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////////////

    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}
