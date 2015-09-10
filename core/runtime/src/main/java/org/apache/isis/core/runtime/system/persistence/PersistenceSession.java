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
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.FreeStandingList;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.persistence.FixturesInstalledFlag;
import org.apache.isis.core.runtime.persistence.NotPersistableException;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapterFactory;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosureAbstract;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosureWithReturnAbstract;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.commands.DataNucleusCreateObjectCommand;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.commands.DataNucleusDeleteObjectCommand;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatContext;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

public class PersistenceSession implements SessionScopedComponent, DebuggableWithTitle {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceSession.class);

    //region > constructor, fields
    private final ObjectFactory objectFactory;

    private final PersistenceSessionFactory persistenceSessionFactory;
    private final PojoAdapterFactory objectAdapterFactory;
    private final OidGenerator oidGenerator;
    private final AdapterManagerDefault adapterManager;

    private final PersistAlgorithm persistAlgorithm ;
    private final ObjectStore objectStore;
    private final Map<ObjectSpecId, RootOid> servicesByObjectType = Maps.newHashMap();

    private final PersistenceQueryFactory persistenceQueryFactory;
    private final IsisConfiguration configuration;
    private final SpecificationLoaderSpi specificationLoader;
    private final AuthenticationSession authenticationSession;

    private final ServicesInjectorSpi servicesInjector;

    // not final only for testing purposes
    private IsisTransactionManager transactionManager;

    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     */
    public PersistenceSession(
            final PersistenceSessionFactory persistenceSessionFactory,
            final IsisConfiguration configuration,
            final SpecificationLoaderSpi specificationLoader,
            final AuthenticationSession authenticationSession) {

        ensureThatArg(persistenceSessionFactory, is(not(nullValue())), "persistence session factory required");

        // injected
        this.configuration = configuration;
        this.specificationLoader = specificationLoader;
        this.authenticationSession = authenticationSession;

        this.persistenceSessionFactory = persistenceSessionFactory;
        this.servicesInjector = persistenceSessionFactory.getServicesInjector();

        // sub-components
        final DataNucleusApplicationComponents applicationComponents = persistenceSessionFactory .getApplicationComponents();
        this.objectStore = new ObjectStore(this, specificationLoader, configuration, applicationComponents);

        this.objectFactory = new ObjectFactory(this, servicesInjector);
        this.oidGenerator = new OidGenerator(this, specificationLoader);
        this.adapterManager = new AdapterManagerDefault();
        this.persistAlgorithm = new PersistAlgorithm();
        this.objectAdapterFactory = new PojoAdapterFactory(adapterManager, specificationLoader, authenticationSession);

        this.persistenceQueryFactory = new PersistenceQueryFactory(getSpecificationLoader(), adapterManager);
        this.transactionManager = new IsisTransactionManager(this, objectStore, servicesInjector);

        setState(State.NOT_INITIALIZED);

        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + this);
        }
    }

    //endregion

    //region > open

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
        final List<Object> registeredServices =
                persistenceSessionFactory.getServicesInjector().getRegisteredServices();
        createServiceAdapters(registeredServices);
    }

    /**
     * Creates {@link ObjectAdapter adapters} for the service list.
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
        return persistenceSessionFactory.getServicesInjector().lookupService(serviceType);
    }

    //endregion

    //region > close


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

        try {
            try {
                objectStore.close();
            } catch(final Throwable ex) {
                // ignore
                LOG.error("objectStore#close() failed while closing the session; continuing to avoid memory leakage");
            }

            try {
                adapterManager.close();
            } catch(final Throwable ex) {
                // ignore
                LOG.error("adapterManager#close() failed while closing the session; continuing to avoid memory leakage");
            }

        } finally {
            setState(State.CLOSED);
        }

    }
    //endregion

    //region > State

    private enum State {
        NOT_INITIALIZED, OPEN, CLOSED
    }

    private State state;

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

    public void ensureOpened() {
        ensureStateIs(State.OPEN);
    }

    private void ensureStateIs(final State stateRequired) {
        if (state == stateRequired) {
            return;
        }
        throw new IllegalStateException("State is: " + state + "; should be: " + stateRequired);
    }



    //endregion

    //region > createTransientInstance, createViewModelInstance

    /**
     * Create a root or standalone {@link ObjectAdapter adapter}.
     *
     * <p>
     * Creates a new instance of the specified type and returns it in an adapter.
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
    //endregion

    //region > findInstances, getInstances

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
            }
        });
    }
    //endregion

    //region > Services

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
        final List<Object> services = persistenceSessionFactory.getServicesInjector().getRegisteredServices();
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

    //endregion

    //region > fixture installation

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
     * {@link PersistenceSessionFactory}.
     * <p>
     * This caching is important because if we've determined, for a given run,
     * that fixtures are not installed, then we don't want to change our mind by
     * asking the object store again in another session.
     * 
     * @see FixturesInstalledFlag
     */
    public boolean isFixturesInstalled() {
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
    //endregion

    //region > loadObject, reload
    /**
     * Loads the object identified by the specified {@link RootOid} from the
     * persisted set of objects.
     */
    public ObjectAdapter loadObject(final RootOid oid) {
        
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

    private ObjectAdapter loadMappedObjectFromObjectStore(final RootOid oid) {
        final ObjectAdapter adapter = getTransactionManager().executeWithinTransaction(new TransactionalClosureWithReturnAbstract<ObjectAdapter>() {
            @Override
            public ObjectAdapter execute() {
                return objectStore.loadInstanceAndAdapt(oid);
            }
        });
        return adapter;
    }

    //endregion

    //region > resolveImmediately, resolveField

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
            Assert.assertTrue("only resolve object that is persistent", adapter, adapter.representsPersistent());
            resolveImmediatelyFromPersistenceLayer(adapter);
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

    //endregion

    //region > makePersistent

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

    //endregion

    //region > destroyObject

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
                final DestroyObjectCommand command = createDestroyObjectCommand(adapter);
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

    //endregion

    //region > createXxxCommand
    /**
     * Makes an {@link ObjectAdapter} persistent. The specified object should be
     * stored away via this object store's persistence mechanism, and have an
     * new and unique OID assigned to it (by calling the object's
     * <code>setOid</code> method). The object, should also be added to the
     * cache as the object is implicitly 'in use'.
     *
     * <p>
     * If the object has any associations then each of these, where they aren't
     * already persistent, should also be made persistent by recursively calling
     * this method.
     * </p>
     *
     * <p>
     * If the object to be persisted is a collection, then each element of that
     * collection, that is not already persistent, should be made persistent by
     * recursively calling this method.
     * </p>
     *
     */
    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter adapter) {
        ensureOpened();
        ensureInSession();

        if (LOG.isDebugEnabled()) {
            LOG.debug("create object - creating command for: " + adapter);
        }
        if (adapter.representsPersistent()) {
            throw new IllegalArgumentException("Adapter is persistent; adapter: " + adapter);
        }
        return new DataNucleusCreateObjectCommand(adapter, objectStore.getPersistenceManager());
    }

    private void ensureInSession() {
        ensureThatContext(IsisContext.inSession(), is(true));
    }



    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter adapter) {
        ensureOpened();
        ensureInSession();

        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy object - creating command for: " + adapter);
        }
        if (!adapter.representsPersistent()) {
            throw new IllegalArgumentException("Adapter is not persistent; adapter: " + adapter);
        }
        return new DataNucleusDeleteObjectCommand(adapter, objectStore.getPersistenceManager());
    }
    //endregion

    //region > remappedFrom, addCreateObjectCommand

    private Map<Oid, Oid> persistentByTransient = Maps.newHashMap();


    /**
     * To support ISIS-234; keep track, for the duration of the transaction only,
     * of the old transient {@link Oid}s and their corresponding persistent {@link Oid}s.
     */
    public Oid remappedFrom(final Oid transientOid) {
        return persistentByTransient.get(transientOid);
    }

    /**
     * Uses the {@link ObjectStore} to
     * {@link #createCreateObjectCommand(ObjectAdapter) create} a
     * {@link CreateObjectCommand}, and adds to the
     * {@link IsisTransactionManager}.
     */
    public void addCreateObjectCommand(final ObjectAdapter object) {
        getTransactionManager().addCommand(createCreateObjectCommand(object));
    }

    //endregion

    //region > Debugging

    @Override
    public String debugTitle() {
        return "Object Store Persistor";
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendTitle(getClass().getName());
        debug.appendln();

        adapterManager.debugData(debug);
        debug.appendln();

        debug.appendTitle("OID Generator");
        oidGenerator.debugData(debug);
        debug.appendln();

        debug.appendTitle("Services");
        for (final Object servicePojo : persistenceSessionFactory.getServicesInjector().getRegisteredServices()) {
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
        return new ToString(this).toString();
    }

    //endregion

    //region > dependencies (from constructor)

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return specificationLoader;
    }
    protected AuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }

    /**
     * The configured {@link PojoAdapterFactory}.
     * 
     * <p>
     * Injected in constructor.
     */
    public final PojoAdapterFactory getObjectAdapterFactory() {
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
     * The configured {@link ServicesInjectorSpi}.
     */
    public ServicesInjectorSpi getServicesInjector() {
        return persistenceSessionFactory.getServicesInjector();
    }


    //endregion

    //region > sub components

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
     * The configured {@link IsisTransactionManager}.
     */
    public IsisTransactionManager getTransactionManager() {
        return transactionManager;
    }

    // for testing only
    void setTransactionManager(final IsisTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public ObjectStore getObjectStore() {
        return objectStore;
    }

    /**
     * The configured {@link ObjectFactory}.
     */
    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    //endregion

}
