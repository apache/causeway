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

package org.apache.isis.core.runtime.persistence.adaptermanager;

import org.datanucleus.enhancement.Persistable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.Resettable;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.ensure.IsisAssertException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.ElementSpecificationProviderFromTypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.PojoRecreationException;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.OidGenerator;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * Responsible for managing the {@link ObjectAdapter adapter}s and {@link Oid
 * identities} for each and every POJO that is being used by the framework.
 *
 * <p>
 * It provides a consistent set of adapters in memory, providing an
 * {@link ObjectAdapter adapter} for the POJOs that are in use ensuring that the
 * same object is not loaded twice into memory.
 *
 * <p>
 * Each POJO is given an {@link ObjectAdapter adapter} so that the framework can
 * work with the POJOs even though it does not understand their types. Each POJO
 * maps to an {@link ObjectAdapter adapter} and these are reused.
 */
public class AdapterManagerDefault implements AdapterManager,
        SessionScopedComponent,
        DebuggableWithTitle,
        Resettable {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterManagerDefault.class);

    //region > constructor and fields
    protected final PojoAdapterHashMap pojoAdapterMap = new PojoAdapterHashMap();
    protected final OidAdapterHashMap oidAdapterMap = new OidAdapterHashMap();

    private final PersistenceSession persistenceSession;
    private final SpecificationLoaderSpi specificationLoader;
    private final OidMarshaller oidMarshaller;
    private final OidGenerator oidGenerator;
    private final AuthenticationSession authenticationSession;
    private final ServicesInjector servicesInjector;
    private final IsisConfiguration configuration;
    private boolean concurrencyCheckingGloballyEnabled;

    // //////////////////////////////////////////////////////////////////
    // constructor
    // //////////////////////////////////////////////////////////////////

    /**
     * For object store implementations (eg JDO) that do not provide any mechanism
     * to allow transient objects to be reattached.
     * 
     * @see <a href="http://www.datanucleus.org/servlet/forum/viewthread_thread,7238_lastpage,yes#35976">this thread</a>
     */
    public AdapterManagerDefault(
            final PersistenceSession persistenceSession,
            final SpecificationLoaderSpi specificationLoader,
            final OidMarshaller oidMarshaller,
            final OidGenerator oidGenerator,
            final AuthenticationSession authenticationSession,
            final ServicesInjector servicesInjector,
            final IsisConfiguration configuration) {
        this.persistenceSession = persistenceSession;
        this.specificationLoader = specificationLoader;
        this.oidMarshaller = oidMarshaller;
        this.oidGenerator = oidGenerator;
        this.authenticationSession = authenticationSession;
        this.servicesInjector = servicesInjector;
        this.configuration = configuration;

        final boolean concurrencyCheckingGloballyDisabled =
                configuration.getBoolean("isis.persistor.disableConcurrencyChecking", false);
        this.concurrencyCheckingGloballyEnabled = !concurrencyCheckingGloballyDisabled;
    }
    //endregion

    //region > open, close
    @Override
    public void open() {
        oidAdapterMap.open();
        pojoAdapterMap.open();
    }

    @Override
    public void close() {
        oidAdapterMap.close();
        pojoAdapterMap.close();
    }
    //endregion

    //region > reset
    @Override
    public void reset() {
        oidAdapterMap.reset();
        pojoAdapterMap.reset();
    }
    //endregion

    //region > getAdapterFor
    @Override
    public ObjectAdapter getAdapterFor(final Object pojo) {
        ensureThatArg(pojo, is(notNullValue()));

        return pojoAdapterMap.getAdapter(pojo);
    }

    @Override
    public ObjectAdapter getAdapterFor(final Oid oid) {
        ensureThatArg(oid, is(notNullValue()));
        ensureMapsConsistent(oid);

        return oidAdapterMap.getAdapter(oid);
    }
    //endregion

    //region > adapterFor

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectAdapter adapterFor(final Object pojo) {

        if(pojo == null) {
            return null;
        }
        final ObjectAdapter existingOrValueAdapter = existingOrValueAdapter(pojo);
        if(existingOrValueAdapter != null) {
            return existingOrValueAdapter;
        }
        
        final ObjectAdapter newAdapter = createTransientOrViewModelRootAdapter(pojo);
        
        return mapAndInjectServices(newAdapter);
    }

    private ObjectAdapter existingOrValueAdapter(Object pojo) {

        // attempt to locate adapter for the pojo
        final ObjectAdapter adapter = getAdapterFor(pojo);
        if (adapter != null) {
            return adapter;
        }
        
        // pojo may have been lazily loaded by object store, but we haven't yet seen it
        final ObjectAdapter lazilyLoadedAdapter = lazilyLoaded(pojo);
        if(lazilyLoadedAdapter != null) {
            return lazilyLoadedAdapter;
        }

        // need to create (and possibly map) the adapter.
        final ObjectSpecification objSpec = specificationLoader.loadSpecification(pojo.getClass());
        
        // we create value facets as standalone (so not added to maps)
        if (objSpec.containsFacet(ValueFacet.class)) {
            ObjectAdapter valueAdapter = createStandaloneAdapter(pojo);
            return valueAdapter;
        }
        
        return null;
    }

    private ObjectAdapter lazilyLoaded(Object pojo) {
        if(!(pojo instanceof Persistable)) {
            return null;
        }
        final Persistable persistenceCapable = (Persistable) pojo;
        return persistenceSession.mapRecreatedPersistent(persistenceCapable);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter parentAdapter) {
        
        Ensure.ensureThatArg(parentAdapter, is(not(nullValue())));
        
        final ObjectAdapter existingOrValueAdapter = existingOrValueAdapter(pojo);
        if(existingOrValueAdapter != null) {
            return existingOrValueAdapter;
        }
        
        final ObjectAdapter newAdapter = createTransientOrViewModelRootAdapter(pojo);

        return mapAndInjectServices(newAdapter);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter parentAdapter, final OneToManyAssociation collection) {
        
        Ensure.ensureThatArg(parentAdapter, is(not(nullValue())));
        Ensure.ensureThatArg(collection, is(not(nullValue())));
        
        final ObjectAdapter existingOrValueAdapter = existingOrValueAdapter(pojo);
        if(existingOrValueAdapter != null) {
            return existingOrValueAdapter;
        }
        
        // the List, Set etc. instance gets wrapped in its own adapter
        final ObjectAdapter newAdapter = createCollectionAdapter(pojo, parentAdapter, collection);
        
        return mapAndInjectServices(newAdapter);
    }

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
     * {@link #getAdapterFor(Object) mapped}.
     */
    private ObjectAdapter createCollectionAdapter(final Object pojo, final ObjectAdapter parentAdapter, final OneToManyAssociation otma) {

        ensureMapsConsistent(parentAdapter);
        Assert.assertNotNull(pojo);

        final Oid parentOid = parentAdapter.getOid();

        // persistence of collection follows the parent
        final ParentedCollectionOid collectionOid = new ParentedCollectionOid((RootOid) parentOid, otma);
        final ObjectAdapter collectionAdapter = createCollectionAdapter(pojo, collectionOid);

        // we copy over the type onto the adapter itself
        // [not sure why this is really needed, surely we have enough info in
        // the adapter
        // to look this up on the fly?]
        final TypeOfFacet facet = otma.getFacet(TypeOfFacet.class);
        collectionAdapter.setElementSpecificationProvider(ElementSpecificationProviderFromTypeOfFacet.createFrom(facet));

        return collectionAdapter;
    }




    @Override
    public ObjectAdapter adapterFor(final RootOid rootOid) {
        return adapterFor(rootOid, AdapterManager.ConcurrencyChecking.NO_CHECK);
    }

    @Override
    public ObjectAdapter adapterFor(
            final RootOid rootOid,
            final AdapterManager.ConcurrencyChecking concurrencyChecking) {

        // attempt to locate adapter for the Oid
        ObjectAdapter adapter = getAdapterFor(rootOid);
        if (adapter == null) {
            // else recreate
            try {
                final Object pojo = recreatePojo(rootOid);
                adapter = mapRecreatedPojo(rootOid, pojo);
            } catch(ObjectNotFoundException ex) {
                throw ex; // just rethrow
            } catch(RuntimeException ex) {
                throw new PojoRecreationException(rootOid, ex);
            }
        }

        // sync versions of original, with concurrency checking if required
        Oid adapterOid = adapter.getOid();
        if(adapterOid instanceof RootOid) {
            final RootOid recreatedOid = (RootOid) adapterOid;
            final RootOid originalOid = rootOid;
            
            try {
                if(concurrencyChecking.isChecking()) {
                    
                    // check for exception, but don't throw if suppressed through thread-local
                    final Version otherVersion = originalOid.getVersion();
                    final Version thisVersion = recreatedOid.getVersion();
                    if(thisVersion != null && 
                       otherVersion != null && 
                       thisVersion.different(otherVersion)) {

                        if(concurrencyCheckingGloballyEnabled && AdapterManager.ConcurrencyChecking.isCurrentlyEnabled()) {
                            LOG.info("concurrency conflict detected on " + recreatedOid + " (" + otherVersion + ")");
                            final String currentUser = authenticationSession.getUserName();
                            throw new ConcurrencyException(currentUser, recreatedOid, thisVersion, otherVersion);
                        } else {
                            LOG.warn("concurrency conflict detected but suppressed, on " + recreatedOid + " (" + otherVersion + ")");
                        }
                    }
                }
            } finally {
                final Version originalVersion = originalOid.getVersion();
                final Version recreatedVersion = recreatedOid.getVersion();
                if(recreatedVersion != null && (
                        originalVersion == null || 
                        recreatedVersion.different(originalVersion))
                    ) {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("updating version in oid, on " + originalOid + " (" + originalVersion + ") to (" + recreatedVersion +")");
                    }
                    originalOid.setVersion(recreatedVersion);
                }
            }
        }

        return adapter;
    }

    private Object recreatePojo(RootOid oid) {
        if(oid.isTransient() || oid.isViewModel()) {
            return recreatePojoDefault(oid);
        } else {
            return persistenceSession.loadPojo(oid);
        }
    }

    private Object recreatePojoDefault(final RootOid rootOid) {
        final ObjectSpecification spec =
                specificationLoader.lookupBySpecId(rootOid.getObjectSpecId());
        final Object pojo = persistenceSession.instantiateAndInjectServices(spec);
        if(rootOid.isViewModel()) {
            // initialize the view model pojo from the oid's identifier

            final ViewModelFacet facet = spec.getFacet(ViewModelFacet.class);
            if(facet == null) {
                throw new IllegalArgumentException("spec does not have RecreatableObjectFacet; " + rootOid.toString() + "; spec is " + spec.getFullIdentifier());
            }

            final String memento = rootOid.getIdentifier();

            facet.initialize(pojo, memento);
        }
        return pojo;
    }
    //endregion

    //region > remapRecreatedPojo
    public void remapRecreatedPojo(ObjectAdapter adapter, final Object pojo) {
        removeAdapter(adapter);
        adapter.replacePojo(pojo);
        mapAndInjectServices(adapter);
    }
    //endregion

    //region > mapRecreatedPojo

    /**
     * Either returns an existing {@link ObjectAdapter adapter} (as per
     * {@link #getAdapterFor(Object)} or {@link #getAdapterFor(Oid)}), otherwise
     * re-creates an adapter with the specified (persistent) {@link Oid}.
     *
     * <p>
     * Typically called when the {@link Oid} is already known, that is, when
     * resolving an already-persisted object. Is also available for
     * <tt>Memento</tt> support however, so {@link Oid} could also represent a
     * {@link Oid#isTransient() transient} object.
     *
     * @param oid
     * @param recreatedPojo - already known to the object store impl, or a service
     */
    public ObjectAdapter mapRecreatedPojo(final Oid oid, final Object recreatedPojo) {

        // attempt to locate adapter for the pojo
        // REVIEW: this check is possibly redundant because the pojo will most likely 
        // have just been instantiated, so won't yet be in any maps
        final ObjectAdapter adapterLookedUpByPojo = getAdapterFor(recreatedPojo);
        if (adapterLookedUpByPojo != null) {
            return adapterLookedUpByPojo;
        }

        // attempt to locate adapter for the Oid
        final ObjectAdapter adapterLookedUpByOid = getAdapterFor(oid);
        if (adapterLookedUpByOid != null) {
            return adapterLookedUpByOid;
        }

        final ObjectAdapter createdAdapter = createRootOrAggregatedAdapter(oid, recreatedPojo);
        return mapAndInjectServices(createdAdapter);
    }

    private ObjectAdapter createRootOrAggregatedAdapter(final Oid oid, final Object pojo) {
        final ObjectAdapter createdAdapter;
        if(oid instanceof RootOid) {
            final RootOid rootOid = (RootOid) oid;
            createdAdapter = createRootAdapter(pojo, rootOid);
        } else /*if (oid instanceof CollectionOid)*/ {
            final ParentedCollectionOid collectionOid = (ParentedCollectionOid) oid;
            createdAdapter = createCollectionAdapter(pojo, collectionOid);
        }
        return createdAdapter;
    }
    //endregion

    //region > removeAdapter
    /**
     * Removes the specified object from both the identity-adapter map, and the
     * pojo-adapter map.
     * 
     * <p>
     * This indicates that the object is no longer in use, and therefore that no
     * objects exists within the system.
     * 
     * <p>
     * If an {@link ObjectAdapter adapter} is removed while its pojo still is
     * referenced then a subsequent interaction of that pojo will create a
     * different {@link ObjectAdapter adapter}.
     * 
     * <p>
     * TODO: should do a cascade remove of any aggregated objects.
     */
    @Override
    public void removeAdapter(final ObjectAdapter adapter) {
        ensureMapsConsistent(adapter);

        if (LOG.isDebugEnabled()) {
            LOG.debug("removing adapter: " + adapter);
        }

        unmap(adapter);
    }
    //endregion

    //region > remapAsPersistent
    /**
     * {@inheritDoc}
     * 
     * <p>
     * Note that there is no management of {@link Version}s here. That is
     * because the {@link PersistenceSession} is expected to manage this.
     * 
     * @param hintRootOid - allow a different persistent root oid to be provided.
     */
    public void remapAsPersistent(final ObjectAdapter adapter, RootOid hintRootOid) {
        
        final ObjectAdapter rootAdapter = adapter.getAggregateRoot();  // TODO: REVIEW: think this is redundant; would seem this method is only ever called for roots anyway.
        final RootOid transientRootOid = (RootOid) rootAdapter.getOid();

        // no longer true, because isTransient now looks directly at the underlying pojo's state (for entities)
        // and doesn't apply for services.
//        Ensure.ensureThatArg(rootAdapter.isTransient(), is(true), "root adapter should be transient; oid:" + transientRootOid);
//        Ensure.ensureThatArg(transientRootOid.isTransient(), is(true), "root adapter's OID should be transient; oid:" + transientRootOid);

        final RootAndCollectionAdapters rootAndCollectionAdapters = new RootAndCollectionAdapters(adapter, this);
        
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("remapAsPersistent: " + transientRootOid);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing root adapter from oid map");
        }
        
        boolean removed = oidAdapterMap.remove(transientRootOid);
        if (!removed) {
            LOG.warn("could not remove oid: " + transientRootOid);
            // should we fail here with a more serious error?
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing collection adapter(s) from oid map");
        }
        for (final ObjectAdapter collectionAdapter : rootAndCollectionAdapters) {
            final Oid collectionOid = collectionAdapter.getOid();
            removed = oidAdapterMap.remove(collectionOid);
            if (!removed) {
                LOG.warn("could not remove collectionOid: " + collectionOid);
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
            persistedRootOid = oidGenerator.createPersistentOrViewModelOid(adapter.getObject());
        }
        
        // associate root adapter with the new Oid, and remap
        if (LOG.isDebugEnabled()) {
            LOG.debug("replacing Oid for root adapter and re-adding into maps; oid is now: " + persistedRootOid.enString(
                    oidMarshaller) + " (was: " + transientRootOid.enString(oidMarshaller) + ")");
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
            LOG.debug("made persistent " + adapter + "; was " + transientRootOid);
        }
    }

	private static Object getCollectionPojo(final OneToManyAssociation association, final ObjectAdapter ownerAdapter) {
        final PropertyOrCollectionAccessorFacet accessor = association.getFacet(PropertyOrCollectionAccessorFacet.class);
        return accessor.getProperty(ownerAdapter, InteractionInitiatedBy.FRAMEWORK);
    }

    //endregion

    //region > Helpers: createXxxAdapter
    /**
     * Creates a new transient root {@link ObjectAdapter adapter} for the supplied domain
     * object.
     */
    private ObjectAdapter createTransientOrViewModelRootAdapter(final Object pojo) {
        final RootOid rootOid = oidGenerator.createTransientOrViewModelOid(pojo);
        return createRootAdapter(pojo, rootOid);
    }

    /**
     * Creates a {@link ObjectAdapter adapter} with no {@link Oid}.
     *
     * <p>
     * Standalone adapters are never {@link #mapAndInjectServices(ObjectAdapter) mapped}
     * (they have no {@link Oid}, after all).
     * 
     * <p>
     * Should only be called if the pojo is known not to be
     * {@link #getAdapterFor(Object) mapped}, and for immutable value types
     * referenced.
     */
    private ObjectAdapter createStandaloneAdapter(final Object pojo) {
        return createAdapter(pojo, null);
    }

    /**
     * Creates (but does not {@link #mapAndInjectServices(ObjectAdapter) map}) a new 
     * root {@link ObjectAdapter adapter} for the supplied domain object.
     * 
     * @see #createStandaloneAdapter(Object)
     * @see #createCollectionAdapter(Object, ParentedCollectionOid)
     */
    private ObjectAdapter createRootAdapter(final Object pojo, RootOid rootOid) {
        Ensure.ensureThatArg(rootOid, is(not(nullValue())));
        return createAdapter(pojo, rootOid);
    }

    private ObjectAdapter createCollectionAdapter(
            final Object pojo,
            ParentedCollectionOid collectionOid) {
        Ensure.ensureThatArg(collectionOid, is(not(nullValue())));
        return createAdapter(pojo, collectionOid);
    }

    private PojoAdapter createAdapter(
            final Object pojo,
            final Oid oid) {
        return new PojoAdapter(
                pojo, oid,
                authenticationSession, getLocalization(),
                specificationLoader, persistenceSession);
    }

    //endregion

    //region > Helpers: mapAndInjectServices & unmap
    private ObjectAdapter mapAndInjectServices(final ObjectAdapter adapter) {
        // since the whole point of this method is to map an adapter that's just been created.
        // so we *don't* call ensureMapsConsistent(adapter); 

        Assert.assertNotNull(adapter);
        final Object pojo = adapter.getObject();
        Assert.assertFalse("POJO Map already contains object", pojo, pojoAdapterMap.containsPojo(pojo));

        if (LOG.isDebugEnabled()) {
            // don't interact with the underlying object because may be a ghost
            // and would trigger a resolve
            // don't call toString() on adapter because calls hashCode on
            // underlying object, may also trigger a resolve.
            LOG.debug("adding identity for adapter with oid=" + adapter.getOid());
        }

        // value adapters are not mapped (but all others - root and aggregated adapters - are)
        if (adapter.isValue()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("not mapping value adapter");
            }
            servicesInjector.injectServicesInto(pojo);
            return adapter;
        }

        // add all aggregated collections
        final ObjectSpecification objSpec = adapter.getSpecification();
        if (!adapter.isParentedCollection() || adapter.isParentedCollection() && !objSpec.isImmutable()) {
            pojoAdapterMap.add(pojo, adapter);
        }

        // order is important - add to pojo map first, then identity map
        oidAdapterMap.add(adapter.getOid(), adapter);

        // must inject after mapping, otherwise infinite loop
        servicesInjector.injectServicesInto(pojo);

        return adapter;
    }

    private void unmap(final ObjectAdapter adapter) {
        ensureMapsConsistent(adapter);

        final Oid oid = adapter.getOid();
        if (oid != null) {
            oidAdapterMap.remove(oid);
        }
        pojoAdapterMap.remove(adapter);
    }

    //endregion

    //region > Helpers: ensure invariants
    /**
     * Fail early if any problems.
     */
    private void ensureMapsConsistent(final ObjectAdapter adapter) {
        if (adapter.isValue()) {
            return;
        }
        if (adapter.isParentedCollection()) {
            return;
        }
        ensurePojoAdapterMapConsistent(adapter);
        ensureOidAdapterMapConsistent(adapter);
    }

    /**
     * Fail early if any problems.
     */
    private void ensureMapsConsistent(final Oid oid) {
        ensureThatArg(oid, is(notNullValue()));

        final ObjectAdapter adapter = oidAdapterMap.getAdapter(oid);
        if (adapter == null) {
            return;
        }
        ensureOidAdapterMapConsistent(adapter);
        ensurePojoAdapterMapConsistent(adapter);
    }

    private void ensurePojoAdapterMapConsistent(final ObjectAdapter adapter) {
        final Object adapterPojo = adapter.getObject();
        final ObjectAdapter adapterAccordingToPojoAdapterMap = pojoAdapterMap.getAdapter(adapterPojo);
        // take care not to touch the pojo, since it might have been deleted.
        ensureThatArg(
                adapter, is(adapterAccordingToPojoAdapterMap), 
                "mismatch in PojoAdapterMap: provided adapter's OID: " + adapter.getOid() + "; \n" + " but map's adapter's OID was : " + adapterAccordingToPojoAdapterMap.getOid());
    }

    private void ensureOidAdapterMapConsistent(final ObjectAdapter adapter) {
        final Oid adapterOid = adapter.getOid();
        final ObjectAdapter adapterAccordingToOidAdapterMap = oidAdapterMap.getAdapter(adapterOid);
        // take care not to touch the pojo, since it might have been deleted.
        ensureThatArg(
                adapter, is(adapterAccordingToOidAdapterMap),
                "mismatch in OidAdapter map: " + "adapter's Oid: " + adapterOid + ", " + "provided adapter's OID: "
                        + adapter.getOid() + "; " + "map's adapter's Oid: " + adapterAccordingToOidAdapterMap.getOid());
    }
    //endregion

    //region > debug
    @Override
    public String debugTitle() {
        return "Identity map (adapter manager)";
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendTitle(pojoAdapterMap.debugTitle());
        pojoAdapterMap.debugData(debug);
        debug.appendln();

        debug.appendTitle(oidAdapterMap.debugTitle());
        oidAdapterMap.debugData(debug);

    }
    //endregion

    //region > Injectable

    @Override
    public void injectInto(final Object candidate) {
        if (AdapterManagerAware.class.isAssignableFrom(candidate.getClass())) {
            final AdapterManagerAware cast = AdapterManagerAware.class.cast(candidate);
            cast.setAdapterManager(this);
        }
    }
    //endregion

    //region > dependencies (from context)
    protected Localization getLocalization() {
        return IsisContext.getLocalization();
    }
    //endregion


}
