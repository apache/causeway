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

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.datanucleus.enhancement.Persistable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.eventbus.AbstractLifecycleEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer2;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.ensure.IsisAssertException;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.commons.util.ToString;
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
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.runtimecontext.ConfigurationService;
import org.apache.isis.core.metamodel.runtimecontext.ConfigurationServiceAware;
import org.apache.isis.core.metamodel.runtimecontext.MessageBrokerService;
import org.apache.isis.core.metamodel.runtimecontext.MessageBrokerServiceAware;
import org.apache.isis.core.metamodel.runtimecontext.PersistenceSessionService;
import org.apache.isis.core.metamodel.runtimecontext.PersistenceSessionServiceAware;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.FreeStandingList;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.persistence.FixturesInstalledFlag;
import org.apache.isis.core.runtime.persistence.NotPersistableException;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.PojoRecreationException;
import org.apache.isis.core.runtime.persistence.PojoRefreshException;
import org.apache.isis.core.runtime.persistence.UnsupportedFindException;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.persistence.container.DomainObjectContainerResolve;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerFixtureAbstract;
import org.apache.isis.core.runtime.services.enlist.EnlistedObjectsServiceInternal;
import org.apache.isis.core.runtime.services.metrics.MetricsServiceDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.OidAdapterHashMap;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.PojoAdapterHashMap;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.RootAndCollectionAdapters;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosure;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosureWithReturn;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.commands.DataNucleusCreateObjectCommand;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.commands.DataNucleusDeleteObjectCommand;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryFindAllInstancesProcessor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryFindUsingApplibQueryProcessor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryProcessor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryProcessorAbstract;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.spi.JdoObjectIdSerializer;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatContext;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * A wrapper around the JDO {@link PersistenceManager}, which also manages concurrency
 * and maintains an identity map of {@link ObjectAdapter adapter}s and {@link Oid
 * identities} for each and every POJO that is being used by the framework.
 */
public class PersistenceSession implements
        TransactionalResource,
        SessionScopedComponent,
        DebuggableWithTitle,
        AdapterManager,
        MessageBrokerService,
        PersistenceSessionService,
        ConfigurationService,
        IsisLifecycleListener2.PersistenceSessionLifecycleManagement,
        IsisTransactionManager.PersistenceSessionTransactionManagement,
        PersistenceQueryProcessorAbstract.PersistenceSessionQueryProcessorManagement {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceSession.class);

    /**
     * @see #isFixturesInstalled()
     */
    public static final String INSTALL_FIXTURES_KEY = OptionHandlerFixtureAbstract.DATANUCLEUS_INSTALL_FIXTURES_KEY;
    public static final boolean INSTALL_FIXTURES_DEFAULT = false;

    private static final String ROOT_KEY = OptionHandlerFixtureAbstract.DATANUCLEUS_ROOT_KEY;

    /**
     * Append regular <a href="http://www.datanucleus.org/products/accessplatform/persistence_properties.html">datanucleus properties</a> to this key
     */
    public static final String DATANUCLEUS_PROPERTIES_ROOT = ROOT_KEY + "impl.";


    //region > constructor, fields, finalize()

    private final FixturesInstalledFlag fixturesInstalledFlag;

    private final PersistenceQueryFactory persistenceQueryFactory;
    private final IsisConfigurationDefault configuration;
    private final SpecificationLoaderSpi specificationLoader;
    private final AuthenticationSession authenticationSession;

    private final ServicesInjectorSpi servicesInjector;
    /**
     * Used to create the {@link #persistenceManager} when {@link #open()}ed.
     */
    private final PersistenceManagerFactory jdoPersistenceManagerFactory;

    // not final only for testing purposes
    private IsisTransactionManager transactionManager;

    private final OidMarshaller oidMarshaller = new OidMarshaller();

    /**
     * populated only when {@link #open()}ed.
     */
    private PersistenceManager persistenceManager;

    /**
     * populated only when {@link #open()}ed.
     */
    private final Map<Class<?>, PersistenceQueryProcessor<?>> persistenceQueryProcessorByClass = Maps.newHashMap();

    private final Map<ObjectSpecId, RootOid> registeredServices = Maps.newHashMap();

    private final boolean concurrencyCheckingGloballyEnabled;


    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     */
    public PersistenceSession(
            final IsisConfigurationDefault configuration,
            final ServicesInjectorSpi servicesInjector,
            final SpecificationLoaderSpi specificationLoader,
            final AuthenticationSession authenticationSession,
            final PersistenceManagerFactory jdoPersistenceManagerFactory,
            final FixturesInstalledFlag fixturesInstalledFlag) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + this);
        }

        // injected
        this.configuration = configuration;
        this.specificationLoader = specificationLoader;
        this.authenticationSession = authenticationSession;
        this.fixturesInstalledFlag = fixturesInstalledFlag;

        this.servicesInjector = servicesInjector;
        this.jdoPersistenceManagerFactory = jdoPersistenceManagerFactory;

        // sub-components
        final AdapterManager adapterManager = this;
        this.persistenceQueryFactory = new PersistenceQueryFactory(adapterManager, specificationLoader);
        final IsisTransactionManager.PersistenceSessionTransactionManagement psTranManagement = this;
        this.transactionManager = new IsisTransactionManager(psTranManagement, servicesInjector);

        setState(State.NOT_INITIALIZED);

        final boolean concurrencyCheckingGloballyDisabled =
                configuration.getBoolean("isis.persistor.disableConcurrencyChecking", false);
        this.concurrencyCheckingGloballyEnabled = !concurrencyCheckingGloballyDisabled;

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("finalizing persistence session");
    }

    //endregion

    //region > open

    /**
     * Only populated once {@link #open()}'d
     */
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    /**
     * Injects components, calls open on subcomponents, and then creates service
     * adapters.
     */
    public void open() {
        ensureNotOpened();

        if (LOG.isDebugEnabled()) {
            LOG.debug("opening " + this);
        }

        oidAdapterMap.open();
        pojoAdapterMap.open();

        persistenceManager = jdoPersistenceManagerFactory.getPersistenceManager();

        final IsisLifecycleListener2.PersistenceSessionLifecycleManagement psLifecycleMgmt = this;
        final IsisLifecycleListener2 isisLifecycleListener = new IsisLifecycleListener2(psLifecycleMgmt);
        persistenceManager.addInstanceLifecycleListener(isisLifecycleListener, (Class[]) null);

        final PersistenceQueryProcessorAbstract.PersistenceSessionQueryProcessorManagement psQueryProcessorMgmt = this;
        persistenceQueryProcessorByClass.put(
                PersistenceQueryFindAllInstances.class,
                new PersistenceQueryFindAllInstancesProcessor(psQueryProcessorMgmt));
        persistenceQueryProcessorByClass.put(
                PersistenceQueryFindUsingApplibQueryDefault.class,
                new PersistenceQueryFindUsingApplibQueryProcessor(psQueryProcessorMgmt));

        initServices();

        final MetricsServiceDefault metricsServiceDefault = servicesInjector.lookupService(MetricsServiceDefault.class);
        persistenceManager.addInstanceLifecycleListener(metricsServiceDefault, (Class[])null);

        setState(State.OPEN);
    }


    /**
     * Creates {@link ObjectAdapter adapters} for the service list.
     */
    private void initServices() {
        final List<Object> registeredServices = servicesInjector.getRegisteredServices();
        for (final Object service : registeredServices) {
            final ObjectSpecification serviceSpecification =
                    specificationLoader.loadSpecification(service.getClass());
            serviceSpecification.markAsService();
            final RootOid existingOid = getOidForService(serviceSpecification);
            final ObjectAdapter serviceAdapter =
                    existingOid == null
                            ? adapterFor(service)
                            : mapRecreatedPojo(existingOid, service);
            if (serviceAdapter.getOid().isTransient()) {
                remapAsPersistent(serviceAdapter, null);
            }

            if (existingOid == null) {
                final RootOid persistentOid = (RootOid) serviceAdapter.getOid();
                this.registeredServices.put(persistentOid.getObjectSpecId(), persistentOid);
            }
        }
    }

    //endregion

    //region > close

    /**
     * Closes the subcomponents.
     *
     * <p>
     * Automatically {@link IsisTransactionManager#endTransaction() ends
     * (commits)} the current (Isis) {@link IsisTransaction}. This in turn commits the underlying
     * JDO transaction.
     *
     * <p>
     * The corresponding DataNucleus entity is then closed.
     */
    public void close() {

        if (getState() == State.CLOSED) {
            // nothing to do
            return;
        }

        try {
            final IsisTransaction currentTransaction = transactionManager.getTransaction();
            if (currentTransaction != null && !currentTransaction.getState().isComplete()) {
                if(currentTransaction.getState().canCommit()) {
                    transactionManager.endTransaction();
                } else if(currentTransaction.getState().canAbort()) {
                    transactionManager.abortTransaction();
                }
            }
        } catch(final Throwable ex) {
            // ignore
            LOG.error("close: failed to end transaction; continuing to avoid memory leakage");
        }

        try {
            persistenceManager.close();
        } catch(final Throwable ex) {
            // ignore
            LOG.error(
                "close: failed to close JDO persistenceManager; continuing to avoid memory leakage");
        }
        // TODO: REVIEW ... ??? this is a guess: don't set to null, because we need for -> transactionManager -> transaction -> messageBroker
        // persistenceManager = null;

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

        setState(State.CLOSED);
    }

    //endregion

    //region > Injectable
    @Override
    public void injectInto(final Object candidate) {
        if (AdapterManagerAware.class.isAssignableFrom(candidate.getClass())) {
            final AdapterManagerAware cast = AdapterManagerAware.class.cast(candidate);
            cast.setAdapterManager(this);
        }
        if (MessageBrokerServiceAware.class.isAssignableFrom(candidate.getClass())) {
            final MessageBrokerServiceAware cast = MessageBrokerServiceAware.class.cast(candidate);
            cast.setMessageBrokerService(this);
        }
        if (PersistenceSessionServiceAware.class.isAssignableFrom(candidate.getClass())) {
            final PersistenceSessionServiceAware cast = PersistenceSessionServiceAware.class.cast(candidate);
            cast.setPersistenceSessionService(this);
        }
        if (ConfigurationServiceAware.class.isAssignableFrom(candidate.getClass())) {
            final ConfigurationServiceAware cast = ConfigurationServiceAware.class.cast(candidate);
            cast.setConfigurationService(this);
        }
    }
    //endregion

    //region > QuerySubmitter impl, findInstancesInTransaction

    @Override
    public <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query) {
        final ObjectAdapter instances = findInstancesInTransaction(query, QueryCardinality.MULTIPLE);
        return CollectionFacetUtils.convertToAdapterList(instances);
    }

    @Override
    public <T> ObjectAdapter firstMatchingQuery(final Query<T> query) {
        final ObjectAdapter instances = findInstancesInTransaction(query, QueryCardinality.SINGLE);
        final List<ObjectAdapter> list = CollectionFacetUtils.convertToAdapterList(instances);
        return list.size() > 0 ? list.get(0) : null;
    }

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
    private <T> ObjectAdapter findInstancesInTransaction(final Query<T> query, final QueryCardinality cardinality) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findInstances using (applib) Query: " + query);
        }

        // TODO: unify PersistenceQuery and PersistenceQueryProcessor
        final PersistenceQuery persistenceQuery = createPersistenceQueryFor(query, cardinality);
        if (LOG.isDebugEnabled()) {
            LOG.debug("maps to (core runtime) PersistenceQuery: " + persistenceQuery);
        }

        final PersistenceQueryProcessor<? extends PersistenceQuery> processor = lookupProcessorFor(persistenceQuery);

        final List<ObjectAdapter> instances = transactionManager.executeWithinTransaction(
                new TransactionalClosureWithReturn<List<ObjectAdapter>>() {
                    @Override
                    public List<ObjectAdapter> execute() {
                        return processPersistenceQuery(processor, persistenceQuery);
                    }
                });
        final ObjectSpecification specification = persistenceQuery.getSpecification();
        final FreeStandingList results = new FreeStandingList(specification, instances);
        return adapterFor(results);
    }

    /**
     * Converts the {@link Query applib representation of a query} into the
     * {@link PersistenceQuery NOF-internal representation}.
     */
    private final PersistenceQuery createPersistenceQueryFor(
            final Query<?> query,
            final QueryCardinality cardinality) {

        final PersistenceQuery persistenceQuery =
                persistenceQueryFactory.createPersistenceQueryFor(query, cardinality);
        if (persistenceQuery == null) {
            throw new IllegalArgumentException("Unknown Query type: " + query.getDescription());
        }

        return persistenceQuery;
    }

    private PersistenceQueryProcessor<? extends PersistenceQuery> lookupProcessorFor(final PersistenceQuery persistenceQuery) {
        final Class<? extends PersistenceQuery> persistenceQueryClass = persistenceQuery.getClass();
        final PersistenceQueryProcessor<? extends PersistenceQuery> processor =
                persistenceQueryProcessorByClass.get(persistenceQueryClass);
        if (processor == null) {
            throw new UnsupportedFindException(MessageFormat.format(
                    "Unsupported PersistenceQuery class: {0}", persistenceQueryClass.getName()));
        }
        return processor;
    }
    @SuppressWarnings("unchecked")
    private <Q extends PersistenceQuery> List<ObjectAdapter> processPersistenceQuery(
            final PersistenceQueryProcessor<Q> persistenceQueryProcessor,
            final PersistenceQuery persistenceQuery) {
        return persistenceQueryProcessor.process((Q) persistenceQuery);
    }

    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    public OidMarshaller getOidMarshaller() {
        return oidMarshaller;
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
        return createInstance(objectSpec, Variant.TRANSIENT, null);
    }

    public ObjectAdapter createViewModelInstance(final ObjectSpecification objectSpec, final String memento) {
        return createInstance(objectSpec, Variant.VIEW_MODEL, memento);
    }

    private enum Variant {
        TRANSIENT,
        VIEW_MODEL
    }

    private ObjectAdapter createInstance(
            final ObjectSpecification spec,
            final Variant variant,
            final String memento) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + variant + " instance of " + spec);
        }
        final Object pojo;

        if(variant == Variant.VIEW_MODEL) {
            pojo = recreateViewModel(spec, memento);
        } else {
            pojo = instantiateAndInjectServices(spec);

        }

        final ObjectAdapter adapter = adapterFor(pojo);
        return initializePropertiesAndDoCallback(adapter);
    }

    private Object recreateViewModel(final ObjectSpecification spec, final String memento) {
        final ViewModelFacet facet = spec.getFacet(ViewModelFacet.class);
        if(facet == null) {
            throw new IllegalArgumentException("spec does not have ViewModelFacet; spec is " + spec.getFullIdentifier());
        }

        final Object viewModelPojo;
        if(facet.getRecreationMechanism().isInitializes()) {
            viewModelPojo = instantiateAndInjectServices(spec);
            facet.initialize(viewModelPojo, memento);
        } else {
            viewModelPojo = facet.instantiate(spec.getCorrespondingClass(), memento);
        }
        return viewModelPojo;
    }

    public Object instantiateAndInjectServices(final ObjectSpecification objectSpec) {

        final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
        if (correspondingClass.isArray()) {
            return Array.newInstance(correspondingClass.getComponentType(), 0);
        }

        final Class<?> cls = correspondingClass;
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new IsisException("Cannot create an instance of an abstract class: " + cls);
        }
        final Object newInstance;
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new IsisException("Cannot create an instance of an abstract class: " + cls);
        }

        try {
            newInstance = cls.newInstance();
        } catch (final IllegalAccessException | InstantiationException e) {
            throw new IsisException("Failed to create instance of type " + objectSpec.getFullIdentifier(), e);
        }

        servicesInjector.injectServicesInto(newInstance);
        return newInstance;

    }

    private ObjectAdapter initializePropertiesAndDoCallback(final ObjectAdapter adapter) {

        // initialize new object
        final List<ObjectAssociation> fields = adapter.getSpecification().getAssociations(Contributed.EXCLUDED);
        for (ObjectAssociation field : fields) {
            field.toDefault(adapter);
        }
        final Object pojo = adapter.getObject();
        servicesInjector.injectServicesInto(pojo);

        CallbackFacet.Util.callCallback(adapter, CreatedCallbackFacet.class);

        if (Command.class.isAssignableFrom(pojo.getClass())) {

            // special case... the command object is created while the transaction is being started and before
            // the event bus service is initialized (nb: we initialize services *within* a transaction).  To resolve
            // this catch-22 situation, we simply suppress the posting of this event for this domain class.

            // this seems the least unpleasant of the various options available:
            // * we could have put a check in the EventBusService to ignore the post if not yet initialized;
            //   however this might hide other genuine errors
            // * we could have used the thread-local in JdoStateManagerForIsis and the "skip(...)" hook in EventBusServiceJdo
            //   to have this event be skipped; but that seems like co-opting some other design
            // * we could have the transaction initialize the EventBusService as a "special case" before creating the Command;
            //   but then do we worry about it being re-init'd later by the ServicesInitializer?

            // so, doing it this way is this simplest, least obscure.

            if(LOG.isDebugEnabled()) {
                LOG.debug("Skipping postEvent for creation of Command pojo");
            }

        } else {
            postLifecycleEventIfRequired(adapter, CreatedLifecycleEventFacet.class);
        }

        return adapter;
    }


    //endregion

    //region > getServices, getService

    public List<ObjectAdapter> getServices() {
        final List<Object> services = servicesInjector.getRegisteredServices();
        final List<ObjectAdapter> serviceAdapters = Lists.newArrayList();
        for (final Object servicePojo : services) {
            serviceAdapters.add(getService(servicePojo));
        }
        return serviceAdapters;
    }

    private ObjectAdapter getService(final Object servicePojo) {
        final ObjectSpecification serviceSpecification =
                specificationLoader.loadSpecification(servicePojo.getClass());
        final RootOid oid = getOidForService(serviceSpecification);
        final ObjectAdapter serviceAdapter = mapRecreatedPojo(oid, servicePojo);

        return serviceAdapter;
    }

    /**
     * Returns the OID for the adapted service. This allows a service object to
     * be given the same OID that it had when it was created in a different
     * session.
     */
    private RootOid getOidForService(final ObjectSpecification serviceSpec) {
        final ObjectSpecId serviceSpecId = serviceSpec.getSpecId();
        final RootOid oid = this.registeredServices.get(serviceSpecId);
        return oid;
    }

    //endregion

    //region > helper: postEvent

    void postLifecycleEventIfRequired(
            final ObjectAdapter adapter,
            final Class<? extends LifecycleEventFacet> lifecycleEventFacetClass) {
        final LifecycleEventFacet facet = adapter.getSpecification().getFacet(lifecycleEventFacetClass);
        if(facet != null) {
            final Class<? extends AbstractLifecycleEvent<?>> eventType = facet.getEventType();
            final Object instance = InstanceUtil.createInstance(eventType);
            final Object pojo = adapter.getObject();
            postEvent((AbstractLifecycleEvent) instance, pojo);
        }
    }

    void postEvent(final AbstractLifecycleEvent<Object> event, final Object pojo) {
        final EventBusService eventBusService = getServicesInjector().lookupService(EventBusService.class);
        event.setSource(pojo);
        eventBusService.post(event);
    }
    //endregion



    //region > fixture installation

    /**
     * Determine if the object store has been initialized with its set of start
     * up objects.
     * 
     * <p>
     * This method is called only once after the init has been called. If this flag
     * returns <code>false</code> the framework will run the fixtures to
     * initialise the persistor.
     * 
     * <p>
     * Returns the cached value of {@link #isFixturesInstalled()
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
        if (fixturesInstalledFlag.isFixturesInstalled() == null) {
            fixturesInstalledFlag.setFixturesInstalled(objectStoreIsFixturesInstalled());
        }
        return fixturesInstalledFlag.isFixturesInstalled();
    }


    /**
     * Determine if the object store has been initialized with its set of start
     * up objects.
     *
     * <p>
     * This method is called only once after the session is opened called. If it returns <code>false</code> then the
     * framework will run the fixtures to initialise the object store.
     *
     * <p>
     * Implementation looks for the {@link #INSTALL_FIXTURES_KEY} in the injected {@link #configuration configuration}.
     *
     * <p>
     * By default this is not expected to be there, but utilities can add in on
     * the fly during bootstrapping if required.
     */
    public boolean objectStoreIsFixturesInstalled() {
        final boolean installFixtures = configuration.getBoolean(INSTALL_FIXTURES_KEY, INSTALL_FIXTURES_DEFAULT);
        LOG.info("isFixturesInstalled: {} = {}", INSTALL_FIXTURES_KEY, installFixtures);
        return !installFixtures;
    }

    //endregion

    //region > loadObject

    /**
     * Loads the object identified by the specified {@link RootOid}.
     *
     * <p>
     * That is, it retrieves the object identified by the specified {@link RootOid} from the object
     * store, {@link AdapterManager#mapRecreatedPojo(org.apache.isis.core.metamodel.adapter.oid.Oid, Object) mapped by
     * the adapter manager}.
     *
     * <p>The cache should be checked first and, if the object is cached,
     * the cached version should be returned. It is important that if this
     * method is called again, while the originally returned object is in
     * working memory, then this method must return that same Java object.
     *
     * <p>
     * Assuming that the object is not cached then the data for the object
     * should be retrieved from the persistence mechanism and the object
     * recreated (as describe previously). The specified OID should then be
     * assigned to the recreated object by calling its <method>setOID </method>.
     * Before returning the object its resolved flag should also be set by
     * calling its <method>setResolved </method> method as well.
     *
     * <p>
     * If the persistence mechanism does not known of an object with the
     * specified {@link RootOid} then a {@link org.apache.isis.core.runtime.persistence.ObjectNotFoundException} should be
     * thrown.
     *
     * <p>
     * Note that the OID could be for an internal collection, and is
     * therefore related to the parent object (using a {@link ParentedCollectionOid}).
     * The elements for an internal collection are commonly stored as
     * part of the parent object, so to get element the parent object needs to
     * be retrieved first, and the internal collection can be got from that.
     *
     * <p>
     * Returns the stored {@link ObjectAdapter} object.
     *
     *
     * @return the requested {@link ObjectAdapter} that has the specified
     *         {@link RootOid}.
     *
     * @throws org.apache.isis.core.runtime.persistence.ObjectNotFoundException
     *             when no object corresponding to the oid can be found
     */
    public ObjectAdapter loadObjectInTransaction(final RootOid oid) {
        
        // REVIEW: 
        // this method does not account for the oid possibly being a view model
        // alternatively, can call #adapterFor(oid); this code
        // delegates to the PojoRecreator which *does* take view models into account
        //
        // it's possible, therefore, that existing callers to this method (the Scimpi viewer)
        // could be refactored to use #adapterFor(...)
        ensureThatArg(oid, is(notNullValue()));

        final ObjectAdapter adapter = getAdapterFor(oid);
        if (adapter != null) {
            return adapter;
        }

        return transactionManager.executeWithinTransaction(
                new TransactionalClosureWithReturn<ObjectAdapter>() {
                    @Override
                    public ObjectAdapter execute() {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("getObject; oid=" + oid);
                        }

                        final Object pojo = loadPojo(oid);
                        return mapRecreatedPojo(oid, pojo);
                    }
                });
    }


    //endregion

    //region > loadPojo

    public Object loadPojo(final RootOid rootOid) {

        Object result;
        try {
            final Class<?> cls = clsOf(rootOid);
            final Object jdoObjectId = JdoObjectIdSerializer.toJdoObjectId(rootOid);
            FetchPlan fetchPlan = persistenceManager.getFetchPlan();
            fetchPlan.addGroup(FetchGroup.DEFAULT);
            result = persistenceManager.getObjectById(cls, jdoObjectId);
        } catch (final RuntimeException e) {

            final List<ExceptionRecognizer> exceptionRecognizers = getServicesInjector().lookupServices(ExceptionRecognizer.class);
            for (ExceptionRecognizer exceptionRecognizer : exceptionRecognizers) {
                if(exceptionRecognizer instanceof ExceptionRecognizer2) {
                    final ExceptionRecognizer2 recognizer = (ExceptionRecognizer2) exceptionRecognizer;
                    final ExceptionRecognizer2.Recognition recognition = recognizer.recognize2(e);
                    if(recognition != null) {
                        if(recognition.getCategory() == ExceptionRecognizer2.Category.NOT_FOUND) {
                            throw new ObjectNotFoundException(rootOid, e);
                        }
                    }
                }
            }

            throw e;
        }

        if (result == null) {
            throw new ObjectNotFoundException(rootOid);
        }
        return result;
    }

    private Class<?> clsOf(final RootOid oid) {
        final ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecId(oid.getObjectSpecId());
        return objectSpec.getCorrespondingClass();
    }

    //endregion

    //region > lazilyLoaded


    public ObjectAdapter mapPersistent(final Persistable pojo) {
        if (persistenceManager.getObjectId(pojo) == null) {
            return null;
        }
        final RootOid oid = createPersistentOrViewModelOid(pojo);
        final ObjectAdapter adapter = mapRecreatedPojo(oid, pojo);
        return adapter;
    }


    //endregion

    //region > refreshRootInTransaction, refreshRoot

    /**
     * Re-initialises the fields of an object. If the object is unresolved then
     * the object's missing data should be retrieved from the persistence
     * mechanism and be used to set up the value objects and associations.
     */
    public void refreshRootInTransaction(final ObjectAdapter adapter) {
        Assert.assertTrue("only resolve object that is persistent", adapter, adapter.representsPersistent());
        getTransactionManager().executeWithinTransaction(new TransactionalClosure() {

            @Override
            public void execute() {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("resolveImmediately; oid=" + adapter.getOid().enString(oidMarshaller));
                }

                if (!adapter.representsPersistent()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("; not persistent - ignoring");
                    }
                    return;
                }

                refreshRoot(adapter);
            }

        });
    }

    /**
     * Forces a reload (refresh in JDO terminology) of the domain object wrapped in the {@link ObjectAdapter}.
     */
    public void refreshRoot(final ObjectAdapter adapter) {

        final Object domainObject = adapter.getObject();
        if (domainObject == null) {
            // REVIEW: is this possible?
            throw new PojoRefreshException(adapter.getOid());
        }

        try {
            persistenceManager.refresh(domainObject);
        } catch (final RuntimeException e) {
            throw new PojoRefreshException(adapter.getOid(), e);
        }

        // possibly redundant because also called in the post-load event
        // listener, but (with JPA impl) found it was required if we were ever to
        // get an eager left-outer-join as the result of a refresh (sounds possible).
        initializeMapAndCheckConcurrency((Persistable) domainObject);
    }
    //endregion

    //region > makePersistent

    /**
     * Makes an {@link ObjectAdapter} persistent. The specified object should be
     * stored away via this object store's persistence mechanism, and have a
     * new and unique OID assigned to it. The object, should also be added to
     * the {@link PersistenceSession} as the object is implicitly 'in use'.
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
    public void makePersistentInTransaction(final ObjectAdapter adapter) {
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

        getTransactionManager().executeWithinTransaction(new TransactionalClosure() {

            @Override
            public void execute() {
                makePersistentTransactionAssumed(adapter);

                // clear out the map of transient -> persistent
                PersistenceSession.this.persistentByTransient.clear();
            }

        });
    }

    private void makePersistentTransactionAssumed(final ObjectAdapter adapter) {
        if (alreadyPersistedOrNotPersistable(adapter)) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist " + adapter);
        }

        // previously we called the PersistingCallback here.
        // this is now done in the JDO framework synchronizer.
        //
        // the guard below used to be because (apparently)
        // the callback might have caused the adapter to become persistent.
        // leaving it in as think it does no harm...
        if (alreadyPersistedOrNotPersistable(adapter)) {
            return;
        }
        addCreateObjectCommand(adapter);
    }

    /**
     * {@link #newCreateObjectCommand(ObjectAdapter) Create}s a {@link CreateObjectCommand}, and adds to the
     * {@link IsisTransactionManager}.
     */
    private void addCreateObjectCommand(final ObjectAdapter object) {
        final CreateObjectCommand createObjectCommand = newCreateObjectCommand(object);
        transactionManager.addCommand(createObjectCommand);
    }



    private static boolean alreadyPersistedOrNotPersistable(final ObjectAdapter adapter) {
        return adapter.representsPersistent() || objectSpecNotPersistable(adapter);
    }


    private static boolean objectSpecNotPersistable(final ObjectAdapter adapter) {
        return !adapter.getSpecification().persistability().isPersistable() || adapter.isParentedCollection();
    }


    //endregion

    //region > ObjectPersistor impl
    @Override
    public void makePersistent(final ObjectAdapter adapter) {
        makePersistentInTransaction(adapter);
    }

    @Override
    public void remove(final ObjectAdapter adapter) {
        destroyObjectInTransaction(adapter);
    }
    //endregion


    //region > destroyObjectInTransaction

    /**
     * Removes the specified object from the system. The specified object's data
     * should be removed from the persistence mechanism.
     */
    public void destroyObjectInTransaction(final ObjectAdapter adapter) {
        final ObjectSpecification spec = adapter.getSpecification();
        if (spec.isParented()) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroyObject " + adapter);
        }
        transactionManager.executeWithinTransaction(new TransactionalClosure() {
            @Override
            public void execute() {
                final DestroyObjectCommand command = newDestroyObjectCommand(adapter);
                transactionManager.addCommand(command);
            }
        });
    }

    //endregion

    //region > newXxxCommand
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
    private CreateObjectCommand newCreateObjectCommand(final ObjectAdapter adapter) {
        ensureOpened();
        ensureInSession();

        if (LOG.isDebugEnabled()) {
            LOG.debug("create object - creating command for: " + adapter);
        }
        if (adapter.representsPersistent()) {
            throw new IllegalArgumentException("Adapter is persistent; adapter: " + adapter);
        }
        return new DataNucleusCreateObjectCommand(adapter, persistenceManager);
    }

    private void ensureInSession() {
        ensureThatContext(IsisContext.inSession(), is(true));
    }

    private DestroyObjectCommand newDestroyObjectCommand(final ObjectAdapter adapter) {
        ensureOpened();
        ensureInSession();

        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy object - creating command for: " + adapter);
        }
        if (!adapter.representsPersistent()) {
            throw new IllegalArgumentException("Adapter is not persistent; adapter: " + adapter);
        }
        return new DataNucleusDeleteObjectCommand(adapter, persistenceManager);
    }
    //endregion

    //region > execute
    public void execute(final List<PersistenceCommand> commands) {

        ensureOpened();
        ensureInTransaction();

        // previously we used to check that there were some commands, and skip processing otherwise.
        // we no longer do that; it could be (is quite likely) that DataNucleus has some dirty objects anyway that
        // don't have commands wrapped around them...

        executeCommands(commands);
    }

    private void executeCommands(final List<PersistenceCommand> commands) {

        for (final PersistenceCommand command : commands) {
            command.execute(null);
        }
        persistenceManager.flush();
    }
    //endregion

    //region > getAggregateRoot, remappedFrom

    private Map<Oid, Oid> persistentByTransient = Maps.newHashMap();

    public ObjectAdapter getAggregateRoot(final ParentedCollectionOid collectionOid) {
        final Oid rootOid = collectionOid.getRootOid();
        ObjectAdapter rootadapter = getAdapterFor(rootOid);
        if(rootadapter == null) {
            final Oid parentOidNowPersisted = remappedFrom(rootOid);
            rootadapter = getAdapterFor(parentOidNowPersisted);
        }
        return rootadapter;
    }

    /**
     * To support ISIS-234; keep track, for the duration of the transaction only,
     * of the old transient {@link Oid}s and their corresponding persistent {@link Oid}s.
     */
    private Oid remappedFrom(final Oid transientOid) {
        return persistentByTransient.get(transientOid);
    }


    //endregion

    //region > transactions
    public void startTransaction() {
        final javax.jdo.Transaction transaction = persistenceManager.currentTransaction();
        if (transaction.isActive()) {
            throw new IllegalStateException("Transaction already active");
        }
        transaction.begin();
    }

    public void endTransaction() {
        final javax.jdo.Transaction transaction = persistenceManager.currentTransaction();
        if (transaction.isActive()) {
            transaction.commit();
        }
    }

    public void abortTransaction() {
        final javax.jdo.Transaction transaction = persistenceManager.currentTransaction();
        if (transaction.isActive()) {
            transaction.rollback();
        }
    }

    private void ensureInTransaction() {
        ensureThatContext(IsisContext.inTransaction(), is(true));
        javax.jdo.Transaction currentTransaction = persistenceManager.currentTransaction();
        ensureThatState(currentTransaction, is(notNullValue()));
        ensureThatState(currentTransaction.isActive(), is(true));
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

        debug.appendTitle("OID Generator");
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

        debug.appendTitle(pojoAdapterMap.debugTitle());
        pojoAdapterMap.debugData(debug);
        debug.appendln();

        debug.appendTitle(oidAdapterMap.debugTitle());
        oidAdapterMap.debugData(debug);

        debug.appendTitle("Persistor");
        getTransactionManager().debugData(debug);
        debug.appendln();
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
     * The configured {@link ServicesInjectorSpi}.
     */
    public ServicesInjectorSpi getServicesInjector() {
        return servicesInjector;
    }


    //endregion

    //region > sub components


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


    //endregion


    //region > jdoPersistenceManager delegate methods
    public javax.jdo.Query newJdoQuery(Class<?> cls) {
        return persistenceManager.newQuery(cls);
    }

    public javax.jdo.Query newJdoNamedQuery (Class<?> cls, String queryName) {
        return persistenceManager.newNamedQuery(cls, queryName);
    }

    public javax.jdo.Query newJdoQuery (Class<?> cls, String filter) {
        return persistenceManager.newQuery(cls, filter);
    }
    // endregion

    //region > AdapterManager implementation

    private final PojoAdapterHashMap pojoAdapterMap = new PojoAdapterHashMap();
    private final OidAdapterHashMap oidAdapterMap = new OidAdapterHashMap();

    /**
     * @deprecated
     * @return - simply returns this {@link PersistenceSession}.
     */
    @Deprecated
    public AdapterManager getAdapterManager() {
        return this;
    }

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


    private ObjectAdapter existingOrValueAdapter(Object pojo) {

        // attempt to locate adapter for the pojo
        ObjectAdapter adapter = getAdapterFor(pojo);
        if (adapter != null) {
            return adapter;
        }

        // pojo may have been lazily loaded by object store, but we haven't yet seen it
        if (pojo instanceof Persistable) {
            adapter = mapPersistent((Persistable) pojo);

            // TODO: could return null if the pojo passed in !dnIsPersistent() || !dnIsDetached()
            // in which case, we would ought to map as a transient object, rather than fall through and treat as a value?
        } else {
            adapter = null;
        }

        if(adapter != null) {
            return adapter;
        }

        // need to create (and possibly map) the adapter.
        final ObjectSpecification objSpec = specificationLoader.loadSpecification(pojo.getClass());

        // we create value facets as standalone (so not added to maps)
        if (objSpec.containsFacet(ValueFacet.class)) {
            adapter = createStandaloneAdapter(pojo);
            return adapter;
        }

        return null;
    }



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
        final ObjectAdapter adapterAccordingToMap = pojoAdapterMap.getAdapter(adapterPojo);

        if(adapterPojo == null) {
            // nothing to check
            return;
        }
        ensureMapConsistent(adapter, adapterAccordingToMap, "PojoAdapterMap");
    }

    private void ensureOidAdapterMapConsistent(final ObjectAdapter adapter) {
        final Oid adapterOid = adapter.getOid();
        final ObjectAdapter adapterAccordingToMap = oidAdapterMap.getAdapter(adapterOid);

        if(adapterOid == null) {
            // nothing to check
            return;
        }
        ensureMapConsistent(adapter, adapterAccordingToMap, "OidAdapterMap");
    }

    private void ensureMapConsistent(
            final ObjectAdapter adapter,
            final ObjectAdapter adapterAccordingToMap,
            final String mapName) {

        final Oid adapterOid = adapter.getOid();

        // take care not to touch the pojo, since it might have been deleted.

        if(adapterAccordingToMap == null) {
            throw new IllegalStateException("mismatch in "
                    + mapName
                    + ": provided adapter's OID: " + adapterOid + "; but no adapter found in map");
        }
        ensureThatArg(
                adapter, is(adapterAccordingToMap),
                "mismatch in "
                        + mapName
                        + ": provided adapter's OID: " + adapterOid + ", \n"
                        + "but map's adapter's OID was: " + adapterAccordingToMap.getOid());
    }

    public ObjectAdapter adapterForAny(RootOid rootOid) {

        final ObjectSpecId specId = rootOid.getObjectSpecId();
        final ObjectSpecification spec = getSpecificationLoader().lookupBySpecId(specId);
        if(spec == null) {
            // eg "NONEXISTENT:123"
            return null;
        }

        if(spec.containsFacet(ViewModelFacet.class)) {

            // this is a hack; the RO viewer when rendering the URL for the view model loses the "view model" indicator
            // ("*") from the specId, meaning that the marshalling logic above in RootOidDefault.deString() creates an
            // oid in the wrong state.  The code below checks for this and recreates the oid with the current state of 'view model'
            if(!rootOid.isViewModel()) {
                rootOid = new RootOid(rootOid.getObjectSpecId(), rootOid.getIdentifier(), Oid.State.VIEWMODEL);
            }

            try {
                return adapterFor(rootOid);
            } catch(final ObjectNotFoundException ex) {
                return null;
            } catch(final PojoRecreationException ex) {
                return null;
            }
        } else {
            try {
                ObjectAdapter objectAdapter = loadObjectInTransaction(rootOid);
                return objectAdapter.isTransient() ? null : objectAdapter;
            } catch(final ObjectNotFoundException ex) {
                return null;
            }
        }
    }

    /**
     * As per {@link #adapterFor(RootOid, ConcurrencyChecking)}, with
     * {@link ConcurrencyChecking#NO_CHECK no checking}.
     *
     * <p>
     * This method  will <i>always</i> return an object, possibly indicating it is persistent; so make sure that you
     * know that the oid does indeed represent an object you know exists.
     * </p>
     */
    public ObjectAdapter adapterFor(final RootOid rootOid) {
        return adapterFor(rootOid, ConcurrencyChecking.NO_CHECK);
    }


    /**
     * Either returns an existing {@link ObjectAdapter adapter} (as per
     * {@link #getAdapterFor(Oid)}), otherwise re-creates an adapter with the
     * specified (persistent) {@link Oid}.
     *
     * <p>
     * Typically called when the {@link Oid} is already known, that is, when
     * resolving an already-persisted object. Is also available for
     * <tt>Memento</tt> support however, so {@link Oid} could also represent a
     * {@link Oid#isTransient() transient} object.
     *
     * <p>
     * The pojo itself is recreated by delegating to a {@link AdapterManager}.
     *
     * <p>
     * The {@link ConcurrencyChecking} parameter determines whether concurrency checking is performed.
     * If it is requested, then a check is made to ensure that the {@link Oid#getVersion() version}
     * of the {@link RootOid oid} of the recreated adapter is the same as that of the provided {@link RootOid oid}.
     * If the version differs, then a {@link ConcurrencyException} is thrown.
     *
     * <p>
     * ALSO, even if a {@link ConcurrencyException}, then the provided {@link RootOid oid}'s {@link Version version}
     * will be {@link RootOid#setVersion(Version) set} to the current
     * value.  This allows the client to retry if they wish.
     *
     * @throws {@link org.apache.isis.core.runtime.persistence.ObjectNotFoundException} if the object does not exist.
     */
    public ObjectAdapter adapterFor(
            final RootOid rootOid,
            final ConcurrencyChecking concurrencyChecking) {

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
                    if( thisVersion != null &&
                        otherVersion != null &&
                        thisVersion.different(otherVersion)) {

                        if(concurrencyCheckingGloballyEnabled && ConcurrencyChecking.isCurrentlyEnabled()) {
                            LOG.info("concurrency conflict detected on " + recreatedOid + " (" + otherVersion + ")");
                            final String currentUser = authenticationSession.getUserName();
                            throw new ConcurrencyException(currentUser, recreatedOid, thisVersion, otherVersion);
                        } else {
                            LOG.info("concurrency conflict detected but suppressed, on " + recreatedOid + " (" + otherVersion + ")");
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
            return loadPojo(oid);
        }
    }

    private Object recreatePojoDefault(final RootOid rootOid) {
        final ObjectSpecification spec =
                specificationLoader.lookupBySpecId(rootOid.getObjectSpecId());
        final Object pojo;

        if(rootOid.isViewModel()) {

            final String memento = rootOid.getIdentifier();
            pojo = recreateViewModel(spec, memento);

        } else {
            pojo = instantiateAndInjectServices(spec);

        }
        return pojo;
    }

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
    private ObjectAdapter createCollectionAdapter(
            final Object pojo,
            final ObjectAdapter parentAdapter,
            final OneToManyAssociation otma) {

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
            persistedRootOid = createPersistentOrViewModelOid(adapter.getObject());
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
    @Override
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

    private void unmap(final ObjectAdapter adapter) {
        ensureMapsConsistent(adapter);

        final Oid oid = adapter.getOid();
        if (oid != null) {
            oidAdapterMap.remove(oid);
        }
        pojoAdapterMap.remove(adapter);
    }


    public void remapRecreatedPojo(ObjectAdapter adapter, final Object pojo) {
        removeAdapter(adapter);
        adapter.replacePojo(pojo);
        mapAndInjectServices(adapter);
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

    /**
     * Creates a new transient root {@link ObjectAdapter adapter} for the supplied domain
     * object.
     */
    private ObjectAdapter createTransientOrViewModelRootAdapter(final Object pojo) {
        final RootOid rootOid = createTransientOrViewModelOid(pojo);
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
                specificationLoader, this);
    }


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


    //endregion

    //region > dependencies (from context)
    protected Localization getLocalization() {
        return IsisContext.getLocalization();
    }

    //endregion


    //region > TransactionManager delegate methods
    protected IsisTransaction getCurrentTransaction() {
        return transactionManager.getTransaction();
    }
    //endregion

    //region > FrameworkSynchronizer delegate methods

    public void enlistDeletingAndInvokeIsisRemovingCallbackFacet(final Persistable pojo) {
        ObjectAdapter adapter = adapterFor(pojo);

        final EnlistedObjectsServiceInternal enlistedObjectsServiceInternal =
                getServicesInjector().lookupService(EnlistedObjectsServiceInternal.class);

        enlistedObjectsServiceInternal.enlistDeleting(adapter);

        CallbackFacet.Util.callCallback(adapter, RemovingCallbackFacet.class);
        postLifecycleEventIfRequired(adapter, RemovingLifecycleEventFacet.class);
    }


    public void initializeMapAndCheckConcurrency(final Persistable pojo) {
        final Persistable pc = pojo;

        // need to do eagerly, because (if a viewModel then) a
        // viewModel's #viewModelMemento might need to use services
        servicesInjector.injectInto(pojo);

        final Version datastoreVersion = getVersionIfAny(pc);

        final RootOid originalOid;
        ObjectAdapter adapter = getAdapterFor(pojo);
        if (adapter != null) {
            ensureRootObject(pojo);
            originalOid = (RootOid) adapter.getOid();

            final Version originalVersion = adapter.getVersion();

            // sync the pojo held by the adapter with that just loaded
            remapRecreatedPojo(adapter, pojo);

            // since there was already an adapter, do concurrency check
            // (but don't set abort cause if checking is suppressed through thread-local)
            final RootOid thisOid = originalOid;
            final Version thisVersion = originalVersion;
            final Version otherVersion = datastoreVersion;

            if (    thisVersion != null &&
                    otherVersion != null &&
                    thisVersion.different(otherVersion)) {

                if (ConcurrencyChecking.isCurrentlyEnabled()) {
                    LOG.info("concurrency conflict detected on " + thisOid + " (" + otherVersion + ")");
                    final String currentUser = authenticationSession.getUserName();
                    final ConcurrencyException abortCause = new ConcurrencyException(currentUser, thisOid,
                            thisVersion, otherVersion);
                    getCurrentTransaction().setAbortCause(abortCause);

                } else {
                    LOG.info("concurrency conflict detected but suppressed, on " + thisOid + " (" + otherVersion
                            + ")");
                }
            }
        } else {
            originalOid = createPersistentOrViewModelOid(pojo);

            // it appears to be possible that there is already an adapter for this Oid,
            // ie from ObjectStore#resolveImmediately()
            adapter = getAdapterFor(originalOid);
            if (adapter != null) {
                remapRecreatedPojo(adapter, pojo);
            } else {
                adapter = mapRecreatedPojo(originalOid, pojo);

                CallbackFacet.Util.callCallback(adapter, LoadedCallbackFacet.class);
                postLifecycleEventIfRequired(adapter, LoadedLifecycleEventFacet.class);
            }
        }

        adapter.setVersion(datastoreVersion);
    }

    //region > create...Oid (main API)
    /**
     * Create a new {@link Oid#isTransient() transient} {@link Oid} for the
     * supplied pojo, uniquely distinguishable from any other {@link Oid}.
     */
    public final RootOid createTransientOrViewModelOid(final Object pojo) {
        return newIdentifier(pojo, Type.TRANSIENT);
    }

    /**
     * Return an equivalent {@link RootOid}, but being persistent.
     *
     * <p>
     * It is the responsibility of the implementation to determine the new unique identifier.
     * For example, the generator may simply assign a new value from a sequence, or a GUID;
     * or, the generator may use the oid to look up the object and inspect the object in order
     * to obtain an application-defined value.
     *
     * @param pojo - being persisted
     */
    public final RootOid createPersistentOrViewModelOid(Object pojo) {
        return newIdentifier(pojo, Type.PERSISTENT);
    }

    enum Type {
        TRANSIENT,
        PERSISTENT
    }

    private RootOid newIdentifier(final Object pojo, final Type type) {
        final ObjectSpecification spec = objectSpecFor(pojo);
        if(spec.isService()) {
            return newRootId(spec, "1", type);
        }

        final ViewModelFacet recreatableObjectFacet = spec.getFacet(ViewModelFacet.class);
        final String identifier =
                recreatableObjectFacet != null
                        ? recreatableObjectFacet.memento(pojo)
                        : newIdentifierFor(pojo, type);

        return newRootId(spec, identifier, type);
    }

    private String newIdentifierFor(final Object pojo, final Type type) {
        return type == Type.TRANSIENT
                ? UUID.randomUUID().toString()
                : JdoObjectIdSerializer.toOidIdentifier(getPersistenceManager().getObjectId(pojo));
    }

    private RootOid newRootId(final ObjectSpecification spec, final String identifier, final Type type) {
        final Oid.State state =
                spec.containsDoOpFacet(ViewModelFacet.class)
                        ? Oid.State.VIEWMODEL
                        : type == Type.TRANSIENT
                        ? Oid.State.TRANSIENT
                        : Oid.State.PERSISTENT;
        final ObjectSpecId objectSpecId = spec.getSpecId();
        return new RootOid(objectSpecId, identifier, state);
    }

    private ObjectSpecification objectSpecFor(final Object pojo) {
        final Class<?> pojoClass = pojo.getClass();
        return getSpecificationLoader().loadSpecification(pojoClass);
    }
    //endregion


    /**
     * Called either when an entity is initially persisted, or when an entity is updated; fires the appropriate
     * lifecycle callback.
     *
     * <p>
     * The implementation therefore uses Isis' {@link org.apache.isis.core.metamodel.adapter.oid.Oid#isTransient() oid}
     * to determine which callback to fire.
     */
    public void invokeIsisPersistingCallback(final Persistable pojo) {
        final ObjectAdapter adapter = getAdapterFor(pojo);
        if (adapter == null) {
            // not expected.
            return;
        }

        final RootOid isisOid = (RootOid) adapter.getOid();
        if (isisOid.isTransient()) {
            // persisting
            // previously this was performed in the DataNucleusSimplePersistAlgorithm.
            CallbackFacet.Util.callCallback(adapter, PersistingCallbackFacet.class);
            postLifecycleEventIfRequired(adapter, PersistingLifecycleEventFacet.class);

        } else {
            // updating

            // don't call here, already called in preDirty.

            // CallbackFacet.Util.callCallback(adapter, UpdatingCallbackFacet.class);
        }
    }

    /**
     * Called either when an entity is initially persisted, or when an entity is updated;
     * fires the appropriate lifecycle callback
     *
     * <p>
     * The implementation therefore uses Isis' {@link org.apache.isis.core.metamodel.adapter.oid.Oid#isTransient() oid}
     * to determine which callback to fire.
     */
    public void enlistCreatedAndRemapIfRequiredThenInvokeIsisInvokePersistingOrUpdatedCallback(final Persistable pojo) {
        final ObjectAdapter adapter = adapterFor(pojo);

        final RootOid rootOid = (RootOid) adapter.getOid(); // ok since this is for a Persistable

        if (rootOid.isTransient()) {
            // persisting
            final RootOid persistentOid = createPersistentOrViewModelOid(pojo);

            remapAsPersistent(adapter, persistentOid);

            CallbackFacet.Util.callCallback(adapter, PersistedCallbackFacet.class);
            postLifecycleEventIfRequired(adapter, PersistedLifecycleEventFacet.class);


            final EnlistedObjectsServiceInternal enlistedObjectsServiceInternal =
                    getServicesInjector().lookupService(EnlistedObjectsServiceInternal.class);

            enlistedObjectsServiceInternal.enlistCreated(adapter);

        } else {
            // updating;
            // the callback and transaction.enlist are done in the preDirty callback
            // (can't be done here, as the enlist requires to capture the 'before' values)
            CallbackFacet.Util.callCallback(adapter, UpdatedCallbackFacet.class);
            postLifecycleEventIfRequired(adapter, UpdatedLifecycleEventFacet.class);
        }

        Version versionIfAny = getVersionIfAny(pojo);
        adapter.setVersion(versionIfAny);
    }

    public void enlistUpdatingAndInvokeIsisUpdatingCallback(final Persistable pojo) {
        ObjectAdapter adapter = getAdapterFor(pojo);
        if (adapter == null) {
            // seen this happen in the case when a parent entity (LeaseItem) has a collection of children
            // objects (LeaseTerm) for which we haven't had a loaded callback fired and so are not yet
            // mapped.

            // it seems reasonable in this case to simply map into Isis here ("just-in-time"); presumably
            // DN would not be calling this callback if the pojo was not persistent.

            adapter = mapPersistent(pojo);
            if (adapter == null) {
                throw new RuntimeException(
                        "DN could not find objectId for pojo (unexpected) and so could not map into Isis; pojo=["
                                + pojo + "]");
            }
        }
        if (adapter.isTransient()) {
            // seen this happen in the case when there's a 1<->m bidirectional collection, and we're
            // attaching the child object, which is being persisted by DN as a result of persistence-by-reachability,
            // and it "helpfully" sets up the parent attribute on the child, causing this callback to fire.
            //
            // however, at the same time, Isis has only queued up a CreateObjectCommand for the transient object, but it
            // hasn't yet executed, so thinks that the adapter is still transient.
            return;
        }

        CallbackFacet.Util.callCallback(adapter, UpdatingCallbackFacet.class);
        postLifecycleEventIfRequired(adapter, UpdatingLifecycleEventFacet.class);


        final EnlistedObjectsServiceInternal enlistedObjectsServiceInternal =
                getServicesInjector().lookupService(EnlistedObjectsServiceInternal.class);

        enlistedObjectsServiceInternal.enlistUpdating(adapter);

        ensureRootObject(pojo);
    }

    /**
     * makes sure the entity is known to Isis and is a root
     * @param pojo
     */
    public void ensureRootObject(final Persistable pojo) {
        final Oid oid = adapterFor(pojo).getOid();
        if (!(oid instanceof RootOid)) {
            throw new IsisException(MessageFormat.format("Not a RootOid: oid={0}, for {1}", oid, pojo));
        }
    }

    private Version getVersionIfAny(final Persistable pojo) {
        return Utils.getVersionIfAny(pojo, authenticationSession);
    }


    //endregion

    //region > DomainObjectServices impl


    @Override
    public Object lookup(
            final Bookmark bookmark,
            final BookmarkService2.FieldResetPolicy fieldResetPolicy) {
        return new DomainObjectContainerResolve().lookup(bookmark, fieldResetPolicy);
    }

    @Override
    public Bookmark bookmarkFor(Object domainObject) {
        return new DomainObjectContainerResolve().bookmarkFor(domainObject);
    }

    @Override
    public Bookmark bookmarkFor(Class<?> cls, String identifier) {
        return new DomainObjectContainerResolve().bookmarkFor(cls, identifier);
    }


    @Override
    public void resolve(final Object parent) {
        new DomainObjectContainerResolve().resolve(parent);
    }

    @Override
    public void resolve(final Object parent, final Object field) {
        new DomainObjectContainerResolve().resolve(parent, field);
    }

    @Override
    public void beginTran() {
        getTransactionManager().startTransaction();
    }

    @Override
    public boolean flush() {
        return getTransactionManager().flushTransaction();
    }

    @Override
    public void commit() {
        getTransactionManager().endTransaction();
    }

    @Override
    public void informUser(final String message) {
        getCurrentTransaction().getMessageBroker().addMessage(message);
    }

    @Override
    public void warnUser(final String message) {
        getCurrentTransaction().getMessageBroker().addWarning(message);
    }

    @Override
    public void raiseError(final String message) {
        throw new RecoverableException(message);
    }



    //endregion

    //region > ConfigurationService delegate

    @Override
    public String getProperty(final String name) {
        return configuration.getProperty(name);
    }

    @Override
    public List<String> getPropertyNames() {
        return configuration.getPropertyNames();
    }

    //endregion


}



