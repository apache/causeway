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

import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.ensure.IsisAssertException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
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
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 *  
 * @since 2.0.0-M2
 */
public class ObjectAdapterContext {
    
    private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterContext.class);
    
    private final PojoAdapterHashMap pojoAdapterMap = new PojoAdapterHashMap();
    private final OidAdapterHashMap oidAdapterMap = new OidAdapterHashMap();
    private final PersistenceSession persistenceSession; 
    private final ServicesInjector servicesInjector;
    private final SpecificationLoader specificationLoader;
    private final ObjectAdapterContext_Consistency consistencyMixin;
    private final ObjectAdapterContext_AdapterManager adapterManagerMixin;
    private final ObjectAdapterContext_MementoSupport mementoSupportMixin;
    
    ObjectAdapterContext(
            ServicesInjector servicesInjector, 
            AuthenticationSession authenticationSession, 
            SpecificationLoader specificationLoader, 
            PersistenceSession persistenceSession) {
        
        this.consistencyMixin = new ObjectAdapterContext_Consistency(this);
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
        oidAdapterMap.open();
        pojoAdapterMap.open();
    }
    
    public void close() {
        
        try {
            oidAdapterMap.close();
        } catch(final Throwable ex) {
            // ignore
            ObjectAdapterLegacy.LOG.error("close: oidAdapterMap#close() failed; continuing to avoid memory leakage");
        }

        try {
            pojoAdapterMap.close();
        } catch(final Throwable ex) {
            // ignore
            ObjectAdapterLegacy.LOG.error("close: pojoAdapterMap#close() failed; continuing to avoid memory leakage");
        }
    }
    
    // -- CACHING

    @Deprecated // don't expose caching
    public boolean containsAdapterForPojo(Object pojo) {
        return pojoAdapterMap.containsPojo(pojo);
    }
    
    @Deprecated // don't expose caching
    public ObjectAdapter lookupAdapterByPojo(Object pojo) {
        return pojoAdapterMap.getAdapter(pojo);
    }

    @Deprecated // don't expose caching
    public ObjectAdapter lookupAdapterById(Oid oid) {
        return oidAdapterMap.getAdapter(oid);
    }

    @Deprecated // don't expose caching
    public void addAdapter(ObjectAdapter adapter) {
        if(adapter==null) {
            return; // nothing to do
        }
        final Oid oid = adapter.getOid();
        if (oid != null) { // eg. value objects don't have an Oid
            oidAdapterMap.add(oid, adapter);
        }
        pojoAdapterMap.add(adapter.getObject(), adapter);
    }
    
    @Deprecated // don't expose caching
    public void removeAdapter(ObjectAdapter adapter) {
        LOG.debug("removing adapter: {}", adapter);
        if(adapter==null) {
            return; // nothing to do
        }
        final Oid oid = adapter.getOid();
        if (oid != null) { // eg. value objects don't have an Oid
            oidAdapterMap.remove(oid);
        }
        pojoAdapterMap.remove(adapter);
    }
    
    // -- CACHE CONSISTENCY
    
    public void ensureMapsConsistent(final ObjectAdapter adapter) {
        consistencyMixin.ensureMapsConsistent(adapter);
    }

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
    
    public ObjectAdapter addRecreatedPojoToCache(Oid oid, Object recreatedPojo) {
        return adapterManagerMixin.addRecreatedPojoToCache(oid, recreatedPojo);
    }
    
    public ObjectAdapter mapAndInjectServices(final ObjectAdapter adapter) {
        return adapterManagerMixin.mapAndInjectServices(adapter);
    }
    
    public ObjectAdapter lookupAdapterFor(Object pojo) {
        return adapterManagerMixin.lookupAdapterFor(pojo);
    }
    
    public ObjectAdapter lookupAdapterFor(final Oid oid) {
        return adapterManagerMixin.lookupAdapterFor(oid);
    }
    
    public void removeAdapterFromCache(final ObjectAdapter adapter) {
        adapterManagerMixin.removeAdapterFromCache(adapter);
    }
    
    // -- MEMENTO SUPPORT
    
    public static interface MementoRecreateObjectSupport {
        ObjectAdapter recreateObject(ObjectSpecification spec, Oid oid, Data data);
    }
    
    public MementoRecreateObjectSupport mementoSupport() {
        return mementoSupportMixin;
    }
    
    // ------------------------------------------------------------------------------------------------
    
    @Deprecated // don't expose caching
    public void addAdapterHonoringSpecImmutability(Object pojo, ObjectAdapter adapter) {
        // add all aggregated collections
        final ObjectSpecification objSpec = adapter.getSpecification();
        if (!adapter.isParentedCollection() || adapter.isParentedCollection() && !objSpec.isImmutable()) {
            pojoAdapterMap.add(pojo, adapter);
        }

        // order is important - add to pojo map first, then identity map
        oidAdapterMap.add(adapter.getOid(), adapter);
    }
    
    public ObjectSpecification specificationForViewModel(Object viewModelPojo) {
        //FIXME[ISIS-1976]
        // this is horrible, but there's a catch-22 here...
        // we need an adapter in order to query the state of the object via the metamodel, on the other hand
        // we can't create an adapter without the identifier, which is what we're trying to derive
        // so... we create a temporary transient adapter, use it to wrap this adapter and interrogate this pojo,
        // then throw away that adapter (remove from the adapter map)
        final boolean[] createdTemporaryAdapter = {false};
        final ObjectAdapter viewModelAdapter = adapterForViewModel(
                viewModelPojo, 
                (ObjectSpecId objectSpecId)->{
                    createdTemporaryAdapter[0] = true;
                    return RootOid.create(objectSpecId, UUID.randomUUID().toString()); });
    
        final ObjectSpecification spec = viewModelAdapter.getSpecification();
        
        if(createdTemporaryAdapter[0]) {
            adapterManagerMixin.removeAdapterFromCache(viewModelAdapter);
        }
        return spec;
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
     * was in PersisenceSessionX, temporarily moved here to successfully compile
     *
     * <p>
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
        if (ObjectAdapterLegacy.LOG.isDebugEnabled()) {
            ObjectAdapterLegacy.LOG.debug("replacing root adapter and re-adding into maps; oid is now: {} (was: {})", persistedRootOid.enString(), transientRootOid.enString());
        }
        
        final ObjectAdapter adapterReplacement = adapter.withOid(persistedRootOid); 
        
        replaceRootAdapter(adapterReplacement, rootAndCollectionAdapters);
        
        if (ObjectAdapterLegacy.LOG.isDebugEnabled()) {
            ObjectAdapterLegacy.LOG.debug("made persistent {}; was {}", adapterReplacement, transientRootOid);
        }
    }

    private void replaceRootAdapter(
            final ObjectAdapter adapterReplacement, 
            final RootAndCollectionAdapters rootAndCollectionAdapters) {
        
        addAdapter(adapterReplacement);
        
        final RootOid persistedRootOid = (RootOid) adapterReplacement.getOid();
        
//        oidAdapterMap.add(persistedRootOid, adapterReplacement);
//        pojoAdapterMap.add(adapterReplacement.getObject(), adapterReplacement);

        // associate the collection adapters with new Oids, and re-map
        ObjectAdapterLegacy.LOG.debug("replacing Oids for collection adapter(s) and re-adding into maps");
        
        for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
            final ParentedCollectionOid previousCollectionOid = (ParentedCollectionOid) collectionAdapter.getOid();
            final ParentedCollectionOid persistedCollectionOid = previousCollectionOid.asPersistent(persistedRootOid);
            oidAdapterMap.add(persistedCollectionOid, collectionAdapter);
        }

        // some object store implementations may replace collection instances (eg ORM may replace with a cglib-enhanced
        // proxy equivalent.  So, ensure that the collection adapters still wrap the correct pojos.
        if (ObjectAdapterLegacy.LOG.isDebugEnabled()) {
            ObjectAdapterLegacy.LOG.debug("synchronizing collection pojos, remapping in pojo map if required");
        }
        for (final OneToManyAssociation otma : rootAndCollectionAdapters.getCollections()) {
            final ObjectAdapter collectionAdapter = rootAndCollectionAdapters.getCollectionAdapter(otma);

            final Object collectionPojoWrappedByAdapter = collectionAdapter.getObject();
            final Object collectionPojoActuallyOnPojo = getCollectionPojo(otma, adapterReplacement);

            if (collectionPojoActuallyOnPojo != collectionPojoWrappedByAdapter) {
                pojoAdapterMap.remove(collectionAdapter);
                collectionAdapter.replacePojo(collectionPojoActuallyOnPojo);
                pojoAdapterMap.add(collectionPojoActuallyOnPojo, collectionAdapter);
            }
        }
        
    }

    private void removeFromCache(
            final RootAndCollectionAdapters rootAndCollectionAdapters, 
            final RootOid transientRootOid) {
        ObjectAdapterLegacy.LOG.debug("remapAsPersistent: {}", transientRootOid);
        ObjectAdapterLegacy.LOG.debug("removing root adapter from oid map");
    
        boolean removed = oidAdapterMap.remove(transientRootOid);
        if (!removed) {
            ObjectAdapterLegacy.LOG.warn("could not remove oid: {}", transientRootOid);
            // should we fail here with a more serious error?
        }
    
        if (ObjectAdapterLegacy.LOG.isDebugEnabled()) {
            ObjectAdapterLegacy.LOG.debug("removing collection adapter(s) from oid map");
        }
        for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
            final Oid collectionOid = collectionAdapter.getOid();
            removed = oidAdapterMap.remove(collectionOid);
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




}