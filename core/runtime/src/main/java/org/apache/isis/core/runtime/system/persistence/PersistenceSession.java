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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.query.QueryFindAllInstances;
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
import org.apache.isis.core.metamodel.services.container.query.QueryFindByPattern;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByTitle;
import org.apache.isis.core.metamodel.spec.*;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.persistence.FixturesInstalledFlag;
import org.apache.isis.core.runtime.persistence.NotPersistableException;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapterFactory;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.core.runtime.persistence.adaptermanager.PojoRecreatorUnified;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.PersistAlgorithmUnified;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.ToPersistObjectSet;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.core.runtime.persistence.query.*;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.*;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.*;

public class PersistenceSession implements Persistor, EnlistedObjectDirtying, ToPersistObjectSet, RecreatedPojoRemapper, AdapterLifecycleTransitioner, SessionScopedComponent, DebuggableWithTitle {

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

    private boolean dirtiableSupport;

    /**
     * Injected using setter-based injection.
     */
    private IsisTransactionManager transactionManager;

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

        setState(State.NOT_INITIALIZED);

        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + this);
        }

        this.objectStore = objectStore;
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
            ObjectAdapter serviceAdapter = 
                    existingOid == null
                            ? getAdapterManager().adapterFor(service) 
                            : mapRecreatedPojo(existingOid, service);
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
    public <T> T getServiceOrNull(Class<T> serviceType) {
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
        } catch(RuntimeException ex) {
            // ignore
        }

        try {
            adapterManager.close();
        } catch(RuntimeException ex) {
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

    @Override
    public ObjectAdapter createTransientInstance(final ObjectSpecification objectSpec) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating transient instance of " + objectSpec);
        }
        final Object pojo = objectSpec.createObject();
        final ObjectAdapter adapter = getAdapterManager().adapterFor(pojo);
        return objectSpec.initialize(adapter);
    }

    public ObjectAdapter createViewModelInstance(ObjectSpecification objectSpec, String memento) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating view model instance of " + objectSpec);
        }
        final Object pojo = objectSpec.createObject();
        ViewModelFacet facet = objectSpec.getFacet(ViewModelFacet.class);
        facet.initialize(pojo, memento);
        final ObjectAdapter adapter = getAdapterManager().adapterFor(pojo);
        return objectSpec.initialize(adapter);
    }

    @Override
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

    @Override
    public <T> ObjectAdapter findInstances(final Query<T> query, final QueryCardinality cardinality) {
        final PersistenceQuery persistenceQuery = createPersistenceQueryFor(query, cardinality);
        if (persistenceQuery == null) {
            throw new IllegalArgumentException("Unknown query type: " + query.getDescription());
        }
        return findInstances(persistenceQuery);
    }

    @Override
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("createPersistenceQueryFor: " + query.getDescription());
        }
        final ObjectSpecification noSpec = specFor(query);
        if (query instanceof QueryFindAllInstances) {
            final QueryFindAllInstances<?> queryFindAllInstances = (QueryFindAllInstances<?>) query;
            return new PersistenceQueryFindAllInstances(noSpec, queryFindAllInstances.getStart(), queryFindAllInstances.getCount());
        }
        if (query instanceof QueryFindByTitle) {
            final QueryFindByTitle<?> queryByTitle = (QueryFindByTitle<?>) query;
            final String title = queryByTitle.getTitle();
            return new PersistenceQueryFindByTitle(noSpec, title, queryByTitle.getStart(), queryByTitle.getCount());
        }
        if (query instanceof QueryFindByPattern) {
            final QueryFindByPattern<?> queryByPattern = (QueryFindByPattern<?>) query;
            final Object pattern = queryByPattern.getPattern();
            final ObjectAdapter patternAdapter = getAdapterManager().adapterFor(pattern);
            return new PersistenceQueryFindByPattern(noSpec, patternAdapter, queryByPattern.getStart(), queryByPattern.getCount());
        }
        if (query instanceof QueryDefault) {
            final QueryDefault<?> queryDefault = (QueryDefault<?>) query;
            final String queryName = queryDefault.getQueryName();
            final Map<String, ObjectAdapter> argumentsAdaptersByParameterName = wrap(queryDefault.getArgumentsByParameterName());
            return new PersistenceQueryFindUsingApplibQueryDefault(noSpec, queryName, argumentsAdaptersByParameterName, cardinality, queryDefault.getStart(), queryDefault.getCount());
        }
        // fallback; generic serializable applib query.
        return new PersistenceQueryFindUsingApplibQuerySerializable(noSpec, query, cardinality);
    }

    private ObjectSpecification specFor(final Query<?> query) {
        return getSpecificationLoader().loadSpecification(query.getResultType());
    }

    /**
     * Converts a map of pojos keyed by string to a map of adapters keyed by the
     * same strings.
     */
    private Map<String, ObjectAdapter> wrap(final Map<String, Object> argumentsByParameterName) {
        final Map<String, ObjectAdapter> argumentsAdaptersByParameterName = new HashMap<String, ObjectAdapter>();
        for (final Map.Entry<String, Object> entry : argumentsByParameterName.entrySet()) {
            final String parameterName = entry.getKey();
            final Object argument = argumentsByParameterName.get(parameterName);
            final ObjectAdapter argumentAdapter = argument != null ? getAdapterManager().adapterFor(argument) : null;
            argumentsAdaptersByParameterName.put(parameterName, argumentAdapter);
        }
        return argumentsAdaptersByParameterName;
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

    /**
     * @see #setDirtiableSupport(boolean)
     */
    public boolean isCheckObjectsForDirtyFlag() {
        return dirtiableSupport;
    }

    /**
     * Whether to notice {@link Dirtiable manually-dirtied} objects.
     */
    public void setDirtiableSupport(final boolean checkObjectsForDirtyFlag) {
        this.dirtiableSupport = checkObjectsForDirtyFlag;
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
    @Override
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

    @Override
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
    protected RootOid getOidForService(ObjectSpecification serviceSpec) {
        return getOidForServiceFromPersistenceLayer(serviceSpec);
    }

    /**
     * Registers the specified service as having the specified OID.
     */
    protected void registerService(final RootOid rootOid) {
        objectStore.registerService(rootOid);
    }

    public ObjectAdapter getService(final String id) {
        for (final Object service : servicesInjector.getRegisteredServices()) {
            // TODO this (ServiceUtil) uses reflection to access the service
            // object; it should use the
            // reflector, ie call allServices first and use the returned array
            if (id.equals(ServiceUtil.id(service))) {
                return getService(service);
            }
        }
        return null;
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
        final ObjectAdapter serviceAdapter = mapRecreatedPojo(oid, servicePojo);

        serviceAdapter.markAsResolvedIfPossible();
        return serviceAdapter;
    }

    /**
     * Has any services.
     */
    public boolean hasServices() {
        return servicesInjector.getRegisteredServices().size() > 0;
    }

    private RootOid getOidForServiceFromPersistenceLayer(ObjectSpecification serviceSpecification) {
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
        if (persistenceSessionFactory instanceof FixturesInstalledFlag) {
            final FixturesInstalledFlag fixturesInstalledFlag = (FixturesInstalledFlag) persistenceSessionFactory;
            if (fixturesInstalledFlag.isFixturesInstalled() == null) {
                fixturesInstalledFlag.setFixturesInstalled(objectStore.isFixturesInstalled());
            }
            return fixturesInstalledFlag.isFixturesInstalled();
        } else {
            return objectStore.isFixturesInstalled();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("finalizing persistence session");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // loadObject, reload
    // ///////////////////////////////////////////////////////////////////////////

    @Override
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
        ObjectAdapter adapter = getTransactionManager().executeWithinTransaction(new TransactionalClosureWithReturnAbstract<ObjectAdapter>() {
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

    @Override
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

    @Override
    public void resolveField(final ObjectAdapter objectAdapter, final ObjectAssociation field) {
        if (field.isNotPersisted()) {
            return;
        }
        if (field.isOneToManyAssociation()) {
            return;
        }
        if (field.getSpecification().isParented()) {
            return;
        }
        if (field.getSpecification().isValue()) {
            return;
        }
        final ObjectAdapter referenceAdapter = field.get(objectAdapter);
        if (referenceAdapter == null || referenceAdapter.isResolved()) {
            return;
        }
        if (!referenceAdapter.representsPersistent()) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            // don't log object - it's toString() may use the unresolved field
            // or unresolved collection
            LOG.info("resolve field " + objectAdapter.getSpecification().getShortIdentifier() + "." + field.getId() + ": " + referenceAdapter.getSpecification().getShortIdentifier() + " " + referenceAdapter.getResolveState().code() + " " + referenceAdapter.getOid());
        }
        resolveFieldFromPersistenceLayer(objectAdapter, field);
    }

    private void resolveFieldFromPersistenceLayer(final ObjectAdapter objectAdapter, final ObjectAssociation field) {
        getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void execute() {
                objectStore.resolveField(objectAdapter, field);
            }
        });
    }

    // ////////////////////////////////////////////////////////////////
    // makePersistent
    // ////////////////////////////////////////////////////////////////

    @Override
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

    @Override
    public void objectChanged(final ObjectAdapter adapter) {

        if (adapter.isTransient() || (adapter.isParented() && adapter.getAggregateRoot().isTransient())) {
            addObjectChangedForPresentationLayer(adapter);
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
            addObjectChangedForPresentationLayer(adapter);
        }
        if (adapter.respondToChangesInPersistentObjects() || adapter.isTransient()) {
            addObjectChangedForPresentationLayer(adapter);
        }
    }

    private void addObjectChangedForPresentationLayer(final ObjectAdapter adapter) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("object change to update presentation layer " + adapter.getOid());
        }
        getUpdateNotifier().addChangedObject(adapter);
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
        getUpdateNotifier().addChangedObject(adapter);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // destroyObject
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public void destroyObject(final ObjectAdapter adapter) {
        ObjectSpecification spec = adapter.getSpecification();
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
    // hasInstances
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean hasInstances(final ObjectSpecification specification) {
        if (LOG.isInfoEnabled()) {
            LOG.info("hasInstances of " + specification.getShortIdentifier());
        }
        return hasInstancesFromPersistenceLayer(specification);
    }

    private boolean hasInstancesFromPersistenceLayer(final ObjectSpecification specification) {
        return getTransactionManager().executeWithinTransaction(new TransactionalClosureWithReturnAbstract<Boolean>() {
            @Override
            public Boolean execute() {
                return objectStore.hasInstances(specification);
            }
        });
    }

    // ///////////////////////////////////////////////////////////////////////////
    // RecreatedPojoRemapper
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter mapRecreatedPojo(Oid oid, Object recreatedPojo) {
        return adapterManager.mapRecreatedPojo(oid, recreatedPojo);
    }

    @Override
    public void remapRecreatedPojo(ObjectAdapter adapter, Object recreatedPojo) {
        adapterManager.remapRecreatedPojo(adapter, recreatedPojo);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // AdapterLifecycleTransitioner
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public void remapAsPersistent(ObjectAdapter adapter, RootOid hintRootOid) {
        adapterManager.remapAsPersistent(adapter, hintRootOid);
    }

    @Override
    public void removeAdapter(ObjectAdapter adapter) {
        adapterManager.removeAdapter(adapter);
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
    @Override
    public void remapAsPersistent(final ObjectAdapter adapter) {
        final Oid transientOid = adapter.getOid();
        adapterManager.remapAsPersistent(adapter, null);
        final Oid persistentOid = adapter.getOid();
        persistentByTransient.put(transientOid, persistentOid);
    }

    @Override
    public Oid remappedFrom(Oid transientOid) {
        return persistentByTransient.get(transientOid);
    }

    /**
     * Uses the {@link ObjectStore} to
     * {@link ObjectStore#createCreateObjectCommand(ObjectAdapter) create} a
     * {@link CreateObjectCommand}, and adds to the
     * {@link IsisTransactionManager}.
     */
    @Override
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


    private UpdateNotifier getUpdateNotifier() {
        return getTransactionManager().getTransaction().getUpdateNotifier();
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
     * <p>
     * Injected in constructor.
     */
    public final AdapterManager getAdapterManager() {
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
     * Inject the {@link IsisTransactionManager}.
     * 
     * <p>
     * This must be injected using setter-based injection rather than through
     * the constructor because there is a bidirectional relationship between the
     * this class and the {@link IsisTransactionManager}.
     * 
     * @see #getTransactionManager()
     */
    public void setTransactionManager(final IsisTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * The configured {@link IsisTransactionManager}.
     * 
     * @see #setTransactionManager(IsisTransactionManager)
     */
    public IsisTransactionManager getTransactionManager() {
        return transactionManager;
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
