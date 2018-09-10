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

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.ensure.IsisAssertException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterByIdProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.memento.Data;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

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
        
        private ObjectAdapter lookupAdapterById(Oid oid) {
            return oidAdapterMap.getAdapter(oid);
        }
        
        private void addAdapter(ObjectAdapter adapter) {
            if(adapter==null) {
                return; // nothing to do
            }
            final Oid oid = adapter.getOid();
            oidAdapterMap.add(oid, adapter);
            pojoAdapterMap.add(adapter.getObject(), adapter);
        }
        
        private void removeAdapter(ObjectAdapter adapter) {
            if(adapter==null) {
                return; // nothing to do
            }
            final Oid oid = adapter.getOid();
            oidAdapterMap.remove(oid);
            pojoAdapterMap.remove(adapter);
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
    private final ObjectAdapterContext_ServiceLookup serviceLookupMixin;
    private final ObjectAdapterContext_NewIdentifier newIdentifierMixin;
    private final ObjectAdapterContext_ObjectAdapterByIdProvider objectAdapterByIdProviderMixin;
    private final ObjectAdapterContext_DependencyInjection dependencyInjectionMixin;
    private final ObjectAdapterContext_ObjectReCreation objectReCreationMixin;
    
    private ObjectAdapterContext(
            ServicesInjector servicesInjector, 
            AuthenticationSession authenticationSession, 
            SpecificationLoader specificationLoader, 
            PersistenceSession persistenceSession) {
        
        this.consistencyMixin = new ObjectAdapterContext_Consistency(this);
        this.objectAdapterProviderMixin = new ObjectAdapterContext_ObjectAdapterProvider(this, persistenceSession);
        this.adapterManagerMixin = new ObjectAdapterContext_AdapterManager(this, persistenceSession);
        this.mementoSupportMixin = new ObjectAdapterContext_MementoSupport(this, persistenceSession);
        this.serviceLookupMixin = new ObjectAdapterContext_ServiceLookup(this, servicesInjector);
        this.newIdentifierMixin = new ObjectAdapterContext_NewIdentifier(this, persistenceSession);
        this.objectAdapterByIdProviderMixin = new ObjectAdapterContext_ObjectAdapterByIdProvider(this, persistenceSession, authenticationSession);
        this.dependencyInjectionMixin = new ObjectAdapterContext_DependencyInjection(this, persistenceSession);
        this.objectReCreationMixin = new ObjectAdapterContext_ObjectReCreation(this, persistenceSession);
        
        this.persistenceSession = persistenceSession;
        this.servicesInjector = servicesInjector;
        this.specificationLoader = specificationLoader;
        
        this.objectAdapterFactories = new ObjectAdapterContext_Factories(
                authenticationSession, 
                specificationLoader, 
                persistenceSession);
    }

    // -- DEBUG
    
    void printContextInfo(String msg) {
        if(LOG.isDebugEnabled()) {
            String id = Integer.toHexString(this.hashCode());
            String session = ""+persistenceSession;
            LOG.debug(String.format("%s id=%s session='%s'", msg, id, session));
        }
    }
    
    // -- LIFE-CYCLING
    
    public void open() {
        printContextInfo("OPEN_");
        cache.open();
    }
    
    public void close() {
        cache.close();
        printContextInfo("CLOSE");
    }
    
    // -- CACHING POJO

    // package private // don't expose caching
    ObjectAdapter lookupAdapterByPojo(Object pojo) {
        return cache.lookupAdapterByPojo(pojo);
    }
    
    // package private // don't expose caching
    boolean containsAdapterForPojo(Object pojo) {
        return lookupAdapterByPojo(pojo)!=null;
    }
    
    // -- CACHING OID
    
    // package private // don't expose caching
    ObjectAdapter lookupAdapterById(Oid oid) {
        return cache.lookupAdapterById(oid);
    }
    
    // -- CACHING BOTH

    // package private // don't expose caching
    void addAdapter(ObjectAdapter adapter) {
        cache.addAdapter(adapter);
    }
    
    // package private // don't expose caching
    void removeAdapter(ObjectAdapter adapter) {
        cache.removeAdapter(adapter);
    }
    
    // -- CACHE CONSISTENCY
    
    // package private // don't expose caching
    void ensureMapsConsistent(final ObjectAdapter adapter) {
        consistencyMixin.ensureMapsConsistent(adapter);
    }

    // package private // don't expose caching
    void ensureMapsConsistent(final Oid oid) {
        consistencyMixin.ensureMapsConsistent(oid);
    }
    
    // -- NEW IDENTIFIER
    
    public RootOid createPersistentOrViewModelOid(Object pojo) {
        return newIdentifierMixin.createPersistentOid(pojo);
    }
    
    // -- SERVICE LOOKUP
    
    public ObjectAdapter lookupServiceAdapterFor(RootOid rootOid) {
        return serviceLookupMixin.lookupServiceAdapterFor(rootOid);
    }
    
    // -- BY-ID SUPPORT
    
    public ObjectAdapterByIdProvider getObjectAdapterByIdProvider() {
        return objectAdapterByIdProviderMixin;
    }
    
    // -- DEPENDENCY INJECTION
    
    public Object instantiateAndInjectServices(ObjectSpecification objectSpec) {
        return dependencyInjectionMixin.instantiateAndInjectServices(objectSpec);
    }
    
    // -- FACTORIES
    
    // package private
    static interface ObjectAdapterFactories {
        
        /**
         * Creates (but does not {@link #mapAndInjectServices(ObjectAdapter) map}) a new
         * root {@link ObjectAdapter adapter} for the supplied domain object.
         *
         * @see #createStandaloneAdapter(Object)
         * @see #createCollectionAdapter(Object, ParentedCollectionOid)
         */
        ObjectAdapter createRootAdapter(Object pojo, RootOid rootOid);
        
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
    
    // package private
    ObjectAdapterFactories getFactories() {
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
        Objects.requireNonNull(oid);
        consistencyMixin.ensureMapsConsistent(oid);
        return cache.lookupAdapterById(oid);
    }
    
    // package private
    ObjectAdapter lookupParentedCollectionAdapter(ParentedCollectionOid collectionOid) {
        Objects.requireNonNull(collectionOid);
        consistencyMixin.ensureMapsConsistent(collectionOid);
        return cache.lookupAdapterById(collectionOid);
    }
    
    // package private // don't expose caching
    void removeAdapterFromCache(final ObjectAdapter adapter) {
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
    
    // -- OBJECT RECREATION SUPPORT
    
    public Object recreateViewModel(final ObjectSpecification spec, final String memento) {
        return objectReCreationMixin.recreateViewModel(spec, memento);
    }
    
    // ------------------------------------------------------------------------------------------------
    
    public ObjectAdapter disposableAdapterForViewModel(Object viewModelPojo) {
            final ObjectSpecification objectSpecification = 
                    specificationLoader.loadSpecification(viewModelPojo.getClass());
            final ObjectSpecId objectSpecId = objectSpecification.getSpecId();
            final RootOid newRootOid = RootOid.create(objectSpecId, UUID.randomUUID().toString());
            final ObjectAdapter createdAdapter = adapterManagerMixin.createRootOrAggregatedAdapter(newRootOid, viewModelPojo);
            return createdAdapter;
    }
    
    // package private
    ObjectAdapter adapterForViewModel(Object viewModelPojo, Function<ObjectSpecId, RootOid> rootOidFactory) {
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
     * @param newRootOid - allow a different persistent root oid to be provided.
     * @param session 
     */
    @Deprecated // expected to be moved
    public void remapAsPersistent(final ObjectAdapter rootAdapter, RootOid newRootOid, PersistenceSession session) {

        Objects.requireNonNull(newRootOid);
        Assert.assertFalse("expected to not be a parented collection", rootAdapter.isParentedCollection());
        
        final RootOid transientRootOid = (RootOid) rootAdapter.getOid();
        
        final RootAndCollectionAdapters rootAndCollectionAdapters = 
                new RootAndCollectionAdapters(rootAdapter, this);

        removeFromCache(rootAndCollectionAdapters);
        
        final RootOid persistedRootOid;
        {
            if(newRootOid.isTransient()) {
                throw new IsisAssertException("hintRootOid must be persistent");
            }
            final ObjectSpecId hintRootOidObjectSpecId = newRootOid.getObjectSpecId();
            final ObjectSpecId adapterObjectSpecId = rootAdapter.getSpecification().getSpecId();
            if(!hintRootOidObjectSpecId.equals(adapterObjectSpecId)) {
                throw new IsisAssertException("hintRootOid's objectType must be same as that of adapter " +
                        "(was: '" + hintRootOidObjectSpecId + "'; adapter's is " + adapterObjectSpecId + "'");
            }
            // ok
            persistedRootOid = newRootOid;
        } 

        // associate root adapter with the new Oid, and remap
        if (LOG.isDebugEnabled()) {
            LOG.debug("replacing root adapter and re-adding into maps; oid is now: {} (was: {})", persistedRootOid.enString(), transientRootOid.enString());
        }
        
        final ObjectAdapter adapterReplacement = rootAdapter.withOid(persistedRootOid); 
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
            Assert.assertTrue("expected equal", Objects.equals(collectionAdapter.getOid(), persistedCollectionOid));
            addAdapter(collectionAdapter);
        }

        // some object store implementations may replace collection instances (eg ORM may replace with a cglib-enhanced
        // proxy equivalent.  So, ensure that the collection adapters still wrap the correct pojos.
        LOG.debug("synchronizing collection pojos, remapping in pojo map if required");
        for (final OneToManyAssociation otma : rootAndCollectionAdapters.getCollections()) {
            final ObjectAdapter collectionAdapter = rootAndCollectionAdapters.getCollectionAdapter(otma);

            final Object collectionPojoWrappedByAdapter = collectionAdapter.getObject();
            final Object collectionPojoActuallyOnPojo = getCollectionPojo(otma, adapterReplacement);

            if (collectionPojoActuallyOnPojo != collectionPojoWrappedByAdapter) {
                cache.removeAdapter(collectionAdapter);
                final ObjectAdapter newCollectionAdapter = collectionAdapter.withPojo(collectionPojoActuallyOnPojo);
                Assert.assertTrue("expected same", 
                        Objects.equals(newCollectionAdapter.getObject(), collectionPojoActuallyOnPojo));
                cache.addAdapter(collectionAdapter);
            }
        }
        
    }

    private void removeFromCache(final RootAndCollectionAdapters rootAndCollectionAdapters) {
        final ObjectAdapter rootAdapter = rootAndCollectionAdapters.getRootAdapter();
        
        LOG.debug("removing root adapter from oid map");
        cache.removeAdapter(rootAdapter);
    
        LOG.debug("removing collection adapter(s) from oid map");
        for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
            cache.removeAdapter(collectionAdapter);
        }
    }

    private static Object getCollectionPojo(final OneToManyAssociation association, final ObjectAdapter ownerAdapter) {
        final PropertyOrCollectionAccessorFacet accessor = association.getFacet(PropertyOrCollectionAccessorFacet.class);
        return accessor.getProperty(ownerAdapter, InteractionInitiatedBy.FRAMEWORK);
    }

    @Deprecated
    public ObjectAdapter remapRecreatedPojo(ObjectAdapter adapter, final Object pojo) {
        final ObjectAdapter newAdapter = adapter.withPojo(pojo);
        cache.removeAdapter(adapter);
        cache.removeAdapter(newAdapter);
        mapAndInjectServices(newAdapter);
        return newAdapter;
    }




}