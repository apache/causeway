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

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import com.google.common.collect.Maps;

import org.datanucleus.enhancement.Persistable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer2;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.PojoRefreshException;
import org.apache.isis.core.runtime.persistence.UnsupportedFindException;
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
import org.apache.isis.core.runtime.system.persistence.FrameworkSynchronizer.CalledFrom;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.commands.DataNucleusCreateObjectCommand;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.commands.DataNucleusDeleteObjectCommand;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryFindAllInstancesProcessor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryFindUsingApplibQueryProcessor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.queries.PersistenceQueryProcessor;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.spi.JdoObjectIdSerializer;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatContext;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class ObjectStore implements TransactionalResource, DebuggableWithTitle, SessionScopedComponent {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectStore.class);

    //region > constructors, fields

    private final PersistenceSession persistenceSession;
    private final SpecificationLoaderSpi specificationLoader;
    private final IsisConfiguration configuration;
    private final OidMarshaller oidMarshaller;

    private static final String ROOT_KEY = OptionHandlerFixtureAbstract.DATANUCLEUS_ROOT_KEY;

    /**
     * Append regular <a href="http://www.datanucleus.org/products/accessplatform/persistence_properties.html">datanucleus properties</a> to this key
     */
    public static final String DATANUCLEUS_PROPERTIES_ROOT = ROOT_KEY + "impl.";

    /**
     * @see #isFixturesInstalled()
     */
    public static final String INSTALL_FIXTURES_KEY = OptionHandlerFixtureAbstract.DATANUCLEUS_INSTALL_FIXTURES_KEY;
    public static final boolean INSTALL_FIXTURES_DEFAULT = false;


    private final DataNucleusApplicationComponents applicationComponents;
    
    private final Map<ObjectSpecId, RootOid> registeredServices = Maps.newHashMap();

    private PersistenceManager persistenceManager;

    private final Map<Class<?>, PersistenceQueryProcessor<?>> persistenceQueryProcessorByClass = Maps.newHashMap();
    private final FrameworkSynchronizer frameworkSynchronizer;

    public ObjectStore(
            final PersistenceSession persistenceSession,
            final SpecificationLoaderSpi specificationLoader,
            final IsisConfiguration configuration,
            final DataNucleusApplicationComponents applicationComponents) {

        this.persistenceSession = persistenceSession;
        this.specificationLoader = specificationLoader;
        this.configuration = configuration;
        this.applicationComponents = applicationComponents;

        this.state = State.NOT_YET_OPEN;

        this.frameworkSynchronizer = applicationComponents.getFrameworkSynchronizer();
        this.oidMarshaller = new OidMarshaller();
    }

    //endregion

    //region > open, close

    enum State {
        NOT_YET_OPEN, OPEN, CLOSED;
    }

    private State state;

    public void open() {
        ensureNotYetOpen();

        this.persistenceManager = applicationComponents.createPersistenceManager();

        persistenceQueryProcessorByClass.put(
                PersistenceQueryFindAllInstances.class,
                new PersistenceQueryFindAllInstancesProcessor(persistenceManager, frameworkSynchronizer));
        persistenceQueryProcessorByClass.put(
                PersistenceQueryFindUsingApplibQueryDefault.class,
                new PersistenceQueryFindUsingApplibQueryProcessor(persistenceManager, frameworkSynchronizer));

        state = State.OPEN;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Automatically {@link IsisTransactionManager#endTransaction() ends
     * (commits)} the current (Isis) {@link IsisTransaction}. This in turn
     * {@link ObjectStore#commitJdoTransaction() commits the underlying
     * JDO transaction}.
     *
     * <p>
     * The corresponding DataNucleus entity is then closed.
     */
    public void close() {
        ensureOpened();
        ensureThatState(persistenceManager, is(notNullValue()));

        try {
            final IsisTransaction currentTransaction = getTransactionManager().getTransaction();
            if (currentTransaction != null && !currentTransaction.getState().isComplete()) {
                if(currentTransaction.getState().canCommit()) {
                    getTransactionManager().endTransaction();
                } else if(currentTransaction.getState().canAbort()) {
                    getTransactionManager().abortTransaction();
                }
            }
        } finally {
            // make sure release everything ok.
            persistenceManager.close();
            state = State.CLOSED;
        }
    }
    //endregion

    //region > isFixturesInstalled
    /**
     * Determine if the object store has been initialized with its set of start
     * up objects.
     *
     * <p>
     * This method is called only once after the session is opened called. If it returns <code>false</code> then the
     * framework will run the fixtures to initialise the object store.
     *
     * <p>
     * Implementation looks for the {@link #INSTALL_FIXTURES_KEY} in the
     * {@link #getConfiguration() configuration}.
     *
     * <p>
     * By default this is not expected to be there, but utilities can add in on
     * the fly during bootstrapping if required.
     */
    public boolean isFixturesInstalled() {
        final boolean installFixtures = getConfiguration().getBoolean(INSTALL_FIXTURES_KEY, INSTALL_FIXTURES_DEFAULT);
        LOG.info("isFixturesInstalled: {} = {}", INSTALL_FIXTURES_KEY, installFixtures);
        return !installFixtures;
    }
    //endregion

    //region > transactions
    public void startTransaction() {
        beginJdoTransaction();
    }

    public void endTransaction() {
        commitJdoTransaction();
    }

    public void abortTransaction() {
        rollbackJdoTransaction();
    }

    private void beginJdoTransaction() {
        final javax.jdo.Transaction transaction = getPersistenceManager().currentTransaction();
        if (transaction.isActive()) {
            throw new IllegalStateException("Transaction already active");
        }
        transaction.begin();
    }

    private void commitJdoTransaction() {
        final javax.jdo.Transaction transaction = getPersistenceManager().currentTransaction();
        if (transaction.isActive()) {
            transaction.commit();
        }
    }

    private void rollbackJdoTransaction() {
        final javax.jdo.Transaction transaction = getPersistenceManager().currentTransaction();
        if (transaction.isActive()) {
            transaction.rollback();
        }
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
        return new DataNucleusCreateObjectCommand(adapter, getPersistenceManager());
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
        return new DataNucleusDeleteObjectCommand(adapter, getPersistenceManager());
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
        getPersistenceManager().flush();
    }
    //endregion

    //region > loadInstanceAndAdapt
    /**
     * Retrieves the object identified by the specified {@link RootOid} from the object
     * store, {@link AdapterManager#mapRecreatedPojo(org.apache.isis.core.metamodel.adapter.oid.Oid, Object) mapped by the adapter manager}.
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
    public ObjectAdapter loadInstanceAndAdapt(final RootOid oid) {
        ensureOpened();
        ensureInTransaction();

        if (LOG.isDebugEnabled()) {
            LOG.debug("getObject; oid=" + oid);
        }

        final Object pojo = loadPojo(oid);
        return getPersistenceSession().getAdapterManager().mapRecreatedPojo(oid, pojo);
    }

    //endregion

    //region > loadPojo, lazilyLoaded, resolveImmediately

    public Object loadPojo(final RootOid rootOid) {
    	
        Object result = null;
        try {
            final Class<?> cls = clsOf(rootOid);
            final Object jdoObjectId = JdoObjectIdSerializer.toJdoObjectId(rootOid);
            final PersistenceManager pm = getPersistenceManager();
            FetchPlan fetchPlan = pm.getFetchPlan();
            fetchPlan.addGroup(FetchGroup.DEFAULT);
            result = pm.getObjectById(cls, jdoObjectId);
        } catch (final RuntimeException e) {

            final List<ExceptionRecognizer> exceptionRecognizers = getPersistenceSession().getServicesInjector().lookupServices(ExceptionRecognizer.class);
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

    
    public ObjectAdapter lazilyLoaded(Object pojo) {
        if(!(pojo instanceof Persistable)) {
            return null;
        } 
        final Persistable persistenceCapable = (Persistable) pojo;
        return frameworkSynchronizer.lazilyLoaded(persistenceCapable, CalledFrom.OS_LAZILYLOADED);
    }



    public void resolveImmediately(final ObjectAdapter adapter) {
        ensureOpened();
        ensureInTransaction();

        if (LOG.isDebugEnabled()) {
            LOG.debug("resolveImmediately; oid=" + adapter.getOid().enString(getOidMarshaller()));
        }

        if (!adapter.representsPersistent()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("; not persistent - ignoring");
            }
            return;
        }

        refreshRoot(adapter);
    }

    /**
     * Not API; provides the ability to force a reload (refresh in JDO terms)
     * of the domain object wrapped in the {@link ObjectAdapter}.
     */
    public void refreshRoot(final ObjectAdapter adapter) {
        
        final Object domainObject = adapter.getObject();
		if (domainObject == null) {
		    // REVIEW: is this possible?
            throw new PojoRefreshException(adapter.getOid());
        }

        try {
            getPersistenceManager().refresh(domainObject);
        } catch (final RuntimeException e) {
            throw new PojoRefreshException(adapter.getOid(), e);
        }

        // possibly redundant because also called in the post-load event
        // listener, but (with JPA impl) found it was required if we were ever to 
        // get an eager left-outer-join as the result of a refresh (sounds possible).
        
        frameworkSynchronizer.postLoadProcessingFor((Persistable) domainObject, CalledFrom.OS_RESOLVE);
    }

    //endregion

    //region > loadInstancesAndAdapt
    public List<ObjectAdapter> loadInstancesAndAdapt(final PersistenceQuery persistenceQuery) {
        ensureOpened();
        ensureInTransaction();

        final PersistenceQueryProcessor<? extends PersistenceQuery> processor =
                persistenceQueryProcessorByClass.get(persistenceQuery.getClass());
        if (processor == null) {
            throw new UnsupportedFindException(MessageFormat.format(
                    "Unsupported criteria type: {0}", persistenceQuery.getClass().getName()));
        }
        return processPersistenceQuery(processor, persistenceQuery);
    }

    @SuppressWarnings("unchecked")
    private <Q extends PersistenceQuery> List<ObjectAdapter> processPersistenceQuery(final PersistenceQueryProcessor<Q> persistenceQueryProcessor, final PersistenceQuery persistenceQuery) {
        return persistenceQueryProcessor.process((Q)persistenceQuery);
    }

    //endregion

    //region > registerServices, getOidForService
    public void registerService(RootOid rootOid) {
        ensureOpened();
        this.registeredServices.put(rootOid.getObjectSpecId(), rootOid);
    }

    public RootOid getOidForService(ObjectSpecification serviceSpec) {
        ensureOpened();
        return this.registeredServices.get(serviceSpec.getSpecId());
    }

    //endregion

    //region > helpers

    private void ensureNotYetOpen() {
        ensureStateIs(State.NOT_YET_OPEN);
    }

    private void ensureOpened() {
        ensureStateIs(State.OPEN);
    }

    private void ensureInSession() {
        ensureThatContext(IsisContext.inSession(), is(true));
    }

    private void ensureInTransaction() {
        ensureThatContext(IsisContext.inTransaction(), is(true));
        ensureInJdoTransaction();
    }

    private void ensureInJdoTransaction() {
        javax.jdo.Transaction currentTransaction = getPersistenceManager().currentTransaction();
        ensureThatState(currentTransaction, is(notNullValue()));
        ensureThatState(currentTransaction.isActive(), is(true));
    }

    private void ensureStateIs(final State stateRequired) {
        if (state == stateRequired) {
            return;
        }
        throw new IllegalStateException("State is: " + state + "; should be: " + stateRequired);
    }

    private Class<?> clsOf(final RootOid oid) {
        final ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecId(oid.getObjectSpecId());
        return objectSpec.getCorrespondingClass();
    }

    //endregion

    //region > debug

    public void debugData(final DebugBuilder debug) {
        // no-op
        debug.append("this object store does not currently provide any debug data");
    }

    public String debugTitle() {
        return "JDO (DataNucleus) ObjectStore";
    }

    //endregion

    //region > dependencies (from constructor)

    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return specificationLoader;
    }

    protected PersistenceSession getPersistenceSession() {
        return persistenceSession;
    }

    protected AdapterManager getAdapterManager() {
        return persistenceSession.getAdapterManager();
    }
    
    protected IsisTransactionManager getTransactionManager() {
        return persistenceSession.getTransactionManager();
    }

    protected OidMarshaller getOidMarshaller() {
        return oidMarshaller;
    }

    //endregion


}
