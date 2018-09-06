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
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.cache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.ensure.IsisAssertException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.memento.Data;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;

/**
 * Encapsulate ObjectAdpater life-cycling.  
 *  
 * @since 2.0.0-M2
 */
public class ObjectAdapterContext {
    
    private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterContext.class);
    
    public static ObjectAdapterContext openContext(
            ServicesInjector servicesInjector, 
            AuthenticationSession authenticationSession, 
            SpecificationLoader specificationLoader, 
            PersistenceSession persistenceSession) {
        final ObjectAdapterContext objectAdapterContext = 
                new ObjectAdapterContext(servicesInjector, authenticationSession, 
                        specificationLoader, persistenceSession);
        objectAdapterContext.open();
        return objectAdapterContext;
    }
    
    private final static class Cache {
        private final PojoAdapterHashMap pojoAdapterMap = new PojoAdapterHashMap();
        private final OidAdapterHashMap oidAdapterMap = new OidAdapterHashMap();
        
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
        
        private ObjectAdapter lookupAdapterByPojo(Object pojo) {
            final ObjectAdapter adapter = pojoAdapterMap.getAdapter(pojo);
            return adapter;
        }
        
        private void putPojo(Object pojo, ObjectAdapter adapter) {
            pojoAdapterMap.add(adapter.getObject(), adapter);
        }
        
        private void removePojo(ObjectAdapter adapter) {
            pojoAdapterMap.remove(adapter);
        }
        
        private ObjectAdapter lookupAdapterById(Oid oid) {
            return oidAdapterMap.getAdapter(oid);
        }
        
        private void addAdapter(ObjectAdapter adapter) {
            if(adapter==null) {
                return; // nothing to do
            }
            final Oid oid = adapter.getOid();
            if (oid != null) { // eg. value objects don't have an Oid
                oidAdapterMap.add(oid, adapter);
            }
            putPojo(adapter.getObject(), adapter);
        }
        
        private void removeAdapter(ObjectAdapter adapter) {
            LOG.debug("removing adapter: {}", adapter);
            if(adapter==null) {
                return; // nothing to do
            }
            final Oid oid = adapter.getOid();
            if (oid != null) { // eg. value objects don't have an Oid
                oidAdapterMap.remove(oid);
            }
            removePojo(adapter);
        }
        
    }
    
    private final Cache cache = new Cache();
    
    private final PersistenceSession persistenceSession; 
    private final ServicesInjector servicesInjector;
    private final SpecificationLoader specificationLoader;
    private final ObjectAdapterContext_Consistency consistencyMixin;
    private final ObjectAdapterContext_ObjectAdapterProvider objectAdapterProviderMixin;
    private final ObjectAdapterContext_AdapterManager adapterManagerMixin;
    private final ObjectAdapterContext_MementoSupport mementoSupportMixin;
    
    private ObjectAdapterContext(
            ServicesInjector servicesInjector, 
            AuthenticationSession authenticationSession, 
            SpecificationLoader specificationLoader, 
            PersistenceSession persistenceSession) {
        
        this.consistencyMixin = new ObjectAdapterContext_Consistency(this);
        this.objectAdapterProviderMixin = new ObjectAdapterContext_ObjectAdapterProvider(this, persistenceSession);
        this.adapterManagerMixin = new ObjectAdapterContext_AdapterManager(this, persistenceSession);
        this.mementoSupportMixin = new ObjectAdapterContext_MementoSupport(this, persistenceSession);
        
        this.persistenceSession = persistenceSession;
        this.servicesInjector = servicesInjector;
        this.specificationLoader = specificationLoader;
        
        this.objectAdapterFactories = new ObjectAdapterContext_Factories(
                authenticationSession, 
                specificationLoader, 
                persistenceSession);
    }

    // -- LIFE-CYCLING
    
    public void open() {
        cache.open();
        initServices();
    }
    
    public void close() {
        cache.close();
    }
    
    // -- CACHING POJO

    @Deprecated // don't expose caching
    protected ObjectAdapter lookupAdapterByPojo(Object pojo) {
        return cache.lookupAdapterByPojo(pojo);
    }
    
    @Deprecated // don't expose caching
    public boolean containsAdapterForPojo(Object pojo) {
        return lookupAdapterByPojo(pojo)!=null;
    }
    
    // -- CACHING OID
    
    @Deprecated // don't expose caching
    protected ObjectAdapter lookupAdapterById(Oid oid) {
        return cache.lookupAdapterById(oid);
    }
    
    private OidAdapterHashMap oidAdapterMap() {
        return cache.oidAdapterMap;
    }
    
    // -- CACHING BOTH

    @Deprecated // don't expose caching
    public void addAdapter(ObjectAdapter adapter) {
        cache.addAdapter(adapter);
    }
    
    @Deprecated // don't expose caching
    public void removeAdapter(ObjectAdapter adapter) {
        cache.removeAdapter(adapter);
    }
    
    // -- CACHE CONSISTENCY
    
    @Deprecated // don't expose caching
    public void ensureMapsConsistent(final ObjectAdapter adapter) {
        consistencyMixin.ensureMapsConsistent(adapter);
    }

    @Deprecated // don't expose caching
    public void ensureMapsConsistent(final Oid oid) {
        consistencyMixin.ensureMapsConsistent(oid);
    }
    
    // -- FACTORIES
    
    public static interface ObjectAdapterFactories {
        
        /**
         * Creates (but does not {@link #mapAndInjectServices(ObjectAdapter) map}) a new
         * root {@link ObjectAdapter adapter} for the supplied domain object.
         *
         * @see #createStandaloneAdapter(Object)
         * @see #createCollectionAdapter(Object, ParentedCollectionOid)
         */
        ObjectAdapter createRootAdapter(Object pojo, RootOid rootOid);
        
        /**
         * Creates a {@link ObjectAdapter adapter} with no {@link Oid}.
         *
         * <p>
         * Standalone adapters are never {@link #mapAndInjectServices(ObjectAdapter) mapped}
         * (they have no {@link Oid}, after all).
         *
         * <p>
         * Should only be called if the pojo is known not to be
         * {@link #lookupAdapterFor(Object) mapped}, and for immutable value types
         * referenced.
         */
        ObjectAdapter createStandaloneAdapter(Object pojo);

        ObjectAdapter createCollectionAdapter(Object pojo, ParentedCollectionOid collectionOid);

        /**
         * Creates an {@link ObjectAdapter adapter} to represent a collection
         * of the parent.
         *
         * <p>
         * The returned adapter will have a {@link ParentedCollectionOid}; its version
         * and its persistence are the same as its owning parent.
         *
         * <p>
         * Should only be called if the pojo is known not to be
         * {@link #lookupAdapterFor(Object) mapped}.
         */
        ObjectAdapter createCollectionAdapter(Object pojo, ObjectAdapter parentAdapter, OneToManyAssociation otma);
    }
    
    private final ObjectAdapterFactories objectAdapterFactories;
    
    public ObjectAdapterFactories getFactories() {
        return objectAdapterFactories;
    }
    
    // -- ADAPTER MANAGER LEGACY
    
    @Deprecated // don't expose caching
    public ObjectAdapter addRecreatedPojoToCache(Oid oid, Object recreatedPojo) {
        return adapterManagerMixin.addRecreatedPojoToCache(oid, recreatedPojo);
    }
    
    @Deprecated // don't expose caching
    public ObjectAdapter mapAndInjectServices(final ObjectAdapter adapter) {
        return adapterManagerMixin.mapAndInjectServices(adapter);
    }
    
    @Deprecated // don't expose caching
    public ObjectAdapter lookupAdapterFor(Object pojo) {
        return adapterManagerMixin.lookupAdapterFor(pojo);
    }
    
    @Deprecated // don't expose caching
    public ObjectAdapter lookupAdapterFor(final Oid oid) {
        return adapterManagerMixin.lookupAdapterFor(oid);
    }
    
    @Deprecated // don't expose caching
    public void removeAdapterFromCache(final ObjectAdapter adapter) {
        adapterManagerMixin.removeAdapterFromCache(adapter);
    }
    
    @Deprecated // don't expose caching
    public ObjectAdapter addPersistentToCache(final Object pojo) {
        return objectAdapterProviderMixin.addPersistentToCache(pojo);
    }
    
    // -- OBJECT ADAPTER PROVIDER SUPPORT
    
    public ObjectAdapterProvider getObjectAdapterProvider() {
        return objectAdapterProviderMixin;
    }
    
    // -- MEMENTO SUPPORT
    
    public static interface MementoRecreateObjectSupport {
        ObjectAdapter recreateObject(ObjectSpecification spec, Oid oid, Data data);
    }
    
    public MementoRecreateObjectSupport mementoSupport() {
        return mementoSupportMixin;
    }
    
    // ------------------------------------------------------------------------------------------------
    
    /**
     * Creates {@link ObjectAdapter adapters} for the service list, ensuring that these are mapped correctly,
     * and have the same OIDs as in any previous sessions.
     * 
     * @deprecated https://issues.apache.org/jira/browse/ISIS-1976
     */
    @Deprecated
    private void initServices() {
        final List<Object> registeredServices = servicesInjector.getRegisteredServices();
        for (final Object service : registeredServices) {
            final ObjectAdapter serviceAdapter = objectAdapterProviderMixin.adapterFor(service);
            
            // remap as Persistent if required
            if (serviceAdapter.getOid().isTransient()) {
                _Exceptions.unexpectedCodeReach();
                //remapAsPersistent(serviceAdapter, null, persistenceSession);
            }
        }
    }
    
    @Deprecated // don't expose caching
    public void addAdapterHonoringSpecImmutability(Object pojo, ObjectAdapter adapter) {
        // add all aggregated collections
        final ObjectSpecification objSpec = adapter.getSpecification();
        if (!adapter.isParentedCollection() || adapter.isParentedCollection() && !objSpec.isImmutable()) {
            cache.putPojo(pojo, adapter);
        }

        // order is important - add to pojo map first, then identity map
        oidAdapterMap().add(adapter.getOid(), adapter);
    }
    
    public ObjectAdapter disposableAdapterForViewModel(Object viewModelPojo) {
            final ObjectSpecification objectSpecification = 
                    specificationLoader.loadSpecification(viewModelPojo.getClass());
            final ObjectSpecId objectSpecId = objectSpecification.getSpecId();
            final RootOid newRootOid = RootOid.create(objectSpecId, UUID.randomUUID().toString());
            final ObjectAdapter createdAdapter = adapterManagerMixin.createRootOrAggregatedAdapter(newRootOid, viewModelPojo);
            return createdAdapter;
    }
    
    public ObjectAdapter adapterForViewModel(Object viewModelPojo, Function<ObjectSpecId, RootOid> rootOidFactory) {
        ObjectAdapter viewModelAdapter = adapterManagerMixin.lookupAdapterFor(viewModelPojo);
        if(viewModelAdapter == null) {
            final ObjectSpecification objectSpecification = 
                    specificationLoader.loadSpecification(viewModelPojo.getClass());
            final ObjectSpecId objectSpecId = objectSpecification.getSpecId();
            final RootOid newRootOid = rootOidFactory.apply(objectSpecId);

            viewModelAdapter = adapterManagerMixin.addRecreatedPojoToCache(newRootOid, viewModelPojo);
        }
        return viewModelAdapter;
    }
    
    /**
     * Note that there is no management of {@link Version}s here. That is
     * because the {@link PersistenceSession} is expected to manage this.
     *
     * @param hintRootOid - allow a different persistent root oid to be provided.
     * @param session 
     */
    @Deprecated // expected to be moved
    public void remapAsPersistent(final ObjectAdapter adapter, RootOid hintRootOid, PersistenceSession session) {

        final ObjectAdapter rootAdapter = adapter.getAggregateRoot();  // TODO: REVIEW: think this is redundant; would seem this method is only ever called for roots anyway.
        final RootOid transientRootOid = (RootOid) rootAdapter.getOid();

        final RootAndCollectionAdapters rootAndCollectionAdapters = 
                new RootAndCollectionAdapters(adapter, adapterManagerMixin);

        removeFromCache(rootAndCollectionAdapters, transientRootOid);
        
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
            LOG.debug("replacing root adapter and re-adding into maps; oid is now: {} (was: {})", persistedRootOid.enString(), transientRootOid.enString());
        }
        
        final ObjectAdapter adapterReplacement = adapter.withOid(persistedRootOid); 
        
        replaceRootAdapter(adapterReplacement, rootAndCollectionAdapters);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("made persistent {}; was {}", adapterReplacement, transientRootOid);
        }
    }

    private void replaceRootAdapter(
            final ObjectAdapter adapterReplacement, 
            final RootAndCollectionAdapters rootAndCollectionAdapters) {
        
        addAdapter(adapterReplacement);
        
        final RootOid persistedRootOid = (RootOid) adapterReplacement.getOid();
        
        // associate the collection adapters with new Oids, and re-map
        LOG.debug("replacing Oids for collection adapter(s) and re-adding into maps");
        
        for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
            final ParentedCollectionOid previousCollectionOid = (ParentedCollectionOid) collectionAdapter.getOid();
            final ParentedCollectionOid persistedCollectionOid = previousCollectionOid.asPersistent(persistedRootOid);
            oidAdapterMap().add(persistedCollectionOid, collectionAdapter);
        }

        // some object store implementations may replace collection instances (eg ORM may replace with a cglib-enhanced
        // proxy equivalent.  So, ensure that the collection adapters still wrap the correct pojos.
        if (LOG.isDebugEnabled()) {
            LOG.debug("synchronizing collection pojos, remapping in pojo map if required");
        }
        for (final OneToManyAssociation otma : rootAndCollectionAdapters.getCollections()) {
            final ObjectAdapter collectionAdapter = rootAndCollectionAdapters.getCollectionAdapter(otma);

            final Object collectionPojoWrappedByAdapter = collectionAdapter.getObject();
            final Object collectionPojoActuallyOnPojo = getCollectionPojo(otma, adapterReplacement);

            if (collectionPojoActuallyOnPojo != collectionPojoWrappedByAdapter) {
                cache.removePojo(collectionAdapter);
                final ObjectAdapter newCollectionAdapter = collectionAdapter.withPojo(collectionPojoActuallyOnPojo);
                cache.putPojo(collectionPojoActuallyOnPojo, newCollectionAdapter);
            }
        }
        
    }

    private void removeFromCache(
            final RootAndCollectionAdapters rootAndCollectionAdapters, 
            final RootOid transientRootOid) {
        
        LOG.debug("removing root adapter from oid map");
    
        boolean removed = oidAdapterMap().remove(transientRootOid);
        if (!removed) {
            LOG.warn("could not remove oid: {}", transientRootOid);
            // should we fail here with a more serious error?
        }
    
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing collection adapter(s) from oid map");
        }
        for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
            final Oid collectionOid = collectionAdapter.getOid();
            removed = oidAdapterMap().remove(collectionOid);
            if (!removed) {
                ObjectAdapterLegacy.LOG.warn("could not remove collectionOid: {}", collectionOid);
                // should we fail here with a more serious error?
            }
        }
    }

    private static Object getCollectionPojo(final OneToManyAssociation association, final ObjectAdapter ownerAdapter) {
        final PropertyOrCollectionAccessorFacet accessor = association.getFacet(PropertyOrCollectionAccessorFacet.class);
        return accessor.getProperty(ownerAdapter, InteractionInitiatedBy.FRAMEWORK);
    }

    /**
     * @deprecated https://issues.apache.org/jira/browse/ISIS-1976
     */
    @Deprecated
    public ObjectAdapter remapRecreatedPojo(ObjectAdapter adapter, final Object pojo) {
        final ObjectAdapter newAdapter = adapter.withPojo(pojo);
        cache.removeAdapter(adapter);
        cache.removeAdapter(newAdapter);

        //FIXME[ISIS-1976] can't remove yet, does have strange side-effects 
        if(true){
            adapter.friend().replacePojo(pojo);
            mapAndInjectServices(adapter);
            return adapter;
        }
        //---
        
        mapAndInjectServices(newAdapter);
        return newAdapter;
    }


}