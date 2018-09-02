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
package org.apache.isis.core.runtime.system.persistence.adaptermanager;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.ensure.IsisAssertException;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.memento.CollectionData;
import org.apache.isis.core.runtime.memento.Data;
import org.apache.isis.core.runtime.memento.ObjectData;
import org.apache.isis.core.runtime.memento.StandaloneData;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.schema.common.v1.CollectionDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;
import org.apache.isis.schema.utils.CommonDtoUtils;

/**
 * Interim class, expected to be removed with https://issues.apache.org/jira/browse/ISIS-1976 
 */
public class ObjectAdapterLegacy {
    
    private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterLegacy.class);
    
    public static ObjectAdapterContext openContext(ServicesInjector servicesInjector) {
        final ObjectAdapterContext objectAdapterContext = new ObjectAdapterContext(servicesInjector);
        objectAdapterContext.open();
        return objectAdapterContext;
    }
    
    public static interface ObjectAdapterProvider extends Supplier<ObjectAdapter> {
        
    }
    
    public static class ObjectAdapterContext /*implements AdapterManager*/ {
        private final PojoAdapterHashMap pojoAdapterMap = new PojoAdapterHashMap();
        private final OidAdapterHashMap oidAdapterMap = new OidAdapterHashMap();
        private final ServicesInjector servicesInjector;
        
        private ObjectAdapterContext(ServicesInjector servicesInjector) {
            this.servicesInjector = servicesInjector;
        }

        // -- LIFE-CYCLING
        
        public void open() {
            oidAdapterMap.open();
            pojoAdapterMap.open();
        }
        
        public void close() {
            
            try {
                oidAdapterMap.close();
            } catch(final Throwable ex) {
                // ignore
                LOG.error("close: oidAdapterMap#close() failed; continuing to avoid memory leakage");
            }

            try {
                pojoAdapterMap.close();
            } catch(final Throwable ex) {
                // ignore
                LOG.error("close: pojoAdapterMap#close() failed; continuing to avoid memory leakage");
            }
        }

        public boolean containsPojoAdapter(Object pojo) {
            return pojoAdapterMap.containsPojo(pojo);
        }
        
        public ObjectAdapter lookupPojoAdapter(Object pojo) {
            return pojoAdapterMap.getAdapter(pojo);
        }

        public ObjectAdapter lookupOidAdapter(Oid oid) {
            return oidAdapterMap.getAdapter(oid);
        }
        
        public void removeAdapter(ObjectAdapter adapter) {
            final Oid oid = adapter.getOid();
            if (oid != null) {
                oidAdapterMap.remove(oid);
            }
            pojoAdapterMap.remove(adapter);
        }
        
        // ------------------------------------------------------------------------------------------------
        
        public void addAdapterHonoringSpecImmutability(Object pojo, ObjectAdapter adapter) {
            // add all aggregated collections
            final ObjectSpecification objSpec = adapter.getSpecification();
            if (!adapter.isParentedCollection() || adapter.isParentedCollection() && !objSpec.isImmutable()) {
                pojoAdapterMap.add(pojo, adapter);
            }

            // order is important - add to pojo map first, then identity map
            oidAdapterMap.add(adapter.getOid(), adapter);
        }
        
        /**
         * was in PersisenceSessionX, temporarily moved here to successfully compile
         *
         * <p>
         * Note that there is no management of {@link Version}s here. That is
         * because the {@link PersistenceSession5} is expected to manage this.
         *
         * @param hintRootOid - allow a different persistent root oid to be provided.
         * @param session 
         */
        public void remapAsPersistent(final ObjectAdapter adapter, RootOid hintRootOid, PersistenceSession session) {

            final ObjectAdapter rootAdapter = adapter.getAggregateRoot();  // TODO: REVIEW: think this is redundant; would seem this method is only ever called for roots anyway.
            final RootOid transientRootOid = (RootOid) rootAdapter.getOid();

            // no longer true, because isTransient now looks directly at the underlying pojo's state (for entities)
            // and doesn't apply for services.
            //        Ensure.ensureThatArg(rootAdapter.isTransient(), is(true), "root adapter should be transient; oid:" + transientRootOid);
            //        Ensure.ensureThatArg(transientRootOid.isTransient(), is(true), "root adapter's OID should be transient; oid:" + transientRootOid);

            final RootAndCollectionAdapters rootAndCollectionAdapters = new RootAndCollectionAdapters(adapter, session);

            LOG.debug("remapAsPersistent: {}", transientRootOid);
            LOG.debug("removing root adapter from oid map");

            boolean removed = oidAdapterMap.remove(transientRootOid);
            if (!removed) {
                LOG.warn("could not remove oid: {}", transientRootOid);
                // should we fail here with a more serious error?
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("removing collection adapter(s) from oid map");
            }
            for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
                final Oid collectionOid = collectionAdapter.getOid();
                removed = oidAdapterMap.remove(collectionOid);
                if (!removed) {
                    LOG.warn("could not remove collectionOid: {}", collectionOid);
                    // should we fail here with a more serious error?
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("updating the Oid");
            }

            // intended for testing (bit nasty)
            final RootOid persistedRootOid;
            if(hintRootOid != null) {
                if(hintRootOid.isTransient()) {
                    throw new IsisAssertException("hintRootOid must be persistent");
                }
                final ObjectSpecId hintRootOidObjectSpecId = hintRootOid.getObjectSpecId();
                final ObjectSpecId adapterObjectSpecId = adapter.getSpecification().getSpecId();
                if(!hintRootOidObjectSpecId.equals(adapterObjectSpecId)) {
                    throw new IsisAssertException("hintRootOid's objectType must be same as that of adapter " +
                            "(was: '" + hintRootOidObjectSpecId + "'; adapter's is " + adapterObjectSpecId + "'");
                }
                // ok
                persistedRootOid = hintRootOid;
            } else {
                // normal flow - delegate to OidGenerator to obtain a persistent root oid
                persistedRootOid = session.createPersistentOrViewModelOid(adapter.getObject());
            }

            // associate root adapter with the new Oid, and remap
            if (LOG.isDebugEnabled()) {
                LOG.debug("replacing Oid for root adapter and re-adding into maps; oid is now: {} (was: {})", persistedRootOid.enString(), transientRootOid.enString());
            }
            adapter.replaceOid(persistedRootOid);
            oidAdapterMap.add(persistedRootOid, adapter);

            // associate the collection adapters with new Oids, and re-map
            if (LOG.isDebugEnabled()) {
                LOG.debug("replacing Oids for collection adapter(s) and re-adding into maps");
            }
            for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
                final ParentedCollectionOid previousCollectionOid = (ParentedCollectionOid) collectionAdapter.getOid();
                final ParentedCollectionOid persistedCollectionOid = previousCollectionOid.asPersistent(persistedRootOid);
                oidAdapterMap.add(persistedCollectionOid, collectionAdapter);
            }


            // some object store implementations may replace collection instances (eg ORM may replace with a cglib-enhanced
            // proxy equivalent.  So, ensure that the collection adapters still wrap the correct pojos.
            if (LOG.isDebugEnabled()) {
                LOG.debug("synchronizing collection pojos, remapping in pojo map if required");
            }
            for (final OneToManyAssociation otma : rootAndCollectionAdapters.getCollections()) {
                final ObjectAdapter collectionAdapter = rootAndCollectionAdapters.getCollectionAdapter(otma);

                final Object collectionPojoWrappedByAdapter = collectionAdapter.getObject();
                final Object collectionPojoActuallyOnPojo = getCollectionPojo(otma, adapter);

                if (collectionPojoActuallyOnPojo != collectionPojoWrappedByAdapter) {
                    pojoAdapterMap.remove(collectionAdapter);
                    collectionAdapter.replacePojo(collectionPojoActuallyOnPojo);
                    pojoAdapterMap.add(collectionPojoActuallyOnPojo, collectionAdapter);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("made persistent {}; was {}", adapter, transientRootOid);
            }
        }
        
        private static Object getCollectionPojo(final OneToManyAssociation association, final ObjectAdapter ownerAdapter) {
            final PropertyOrCollectionAccessorFacet accessor = association.getFacet(PropertyOrCollectionAccessorFacet.class);
            return accessor.getProperty(ownerAdapter, InteractionInitiatedBy.FRAMEWORK);
        }




       
    }
    
    // -- CommandExecutorServiceDefault --------------------------------------------------------
    
    public static class __CommandExecutorServiceDefault {

        public static ObjectAdapter adapterFor(Object targetObject) {
            if(targetObject instanceof OidDto) {
                final OidDto oidDto = (OidDto) targetObject;
                return adapterFor(oidDto);
            }
            if(targetObject instanceof CollectionDto) {
                final CollectionDto collectionDto = (CollectionDto) targetObject;
                final List<ValueDto> valueDtoList = collectionDto.getValue();
                final List<Object> pojoList = Lists.newArrayList();
                for (final ValueDto valueDto : valueDtoList) {
                    ValueType valueType = collectionDto.getType();
                    final Object valueOrOidDto = CommonDtoUtils.getValue(valueDto, valueType);
                    // converting from adapter and back means we handle both
                    // collections of references and of values
                    final ObjectAdapter objectAdapter = adapterFor(valueOrOidDto);
                    Object pojo = objectAdapter != null ? objectAdapter.getObject() : null;
                    pojoList.add(pojo);
                }
                return adapterFor(pojoList);
            }
            if(targetObject instanceof Bookmark) {
                final Bookmark bookmark = (Bookmark) targetObject;
                return adapterFor(bookmark);
            }
            return getPersistenceSession().adapterFor(targetObject);
        }
        
        private static ObjectAdapter adapterFor(final OidDto oidDto) {
            final Bookmark bookmark = Bookmark.from(oidDto);
            return adapterFor(bookmark);
        }

        private static ObjectAdapter adapterFor(final Bookmark bookmark) {
            final RootOid rootOid = RootOid.create(bookmark);
            return adapterFor(rootOid);
        }

        private static ObjectAdapter adapterFor(final RootOid rootOid) {
            return getPersistenceSession().adapterFor(rootOid);
        }

        private static PersistenceSession getPersistenceSession() {
            return IsisContext.getPersistenceSession().orElseThrow(_Exceptions::unexpectedCodeReach);
        }
        
    }
    
    // -- Memento --------------------------------------------------------

    public static class __Memento {
        
        public static ObjectAdapter recreateObject(ObjectSpecification spec, Oid oid, Data data) {
            ObjectAdapter adapter;
            
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

                ObjectAdapterLegacy.__Memento.updateObject(adapter, data);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("recreated object {}", adapter.getOid());
            }
            return adapter;
        }

        private static ObjectAdapter recreateReference(Data data) {
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
        
        private static void updateObject(final ObjectAdapter adapter, final Data data) {
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
        
        private static void populateCollection(final ObjectAdapter collectionAdapter, final CollectionData state) {
            final ObjectAdapter[] initData = new ObjectAdapter[state.getElements().length];
            int i = 0;
            for (final Data elementData : state.getElements()) {
                initData[i++] = recreateReference(elementData);
            }
            final CollectionFacet facet = collectionAdapter.getSpecification().getFacet(CollectionFacet.class);
            facet.init(collectionAdapter, initData);
        }
        
        private static void updateFieldsAndResolveState(final ObjectAdapter objectAdapter, final Data data) {

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
        
        private static void updateFields(final ObjectAdapter object, final Data state) {
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

        private static void updateField(final ObjectAdapter objectAdapter, final ObjectData objectData, final ObjectAssociation objectAssoc) {
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

        private static void updateOneToManyAssociation(final ObjectAdapter objectAdapter, final OneToManyAssociation otma, final CollectionData collectionData) {
            final ObjectAdapter collection = otma.get(objectAdapter, InteractionInitiatedBy.FRAMEWORK);
            final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
            final List<ObjectAdapter> original = _Lists.newArrayList();
            for (final ObjectAdapter adapter : facet.iterable(collection)) {
                original.add(adapter);
            }

            final Data[] elements = collectionData.getElements();
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

        private static void updateOneToOneAssociation(final ObjectAdapter objectAdapter, final OneToOneAssociation otoa, final Data assocData) {
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
        
        private static PersistenceSession getPersistenceSession() {
            return IsisContext.getPersistenceSession().orElseThrow(_Exceptions::unexpectedCodeReach);
        }


        
    }
    
    

}
