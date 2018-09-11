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

import static java.util.Objects.requireNonNull;
import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.identity.SingleFieldIdentity;
import javax.jdo.listener.InstanceLifecycleListener;

import com.google.common.collect.Maps;

import org.datanucleus.enhancement.Persistable;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.identity.DatastoreIdImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterByIdProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
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
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.FreeStandingList;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.FixturesInstalledFlag;
import org.apache.isis.core.runtime.persistence.NotPersistableException;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.PojoRefreshException;
import org.apache.isis.core.runtime.persistence.UnsupportedFindException;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;
import org.apache.isis.core.runtime.services.RequestScopedService;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.ObjectAdapterContext;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.ObjectAdapterContext.MementoRecreateObjectSupport;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.commands.DataNucleusCreateObjectCommand;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.commands.DataNucleusDeleteObjectCommand;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryFindAllInstancesProcessor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryFindUsingApplibQueryProcessor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryProcessor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.spi.JdoObjectIdSerializer;

/**
 * A wrapper around the JDO {@link PersistenceManager}, which also manages concurrency
 * and maintains an identity map of {@link ObjectAdapter adapter}s and {@link Oid
 * identities} for each and every POJO that is being used by the framework.
 */
public class PersistenceSession5 extends PersistenceSessionBase
implements IsisLifecycleListener.PersistenceSessionLifecycleManagement {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceSession5.class);
    private ObjectAdapterContext objectAdapterContext;

    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     */
    public PersistenceSession5(
            final ServicesInjector servicesInjector,
            final AuthenticationSession authenticationSession,
            final PersistenceManagerFactory jdoPersistenceManagerFactory,
            final FixturesInstalledFlag fixturesInstalledFlag) {

        super(servicesInjector, authenticationSession, jdoPersistenceManagerFactory, fixturesInstalledFlag);
    }

    // -- open

    /**
     * Injects components, calls open on subcomponents, and then creates service
     * adapters.
     */
    @Override
    public void open() {
        ensureNotOpened();

        if (LOG.isDebugEnabled()) {
            LOG.debug("opening {}", this);
        }

        persistenceManager = jdoPersistenceManagerFactory.getPersistenceManager();

        final IsisLifecycleListener.PersistenceSessionLifecycleManagement psLifecycleMgmt = this;
        final IsisLifecycleListener isisLifecycleListener = new IsisLifecycleListener(psLifecycleMgmt);
        persistenceManager.addInstanceLifecycleListener(isisLifecycleListener, (Class[]) null);

        persistenceQueryProcessorByClass.put(
                PersistenceQueryFindAllInstances.class,
                new PersistenceQueryFindAllInstancesProcessor(this));
        persistenceQueryProcessorByClass.put(
                PersistenceQueryFindUsingApplibQueryDefault.class,
                new PersistenceQueryFindUsingApplibQueryProcessor(this));

        objectAdapterContext = ObjectAdapterContext.openContext(servicesInjector, authenticationSession, specificationLoader, this);

        // tell the proxy of all request-scoped services to instantiate the underlying
        // services, store onto the thread-local and inject into them...
        startRequestOnRequestScopedServices();

        // ... and invoke all @PostConstruct
        postConstructOnRequestScopedServices();

        if(metricsService instanceof InstanceLifecycleListener) {
            final InstanceLifecycleListener metricsService = (InstanceLifecycleListener) this.metricsService;
            persistenceManager.addInstanceLifecycleListener(metricsService, (Class[]) null);
        }


        final Command command = createCommand();
        final UUID transactionId = UUID.randomUUID();
        final Interaction interaction = factoryService.instantiate(Interaction.class);

        final Timestamp timestamp = clockService.nowAsJavaSqlTimestamp();
        final String userName = userService.getUser().getName();

        command.setTimestamp(timestamp);
        command.setUser(userName);
        command.setTransactionId(transactionId);

        interaction.setTransactionId(transactionId);

        commandContext.setCommand(command);
        interactionContext.setInteraction(interaction);

        this.state = State.OPEN;
    }

    private void postConstructOnRequestScopedServices() {
        for (final Object service : servicesInjector.getRegisteredServices()) {
            if(service instanceof RequestScopedService) {
                ((RequestScopedService)service).__isis_postConstruct();
            }
        }
    }

    private void startRequestOnRequestScopedServices() {
        for (final Object service : servicesInjector.getRegisteredServices()) {
            if(service instanceof RequestScopedService) {
                ((RequestScopedService)service).__isis_startRequest(servicesInjector);
            }
        }
    }

    private Command createCommand() {
        final Command command = commandService.create();

        servicesInjector.injectServicesInto(command);
        return command;
    }

    // -- close

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
    @Override
    public void close() {

        if (state == State.CLOSED) {
            // nothing to do
            return;
        }

        completeCommandFromInteractionAndClearDomainEvents();
        transactionManager.flushTransaction();

        try {
            final IsisTransaction currentTransaction = transactionManager.getCurrentTransaction();
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

        // tell the proxy of all request-scoped services to invoke @PreDestroy
        // (if any) on all underlying services stored on their thread-locals...
        preDestroyOnRequestScopedServices();

        // ... and then remove those underlying services from the thread-local
        endRequestOnRequestScopeServices();

        try {
            persistenceManager.close();
        } catch(final Throwable ex) {
            // ignore
            LOG.error(
                    "close: failed to close JDO persistenceManager; continuing to avoid memory leakage");
        }

        objectAdapterContext.close();

        this.state = State.CLOSED;
    }

    private void endRequestOnRequestScopeServices() {
        for (final Object service : servicesInjector.getRegisteredServices()) {
            if(service instanceof RequestScopedService) {
                ((RequestScopedService)service).__isis_endRequest();
            }
        }
    }

    private void preDestroyOnRequestScopedServices() {
        for (final Object service : servicesInjector.getRegisteredServices()) {
            if(service instanceof RequestScopedService) {
                ((RequestScopedService)service).__isis_preDestroy();
            }
        }
    }

    private void completeCommandFromInteractionAndClearDomainEvents() {

        final Command command = commandContext.getCommand();
        final Interaction interaction = interactionContext.getInteraction();


        if(command.getStartedAt() != null && command.getCompletedAt() == null) {
            // the guard is in case we're here as the result of a redirect following a previous exception;just ignore.

            final Timestamp completedAt;
            final Interaction.Execution<?, ?> priorExecution = interaction.getPriorExecution();
            if (priorExecution != null) {
                // copy over from the most recent (which will be the top-level) interaction
                completedAt = priorExecution.getCompletedAt();
            } else {
                // this could arise as the result of calling SessionManagementService#nextSession within an action
                // the best we can do is to use the current time

                // REVIEW: as for the interaction object, it is left somewhat high-n-dry.
                completedAt = clockService.nowAsJavaSqlTimestamp();
            }

            command.setCompletedAt(completedAt);
        }

        // ensureCommandsPersistedIfDirtyXactn

        // ensure that any changed objects means that the command should be persisted
        if(command.getMemberIdentifier() != null) {
            if(metricsService.numberObjectsDirtied() > 0) {
                command.setPersistHint(true);
            }
        }

        commandService.complete(command);

        command.flushActionDomainEvents();

        interaction.clear();
    }

    // -- QuerySubmitter impl, findInstancesInTransaction
    @Override
    public <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query) {
        final ObjectAdapter instances = findInstancesInTransaction(query, QueryCardinality.MULTIPLE);
        return CollectionFacet.Utils.convertToAdapterList(instances);
    }
    @Override
    public <T> ObjectAdapter firstMatchingQuery(final Query<T> query) {
        final ObjectAdapter instances = findInstancesInTransaction(query, QueryCardinality.SINGLE);
        final List<ObjectAdapter> list = CollectionFacet.Utils.convertToAdapterList(instances);
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
            LOG.debug("findInstances using (applib) Query: {}", query);
        }

        // TODO: unify PersistenceQuery and PersistenceQueryProcessor
        final PersistenceQuery persistenceQuery = createPersistenceQueryFor(query, cardinality);
        if (LOG.isDebugEnabled()) {
            LOG.debug("maps to (core runtime) PersistenceQuery: {}", persistenceQuery);
        }

        final PersistenceQueryProcessor<? extends PersistenceQuery> processor = lookupProcessorFor(persistenceQuery);

        final List<ObjectAdapter> instances = transactionManager.executeWithinTransaction(
                ()->processPersistenceQuery(processor, persistenceQuery) );
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


    // -- fixture installation

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
    @Override
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
    private boolean objectStoreIsFixturesInstalled() {
        final boolean installFixtures = configuration.getBoolean(INSTALL_FIXTURES_KEY, INSTALL_FIXTURES_DEFAULT);
        LOG.info("isFixturesInstalled: {} = {}", INSTALL_FIXTURES_KEY, installFixtures);
        return !installFixtures;
    }

    // -- FETCHING

    @Override
    public Object fetchPersistentPojo(final RootOid rootOid) {
        Objects.requireNonNull(rootOid);
        LOG.debug("getObject; oid={}", rootOid);
        
        Object result;
        try {
            final Class<?> cls = clsOf(rootOid);
            final Object jdoObjectId = JdoObjectIdSerializer.toJdoObjectId(rootOid);
            FetchPlan fetchPlan = persistenceManager.getFetchPlan();
            fetchPlan.addGroup(FetchGroup.DEFAULT);
            result = persistenceManager.getObjectById(cls, jdoObjectId);
        } catch (final RuntimeException e) {

            Class<ExceptionRecognizer> serviceClass = ExceptionRecognizer.class;
            final List<ExceptionRecognizer> exceptionRecognizers = lookupServices(serviceClass);
            for (ExceptionRecognizer exceptionRecognizer : exceptionRecognizers) {
                final ExceptionRecognizer.Recognition recognition =
                        exceptionRecognizer.recognize2(e);
                if(recognition != null) {
                    if(recognition.getCategory() == ExceptionRecognizer.Category.NOT_FOUND) {
                        throw new ObjectNotFoundException(rootOid, e);
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

    @Override
    public Map<RootOid,Object> fetchPersistentPojos(final List<RootOid> rootOids) {

        if(rootOids.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<Object> dnOids = new ArrayList<>(rootOids.size());
        for (final RootOid rootOid : rootOids) {
            final Object id = JdoObjectIdSerializer.toJdoObjectId(rootOid);
            if(id instanceof SingleFieldIdentity) {
                dnOids.add(id);
            } else if (id instanceof String && ((String) id).contains("[OID]")) {
                final DatastoreIdImpl datastoreId = new DatastoreIdImpl((String)id);
                dnOids.add(datastoreId);
            } else {
                // application identity
                final DatastoreIdImpl datastoreId = new DatastoreIdImpl(clsOf(rootOid).getName(), id);
                dnOids.add(datastoreId);
            }
        }
        FetchPlan fetchPlan = persistenceManager.getFetchPlan();
        fetchPlan.addGroup(FetchGroup.DEFAULT);
        final List<Object> persistentPojos = new ArrayList<>(rootOids.size());
        try {
            final Collection<Object> pojos = uncheckedCast(persistenceManager.getObjectsById(dnOids, true));
            for (final Object pojo : pojos) {
                try {
                    persistentPojos.add(pojo);
                } catch(Exception ex) {
                    persistentPojos.add(null);
                }
            }
        } catch(NucleusObjectNotFoundException nonfe) {
            // at least one not found; fall back to loading one by one
            for (final Object dnOid : dnOids) {
                try {
                    final Object persistentPojo = persistenceManager.getObjectById(dnOid);
                    persistentPojos.add(persistentPojo);
                } catch(Exception ex) {
                    persistentPojos.add(null);
                }
            }
        }
        Map<RootOid, Object> pojoByOid = zip(rootOids, persistentPojos);
        return pojoByOid;
    }

    private static Map<RootOid, Object> zip(final List<RootOid> rootOids, final Collection<Object> pojos) {
        final Map<RootOid,Object> pojoByOid = Maps.newLinkedHashMap();
        int i = 0;
        for (final Object pojo : pojos) {
            final RootOid rootOid = rootOids.get(i++);
            pojoByOid.put(rootOid, pojo);
        }
        return pojoByOid;
    }

    private Class<?> clsOf(final RootOid oid) {
        final ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecId(oid.getObjectSpecId());
        return objectSpec.getCorrespondingClass();
    }


    // -- REFRESH
    
    @Override
    public void refreshRoot(final Object domainObject) {
        
        if(!isRepresentingPersistent(domainObject)) {
            debugLogNotPersistentIgnoring(domainObject);
            return; // only resolve object that is representing persistent
        }
        
        debugLogRefreshImmediately(domainObject);

        try {
            persistenceManager.refresh(domainObject);
        } catch (final RuntimeException e) {
            final Oid oid = oidFor(domainObject);
            throw new PojoRefreshException(oid, e);
        }

        // possibly redundant because also called in the post-load event
        // listener, but (with JPA impl) found it was required if we were ever to
        // get an eager left-outer-join as the result of a refresh (sounds possible).
        initializeMapAndCheckConcurrency((Persistable) domainObject);
    }
    
    // -- makePersistent

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
    @Override
    public void makePersistentInTransaction(final ObjectAdapter adapter) {
        if (adapter.representsPersistent()) {
            throw new NotPersistableException("Object already persistent: " + adapter);
        }
        final ObjectSpecification specification = adapter.getSpecification();
        if (specification.isService()) {
            throw new NotPersistableException("Cannot persist services: " + adapter);
        }

        getTransactionManager().executeWithinTransaction(()->{
                makePersistentTransactionAssumed(adapter);
                // clear out the map of transient -> persistent
                // already empty // PersistenceSession5.this.persistentByTransient.clear();
        });
    }

    private void makePersistentTransactionAssumed(final ObjectAdapter adapter) {
        if (alreadyPersistedOrNotPersistable(adapter)) {
            return;
        }
        LOG.debug("persist {}", adapter);

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
        return adapter.isParentedCollection();
    }

    // -- destroyObjectInTransaction

    /**
     * Removes the specified object from the system. The specified object's data
     * should be removed from the persistence mechanism.
     */
    @Override
    public void destroyObjectInTransaction(final ObjectAdapter adapter) {
        final ObjectSpecification spec = adapter.getSpecification();
        if (spec.isParented()) {
            return;
        }
        LOG.debug("destroyObject {}", adapter);
        transactionManager.executeWithinTransaction(()->{
                final DestroyObjectCommand command = newDestroyObjectCommand(adapter);
                transactionManager.addCommand(command);
        });
    }

    // -- newXxxCommand
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

        LOG.debug("create object - creating command for: {}", adapter);
        if (adapter.representsPersistent()) {
            throw new IllegalArgumentException("Adapter is persistent; adapter: " + adapter);
        }
        return new DataNucleusCreateObjectCommand(adapter, persistenceManager);
    }

    private DestroyObjectCommand newDestroyObjectCommand(final ObjectAdapter adapter) {
        ensureOpened();

        LOG.debug("destroy object - creating command for: {}", adapter);
        if (!adapter.representsPersistent()) {
            throw new IllegalArgumentException("Adapter is not persistent; adapter: " + adapter);
        }
        return new DataNucleusDeleteObjectCommand(adapter, persistenceManager);
    }


    // -- execute
    @Override
    public void execute(final List<PersistenceCommand> commands) {

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

    // -- FrameworkSynchronizer delegate methods

    @Override
    public void enlistDeletingAndInvokeIsisRemovingCallbackFacet(final Persistable pojo) {
        ObjectAdapter adapter = adapterFor(pojo);

        changedObjectsServiceInternal.enlistDeleting(adapter);

        CallbackFacet.Util.callCallback(adapter, RemovingCallbackFacet.class);
        objectAdapterContext.postLifecycleEventIfRequired(adapter, RemovingLifecycleEventFacet.class);
    }

    @Override
    public ObjectAdapter initializeMapAndCheckConcurrency(final Persistable pojo) {
        final Persistable pc = pojo;

        // need to do eagerly, because (if a viewModel then) a
        // viewModel's #viewModelMemento might need to use services
        servicesInjector.injectInto(pojo);

        final Version datastoreVersion = getVersionIfAny(pc);
        final RootOid originalOid = objectAdapterContext.createPersistentOrViewModelOid(pojo);

        final ObjectAdapter adapter = objectAdapterContext.addRecreatedPojoToCache(originalOid, pojo);
        adapter.setVersion(datastoreVersion);

        CallbackFacet.Util.callCallback(adapter, LoadedCallbackFacet.class);
        objectAdapterContext.postLifecycleEventIfRequired(adapter, LoadedLifecycleEventFacet.class);
        
        return adapter;
    }

    @Override
    public String identifierFor(final Object pojo) {
        final Object jdoOid = pm().getObjectId(pojo);
        requireNonNull(jdoOid, 
                ()->String.format("Pojo of type '%s' is not recognized by JDO.", 
                        pojo.getClass().getName()));
        return JdoObjectIdSerializer.toOidIdentifier(jdoOid);
    }


    /**
     * Called either when an entity is initially persisted, or when an entity is updated; fires the appropriate
     * lifecycle callback.
     *
     * <p>
     * The implementation therefore uses Isis' {@link org.apache.isis.core.metamodel.adapter.oid.Oid#isTransient() oid}
     * to determine which callback to fire.
     */
    @Override
    public void invokeIsisPersistingCallback(final Persistable pojo) {
        final ObjectAdapter adapter = null;
        if (adapter == null) {
            // not expected.
            return;
        }

        final RootOid isisOid = (RootOid) adapter.getOid();
        if (isisOid.isTransient()) {
            // persisting
            // previously this was performed in the DataNucleusSimplePersistAlgorithm.
            CallbackFacet.Util.callCallback(adapter, PersistingCallbackFacet.class);
            objectAdapterContext.postLifecycleEventIfRequired(adapter, PersistingLifecycleEventFacet.class);

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
    @Override
    public void enlistCreatedAndRemapIfRequiredThenInvokeIsisInvokePersistingOrUpdatedCallback(final Persistable pojo) {
        final ObjectAdapter adapter = adapterFor(pojo);

        final RootOid rootOid = (RootOid) adapter.getOid(); // ok since this is for a Persistable

        if (rootOid.isTransient()) {
            // persisting
            final RootOid persistentOid = objectAdapterContext.createPersistentOrViewModelOid(pojo);

            objectAdapterContext.remapAsPersistent(adapter, persistentOid, this);

            CallbackFacet.Util.callCallback(adapter, PersistedCallbackFacet.class);
            objectAdapterContext.postLifecycleEventIfRequired(adapter, PersistedLifecycleEventFacet.class);

            changedObjectsServiceInternal.enlistCreated(adapter);

        } else {
            // updating;
            // the callback and transaction.enlist are done in the preDirty callback
            // (can't be done here, as the enlist requires to capture the 'before' values)
            CallbackFacet.Util.callCallback(adapter, UpdatedCallbackFacet.class);
            objectAdapterContext.postLifecycleEventIfRequired(adapter, UpdatedLifecycleEventFacet.class);
        }

        Version versionIfAny = getVersionIfAny(pojo);
        adapter.setVersion(versionIfAny);
    }

    @Override
    public void enlistUpdatingAndInvokeIsisUpdatingCallback(final Persistable pojo) {
        {
            // seen this happen in the case when a parent entity (LeaseItem) has a collection of children
            // objects (LeaseTerm) for which we haven't had a loaded callback fired and so are not yet
            // mapped.

            // it seems reasonable in this case to simply map into Isis here ("just-in-time"); presumably
            // DN would not be calling this callback if the pojo was not persistent.

            final ObjectAdapter adapter = objectAdapterContext.addPersistentToCache(pojo);
            if (adapter == null) {
                throw new RuntimeException(
                        "DN could not find objectId for pojo (unexpected) and so could not map into Isis; pojo=["
                                + pojo + "]");
            }
        }
    }

    /**
     * makes sure the entity is known to Isis and is a root
     * @param pojo
     */
    @Override
    public void ensureRootObject(final Persistable pojo) {
        final Oid oid = adapterFor(pojo).getOid();
        if (!(oid instanceof RootOid)) {
            throw new IsisException(MessageFormat.format("Not a RootOid: oid={0}, for {1}", oid, pojo));
        }
    }

    private Version getVersionIfAny(final Persistable pojo) {
        return Utils.getVersionIfAny(pojo, authenticationSession);
    }

    // -- 
    
    @Override
    public boolean isTransient(@Nullable Object pojo) {
        if (pojo!=null && pojo instanceof Persistable) {
            final Persistable p = (Persistable) pojo;
            final boolean isPersistent = p.dnIsPersistent();
            final boolean isDeleted = p.dnIsDeleted();
            if (!isPersistent && !isDeleted) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isRepresentingPersistent(@Nullable Object pojo) {
        if (pojo!=null && pojo instanceof Persistable) {
            final Persistable p = (Persistable) pojo;
            final boolean isPersistent = p.dnIsPersistent();
            if (isPersistent) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDestroyed(@Nullable Object pojo) {
        if (pojo!=null && pojo instanceof Persistable) {
            final Persistable p = (Persistable) pojo;
            final boolean isDeleted = p.dnIsDeleted();
            if (isDeleted) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean isRecognized(Object pojo) {
        if (pojo!=null && pojo instanceof Persistable) {
            final Object jdoOid = pm().getObjectId(pojo);
            if(jdoOid!=null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MementoRecreateObjectSupport mementoSupport() {
        return objectAdapterContext.mementoSupport();
    }

    @Override
    public ObjectAdapterProvider getObjectAdapterProvider() {
        return objectAdapterContext.getObjectAdapterProvider();
    }
    
    @Override
    public ObjectAdapterByIdProvider getObjectAdapterByIdProvider() {
        return objectAdapterContext.getObjectAdapterByIdProvider();
    }

    // -- HELPER
    
    private void debugLogNotPersistentIgnoring(Object domainObject) {
        if (LOG.isDebugEnabled() && domainObject!=null) {
            final Oid oid = oidFor(domainObject);
            LOG.debug("; oid={} not persistent - ignoring", oid.enString());
        }     
    }

    private void debugLogRefreshImmediately(Object domainObject) {
        if (LOG.isDebugEnabled()) {
            final Oid oid = oidFor(domainObject);
            LOG.debug("refresh immediately; oid={}", oid.enString());
        }
    }
    
}



