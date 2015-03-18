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
package org.apache.isis.core.runtime.system.persistence;

import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacetUtils;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.*;
import org.apache.isis.core.runtime.persistence.FixturesInstalledFlag;
import org.apache.isis.core.runtime.persistence.NotPersistableException;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapterFactory;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.core.runtime.persistence.adaptermanager.PojoRecreatorUnified;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.PersistAlgorithmUnified;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosureAbstract;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosureWithReturnAbstract;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.*;

public class PersistenceSession implements SessionScopedComponent, DebuggableWithTitle {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceSession.class);

    private final ObjectFactory objectFactory = new ObjectFactory();

    private final PersistenceSessionFactory persistenceSessionFactory;
    private final ObjectAdapterFactory objectAdapterFactory;
    private final ServicesInjectorSpi servicesInjector;
    private final OidGenerator oidGenerator;
    private final AdapterManagerDefault adapterManager;

    private final PersistAlgorithm persistAlgorithm ;
    private final ObjectStore objectStore;
    private final Map<ObjectSpecId, RootOid> servicesByObjectType = Maps.newHashMap();

    private final PersistenceQueryFactory persistenceQueryFactory;

    private IsisTransactionManager transactionManager;

    private boolean dirtiableSupport = true;



    private static enum State {
        NOT_INITIALIZED, OPEN, CLOSED
    }

    private State state;

    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     */
    public PersistenceSession(
            final PersistenceSessionFactory persistenceSessionFactory,
            final ServicesInjectorSpi servicesInjector,
            final ObjectStore objectStore,
            final IsisConfiguration configuration) {

        ensureThatArg(persistenceSessionFactory, is(not(nullValue())), "persistence session factory required");
        ensureThatArg(servicesInjector, is(not(nullValue())), "services injector required");
        ensureThatArg(objectStore, is(not(nullValue())), "object store required");

        // owning, application scope
        this.persistenceSessionFactory = persistenceSessionFactory;

        // session scope
        this.servicesInjector = servicesInjector;

        this.objectAdapterFactory = new PojoAdapterFactory();
        this.oidGenerator = new OidGenerator(new IdentifierGeneratorUnified(configuration));
        this.adapterManager = new AdapterManagerDefault(new PojoRecreatorUnified(configuration));
        this.persistAlgorithm = new PersistAlgorithmUnified(configuration);
        this.objectStore = objectStore;

        this.persistenceQueryFactory = new PersistenceQueryFactory(getSpecificationLoader(), adapterManager);
        this.transactionManager = new IsisTransactionManager(this, objectStore, servicesInjector);

        setState(State.NOT_INITIALIZED);

        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + this);
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // PersistenceSessionFactory
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * The {@link PersistenceSessionFactory} that created this
     * {@link PersistenceSession}.
     */
    public PersistenceSessionFactory getPersistenceSessionFactory() {
        return persistenceSessionFactory;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // open
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Injects components, calls  {@link org.apache.isis.core.commons.components.SessionScopedComponent#open()} on subcomponents, and then creates service
     * adapters.
     */
    @Override
    public void open() {
        ensureNotOpened();

        if (LOG.isDebugEnabled()) {
            LOG.debug("opening " + this);
        }

        // injected via setters
        ensureThatState(transactionManager, is(not(nullValue())), "TransactionManager missing");

        // inject any required dependencies into object factory
        servicesInjector.injectInto(objectFactory);

        // wire dependencies into adapterManager
        servicesInjector.injectInto(adapterManager);

        adapterManager.open();

        // doOpen..
        ensureThatState(objectStore, is(notNullValue()), "object store required");
        ensureThatState(getTransactionManager(), is(notNullValue()), "transaction manager required");
        ensureThatState(persistAlgorithm, is(notNullValue()), "persist algorithm required");

        getAdapterManager().injectInto(objectStore);
        getSpecificationLoader().injectInto(objectStore);

        objectStore.open();

        initServices();

        setState(State.OPEN);
    }

    
    private void initServices() {

        final List<Object> registeredServices = servicesInjector.getRegisteredServices();
        
        createServiceAdapters(registeredServices);
    }

    /**
     * Creates (or recreates following a {@link #testReset()})
     * {@link ObjectAdapter adapters} for the service list.
     */
    private void createServiceAdapters(final List<Object> registeredServices) {
        for (final Object service : registeredServices) {
            final ObjectSpecification serviceSpecification = getSpecificationLoader().loadSpecification(service.getClass());
            serviceSpecification.markAsService();
            final RootOid existingOid = getOidForService(serviceSpecification);
            final ObjectAdapter serviceAdapter =
                    existingOid == null
                            ? getAdapterManager().adapterFor(service) 
                            : getAdapterManager().mapRecreatedPojo(existingOid, service);
            if (serviceAdapter.getOid().isTransient()) {
                adapterManager.remapAsPersistent(serviceAdapter, null);
            }

            serviceAdapter.markAsResolvedIfPossible();
            if (existingOid == null) {
                final RootOid persistentOid = (RootOid) serviceAdapter.getOid();
                registerService(persistentOid);
            }
        }
    }


    /**
     * @return - the service, or <tt>null</tt> if no service registered of specified type.
     */
    public <T> T getServiceOrNull(final Class<T> serviceType) {
        return servicesInjector.lookupService(serviceType);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // close
    // ///////////////////////////////////////////////////////////////////////////


    /**
     * Calls {@link org.apache.isis.core.commons.components.SessionScopedComponent#close()}
     * on the subcomponents.
     */
    @Override
    public void close() {
        if (getState() == State.CLOSED) {
            // nothing to do
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("closing " + this);
        }
        
        try {
            objectStore.close();
        } catch(final RuntimeException ex) {
            // ignore
        }

        try {
            adapterManager.close();
        } catch(final RuntimeException ex) {
            // ignore
        }

        setState(State.CLOSED);
    }


    // ///////////////////////////////////////////////////////////////////////////
    // State Management
    // ///////////////////////////////////////////////////////////////////////////

    private State getState() {
        return state;
    }
    
    private void setState(final State state) {
        this.state = state;
    }
    
    protected void ensureNotOpened() {
        if (getState() != State.NOT_INITIALIZED) {
            throw new IllegalStateException("Persistence session has already been initialized");
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Factory
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a root or standalone {@link ObjectAdapter adapter}.
     *
     * <p>
     * Creates a new instance of the specified type and returns it in an adapter
     * whose resolved state set to {@link ResolveState#TRANSIENT} (except if the
     * type is marked as {@link ObjectSpecification#isValueOrIsParented()
     * aggregated} in which case it will be set to {@link ResolveState#VALUE}).
     *
     * <p>
     * The returned object will be initialised (had the relevant callback
     * lifecycle methods invoked).
     *
     * <p>
     * While creating the object it will be initialised with default values and
     * its created lifecycle method (its logical constructor) will be invoked.
     *
     * <p>
     * This method is ultimately delegated to by the
     * {@link org.apache.isis.applib.DomainObjectContainer}.
     */
    public ObjectAdapter createTransientInstance(final ObjectSpecification objectSpec) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating transient instance of " + objectSpec);
        }
        final Object pojo = objectSpec.createObject();
        final ObjectAdapter adapter = getAdapterManager().adapterFor(pojo);
        return objectSpec.initialize(adapter);
    }

    public ObjectAdapter createViewModelInstance(final ObjectSpecification objectSpec, final String memento) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating view model instance of " + objectSpec);
        }
        final Object pojo = objectSpec.createObject();
        final ViewModelFacet facet = objectSpec.getFacet(ViewModelFacet.class);
        facet.initialize(pojo, memento);
        final ObjectAdapter adapter = getAdapterManager().adapterFor(pojo);
        return objectSpec.initialize(adapter);
    }

    /**
     * Creates a new instance of the specified type and returns an adapter with
     * an aggregated OID that show that this new object belongs to the specified
     * parent. The new object's resolved state is set to
     * {@link ResolveState#RESOLVED} as it state is part of it parent.
     *
     * <p>
     * While creating the object it will be initialised with default values and
     * its created lifecycle method (its logical constructor) will be invoked.
     *
     * <p>
     * This method is ultimately delegated to by the
     * {@link org.apache.isis.applib.DomainObjectContainer}.
     */
    public ObjectAdapter createAggregatedInstance(final ObjectSpecification objectSpec, final ObjectAdapter parentAdapter) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating aggregated instance of " + objectSpec);
        }
        final Object pojo = objectSpec.createObject();
        final ObjectAdapter adapter = getAdapterManager().adapterFor(pojo, parentAdapter);
        // returned adapter's ResolveState will either be TRANSIENT or GHOST
        objectSpec.initialize(adapter);
        if (adapter.isGhost()) {
            adapter.changeState(ResolveState.RESOLVING);
            adapter.changeState(ResolveState.RESOLVED);
        }
        return adapter;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // findInstances, getInstances
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Finds and returns instances that match the specified query.
     *
     * <p>
     * The {@link QueryCardinality} determines whether all instances or just the
     * first matching instance is returned.
     *
     * @throws org.apache.isis.core.runtime.persistence.UnsupportedFindException
     *             if the criteria is not support by this persistor
     */
    public <T> ObjectAdapter findInstances(final Query<T> query, final QueryCardinality cardinality) {
        final PersistenceQuery persistenceQuery = createPersistenceQueryFor(query, cardinality);
        if (persistenceQuery == null) {
            throw new IllegalArgumentException("Unknown query type: " + query.getDescription());
        }
        return findInstances(persistenceQuery);
    }

    /**
     * Finds and returns instances that match the specified
     * {@link PersistenceQuery}.
     *
     * <p>
     * Compared to {@link #findInstances(Query, QueryCardinality)}, not that
     * there is no {@link QueryCardinality} parameter. That's because
     * {@link PersistenceQuery} intrinsically carry the knowledge as to how many
     * rows they return.
     *
     * @throws org.apache.isis.core.runtime.persistence.UnsupportedFindException
     *             if the criteria is not support by this persistor
     */
    public ObjectAdapter findInstances(final PersistenceQuery persistenceQuery) {
        final List<ObjectAdapter> instances = getInstances(persistenceQuery);
        final ObjectSpecification specification = persistenceQuery.getSpecification();
        final FreeStandingList results = new FreeStandingList(specification, instances);
        return getAdapterManager().adapterFor(results);
    }

    /**
     * Converts the {@link Query applib representation of a query} into the
     * {@link PersistenceQuery NOF-internal representation}.
     */
    protected final PersistenceQuery createPersistenceQueryFor(final Query<?> query, final QueryCardinality cardinality) {
        return persistenceQueryFactory.createPersistenceQueryFor(query, cardinality);
    }


    protected List<ObjectAdapter> getInstances(final PersistenceQuery persistenceQuery) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstances matching " + persistenceQuery);
        }
        return getInstancesFromPersistenceLayer(persistenceQuery);
    }

    private List<ObjectAdapter> getInstancesFromPersistenceLayer(final PersistenceQuery persistenceQuery) {
        return getTransactionManager().executeWithinTransaction(new TransactionalClosureWithReturnAbstract<List<ObjectAdapter>>() {
            @Override
            public List<ObjectAdapter> execute() {
                return objectStore.loadInstancesAndAdapt(persistenceQuery);
            }

            @Override
            public void onSuccess() {
                clearAllDirty();
            }
        });
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Manual dirtying support
    // ///////////////////////////////////////////////////////////////////////////

    public boolean isCheckObjectsForDirtyFlag() {
        return dirtiableSupport;
    }


    /**
     * Mark as {@link #objectChanged(ObjectAdapter) changed } all
     * {@link Dirtiable} objects that have been
     * {@link Dirtiable#markDirty(ObjectAdapter) manually marked} as dirty.
     * 
     * <p>
     * If {@link #isCheckObjectsForDirtyFlag() enabled}, will mark as
     * {@link #objectChanged(ObjectAdapter) changed} any {@link Dirtiable}
     * objects that have manually been
     * {@link Dirtiable#markDirty(ObjectAdapter) marked as dirty}.
     * 
     * <p>
     * Called by the {@link IsisTransactionManager}.
     */
    public void objectChangedAllDirty() {
        if (!dirtiableSupport) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("marking as changed any objects that have been manually set as dirty");
        }
        for (final ObjectAdapter adapter : adapterManager) {
            if (adapter.getSpecification().isDirty(adapter)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  found dirty object " + adapter);
                }
                objectChanged(adapter);
                adapter.getSpecification().clearDirty(adapter);
            }
        }
    }

    /**
     * Set as {@link Dirtiable#clearDirty(ObjectAdapter) clean} any
     * {@link Dirtiable} objects.
     */
    public synchronized void clearAllDirty() {
        if (!isCheckObjectsForDirtyFlag()) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("cleaning any manually dirtied objects");
        }

        for (final ObjectAdapter object : adapterManager) {
            if (object.getSpecification().isDirty(object)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  found dirty object " + object);
                }
                object.getSpecification().clearDirty(object);
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Services
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the OID for the adapted service. This allows a service object to
     * be given the same OID that it had when it was created in a different
     * session.
     */
    protected RootOid getOidForService(final ObjectSpecification serviceSpec) {
        return getOidForServiceFromPersistenceLayer(serviceSpec);
    }

    /**
     * Registers the specified service as having the specified OID.
     */
    protected void registerService(final RootOid rootOid) {
        objectStore.registerService(rootOid);
    }

    // REVIEW why does this get called multiple times when starting up
    public List<ObjectAdapter> getServices() {
        final List<Object> services = servicesInjector.getRegisteredServices();
        final List<ObjectAdapter> serviceAdapters = Lists.newArrayList();
        for (final Object servicePojo : services) {
            serviceAdapters.add(getService(servicePojo));
        }
        return serviceAdapters;
    }

    private ObjectAdapter getService(final Object servicePojo) {
        final ObjectSpecification serviceSpecification = getSpecificationLoader().loadSpecification(servicePojo.getClass());
        final RootOid oid = getOidForService(serviceSpecification);
        final ObjectAdapter serviceAdapter = getAdapterManager().mapRecreatedPojo(oid, servicePojo);

        serviceAdapter.markAsResolvedIfPossible();
        return serviceAdapter;
    }

    private RootOid getOidForServiceFromPersistenceLayer(final ObjectSpecification serviceSpecification) {
        final ObjectSpecId objectSpecId = serviceSpecification.getSpecId();
        RootOid oid = servicesByObjectType.get(objectSpecId);
        if (oid == null) {
            oid = objectStore.getOidForService(serviceSpecification);
            servicesByObjectType.put(objectSpecId, oid);
        }
        return oid;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // fixture installation
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Determine if the object store has been initialized with its set of start
     * up objects.
     * 
     * <p>
     * This method is called only once after the
     * {@link ApplicationScopedComponent#init()} has been called. If this flag
     * returns <code>false</code> the framework will run the fixtures to
     * initialise the persistor.
     * 
     * <p>
     * Returns the cached value of {@link ObjectStore#isFixturesInstalled()
     * whether fixtures are installed} from the
     * {@link PersistenceSessionFactory} (provided it implements
     * {@link FixturesInstalledFlag}), otherwise queries {@link ObjectStore}
     * directly.
     * <p>
     * This caching is important because if we've determined, for a given run,
     * that fixtures are not installed, then we don't want to change our mind by
     * asking the object store again in another session.
     * 
     * @see FixturesInstalledFlag
     */
    public boolean isFixturesInstalled() {
        final PersistenceSessionFactory persistenceSessionFactory = getPersistenceSessionFactory();
        if (persistenceSessionFactory.isFixturesInstalled() == null) {
            persistenceSessionFactory.setFixturesInstalled(objectStore.isFixturesInstalled());
        }
        return persistenceSessionFactory.isFixturesInstalled();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("finalizing persistence session");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // loadObject, reload
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Loads the object identified by the specified {@link TypedOid} from the
     * persisted set of objects.
     */
    public ObjectAdapter loadObject(final TypedOid oid) {
        
        // REVIEW: 
        // this method does not account for the oid possibly being a view model
        // alternatively, can call getAdapterManager().adapterFor(oid); this code
        // delegates to the PojoRecreator which *does* take view models into account
        //
        // it's possible, therefore, that existing callers to this method (the Scimpi viewer)
        // could be refactored to use getAdapterManager().adapterFor(...)
        ensureThatArg(oid, is(notNullValue()));

        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        if (adapter != null) {
            return adapter;
        }

        return loadMappedObjectFromObjectStore(oid);
    }

    private ObjectAdapter loadMappedObjectFromObjectStore(final TypedOid oid) {
        final ObjectAdapter adapter = getTransactionManager().executeWithinTransaction(new TransactionalClosureWithReturnAbstract<ObjectAdapter>() {
            @Override
            public ObjectAdapter execute() {
                return objectStore.loadInstanceAndAdapt(oid);
            }
        });
        return adapter;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // resolveImmediately, resolveField
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Re-initialises the fields of an object. If the object is unresolved then
     * the object's missing data should be retrieved from the persistence
     * mechanism and be used to set up the value objects and associations.
     */
    public void resolveImmediately(final ObjectAdapter adapter) {
        // synchronize on the current session because getting race
        // conditions, I think between different UI threads when running
        // with DnD viewer + in-memory object store +
        // cglib bytecode enhancement
        synchronized (getAuthenticationSession()) {
            if (!adapter.canTransitionToResolving()) {
                return;
            }
            Assert.assertFalse("only resolve object that is not yet resolved", adapter, adapter.isResolved());
            Assert.assertTrue("only resolve object that is persistent", adapter, adapter.representsPersistent());
            resolveImmediatelyFromPersistenceLayer(adapter);
            if (LOG.isDebugEnabled()) {
                // don't log object - its toString() may use the unresolved
                // field, or unresolved collection
                LOG.debug("resolved: " + adapter.getSpecification().getShortIdentifier() + " " + adapter.getResolveState().code() + " " + adapter.getOid());
            }
        }
    }

    private void resolveImmediatelyFromPersistenceLayer(final ObjectAdapter adapter) {
        getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void preExecute() {
                // previously there was callback to LoadingCallbackFacet.class
                // for JDO objectstore at least this codepath does not fire, and JDO (not surprisingly)
                // provides no preLoad callback.
                //
                // for consistency, have therefore removed this call.
            }

            @Override
            public void execute() {
                objectStore.resolveImmediately(adapter);
            }

            @Override
            public void onSuccess() {
                // previously there was callback to LoadedCallbackFacet.class
                // for JDO objectstore at least this codepath does not fire, and instead we rely on the
                // JDO lifecycle event (IsisLifecycleListener/FrameworkSynchronizer) to perform the callback.
                //
                // have therefore removed this call.
            }

            @Override
            public void onFailure() {
                // should we do something here?
            }
        });
    }

    // ////////////////////////////////////////////////////////////////
    // makePersistent
    // ////////////////////////////////////////////////////////////////

    /**
     * Makes an {@link ObjectAdapter} persistent. The specified object should be
     * stored away via this object store's persistence mechanism, and have a
     * new and unique OID assigned to it. The object, should also be added to
     * the {@link org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault} as the object is implicitly 'in use'.
     *
     * <p>
     * If the object has any associations then each of these, where they aren't
     * already persistent, should also be made persistent by recursively calling
     * this method.
     *
     * <p>
     * If the object to be persisted is a collection, then each element of that
     * collection, that is not already persistent, should be made persistent by
     * recursively calling this method.
     *
     * @see #remapAsPersistent(ObjectAdapter)
     */
    public void makePersistent(final ObjectAdapter adapter) {
        if (adapter.representsPersistent()) {
            throw new NotPersistableException("Object already persistent: " + adapter);
        }
        if (!adapter.getSpecification().persistability().isPersistable()) {
            throw new NotPersistableException("Object is not persistable: " + adapter);
        }
        final ObjectSpecification specification = adapter.getSpecification();
        if (specification.isService()) {
            throw new NotPersistableException("Cannot persist services: " + adapter);
        }

        makePersistentInPersistenceLayer(adapter);
    }

    protected void makePersistentInPersistenceLayer(final ObjectAdapter adapter) {
        getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void preExecute() {
                // callbacks are called by the persist algorithm
            }

            @Override
            public void execute() {
                persistAlgorithm.makePersistent(adapter, PersistenceSession.this);

                // clear out the map of transient -> persistent
                PersistenceSession.this.persistentByTransient.clear();
            }

            @Override
            public void onSuccess() {
                // callbacks are called by the persist algorithm
            }

            @Override
            public void onFailure() {
                // TODO: some sort of callback?
            }
        });
    }

    // ///////////////////////////////////////////////////////////////////////////
    // objectChanged
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Mark the {@link ObjectAdapter} as changed, and therefore requiring
     * flushing to the persistence mechanism.
     */
    public void objectChanged(final ObjectAdapter adapter) {

        if (adapter.isTransient() || (adapter.isParented() && adapter.getAggregateRoot().isTransient())) {
            return;
        }

        if (adapter.respondToChangesInPersistentObjects()) {
            if (isImmutable(adapter)) {
                // previously used to throw
                // new
                // ObjectPersistenceException("cannot change immutable object");
                // however, since the the bytecode enhancers effectively make
                // calling objectChanged() the responsibility of the framework,
                // we may as well now do the check here and ignore if doesn't
                // apply.
                return;
            }

            addObjectChangedForPersistenceLayer(adapter);
        }
    }


    private void addObjectChangedForPersistenceLayer(final ObjectAdapter adapter) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("object change to be persisted " + adapter.getOid());
        }
        getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void preExecute() {
                // previously called the UpdatingCallbackFacet here (not sure if actually called though, see ISIS-796);
                // at any rate, is now done by the object store.
            }

            @Override
            public void execute() {
                final SaveObjectCommand saveObjectCommand = objectStore.createSaveObjectCommand(adapter);
                getTransactionManager().addCommand(saveObjectCommand);
            }

            @Override
            public void onSuccess() {
                // previously called the UpdatedCallbackFacet here (though not sure if actually called though, see in part ISIS-796);
                // at any rate, is now done by the object store.
            }

            @Override
            public void onFailure() {
                // should we do something here?
            }
        });
    }

    // ///////////////////////////////////////////////////////////////////////////
    // destroyObject
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Removes the specified object from the system. The specified object's data
     * should be removed from the persistence mechanism.
     */
    public void destroyObject(final ObjectAdapter adapter) {
        final ObjectSpecification spec = adapter.getSpecification();
        if (spec.isParented()) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroyObject " + adapter);
        }
        destroyObjectInPersistenceLayer(adapter);
    }

    private void destroyObjectInPersistenceLayer(final ObjectAdapter adapter) {
        getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void preExecute() {
                // previously called the RemovingCallbackFacet here; now done through the object store (see ISIS-796).
            }

            @Override
            public void execute() {
                final DestroyObjectCommand command = objectStore.createDestroyObjectCommand(adapter);
                getTransactionManager().addCommand(command);
            }

            @Override
            public void onSuccess() {
                // previously called the RemovedCallbackFacet here; now done through the object store (see ISIS-796).
            }

            @Override
            public void onFailure() {
                // some sort of callback?
            }
        });
    }


    // ///////////////////////////////////////////////////////////////////////////
    // ToPersistObjectSet
    // ///////////////////////////////////////////////////////////////////////////

    private Map<Oid, Oid> persistentByTransient = Maps.newHashMap();

    /**
     * Callback from the {@link PersistAlgorithm} (or equivalent; some object
     * stores such as Hibernate will use listeners instead) to indicate that the
     * {@link ObjectAdapter adapter} is persisted, and the adapter maps should
     * be updated.
     * 
     * <p>
     * The object store is expected to have already updated the {@link Oid}
     * state and the {@link ResolveState} . Some object stores (again, we're
     * thinking Hibernate here) might also have updated collections, both the
     * Oid of the collection and the pojo wrapped by the adapter.
     * 
     * <p>
     * The {@link PersistAlgorithm} is called from
     * {@link #makePersistent(ObjectAdapter)}.
     * 
     * @see #remapAsPersistent(ObjectAdapter)
     */
    public void remapAsPersistent(final ObjectAdapter adapter) {
        final Oid transientOid = adapter.getOid();
        adapterManager.remapAsPersistent(adapter, null);
        final Oid persistentOid = adapter.getOid();
        persistentByTransient.put(transientOid, persistentOid);
    }

    /**
     * To support ISIS-234; keep track, for the duration of the transaction only,
     * of the old transient {@link Oid}s and their corresponding persistent {@link Oid}s.
     */
    public Oid remappedFrom(final Oid transientOid) {
        return persistentByTransient.get(transientOid);
    }

    /**
     * Uses the {@link ObjectStore} to
     * {@link ObjectStore#createCreateObjectCommand(ObjectAdapter) create} a
     * {@link CreateObjectCommand}, and adds to the
     * {@link IsisTransactionManager}.
     */
    public void addCreateObjectCommand(final ObjectAdapter object) {
        getTransactionManager().addCommand(objectStore.createCreateObjectCommand(object));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public String debugTitle() {
        return "Object Store Persistor";
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendTitle(getClass().getName());
        debug.appendln("container", servicesInjector);
        debug.appendln();

        adapterManager.debugData(debug);
        debug.appendln();

        debug.appendln("manually dirtiable support (isDirty flag)?", dirtiableSupport);

        debug.appendTitle("OID Generator");
        oidGenerator.debugData(debug);
        debug.appendln();

        debug.appendTitle("Services");
        for (final Object servicePojo : servicesInjector.getRegisteredServices()) {
            final String id = ServiceUtil.id(servicePojo);
            final Class<? extends Object> serviceClass = servicePojo.getClass();
            final ObjectSpecification serviceSpecification = getSpecificationLoader().loadSpecification(serviceClass);
            final String serviceClassName = serviceClass.getName();
            final Oid oidForService = getOidForService(serviceSpecification);
            final String serviceId = id + (id.equals(serviceClassName) ? "" : " (" + serviceClassName + ")");
            debug.appendln(oidForService != null ? oidForService.toString() : "[NULL]", serviceId);
        }
        debug.appendln();

        debug.appendTitle("Persistor");
        getTransactionManager().debugData(debug);
        debug.appendln("Persist Algorithm", persistAlgorithm);
        debug.appendln("Object Store", objectStore);
        debug.appendln();

        objectStore.debugData(debug);
    }

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        if (objectStore != null) {
            toString.append("objectStore", objectStore.name());
        }
        if (persistAlgorithm != null) {
            toString.append("persistAlgorithm", persistAlgorithm.name());
        }
        return toString.toString();
    }

    // ////////////////////////////////////////////////////////////////////
    // Helpers
    // ////////////////////////////////////////////////////////////////////

    protected boolean isImmutable(final ObjectAdapter adapter) {
        final ObjectSpecification noSpec = adapter.getSpecification();
        return ImmutableFacetUtils.isAlwaysImmutable(noSpec) || (ImmutableFacetUtils.isImmutableOncePersisted(noSpec) && adapter.representsPersistent());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // test support
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * For testing purposes only.
     */
    public void testReset() {
        objectStore.reset();
        adapterManager.reset();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Dependencies (injected in constructor, possibly implicitly)
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Injected by constructor.
     */
    public ObjectStore getObjectStore() {
        return objectStore;
    }


    /**
     * The configured {@link ObjectAdapterFactory}.
     * 
     * <p>
     * Injected in constructor.
     */
    public final ObjectAdapterFactory getObjectAdapterFactory() {
        return objectAdapterFactory;
    }

    /**
     * The configured {@link OidGenerator}.
     * 
     * <p>
     * Injected in constructor.
     */
    public final OidGenerator getOidGenerator() {
        return oidGenerator;
    }

    /**
     * The configured {@link AdapterManager}.
     *
     * Access to looking up (and possibly lazily loading) adapters.
     *
     * <p>
     * However, manipulating of adapters is not part of this interface.
     *
     * <p>
     * Injected in constructor.
     */
    public final AdapterManagerDefault getAdapterManager() {
        return adapterManager;
    }

    /**
     * The configured {@link ServicesInjectorSpi}.
     */
    public ServicesInjectorSpi getServicesInjector() {
        return servicesInjector;
    }

    /**
     * The configured {@link ObjectFactory}.
     * 
     * <p>
     * Obtained indirectly from the injected reflector.
     */
    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Dependencies (injected)
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * The configured {@link IsisTransactionManager}.
     */
    public IsisTransactionManager getTransactionManager() {
        return transactionManager;
    }

    // for testing only
    void setTransactionManager(final IsisTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////////////////////////

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

}
