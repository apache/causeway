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
import java.util.Properties;

import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.datanucleus.enhancement.Persistable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer2;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.runtimecontext.DomainObjectServices;
import org.apache.isis.core.metamodel.runtimecontext.DomainObjectServicesAware;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.runtimecontext.ObjectPersistor;
import org.apache.isis.core.metamodel.runtimecontext.ObjectPersistorAware;
import org.apache.isis.core.metamodel.runtimecontext.MessageBrokerService;
import org.apache.isis.core.metamodel.runtimecontext.MessageBrokerServiceAware;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
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
import org.apache.isis.core.runtime.persistence.PojoRefreshException;
import org.apache.isis.core.runtime.persistence.UnsupportedFindException;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.core.runtime.persistence.container.DomainObjectContainerResolve;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.TransactionalResource;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerFixtureAbstract;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosure;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosureWithReturn;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.commands.DataNucleusCreateObjectCommand;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.commands.DataNucleusDeleteObjectCommand;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryFindAllInstancesProcessor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryFindUsingApplibQueryProcessor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryProcessor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.spi.JdoObjectIdSerializer;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatContext;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

public class PersistenceSession implements TransactionalResource, SessionScopedComponent, DebuggableWithTitle, AdapterManager,
        MessageBrokerService, ObjectPersistor, DomainObjectServices {

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
    private final ObjectFactory objectFactory;

    private final PersistenceSessionFactory persistenceSessionFactory;
    private final OidGenerator oidGenerator;
    private final AdapterManagerDefault adapterManager;

    private final PersistenceQueryFactory persistenceQueryFactory;
    private final IsisConfiguration configuration;
    private final SpecificationLoaderSpi specificationLoader;
    private final AuthenticationSession authenticationSession;

    private final ServicesInjectorSpi servicesInjector;

    // not final only for testing purposes
    private IsisTransactionManager transactionManager;

    private final OidMarshaller oidMarshaller;

    /**
     * populated only when {@link #open()}ed.
     */
    private PersistenceManager persistenceManager;

    /**
     * populated only when {@link #open()}ed.
     */
    private final Map<Class<?>, PersistenceQueryProcessor<?>> persistenceQueryProcessorByClass = Maps.newHashMap();

    private final Map<ObjectSpecId, RootOid> registeredServices = Maps.newHashMap();
    private final DataNucleusApplicationComponents applicationComponents;

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
        this.applicationComponents = persistenceSessionFactory.getApplicationComponents();

        // sub-components

        this.oidMarshaller = new OidMarshaller();

        this.objectFactory = new ObjectFactory(this, servicesInjector);
        this.oidGenerator = new OidGenerator(this, specificationLoader);

        this.adapterManager = new AdapterManagerDefault(this, specificationLoader, oidMarshaller,
                oidGenerator, authenticationSession, servicesInjector, configuration);

        this.persistenceQueryFactory = new PersistenceQueryFactory(getSpecificationLoader(), adapterManager);
        this.transactionManager = new IsisTransactionManager(this, servicesInjector);

        setState(State.NOT_INITIALIZED);

        // to implement DomainObjectServices
        final Properties properties = RuntimeContextFromSession.applicationPropertiesFrom(configuration);
        setProperties(properties);

        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + this);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("finalizing persistence session");
    }

    //endregion

    //region > open

    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

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

        // inject any required dependencies into object factory
        servicesInjector.injectInto(objectFactory);
        servicesInjector.injectInto(adapterManager);

        adapterManager.open();

        persistenceManager = applicationComponents.getPersistenceManagerFactory().getPersistenceManager();

        final IsisLifecycleListener2 isisLifecycleListener = new IsisLifecycleListener2(this);
        persistenceManager.addInstanceLifecycleListener(isisLifecycleListener, (Class[])null);

        persistenceQueryProcessorByClass.put(
                PersistenceQueryFindAllInstances.class,
                new PersistenceQueryFindAllInstancesProcessor(this));
        persistenceQueryProcessorByClass.put(
                PersistenceQueryFindUsingApplibQueryDefault.class,
                new PersistenceQueryFindUsingApplibQueryProcessor(this));

        initServices();

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
                            ? getAdapterManager().adapterFor(service)
                            : getAdapterManager().mapRecreatedPojo(existingOid, service);
            if (serviceAdapter.getOid().isTransient()) {
                adapterManager.remapAsPersistent(serviceAdapter, null);
            }

            if (existingOid == null) {
                final RootOid persistentOid = (RootOid) serviceAdapter.getOid();
                this.registeredServices.put(persistentOid.getObjectSpecId(), persistentOid);
            }
        }
    }

    /**
     * @return - the service, or <tt>null</tt> if no service registered of specified type.
     */
    public <T> T getServiceOrNull(final Class<T> serviceType) {
        return servicesInjector.lookupService(serviceType);
    }

    //endregion

    //region > close

    /**
     * Calls {@link org.apache.isis.core.commons.components.SessionScopedComponent#close()}
     * on the subcomponents.
     *
     * <p>
     * Automatically {@link IsisTransactionManager#endTransaction() ends
     * (commits)} the current (Isis) {@link IsisTransaction}. This in turn commits the underlying
     * JDO transaction.
     *
     * <p>
     * The corresponding DataNucleus entity is then closed.
     */
    @Override
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

        try {
            adapterManager.close();
        } catch(final Throwable ex) {
            // ignore
            LOG.error("close: adapterManager#close() failed; continuing to avoid memory leakage");
        }

        setState(State.CLOSED);
    }

    //endregion

    //region > Injectable
    //endregion

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
        if (ObjectPersistorAware.class.isAssignableFrom(candidate.getClass())) {
            final ObjectPersistorAware cast = ObjectPersistorAware.class.cast(candidate);
            cast.setObjectPersistor(this);
        }
        if (DomainObjectServicesAware.class.isAssignableFrom(candidate.getClass())) {
            final DomainObjectServicesAware cast = DomainObjectServicesAware.class.cast(candidate);
            cast.setDomainObjectServices(this);
        }
    }

    //region > QuerySubmitter impl

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
            final ObjectSpecification objectSpec,
            final Variant variant,
            final String memento) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + variant + " instance of " + objectSpec);
        }
        final Object pojo = instantiateAndInjectServices(objectSpec);

        if(variant == Variant.VIEW_MODEL) {
            final ViewModelFacet facet = objectSpec.getFacet(ViewModelFacet.class);
            facet.initialize(pojo, memento);
        }

        final ObjectAdapter adapter = adapterManager.adapterFor(pojo);
        return initializePropertiesAndDoCallback(adapter);
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
        servicesInjector.injectServicesInto(adapter.getObject());

        CallbackFacet.Util.callCallback(adapter, CreatedCallbackFacet.class);

        return adapter;
    }


    //endregion

    //region > findInstancesInTransaction

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
    public <T> ObjectAdapter findInstancesInTransaction(final Query<T> query, final QueryCardinality cardinality) {
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
        return getAdapterManager().adapterFor(results);
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

    //endregion

    //region > getServices, getService

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
        final ObjectSpecification serviceSpecification =
                specificationLoader.loadSpecification(servicePojo.getClass());
        final RootOid oid = getOidForService(serviceSpecification);
        final ObjectAdapter serviceAdapter = getAdapterManager().mapRecreatedPojo(oid, servicePojo);

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
        if (persistenceSessionFactory.isFixturesInstalled() == null) {
            persistenceSessionFactory.setFixturesInstalled(objectStoreIsFixturesInstalled());
        }
        return persistenceSessionFactory.isFixturesInstalled();
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
        // alternatively, can call getAdapterManager().adapterFor(oid); this code
        // delegates to the PojoRecreator which *does* take view models into account
        //
        // it's possible, therefore, that existing callers to this method (the Scimpi viewer)
        // could be refactored to use getAdapterManager().adapterFor(...)
        ensureThatArg(oid, is(notNullValue()));

        final ObjectAdapter adapter = adapterManager.getAdapterFor(oid);
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
                        return adapterManager.mapRecreatedPojo(oid, pojo);
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
        final RootOid oid = oidGenerator.createPersistentOrViewModelOid(pojo);
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
     * {@link #newCreateObjectCommand(ObjectAdapter) Create}s a {@link CreateObjectCommand}, and adds to the
     * {@link IsisTransactionManager}.
     */
    public void addCreateObjectCommand(final ObjectAdapter object) {
        final CreateObjectCommand createObjectCommand = newCreateObjectCommand(object);
        transactionManager.addCommand(createObjectCommand);
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

    /**
     * The configured {@link ObjectFactory}.
     */
    public ObjectFactory getObjectFactory() {
        return objectFactory;
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
    @Override
    public ObjectAdapter getAdapterFor(final Oid oid) {
        return adapterManager.getAdapterFor(oid);
    }

    @Override
    public ObjectAdapter getAdapterFor(final Object pojo) {
        return adapterManager.getAdapterFor(pojo);
    }

    @Override
    public ObjectAdapter adapterFor(final RootOid oid) {
        return adapterManager.adapterFor(oid);
    }

    @Override
    public ObjectAdapter adapterFor(
            final RootOid oid,
            final ConcurrencyChecking concurrencyChecking) {
        return adapterManager.adapterFor(oid, concurrencyChecking);
    }

    public ObjectAdapter adapterFor(final Object pojo) {
        return adapterManager.adapterFor(pojo);
    }

    @Override
    public ObjectAdapter adapterFor(
            final Object pojo, final ObjectAdapter parentAdapter, final OneToManyAssociation collection) {
        return adapterManager.adapterFor(pojo, parentAdapter, collection);
    }

    public void remapAsPersistent(final ObjectAdapter adapter, RootOid hintRootOid) {
        adapterManager.remapAsPersistent(adapter, hintRootOid);
    }

    @Override
    public ObjectAdapter mapRecreatedPojo(final Oid oid, final Object recreatedPojo) {
        return adapterManager.mapRecreatedPojo(oid, recreatedPojo);
    }

    @Override
    public void removeAdapter(final ObjectAdapter adapter) {
        adapterManager.removeAdapter(adapter);
    }

    public void remapRecreatedPojo(ObjectAdapter adapter, final Object pojo) {
        adapterManager.remapRecreatedPojo(adapter, pojo);
    }

    // endregion

    //region > TransactionManager delegate methods
    protected IsisTransaction getCurrentTransaction() {
        return transactionManager.getTransaction();
    }
    //endregion

    //region > FrameworkSynchronizer delegate methods

    public void enlistDeletingAndInvokeIsisRemovingCallbackFacet(final Persistable pojo) {
        ObjectAdapter adapter = adapterFor(pojo);

        final IsisTransaction transaction = getCurrentTransaction();
        transaction.enlistDeleting(adapter);

        CallbackFacet.Util.callCallback(adapter, RemovingCallbackFacet.class);
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

                if (AdapterManager.ConcurrencyChecking.isCurrentlyEnabled()) {
                    LOG.info("concurrency conflict detected on " + thisOid + " (" + otherVersion + ")");
                    final String currentUser = authenticationSession.getUserName();
                    final ConcurrencyException abortCause = new ConcurrencyException(currentUser, thisOid,
                            thisVersion, otherVersion);
                    getCurrentTransaction().setAbortCause(abortCause);

                } else {
                    LOG.warn("concurrency conflict detected but suppressed, on " + thisOid + " (" + otherVersion
                            + ")");
                }
            }
        } else {
            originalOid = oidGenerator.createPersistentOrViewModelOid(pojo);

            // it appears to be possible that there is already an adapter for this Oid,
            // ie from ObjectStore#resolveImmediately()
            adapter = getAdapterFor(originalOid);
            if (adapter != null) {
                remapRecreatedPojo(adapter, pojo);
            } else {
                adapter = mapRecreatedPojo(originalOid, pojo);
                CallbackFacet.Util.callCallback(adapter, LoadedCallbackFacet.class);
            }
        }

        adapter.setVersion(datastoreVersion);
    }

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
        final ObjectAdapter objectAdapter = adapterFor(pojo);

        final ObjectAdapter adapter = objectAdapter;
        final RootOid rootOid = (RootOid) adapter.getOid(); // ok since this is for a Persistable

        if (rootOid.isTransient()) {
            // persisting
            final RootOid persistentOid = oidGenerator.createPersistentOrViewModelOid(pojo);

            remapAsPersistent(adapter, persistentOid);

            CallbackFacet.Util.callCallback(adapter, PersistedCallbackFacet.class);

            final IsisTransaction transaction = getCurrentTransaction();
            transaction.enlistCreated(adapter);

        } else {
            // updating;
            // the callback and transaction.enlist are done in the preDirty callback
            // (can't be done here, as the enlist requires to capture the 'before' values)
            CallbackFacet.Util.callCallback(adapter, UpdatedCallbackFacet.class);
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

        getCurrentTransaction().enlistUpdating(adapter);

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
    public Object lookup(Bookmark bookmark) {
        return new DomainObjectContainerResolve().lookup(bookmark);
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

    private Properties properties;

    private void setProperties(final Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getProperty(final String name) {
        return properties.getProperty(name);
    }

    @Override
    public List<String> getPropertyNames() {
        final List<String> list = Lists.newArrayList();
        for (final Object key : properties.keySet()) {
            list.add((String) key);
        }
        return list;
    }



    //endregion


}



