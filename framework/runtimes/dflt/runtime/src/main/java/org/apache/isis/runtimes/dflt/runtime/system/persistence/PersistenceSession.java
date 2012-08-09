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
package org.apache.isis.runtimes.dflt.runtime.system.persistence;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.log4j.Logger;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackUtils;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacetUtils;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByPattern;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByTitle;
import org.apache.isis.core.metamodel.spec.Dirtiable;
import org.apache.isis.core.metamodel.spec.FreeStandingList;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.FixturesInstalledFlag;
import org.apache.isis.runtimes.dflt.runtime.persistence.NotPersistableException;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSessionAware;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSessionHydratorAware;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtimes.dflt.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStorePersistence;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.ToPersistObjectSet;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByPattern;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindUsingApplibQuerySerializable;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManagerAware;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.UpdateNotifier;
import org.apache.isis.runtimes.dflt.runtime.transaction.ObjectPersistenceException;
import org.apache.isis.runtimes.dflt.runtime.transaction.TransactionalClosureAbstract;
import org.apache.isis.runtimes.dflt.runtime.transaction.TransactionalClosureWithReturnAbstract;

public class PersistenceSession implements PersistenceSessionContainer, PersistenceSessionAdaptedServiceManager, PersistenceSessionTransactionManagement, PersistenceSessionHydrator, PersistenceSessionTestSupport, SpecificationLoaderAware,
        IsisTransactionManagerAware, SessionScopedComponent, Injectable, DebuggableWithTitle, ToPersistObjectSet {


    
    private final PersistenceSessionFactory persistenceSessionFactory;
    private final ObjectAdapterFactory adapterFactory;
    private final ObjectFactory objectFactory;
    private final ServicesInjector servicesInjector;
    private final OidGenerator oidGenerator;
    private final AdapterManagerExtended adapterManager;

    private boolean dirtiableSupport;

    /**
     * Injected using setter-based injection.
     */
    private SpecificationLoader specificationLoader;
    /**
     * Injected using setter-based injection.
     */
    private IsisTransactionManager transactionManager;

    private static enum State {
        NOT_INITIALIZED, OPEN, CLOSED
    }

    private State state;

    
    private static final Logger LOG = Logger.getLogger(PersistenceSession.class);
    
    private final PersistAlgorithm persistAlgorithm;
    private final ObjectStorePersistence objectStore;
    private final Map<ObjectSpecId, RootOid> servicesByObjectType = Maps.newHashMap();

    
    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     */
    public PersistenceSession(final PersistenceSessionFactory persistenceSessionFactory, final ObjectAdapterFactory adapterFactory, final ObjectFactory objectFactory, final ServicesInjector servicesInjector, final OidGenerator oidGenerator, final AdapterManagerExtended adapterManager,
            final PersistAlgorithm persistAlgorithm, final ObjectStorePersistence objectStore) {
        
        ensureThatArg(persistenceSessionFactory, is(not(nullValue())), "persistence session factory required");

        ensureThatArg(adapterFactory, is(not(nullValue())), "adapter factory required");
        ensureThatArg(objectFactory, is(not(nullValue())), "object factory required");
        ensureThatArg(servicesInjector, is(not(nullValue())), "services injector required");
        ensureThatArg(oidGenerator, is(not(nullValue())), "OID generator required");
        ensureThatArg(adapterManager, is(not(nullValue())), "adapter manager required");

        // owning, application scope
        this.persistenceSessionFactory = persistenceSessionFactory;

        // session scope
        this.adapterFactory = adapterFactory;
        this.objectFactory = objectFactory;
        this.servicesInjector = servicesInjector;
        this.oidGenerator = oidGenerator;
        this.adapterManager = adapterManager;

        setState(State.NOT_INITIALIZED);
        
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + this);
        }

        ensureThatArg(persistAlgorithm, is(not(nullValue())), "persist algorithm required");
        ensureThatArg(objectStore, is(not(nullValue())), "object store required");

        this.persistAlgorithm = persistAlgorithm;
        this.objectStore = objectStore;
    }

    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     */
    public PersistenceSession(final PersistenceSessionFactory persistenceSessionFactory, final ObjectAdapterFactory adapterFactory, final ObjectFactory objectFactory, final ServicesInjector servicesInjector, final IdentifierGenerator identifierGenerator, final AdapterManagerExtended identityMap,
            final PersistAlgorithm persistAlgorithm, final ObjectStorePersistence objectStore) {

        this(persistenceSessionFactory, adapterFactory, objectFactory, servicesInjector, new OidGenerator(identifierGenerator), identityMap,
            persistAlgorithm, objectStore);
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
    // open, close
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Injects components, calls {@link #doOpen()}, and then creates service
     * adapters.
     * 
     * @see #doOpen()
     */
    @Override
    public void open() {
        ensureNotOpened();

        if (LOG.isDebugEnabled()) {
            LOG.debug("opening " + this);
        }

        // injected via setters
        ensureThatState(specificationLoader, is(not(nullValue())), "SpecificationLoader missing");
        ensureThatState(transactionManager, is(not(nullValue())), "TransactionManager missing");

        // inject any required dependencies into object factory
        this.injectInto(objectFactory);
        specificationLoader.injectInto(objectFactory);
        servicesInjector.injectInto(objectFactory);

        // wire dependencies into identityMap
        adapterFactory.injectInto(adapterManager);
        specificationLoader.injectInto(adapterManager);
        oidGenerator.injectInto(adapterManager);
        servicesInjector.injectInto(adapterManager);

        // wire dependencies into oid generator
        specificationLoader.injectInto(oidGenerator);

        servicesInjector.open();
        adapterFactory.open();
        objectFactory.open();
        adapterManager.open();
        oidGenerator.open();

        // doOpen..
        ensureThatState(objectStore, is(notNullValue()), "object store required");
        ensureThatState(getTransactionManager(), is(notNullValue()), "transaction manager required");
        ensureThatState(persistAlgorithm, is(notNullValue()), "persist algorithm required");
        
        this.injectInto(objectStore); // as a hydrator
        getAdapterManager().injectInto(objectStore);
        getSpecificationLoader().injectInto(objectStore);
        getTransactionManager().injectInto(objectStore);
        
        getOidGenerator().injectInto(objectStore);
        
        objectStore.open();
        
        
        createServiceAdapters();

        setState(State.OPEN);
    }


    
    /**
     * Calls {@link #doClose()}, then closes all components.
     * 
     * @see #doClose()
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

        objectStore.close();

        adapterManager.close();
        servicesInjector.close();
        objectFactory.close();
        adapterFactory.close();
        oidGenerator.close();

        setState(State.CLOSED);
    }


    /**
     * Creates (or recreates following a {@link #testReset()})
     * {@link ObjectAdapter adapters} for the {@link #serviceList}.
     */
    private void createServiceAdapters() {
        getTransactionManager().startTransaction();
        for (final Object service : servicesInjector.getRegisteredServices()) {
            final ObjectSpecification serviceSpecification = specificationLoader.loadSpecification(service.getClass());
            serviceSpecification.markAsService();
            final RootOid existingOid = getOidForService(serviceSpecification);
            ObjectAdapter serviceAdapter;
            if (existingOid == null) {
                serviceAdapter = getAdapterManager().adapterFor(service);
            } else {
                serviceAdapter = getAdapterManager().recreateAdapter(existingOid, service);
            }

            if (serviceAdapter.getOid().isTransient()) {
                adapterManager.remapAsPersistent(serviceAdapter, null);
            }

            serviceAdapter.markAsResolvedIfPossible();
            if (existingOid == null) {
                final RootOid persistentOid = (RootOid) serviceAdapter.getOid();
                registerService(persistentOid);
            }

        }
        getTransactionManager().endTransaction();
    }

    private State getState() {
        return state;
    }

    private void setState(final State state) {
        this.state = state;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // State Management
    // ///////////////////////////////////////////////////////////////////////////

    protected void ensureNotOpened() {
        if (getState() != State.NOT_INITIALIZED) {
            throw new IllegalStateException("Persistence session has already been initialized");
        }
    }

    protected void ensureOpen() {
        if (getState() != State.OPEN) {
            throw new IllegalStateException("Persistence session is not open");
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // shutdown, reset
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * For testing purposes only.
     */
    public void testReset() {
        objectStore.reset();
        getAdapterManager().reset();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Factory (for transient instance)
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a root or standalone {@link ObjectAdapter adapter}.
     * 
     * <p>
     * The returned object will be initialised (had the relevant callback
     * lifecycle methods invoked).
     * 
     * <p>
     * TODO: this is the same as
     * {@link RuntimeContextFromSession#createTransientInstance(ObjectSpecification)}
     * ; could it be unified?
     */
    @Override
    public ObjectAdapter createInstance(final ObjectSpecification objectSpec) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating transient instance of " + objectSpec);
        }
        final Object pojo = objectSpec.createObject();
        final ObjectAdapter adapter = getAdapterManager().adapterFor(pojo);
        return objectSpec.initialize(adapter);
    }

    @Override
    public ObjectAdapter createInstance(final ObjectSpecification objectSpec, final ObjectAdapter parentAdapter) {
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

    @Override
    public final ObjectAdapter recreateAdapter(final TypedOid oid) {
        final ObjectSpecId objectSpecId = oid.getObjectSpecId();
        final ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecId(objectSpecId);
        return recreateAdapter(objectSpec, oid);
    }

    @Override
    public ObjectAdapter recreateAdapter(final ObjectSpecification specification, final Oid oid) {
        final ObjectAdapter adapterLookedUpByOid = getAdapterManager().getAdapterFor(oid);
        if (adapterLookedUpByOid != null) {
            return adapterLookedUpByOid;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("recreating adapter for Oid: " + oid + " of type " + specification);
        }
        
        final Object pojo = specification.createObject();

        return getAdapterManager().recreateAdapter(oid, pojo);
    }

    @Override
    public ObjectAdapter recreateAdapter(final Oid oid, final Object pojo) {
        final ObjectAdapter adapterLookedUpByOid = getAdapterManager().getAdapterFor(oid);
        if (adapterLookedUpByOid != null) {
            return adapterLookedUpByOid;
        }

        final ObjectAdapter adapterLookedUpByPojo = getAdapterManager().getAdapterFor(pojo);
        if (adapterLookedUpByPojo != null) {
            return adapterLookedUpByPojo;
        }

        if (LOG.isDebugEnabled()) {
            // don't touch pojo in case cause it to resolve.
            LOG.debug("recreating adapter for Oid: " + oid + " for provided pojo ");
        }
        return getAdapterManager().recreateAdapter(oid, pojo);
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
        if(LOG.isDebugEnabled()) {
            LOG.debug("createPersistenceQueryFor: " + query.getDescription());
        }
        final ObjectSpecification noSpec = specFor(query);
        if (query instanceof QueryFindAllInstances) {
            return new PersistenceQueryFindAllInstances(noSpec);
        }
        if (query instanceof QueryFindByTitle) {
            final QueryFindByTitle<?> queryByTitle = (QueryFindByTitle<?>) query;
            final String title = queryByTitle.getTitle();
            return new PersistenceQueryFindByTitle(noSpec, title);
        }
        if (query instanceof QueryFindByPattern) {
            final QueryFindByPattern<?> queryByPattern = (QueryFindByPattern<?>) query;
            final Object pattern = queryByPattern.getPattern();
            final ObjectAdapter patternAdapter = getAdapterManager().adapterFor(pattern);
            return new PersistenceQueryFindByPattern(noSpec, patternAdapter);
        }
        if (query instanceof QueryDefault) {
            final QueryDefault<?> queryDefault = (QueryDefault<?>) query;
            final String queryName = queryDefault.getQueryName();
            final Map<String, ObjectAdapter> argumentsAdaptersByParameterName = wrap(queryDefault.getArgumentsByParameterName());
            return new PersistenceQueryFindUsingApplibQueryDefault(noSpec, queryName, argumentsAdaptersByParameterName, cardinality);
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
     * If {@link #isCheckObjectsForDirtyFlag() enabled}, will mark as
     * {@link #objectChanged(ObjectAdapter) changed} any {@link Dirtiable}
     * objects that have manually been
     * {@link Dirtiable#markDirty(ObjectAdapter) marked as dirty}.
     */
    @Override
    public void objectChangedAllDirty() {
        if (!dirtiableSupport) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("marking as changed any objects that have been manually set as dirty");
        }
        for (final ObjectAdapter adapter : getAdapterManager()) {
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
    @Override
    public synchronized void clearAllDirty() {
        if (!isCheckObjectsForDirtyFlag()) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("cleaning any manually dirtied objects");
        }

        for (final ObjectAdapter object : getAdapterManager()) {
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

    @Override
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
    @Override
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
        final ObjectAdapter serviceAdapter = getAdapterManager().recreateAdapter(oid, servicePojo);
        
        serviceAdapter.markAsResolvedIfPossible();
        return serviceAdapter;
    }

    /**
     * Has any services.
     */
    public boolean hasServices() {
        return servicesInjector.getRegisteredServices().size() > 0;
    }


    // ////////////////////////////////////////////////////////////////////
    // Helpers
    // ////////////////////////////////////////////////////////////////////

    protected boolean isImmutable(final ObjectAdapter adapter) {
        final ObjectSpecification noSpec = adapter.getSpecification();
        return ImmutableFacetUtils.isAlwaysImmutable(noSpec) || (ImmutableFacetUtils.isImmutableOncePersisted(noSpec) && adapter.representsPersistent());
    }

    // ////////////////////////////////////////////////////////////////////
    // injectInto
    // ////////////////////////////////////////////////////////////////////

    @Override
    public void injectInto(final Object candidate) {
        if (PersistenceSessionAware.class.isAssignableFrom(candidate.getClass())) {
            final PersistenceSessionAware cast = PersistenceSessionAware.class.cast(candidate);
            cast.setPersistenceSession(this);
        }
        if (PersistenceSessionHydratorAware.class.isAssignableFrom(candidate.getClass())) {
            final PersistenceSessionHydratorAware cast = PersistenceSessionHydratorAware.class.cast(candidate);
            cast.setHydrator(this);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////////////////////////////

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

    // ///////////////////////////////////////////////////////////////////////////
    // Dependencies (injected in constructor, possibly implicitly)
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * The configured {@link ObjectAdapterFactory}.
     * 
     * <p>
     * Injected in constructor.
     */
    public final ObjectAdapterFactory getAdapterFactory() {
        return adapterFactory;
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
    public final AdapterManagerExtended getAdapterManager() {
        return adapterManager;
    }

    /**
     * The configured {@link ServicesInjector}.
     */
    public ServicesInjector getServicesInjector() {
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

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    /**
     * Inject the {@link SpecificationLoader}.
     * 
     * <p>
     * The need to inject the reflector was introduced to support the
     * HibernateObjectStore, which installs its own
     * <tt>HibernateClassStrategy</tt> to cope with the proxy classes that
     * Hibernate wraps around lists, sets and maps.
     */
    public void setSpecificationLoader(final SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }

    /**
     * Inject the {@link IsisTransactionManager}.
     * 
     * <p>
     * This must be injected using setter-based injection rather than through
     * the constructor because there is a bidirectional relationship between the
     * {@link PersistenceSessionHydrator} and the {@link IsisTransactionManager}.
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
    // init, shutdown
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
        LOG.debug("finalizing object manager");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // loadObject, reload
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter loadObject(final TypedOid oid) {
        ensureThatArg(oid, is(notNullValue()));

        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        if (adapter != null) {
            return adapter;
        }

        return loadObjectFromPersistenceLayer(oid);
    }

    private ObjectAdapter loadObjectFromPersistenceLayer(final TypedOid oid) {
        // the object store will map for us, using its hydrator (calls back
        // to #recreateAdapter)
        return getTransactionManager().executeWithinTransaction(new TransactionalClosureWithReturnAbstract<ObjectAdapter>() {
            @Override
            public ObjectAdapter execute() {
                return objectStore.getObject(oid);
            }
        });
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
                CallbackUtils.callCallback(adapter, LoadingCallbackFacet.class);
            }

            @Override
            public void execute() {
                objectStore.resolveImmediately(adapter);
            }

            @Override
            public void onSuccess() {
                CallbackUtils.callCallback(adapter, LoadedCallbackFacet.class);
            }

            @Override
            public void onFailure() {
                // TODO: should we do something here?
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

    /**
     * Makes an {@link ObjectAdapter} persistent. The specified object should be
     * stored away via this object store's persistence mechanism, and have an
     * new and unique OID assigned to it. The object, should also be added to
     * the {@link AdapterManager} as the object is implicitly 'in use'.
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
        LOG.debug("object change to update presentation layer " + adapter.getOid());
        adapter.fireChangedEvent();
        getUpdateNotifier().addChangedObject(adapter);
    }

    private void addObjectChangedForPersistenceLayer(final ObjectAdapter adapter) {
        LOG.debug("object change to be persisted " + adapter.getOid());
        getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void preExecute() {
                CallbackUtils.callCallback(adapter, UpdatingCallbackFacet.class);
            }

            @Override
            public void execute() {
                final SaveObjectCommand saveObjectCommand = objectStore.createSaveObjectCommand(adapter);
                getTransactionManager().addCommand(saveObjectCommand);
            }

            @Override
            public void onSuccess() {
                CallbackUtils.callCallback(adapter, UpdatedCallbackFacet.class);
            }

            @Override
            public void onFailure() {
                // TODO: should we do something here?
            }
        });
        getUpdateNotifier().addChangedObject(adapter);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // destroyObject
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Removes the specified object from the system. The specified object's data
     * should be removed from the persistence mechanism.
     */
    @Override
    public void destroyObject(final ObjectAdapter adapter) {
        if (adapter.getSpecification().isParented()) {
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
                CallbackUtils.callCallback(adapter, RemovingCallbackFacet.class);
            }

            @Override
            public void execute() {
                if (adapter.getVersion() == null) {
                    throw new ObjectPersistenceException("Object to be deleted does not have a version (maybe it should be resolved first): " + adapter);
                }
                final DestroyObjectCommand command = objectStore.createDestroyObjectCommand(adapter);
                getTransactionManager().addCommand(command);
            }

            @Override
            public void onSuccess() {
                CallbackUtils.callCallback(adapter, RemovedCallbackFacet.class);
            }

            @Override
            public void onFailure() {
                // TODO: some sort of callback?
            }
        });
    }

    // ///////////////////////////////////////////////////////////////////////////
    // remapAsPersistent
    // ///////////////////////////////////////////////////////////////////////////

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
     * <p>
     * TODO: the <tt>PersistenceSessionProxy</tt> doesn't have this method;
     * should document better why this is the case, and where the equivalent
     * functionality is (somewhere in the marshalling stuff, I think).
     * 
     * @see #remapAsPersistent(ObjectAdapter)
     */
    @Override
    public void remapAsPersistent(final ObjectAdapter adapter) {
        final Oid transientOid = adapter.getOid();
        getAdapterManager().remapAsPersistent(adapter, null);
        final Oid persistentOid = adapter.getOid();
        persistentByTransient.put(transientOid, persistentOid);
    }


    private Map<Oid, Oid> persistentByTransient = Maps.newHashMap();
    
    @Override
    public Oid remappedFrom(Oid transientOid) {
        return persistentByTransient.get(transientOid);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // getInstances
    // ///////////////////////////////////////////////////////////////////////////


    private List<ObjectAdapter> getInstancesFromPersistenceLayer(final PersistenceQuery persistenceQuery) {
        return getTransactionManager().executeWithinTransaction(new TransactionalClosureWithReturnAbstract<List<ObjectAdapter>>() {
            @Override
            public List<ObjectAdapter> execute() {
                return objectStore.getInstances(persistenceQuery);
            }

            @Override
            public void onSuccess() {
                clearAllDirty();
            }
        });
    }

    // ///////////////////////////////////////////////////////////////////////////
    // hasInstances
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Checks whether there are any instances of the specified type. The object
     * store should look for instances of the type represented by <variable>type
     * </variable> and return <code>true</code> if there are, or
     * <code>false</code> if there are not.
     */
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
    // Services
    // ///////////////////////////////////////////////////////////////////////////


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
    // TransactionManager
    // ///////////////////////////////////////////////////////////////////////////


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

    // ///////////////////////////////////////////////////////////////////////////
    // Dependencies
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Injected by constructor.
     */
    public ObjectStorePersistence getObjectStore() {
        return objectStore;
    }

    /**
     * Injected by constructor.
     */
    public PersistAlgorithm getPersistAlgorithm() {
        return persistAlgorithm;
    }

    private UpdateNotifier getUpdateNotifier() {
        return getTransactionManager().getTransaction().getUpdateNotifier();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////////////////////////

    private static AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }


    
}
