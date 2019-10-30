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
package org.apache.isis.jdo.persistence;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.enterprise.inject.Vetoed;
import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.identity.SingleFieldIdentity;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.listener.StoreLifecycleListener;

import org.datanucleus.enhancement.Persistable;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.identity.DatastoreIdImpl;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.jdo.datanucleus.persistence.commands.DataNucleusCreateObjectCommand;
import org.apache.isis.jdo.datanucleus.persistence.commands.DataNucleusDeleteObjectCommand;
import org.apache.isis.jdo.datanucleus.persistence.queries.PersistenceQueryFindAllInstancesProcessor;
import org.apache.isis.jdo.datanucleus.persistence.queries.PersistenceQueryFindUsingApplibQueryProcessor;
import org.apache.isis.jdo.datanucleus.persistence.queries.PersistenceQueryProcessor;
import org.apache.isis.jdo.datanucleus.persistence.spi.JdoObjectIdSerializer;
import org.apache.isis.jdo.objectadapter.ObjectAdapterContext;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapterByIdProvider;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.adapter.oid.ObjectNotFoundException;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.PojoRefreshException;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.LoadedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.LoadedLifecycleEventFacet;
import org.apache.isis.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.PersistedLifecycleEventFacet;
import org.apache.isis.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.PersistingLifecycleEventFacet;
import org.apache.isis.metamodel.facets.object.callbacks.RemovingCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.RemovingLifecycleEventFacet;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatedLifecycleEventFacet;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatingCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatingLifecycleEventFacet;
import org.apache.isis.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.metamodel.spec.FreeStandingList;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ManagedObjectState;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.memento.Data;
import org.apache.isis.runtime.persistence.FixturesInstalledState;
import org.apache.isis.runtime.persistence.FixturesInstalledStateHolder;
import org.apache.isis.runtime.persistence.NotPersistableException;
import org.apache.isis.runtime.persistence.UnsupportedFindException;
import org.apache.isis.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;
import org.apache.isis.runtime.services.RequestScopedService;
import org.apache.isis.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.security.authentication.AuthenticationSession;

import static java.util.Objects.requireNonNull;
import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * A wrapper around the JDO {@link PersistenceManager}, which also manages concurrency
 * and maintains an identity map of {@link ObjectAdapter adapter}s and {@link Oid
 * identities} for each and every POJO that is being used by the framework.
 */
@Vetoed @Log4j2
public class PersistenceSession5 extends IsisPersistenceSessionJdoBase
implements IsisLifecycleListener.PersistenceSessionLifecycleManagement {

    private ObjectAdapterContext objectAdapterContext;
    @Getter private final TransactionService transactionService;
    private final StoreLifecycleListener storeLifecycleListener;

    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     * @param storeLifecycleListener 
     */
    public PersistenceSession5(
            final MetaModelContext metaModelContext,
            final AuthenticationSession authenticationSession,
            final PersistenceManagerFactory jdoPersistenceManagerFactory,
            final StoreLifecycleListener storeLifecycleListener, 
            final FixturesInstalledStateHolder stateHolder) {

        super(metaModelContext, authenticationSession, jdoPersistenceManagerFactory, stateHolder);
        this.transactionService = metaModelContext.getTransactionService();
        this.storeLifecycleListener = storeLifecycleListener;
    }

    // -- open

    /**
     * Injects components, calls open on subcomponents, and then creates service
     * adapters.
     */
    @Override
    public void open() {
        ensureNotOpened();

        openedAtSystemNanos = System.nanoTime();

        if (log.isDebugEnabled()) {
            log.debug("opening {}", this);
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

        objectAdapterContext = ObjectAdapterContext.openContext(super.metaModelContext, authenticationSession, this);

        // tell the proxy of all request-scoped services to instantiate the underlying
        // services, store onto the thread-local and inject into them...
        startRequestOnRequestScopedServices();

        // ... and invoke all @PostConstruct
        postConstructOnRequestScopedServices();

        if(metricsService instanceof InstanceLifecycleListener) {
            val metricsService = (InstanceLifecycleListener) this.metricsService;
            persistenceManager.addInstanceLifecycleListener(metricsService, (Class[]) null);
        }

        final Command command = createCommand();
        final Interaction interaction = factoryService.instantiate(Interaction.class);

        final Timestamp timestamp = clockService.nowAsJavaSqlTimestamp();
        final String userName = userService.getUser().getName();

        command.internal().setTimestamp(timestamp);
        command.internal().setUser(userName);

        interaction.setUniqueId(command.getUniqueId());

        commandContext.setCommand(command);
        interactionContext.setInteraction(interaction);
        
        persistenceManager.addInstanceLifecycleListener(storeLifecycleListener, (Class[]) null);

        this.state = State.OPEN;
    }

    private void postConstructOnRequestScopedServices() {
        serviceRegistry.select(RequestScopedService.class)
        .forEach(RequestScopedService::__isis_postConstruct);
    }

    private void startRequestOnRequestScopedServices() {
        serviceRegistry.select(RequestScopedService.class)
        .forEach(service->service.__isis_startRequest(serviceInjector));
    }

    private Command createCommand() {
        final Command command = commandService.create();

        serviceInjector.injectServicesInto(command);
        return command;
    }

    // -- close

    /**
     * Closes the subcomponents.
     *
     * <p>
     * Automatically {@link IsisTransactionManagerJdo#commitTransaction() ends
     * (commits)} the current (Isis) {@link IsisTransactionJdo}. This in turn commits the underlying
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

        //TODO[2125] shold no longer be required		
        //		try {
        //			val currentTransaction = transactionManager.getCurrentTransaction();
        //			transactionManager.flushTransaction(currentTransaction);
        //			if (currentTransaction != null && !currentTransaction.getState().isComplete()) {
        //				if(currentTransaction.getState().canCommit()) {
        //					transactionManager.commitTransaction(currentTransaction);
        //				} else if(currentTransaction.getState().canAbort()) {
        //					transactionManager.abortTransaction(currentTransaction);
        //				}
        //			}
        //		} catch(final Throwable ex) {
        //			// ignore
        //			log.error("close: failed to end transaction; continuing to avoid memory leakage");
        //		}

        // tell the proxy of all request-scoped services to invoke @PreDestroy
        // (if any) on all underlying services stored on their thread-locals...
        preDestroyOnRequestScopedServices();

        // ... and then remove those underlying services from the thread-local
        endRequestOnRequestScopeServices();

        persistenceManager.removeInstanceLifecycleListener(storeLifecycleListener);
        
        try {
            persistenceManager.close();
        } catch(final Throwable ex) {
            // ignore
            log.error(
                    "close: failed to close JDO persistenceManager; continuing to avoid memory leakage");
        }

        objectAdapterContext.close();

        this.state = State.CLOSED;
    }

    private void endRequestOnRequestScopeServices() {
        serviceRegistry.select(RequestScopedService.class)
        .forEach(RequestScopedService::__isis_endRequest);
    }

    private void preDestroyOnRequestScopedServices() {
        serviceRegistry.select(RequestScopedService.class)
        .forEach(RequestScopedService::__isis_preDestroy);
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

            command.internal().setCompletedAt(completedAt);
        }

        // ensureCommandsPersistedIfDirtyXactn

        // ensure that any changed objects means that the command should be persisted
        if(command.getMemberIdentifier() != null) {
            if(metricsService.numberObjectsDirtied() > 0) {
                command.internal().setPersistHint(true);
            }
        }

        commandService.complete(command);

        interaction.clear();
    }

    // -- QuerySubmitter impl, findInstancesInTransaction
    @Override
    public <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query) {
        final ObjectAdapter instances = findInstancesInTransaction(query, QueryCardinality.MULTIPLE);
        return CollectionFacet.Utils.toAdapterList(instances);
    }
    @Override
    public <T> ObjectAdapter firstMatchingQuery(final Query<T> query) {
        final ObjectAdapter instances = findInstancesInTransaction(query, QueryCardinality.SINGLE);
        final List<ObjectAdapter> list = CollectionFacet.Utils.toAdapterList(instances);
        return list.size() > 0 ? list.get(0) : null;
    }

    /**
     * Finds and returns instances that match the specified query.
     *
     * <p>
     * The {@link QueryCardinality} determines whether all instances or just the
     * first matching instance is returned.
     *
     * @throws org.apache.isis.runtime.persistence.UnsupportedFindException
     *             if the criteria is not support by this persistor
     */
    private <T> ObjectAdapter findInstancesInTransaction(final Query<T> query, final QueryCardinality cardinality) {
        if (log.isDebugEnabled()) {
            log.debug("findInstances using (applib) Query: {}", query);
        }

        // TODO: unify PersistenceQuery and PersistenceQueryProcessor
        final PersistenceQuery persistenceQuery = createPersistenceQueryFor(query, cardinality);
        if (log.isDebugEnabled()) {
            log.debug("maps to (core runtime) PersistenceQuery: {}", persistenceQuery);
        }

        final PersistenceQueryProcessor<? extends PersistenceQuery> processor = lookupProcessorFor(persistenceQuery);

        final List<ObjectAdapter> instances = transactionService.executeWithinTransaction(
                ()->processPersistenceQuery(processor, persistenceQuery) );
        final ObjectSpecification specification = persistenceQuery.getSpecification();
        final FreeStandingList results = FreeStandingList.of(specification, instances);
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

    @Override
    public FixturesInstalledState getFixturesInstalledState() {
        if (fixturesInstalledStateHolder.getFixturesInstalledState() == null) {
            val initialStateFromConfig = initialStateFromConfig();
            fixturesInstalledStateHolder.setFixturesInstalledState(initialStateFromConfig);
        }
        return fixturesInstalledStateHolder.getFixturesInstalledState();
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
     * Implementation looks for the {@link IsisConfiguration.Persistor.Datanucleus#isInstallFixtures()} property
     * in the injected {@link #configuration configuration}.
     *
     * <p>
     * By default this is not expected to be there, but utilities can add in on
     * the fly during bootstrapping if required.
     */
    private FixturesInstalledState initialStateFromConfig() {
        val installFixtures = configuration.getPersistor().getDatanucleus().isInstallFixtures();
        log.info("isFixturesInstalled: {} = {}", "'isis.persistor.datanucleus.install-fixtures'", installFixtures);

        val objectStoreIsFixturesInstalled = !installFixtures;
        val initialStateFromConfig = objectStoreIsFixturesInstalled
                ? FixturesInstalledState.Installed
                        : FixturesInstalledState.not_Installed;

        return initialStateFromConfig;
    }

    // -- FETCHING

    @Override
    public Object fetchPersistentPojo(final RootOid rootOid) {
        Objects.requireNonNull(rootOid);
        log.debug("getObject; oid={}", rootOid);

        Object result;
        try {
            val specLoader = super.getSpecificationLoader();
            val cls = clsOf(rootOid);
            val jdoObjectId = JdoObjectIdSerializer.toJdoObjectId(specLoader, rootOid);
            val fetchPlan = persistenceManager.getFetchPlan();
            fetchPlan.addGroup(FetchGroup.DEFAULT);
            result = persistenceManager.getObjectById(cls, jdoObjectId);
        } catch (final RuntimeException e) {

            Class<ExceptionRecognizer> serviceClass = ExceptionRecognizer.class;
            final Iterable<ExceptionRecognizer> exceptionRecognizers = lookupServices(serviceClass);
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

        val specLoader = super.getSpecificationLoader();
        
        final List<Object> dnOids = new ArrayList<>(rootOids.size());
        for (val rootOid : rootOids) {
            final Object id = JdoObjectIdSerializer.toJdoObjectId(specLoader, rootOid);
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
        final Map<RootOid,Object> pojoByOid = _Maps.newLinkedHashMap();
        int i = 0;
        for (final Object pojo : pojos) {
            final RootOid rootOid = rootOids.get(i++);
            pojoByOid.put(rootOid, pojo);
        }
        return pojoByOid;
    }

    private Class<?> clsOf(final RootOid oid) {
        final ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecIdElseLoad(oid.getObjectSpecId());
        return objectSpec.getCorrespondingClass();
    }


    // -- REFRESH

    @Override
    public void refreshRoot(final Object domainObject) {

        val state = stateOf(domainObject);
        val isRepresentingPersistent = state.isAttached() || state.isDestroyed();  

        if(!isRepresentingPersistent) {
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
        if (adapter.isRepresentingPersistent()) {
            throw new NotPersistableException("Object already persistent: " + adapter);
        }
        if (adapter.isParentedCollection()) {
            //or should we just ignore this?
            throw new NotPersistableException("Cannot persist parented collection: " + adapter);
        }
        val spec = adapter.getSpecification();
        if (spec.isManagedBean()) {
            throw new NotPersistableException("Can only persist entity beans: " + adapter);
        }
        transactionService.executeWithinTransaction(()->{
            log.debug("persist {}", adapter);               
            val createObjectCommand = newCreateObjectCommand(adapter);
            transactionManager.addCommand(createObjectCommand);
        });
    }


    // -- destroyObjectInTransaction

    /**
     * Removes the specified object from the system. The specified object's data
     * should be removed from the persistence mechanism.
     */
    @Override
    public void destroyObjectInTransaction(final ObjectAdapter adapter) {
        val spec = adapter.getSpecification();
        if (spec.isParented()) {
            return;
        }
        log.debug("destroyObject {}", adapter);
        transactionService.executeWithinTransaction(()->{
            val destroyObjectCommand = newDestroyObjectCommand(adapter);
            transactionManager.addCommand(destroyObjectCommand);
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

        log.debug("create object - creating command for: {}", adapter);
        if (adapter.isRepresentingPersistent()) {
            throw new IllegalArgumentException("Adapter is persistent; adapter: " + adapter);
        }
        return new DataNucleusCreateObjectCommand(adapter, persistenceManager);
    }

    private DestroyObjectCommand newDestroyObjectCommand(final ObjectAdapter adapter) {
        ensureOpened();

        log.debug("destroy object - creating command for: {}", adapter);
        if (!adapter.isRepresentingPersistent()) {
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
        serviceInjector.injectServicesInto(pojo);

        final Version datastoreVersion = getVersionIfAny(pc);
        final RootOid originalOid = objectAdapterContext.createPersistentOrViewModelOid(pojo);

        final ObjectAdapter adapter = objectAdapterContext.recreatePojo(originalOid, pojo);
        adapter.setVersion(datastoreVersion);

        CallbackFacet.Util.callCallback(adapter, LoadedCallbackFacet.class);
        objectAdapterContext.postLifecycleEventIfRequired(adapter, LoadedLifecycleEventFacet.class);

        return adapter;
    }

    @Override
    public String identifierFor(final Object pojo) {
        final Object jdoOid = getJdoPersistenceManager().getObjectId(pojo);
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
     * The implementation therefore uses Isis' {@link org.apache.isis.metamodel.adapter.oid.Oid#isTransient() oid}
     * to determine which callback to fire.
     */
    @Override
    public void invokeIsisPersistingCallback(final Persistable pojo) {
        if (stateOf(pojo).isDetached()) {
            val managedObject = ManagedObject.of(specificationLoader::loadSpecification, pojo);

            // persisting
            // previously this was performed in the DataNucleusSimplePersistAlgorithm.
            CallbackFacet.Util.callCallback(managedObject, PersistingCallbackFacet.class);
            objectAdapterContext.postLifecycleEventIfRequired(managedObject, PersistingLifecycleEventFacet.class);

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
     * The implementation therefore uses Isis' {@link org.apache.isis.metamodel.adapter.oid.Oid#isTransient() oid}
     * to determine which callback to fire.
     */
    @Override
    public void enlistCreatedAndRemapIfRequiredThenInvokeIsisInvokePersistingOrUpdatedCallback(final Persistable pojo) {
        final ObjectAdapter adapter = adapterFor(pojo);

        final RootOid rootOid = (RootOid) adapter.getOid(); // ok since this is for a Persistable

        if (rootOid.isTransient()) {
            // persisting

            objectAdapterContext.asPersistent(adapter, this);

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

        // seen this happen in the case when a parent entity (LeaseItem) has a collection of children
        // objects (LeaseTerm) for which we haven't had a loaded callback fired and so are not yet
        // mapped.

        // it seems reasonable in this case to simply map into Isis here ("just-in-time"); presumably
        // DN would not be calling this callback if the pojo was not persistent.

        final ObjectAdapter adapter = objectAdapterContext.fetchPersistent(pojo);
        if (adapter == null) {
            throw new RuntimeException(
                    "DN could not find objectId for pojo (unexpected) and so could not map into Isis; pojo=["
                            + pojo + "]");
        }

        final boolean wasAlreadyEnlisted = changedObjectsServiceInternal.isEnlisted(adapter);

        // we call this come what may;
        // additional properties may now have been changed, and the changeKind for publishing might also be modified
        changedObjectsServiceInternal.enlistUpdating(adapter);

        if(!wasAlreadyEnlisted) {
            // prevent an infinite loop... don't call the 'updating()' callback on this object if we have already done so
            CallbackFacet.Util.callCallback(adapter, UpdatingCallbackFacet.class);
            objectAdapterContext.postLifecycleEventIfRequired(adapter, UpdatingLifecycleEventFacet.class);
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

    @Override
    public ManagedObjectState stateOf(@Nullable Object pojo) {

        // guard against misuse
        if(pojo instanceof ObjectAdapter) {
            throw _Exceptions.unexpectedCodeReach();
        }

        if (pojo!=null && pojo instanceof Persistable) {
            val persistable = (Persistable) pojo;
            val isDeleted = persistable.dnIsDeleted();
            if(isDeleted) {
                return ManagedObjectState.persistable_Destroyed;
            }
            val isPersistent = persistable.dnIsPersistent();
            if(isPersistent) {
                return ManagedObjectState.persistable_Attached;
            }
            return ManagedObjectState.persistable_Detached;
        }
        return ManagedObjectState.not_Persistable;
    }

    @Override
    public boolean isRecognized(Object pojo) {
        if (pojo!=null && pojo instanceof Persistable) {
            final Object jdoOid = getJdoPersistenceManager().getObjectId(pojo);
            if(jdoOid!=null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ObjectAdapter adapterFor(final RootOid rootOid) {
        return objectAdapterContext.getObjectAdapterByIdProvider().adapterFor(rootOid);
    }

    // -- MEMENTO SUPPORT

    @Override
    public ObjectAdapter adapterOfMemento(ObjectSpecification spec, Oid oid, Data data) {
        return objectAdapterContext.mementoSupport().recreateObject(spec, oid, data);
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
        if (log.isDebugEnabled() && domainObject!=null) {
            final Oid oid = oidFor(domainObject);
            log.debug("; oid={} not persistent - ignoring", oid.enString());
        }     
    }

    private void debugLogRefreshImmediately(Object domainObject) {
        if (log.isDebugEnabled()) {
            final Oid oid = oidFor(domainObject);
            log.debug("refresh immediately; oid={}", oid.enString());
        }
    }


}



