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

package org.apache.isis.core.runtime.memento;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

/**
 * Holds the state for the specified object in serializable form.
 * 
 * <p>
 * This object is {@link Serializable} and can be passed over the network
 * easily. Also for a persistent objects only the reference's {@link Oid}s are
 * held, avoiding the need for serializing the whole object graph.
 */
public class Memento implements Serializable {

    private final static Logger LOG = LoggerFactory.getLogger(Memento.class);
    private final static long serialVersionUID = 1L;

    private final static OidMarshaller OID_MARSHALLER = OidMarshaller.INSTANCE;

    private final List<Oid> transientObjects = Lists.newArrayList();


    private Data data;


    ////////////////////////////////////////////////
    // constructor, Encodeable
    ////////////////////////////////////////////////

    public Memento(final ObjectAdapter adapter) {
        data = adapter == null ? null : createData(adapter);
        LOG.debug("created memento for {}", this);
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
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(adapter);
        final Data[] collData = new Data[facet.size(adapter)];
        int i = 0;
        for (final ObjectAdapter ref : facet.iterable(adapter)) {
            collData[i++] = createReferenceData(ref);
        }
        final String elementTypeSpecName = adapter.getSpecification().getFullIdentifier();
        return new CollectionData(clone(adapter.getOid()), elementTypeSpecName, collData);
    }

    private ObjectData createObjectData(final ObjectAdapter adapter) {
        final Oid adapterOid = clone(adapter.getOid());
        transientObjects.add(adapterOid);
        final ObjectSpecification cls = adapter.getSpecification();
        final List<ObjectAssociation> associations = cls.getAssociations(Contributed.EXCLUDED);
        final ObjectData data = new ObjectData(adapterOid, cls.getFullIdentifier());
        for (int i = 0; i < associations.size(); i++) {
            if (associations.get(i).isNotPersisted()) {
                if (associations.get(i).isOneToManyAssociation()) {
                    continue;
                }
                if (associations.get(i).containsFacet(PropertyOrCollectionAccessorFacet.class) && !associations.get(i).containsFacet(PropertySetterFacet.class)) {
                    LOG.debug("ignoring not-settable field {}", associations.get(i).getName());
                    continue;
                }
            }
            createAssociationData(adapter, data, associations.get(i));
        }
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

        Oid refOid = clone(referencedAdapter.getOid());

        if (refOid == null) {
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
        final String oidStr = oid.enString();
        return (T) OID_MARSHALLER.unmarshal(oidStr, oid.getClass());
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
                getSpecificationLoader().loadSpecification(data.getClassName());

        ObjectAdapter adapter;
        
        final Oid oid = getOid();
		if (spec.isParentedOrFreeCollection()) {

            final Object recreatedPojo = getPersistenceSession().instantiateAndInjectServices(spec);
        	adapter = getPersistenceSession().mapRecreatedPojo(oid, recreatedPojo);
            populateCollection(adapter, (CollectionData) data);
            
        } else {
        	Assert.assertTrue("oid must be a RootOid representing an object because spec is not a collection and cannot be a value", oid instanceof RootOid);
        	RootOid typedOid = (RootOid) oid;

            // remove adapter if already in the adapter manager maps, because
            // otherwise would (as a side-effect) update the version to that of the current.
            adapter = getPersistenceSession().getAdapterFor(typedOid);
            if(adapter != null) {
                getPersistenceSession().removeAdapter(adapter);
            }

            // recreate an adapter for the original OID (with correct version)
            adapter = getPersistenceSession().adapterFor(typedOid);

            updateObject(adapter, data);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("recreated object {}", adapter.getOid());
        }
        return adapter;
    }



    private void populateCollection(final ObjectAdapter collectionAdapter, final CollectionData state) {
        final ObjectAdapter[] initData = new ObjectAdapter[state.elements.length];
        int i = 0;
        for (final Data elementData : state.elements) {
            initData[i++] = recreateReference(elementData);
        }
        final CollectionFacet facet = collectionAdapter.getSpecification().getFacet(CollectionFacet.class);
        facet.init(collectionAdapter, initData);
    }

    private ObjectAdapter recreateReference(final Data data) {

        // handle values
        if (data instanceof StandaloneData) {
            final StandaloneData standaloneData = (StandaloneData) data;
            return standaloneData.getAdapter();
        }
        
        // reference to entity
        
        Oid oid = data.getOid();
        Assert.assertTrue("can only create a reference to an entity", oid instanceof RootOid);
        
		final RootOid rootOid = (RootOid) oid;
        final ObjectAdapter referencedAdapter = getPersistenceSession().adapterFor(rootOid);

        if (data instanceof ObjectData) {
            if (rootOid.isTransient()) {
        		updateObject(referencedAdapter, data);
        	}
        }
        return referencedAdapter;
    }

    
    ////////////////////////////////////////////////
    // helpers
    ////////////////////////////////////////////////
    
    private void updateObject(final ObjectAdapter adapter, final Data data) {
        final Object oid = adapter.getOid();
        if (oid != null && !oid.equals(data.getOid())) {
            throw new IllegalArgumentException("This memento can only be used to update the ObjectAdapter with the Oid " + data.getOid() + " but is " + oid);
        } 
        if (!(data instanceof ObjectData)) {
            throw new IsisException("Expected an ObjectData but got " + data.getClass());
        }
        
        updateFieldsAndResolveState(adapter, data);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("object updated {}", adapter.getOid());
        }
    }

    private void updateFieldsAndResolveState(final ObjectAdapter objectAdapter, final Data data) {
        
        boolean dataIsTransient = data.getOid().isTransient();
        
        if (!dataIsTransient) {
            updateFields(objectAdapter, data);
            objectAdapter.getOid().setVersion(data.getOid().getVersion());
        } else if (objectAdapter.isTransient() && dataIsTransient) {
            updateFields(objectAdapter, data);
            
        } else if (objectAdapter.isParentedCollection()) {
            // this branch is kind-a wierd, I think it's to handle aggregated adapters.
            updateFields(objectAdapter, data);
            
        } else {
            final ObjectData od = (ObjectData) data;
            if (od.containsField()) {
                throw new IsisException("Resolve state (for " + objectAdapter + ") inconsistent with fact that data exists for fields");
            }
        }
    }

    private void updateFields(final ObjectAdapter object, final Data state) {
        final ObjectData od = (ObjectData) state;
        final List<ObjectAssociation> fields = object.getSpecification().getAssociations(Contributed.EXCLUDED);
        for (final ObjectAssociation field : fields) {
            if (field.isNotPersisted()) {
                if (field.isOneToManyAssociation()) {
                    continue;
                }
                if (field.containsFacet(PropertyOrCollectionAccessorFacet.class) && !field.containsFacet(PropertySetterFacet.class)) {
                    LOG.debug("ignoring not-settable field {}", field.getName());
                    continue;
                }
            }
            updateField(object, od, field);
        }
    }

    private void updateField(final ObjectAdapter objectAdapter, final ObjectData objectData, final ObjectAssociation objectAssoc) {
        final Object fieldData = objectData.getEntry(objectAssoc.getId());

        if (objectAssoc.isOneToManyAssociation()) {
            updateOneToManyAssociation(objectAdapter, (OneToManyAssociation) objectAssoc, (CollectionData) fieldData);

        } else if (objectAssoc.getSpecification().containsFacet(EncodableFacet.class)) {
            final EncodableFacet facet = objectAssoc.getSpecification().getFacet(EncodableFacet.class);
            final ObjectAdapter value = facet.fromEncodedString((String) fieldData);
            ((OneToOneAssociation) objectAssoc).initAssociation(objectAdapter, value);

        } else if (objectAssoc.isOneToOneAssociation()) {
            updateOneToOneAssociation(objectAdapter, (OneToOneAssociation) objectAssoc, (Data) fieldData);
        }
    }

    private void updateOneToManyAssociation(final ObjectAdapter objectAdapter, final OneToManyAssociation otma, final CollectionData collectionData) {
        final ObjectAdapter collection = otma.get(objectAdapter, InteractionInitiatedBy.FRAMEWORK);
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
        final List<ObjectAdapter> original = Lists.newArrayList();
        for (final ObjectAdapter adapter : facet.iterable(collection)) {
            original.add(adapter);
        }

        final Data[] elements = collectionData.elements;
        for (final Data data : elements) {
            final ObjectAdapter elementAdapter = recreateReference(data);
            if (!facet.contains(collection, elementAdapter)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  association {} changed, added {}", otma, elementAdapter.getOid());
                }
                otma.addElement(objectAdapter, elementAdapter, InteractionInitiatedBy.FRAMEWORK);
            } else {
                otma.removeElement(objectAdapter, elementAdapter, InteractionInitiatedBy.FRAMEWORK);
            }
        }

        for (final ObjectAdapter element : original) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("  association {} changed, removed {}", otma, element.getOid());
            }
            otma.removeElement(objectAdapter, element, InteractionInitiatedBy.FRAMEWORK);
        }
    }

    private void updateOneToOneAssociation(final ObjectAdapter objectAdapter, final OneToOneAssociation otoa, final Data assocData) {
        if (assocData == null) {
            otoa.initAssociation(objectAdapter, null);
        } else {
            final ObjectAdapter ref = recreateReference(assocData);
            if (otoa.get(objectAdapter, InteractionInitiatedBy.FRAMEWORK) != ref) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  association {} changed to {}", otoa, ref.getOid());
                }
                otoa.initAssociation(objectAdapter, ref);
            }
        }
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
        return getIsisSessionFactory().getSpecificationLoader();
    }

    protected PersistenceSession getPersistenceSession() {
        return getIsisSessionFactory().getCurrentSession().getPersistenceSession();
    }

    IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

}
