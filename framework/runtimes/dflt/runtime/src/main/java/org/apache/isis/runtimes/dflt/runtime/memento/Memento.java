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

package org.apache.isis.runtimes.dflt.runtime.memento;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.encoding.DataInputStreamExtended;
import org.apache.isis.core.commons.encoding.DataOutputStreamExtended;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistorUtil;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionHydrator;

/**
 * Holds the state for the specified object in serializable form.
 * 
 * <p>
 * This object is {@link Serializable} and can be passed over the network
 * easily. Also for a persistent objects only the reference's {@link Oid}s are
 * held, avoiding the need for serializing the whole object graph.
 */
public class Memento implements Serializable {

    private final static long serialVersionUID = 1L;
    private final static Logger LOG = Logger.getLogger(Memento.class);

    private final List<Oid> transientObjects = Lists.newArrayList();

    private Data state;


    ////////////////////////////////////////////////
    // constructor, Encodeable
    ////////////////////////////////////////////////

    public Memento(final ObjectAdapter object) {
        state = object == null ? null : createData(object);
        if (LOG.isDebugEnabled()) {
            LOG.debug("created memento for " + this);
        }
    }

    
    ////////////////////////////////////////////////
    // createData
    ////////////////////////////////////////////////

    private Data createData(final ObjectAdapter object) {
        if (object.getSpecification().isParentedOrFreeCollection()) {
            return createCollectionData(object);
        } else {
            return createObjectData(object);
        }
    }

    private Data createCollectionData(final ObjectAdapter object) {
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(object);
        final Data[] collData = new Data[facet.size(object)];
        int i = 0;
        for (final ObjectAdapter ref : facet.iterable(object)) {
            collData[i++] = createReferenceData(ref);
        }
        final String elementTypeSpecName = object.getSpecification().getFullIdentifier();
        return new CollectionData(object.getOid(), object.getResolveState(), elementTypeSpecName, collData);
    }

    private ObjectData createObjectData(final ObjectAdapter adapter) {
        transientObjects.add(adapter.getOid());
        final ObjectSpecification cls = adapter.getSpecification();
        final List<ObjectAssociation> fields = cls.getAssociations();
        final ObjectData data = new ObjectData(adapter.getOid(), adapter.getResolveState().name(), cls.getFullIdentifier());
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).isNotPersisted()) {
                if (fields.get(i).isOneToManyAssociation()) {
                    continue;
                }
                if (fields.get(i).containsFacet(PropertyOrCollectionAccessorFacet.class) && !fields.get(i).containsFacet(PropertySetterFacet.class)) {
                    LOG.debug("ignoring not-settable field " + fields.get(i).getName());
                    continue;
                }
            }
            createFieldData(adapter, data, fields.get(i));
        }
        return data;
    }

    private void createFieldData(final ObjectAdapter object, final ObjectData data, final ObjectAssociation field) {
        Object fieldData;
        if (field.isOneToManyAssociation()) {
            final ObjectAdapter coll = field.get(object);
            fieldData = createCollectionData(coll);
        } else if (field.getSpecification().isEncodeable()) {
            final EncodableFacet facet = field.getSpecification().getFacet(EncodableFacet.class);
            final ObjectAdapter value = field.get(object);
            fieldData = facet.toEncodedString(value);
        } else if (field.isOneToOneAssociation()) {
            final ObjectAdapter ref = ((OneToOneAssociation) field).get(object);
            fieldData = createReferenceData(ref);
        } else {
            throw new UnknownTypeException(field);
        }
        data.addField(field.getId(), fieldData);
    }

    private Data createReferenceData(final ObjectAdapter ref) {
        if (ref == null) {
            return null;
        }

        final Oid refOid = ref.getOid();
        if (refOid == null) {
            return createStandaloneData(ref);
        }

        if ((ref.getSpecification().isParented() || refOid.isTransient()) && !transientObjects.contains(refOid)) {
            transientObjects.add(refOid);
            return createObjectData(ref);
        }

        final String resolvedState = ref.getResolveState().name();
        final String specification = ref.getSpecification().getFullIdentifier();
        return new Data(refOid, resolvedState, specification);

    }

    private Data createStandaloneData(final ObjectAdapter adapter) {
        return new StandaloneData(adapter);
    }

    ////////////////////////////////////////////////
    // properties
    ////////////////////////////////////////////////

    public Oid getOid() {
        return state.getOid();
    }

    protected Data getData() {
        return state;
    }
    
    ////////////////////////////////////////////////
    // recreateObject
    ////////////////////////////////////////////////

    public ObjectAdapter recreateObject() {
        if (state == null) {
            return null;
        }
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(state.getClassName());

        ObjectAdapter adapter;
        ResolveState targetState;
        if (getOid().isTransient()) {
            adapter = getHydrator().recreateAdapter(spec, getOid());
            targetState = ResolveState.SERIALIZING_TRANSIENT;
        } else {
            adapter = getHydrator().recreateAdapter(spec, getOid());
            targetState = ResolveState.UPDATING;
        }
        
        if (adapter.getSpecification().isParentedOrFreeCollection()) {
            populateCollection(adapter, (CollectionData) state, targetState);
        } else {
            updateObject(adapter, state, targetState);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("recreated object " + adapter.getOid());
        }
        return adapter;
    }

    private void populateCollection(final ObjectAdapter collection, final CollectionData state, final ResolveState targetState) {
        final ObjectAdapter[] initData = new ObjectAdapter[state.elements.length];
        int i = 0;
        for (final Data elementData : state.elements) {
            initData[i++] = recreateReference(elementData);
        }
        final CollectionFacet facet = collection.getSpecification().getFacet(CollectionFacet.class);
        facet.init(collection, initData);
    }

    private ObjectAdapter recreateReference(final Data data) {
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(data.getClassName());

        if (data instanceof StandaloneData) {
            final StandaloneData standaloneData = (StandaloneData) data;
            return standaloneData.getAdapter();
        } else {
            final Oid oid = data.getOid();
            if (oid == null) {
                return null;
            }
            ObjectAdapter ref;
            ref = getHydrator().recreateAdapter(spec, oid);
            if (data instanceof ObjectData) {
                if (oid.isTransient() || spec.isParented()) {
                    final ResolveState resolveState = spec.isParented() ? ResolveState.GHOST : ResolveState.TRANSIENT;
                    if (ref.getResolveState().isValidToChangeTo(resolveState)) {
                        ref.changeState(resolveState);
                    }
                    updateObject(ref, data, resolveState);
                }
            }
            return ref;
        }
    }

    
    ////////////////////////////////////////////////
    // updateObject
    ////////////////////////////////////////////////
    
    /**
     * Updates the specified object (assuming it is the correct object for this
     * memento) with the state held by this memento.
     * 
     * @throws IllegalArgumentException
     *             if the memento was created from different logical object to
     *             the one specified (i.e. its oid differs).
     */
    public void updateObject(final ObjectAdapter object) {
        updateObject(object, state, ResolveState.RESOLVING);
    }

    private void updateObject(final ObjectAdapter object, final Data state, final ResolveState resolveState) {
        final Object oid = object.getOid();
        if (oid != null && !oid.equals(state.getOid())) {
            throw new IllegalArgumentException("This memento can only be used to update the ObjectAdapter with the Oid " + state.getOid() + " but is " + oid);

        } else {
            if (!(state instanceof ObjectData)) {
                throw new IsisException("Expected an ObjectData but got " + state.getClass());
            } else {
                updateObject(object, resolveState, state);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("object updated " + object.getOid());
            }
        }

    }

    private void updateObject(final ObjectAdapter objectAdapter, final ResolveState targetResolveState, final Data state) {
        if (objectAdapter.getResolveState().isValidToChangeTo(targetResolveState)) {
            PersistorUtil.startStateTransition(objectAdapter, targetResolveState);
            updateFields(objectAdapter, state);
            PersistorUtil.endStateTransition(objectAdapter);
        } else if (objectAdapter.isTransient() && targetResolveState == ResolveState.TRANSIENT) {
            updateFields(objectAdapter, state);
        } else if (objectAdapter.isParented()) {
            updateFields(objectAdapter, state);
        } else {
            final ObjectData od = (ObjectData) state;
            if (od.containsField()) {
                throw new IsisException("Resolve state (for " + objectAdapter + ") inconsistent with fact that data exists for fields");
            }
        }
    }

    private void updateFields(final ObjectAdapter object, final Data state) {
        final ObjectData od = (ObjectData) state;
        final List<ObjectAssociation> fields = object.getSpecification().getAssociations();
        for (final ObjectAssociation field : fields) {
            if (field.isNotPersisted()) {
                if (field.isOneToManyAssociation()) {
                    continue;
                }
                if (field.containsFacet(PropertyOrCollectionAccessorFacet.class) && !field.containsFacet(PropertySetterFacet.class)) {
                    LOG.debug("ignoring not-settable field " + field.getName());
                    continue;
                }
            }
            updateField(object, od, field);
        }
    }

    private void updateField(final ObjectAdapter object, final ObjectData od, final ObjectAssociation field) {
        final Object fieldData = od.getEntry(field.getId());

        if (field.isOneToManyAssociation()) {
            updateOneToManyAssociation(object, (OneToManyAssociation) field, (CollectionData) fieldData);

        } else if (field.getSpecification().containsFacet(EncodableFacet.class)) {
            final EncodableFacet facet = field.getSpecification().getFacet(EncodableFacet.class);
            final ObjectAdapter value = facet.fromEncodedString((String) fieldData);
            ((OneToOneAssociation) field).initAssociation(object, value);

        } else if (field.isOneToOneAssociation()) {
            updateOneToOneAssociation(object, (OneToOneAssociation) field, (Data) fieldData);
        }
    }

    private void updateOneToManyAssociation(final ObjectAdapter object, final OneToManyAssociation field, final CollectionData collectionData) {
        final ObjectAdapter collection = field.get(object);
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
        final List<ObjectAdapter> original = Lists.newArrayList();
        for (final ObjectAdapter adapter : facet.iterable(collection)) {
            original.add(adapter);
        }

        final Data[] elements = collectionData.elements;
        for (final Data data : elements) {
            final ObjectAdapter element = recreateReference(data);
            if (!facet.contains(collection, element)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  association " + field + " changed, added " + element.getOid());
                }
                field.addElement(object, element);
            } else {
                field.removeElement(object, element);
            }
        }

        for (final ObjectAdapter element : original) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("  association " + field + " changed, removed " + element.getOid());
            }
            field.removeElement(object, element);
        }
    }

    private void updateOneToOneAssociation(final ObjectAdapter object, final OneToOneAssociation field, final Data fieldData) {
        if (fieldData == null) {
            field.initAssociation(object, null);
        } else {
            final ObjectAdapter ref = recreateReference(fieldData);
            if (field.get(object) != ref) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  association " + field + " changed to " + ref.getOid());
                }
                field.initAssociation(object, ref);
            }
        }
    }
    
    ////////////////////////////////////////////////
    // encode, restore
    ////////////////////////////////////////////////

    public void encodedData(final DataOutputStreamExtended outputImpl) throws IOException {
        outputImpl.writeEncodable(state);
    }
    
    public void restore(final DataInputStreamExtended input) throws IOException {
        state = input.readEncodable(Data.class);
    }


    public static Memento recreateFrom(DataInputStreamExtended input) throws IOException {
        final Memento memento = new Memento(null);
        memento.restore(input);
        return memento;
    }


    // ///////////////////////////////////////////////////////////////
    // toString, debug
    // ///////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "[" + (state == null ? null : state.getClassName() + "/" + state.getOid() + state) + "]";
    }

    public void debug(final DebugBuilder debug) {
        if (state != null) {
            state.debug(debug);
        }
    }

    // ///////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////////////

    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected PersistenceSessionHydrator getHydrator() {
        return getPersistenceSession();
    }


}
