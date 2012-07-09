package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatContext;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.transaction.Transaction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.log4j.Logger;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.object.namedquery.NamedQuery;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.util.JpaPropertyUtils;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.commands.JpaCreateObjectCommand;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.commands.JpaDeleteObjectCommand;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.commands.JpaUpdateObjectCommand;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.queries.PersistenceQueryFindAllInstancesProcessor;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.queries.PersistenceQueryFindByPatternProcessor;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.queries.PersistenceQueryFindByTitleProcessor;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.queries.PersistenceQueryFindUsingApplibQueryProcessor;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.queries.PersistenceQueryProcessor;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.queries.QueryUtil;
import org.apache.isis.runtimes.dflt.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSessionHydratorAware;
import org.apache.isis.runtimes.dflt.runtime.persistence.UnsupportedFindException;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByPattern;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionHydrator;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransaction;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManagerAware;

public class OpenJpaObjectStore implements ObjectStore, PersistenceSessionHydratorAware, SpecificationLoaderAware, IsisTransactionManagerAware {

    private static final Logger LOG = Logger.getLogger(OpenJpaObjectStore.class);

    static enum State {
        NOT_YET_OPEN, OPEN, CLOSED;
    }

    /**
     * @see #isFixturesInstalled()
     */
    public static final String IS_FIXTURES_INSTALLED_KEY = ConfigurationConstants.ROOT + "persistor.openjpa.install-fixtures";
    public static final boolean IS_FIXTURES_INSTALLED_DEFAULT = true;

    static enum TransactionMode {
        /**
         * Requires transactions to be started explicitly.
         */
        UNCHAINED,
        /**
         * Transactions are started automatically if not already in progress.
         */
        CHAINED;
    }

    private final IsisConfiguration configuration;
    private final ObjectAdapterFactory adapterFactory;
    private final AdapterManager adapterManager;
    private final OpenJpaApplicationComponents applicationComponents;
    
    private final Map<ObjectSpecId, RootOid> registeredServices = Maps.newHashMap();

    private SpecificationLoader specificationLoader;
    private EntityManager entityManager;

    private final Map<Class<?>, PersistenceQueryProcessor<?>> persistenceQueryProcessorByClass = Maps.newHashMap();
    // private final LoadPostProcessor loadPostProcessor;

    private State state;
    private TransactionMode transactionMode;

    private PersistenceSessionHydrator hydrator;
    private IsisTransactionManager transactionManager;

    public OpenJpaObjectStore(IsisConfiguration configuration, ObjectAdapterFactory adapterFactory, AdapterManager adapterManager, OpenJpaApplicationComponents applicationComponents) {
        ensureThatArg(configuration, is(notNullValue()));
        ensureThatArg(adapterFactory, is(notNullValue()));
        ensureThatArg(adapterManager, is(notNullValue()));
        ensureThatArg(applicationComponents, is(notNullValue()));

        this.state = State.NOT_YET_OPEN;
        this.transactionMode = TransactionMode.UNCHAINED;

        this.configuration = configuration;
        this.adapterFactory = adapterFactory;
        this.adapterManager = adapterManager;
        this.applicationComponents = applicationComponents;
    }

    @Override
    public String name() {
        return "openjpa";
    }

    // ///////////////////////////////////////////////////////////////////////
    // open, close
    // ///////////////////////////////////////////////////////////////////////

    public void open() {
        ensureNotYetOpen();
        ensureDependenciesInjected();

        openSession();
        ensureThatState(entityManager, is(notNullValue()));

        addPersistenceQueryProcessors(entityManager);

        state = State.OPEN;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Automatically {@link IsisTransactionManager#endTransaction() ends
     * (commits)} the current (Isis) {@link Transaction}. This in turn
     * {@link OpenJpaObjectStore#commitJpaTransaction() commits the underlying
     * JPA transaction}.
     * <p>
     * The corresponding OpenJPA {@link EntityManager} is then
     * {@link EntityManager#close() close}d.
     */
    public void close() {
        ensureOpened();
        ensureThatState(entityManager, is(notNullValue()));

        final IsisTransaction currentTransaction = getTransactionManager().getTransaction();
        if (currentTransaction != null && currentTransaction.getState().canCommit()) {
            getTransactionManager().endTransaction();
        }

        entityManager.close();

        state = State.CLOSED;
    }

    private EntityManager openSession() {
        return entityManager = applicationComponents.createEntityManager();
    }

    private void addPersistenceQueryProcessors(final EntityManager entityManager) {
        persistenceQueryProcessorByClass.put(PersistenceQueryFindAllInstances.class, new PersistenceQueryFindAllInstancesProcessor(adapterManager, entityManager));
        persistenceQueryProcessorByClass.put(PersistenceQueryFindByTitle.class, new PersistenceQueryFindByTitleProcessor(adapterManager, entityManager));
        persistenceQueryProcessorByClass.put(PersistenceQueryFindByPattern.class, new PersistenceQueryFindByPatternProcessor(adapterManager, entityManager));
        persistenceQueryProcessorByClass.put(PersistenceQueryFindUsingApplibQueryDefault.class, new PersistenceQueryFindUsingApplibQueryProcessor(adapterManager, entityManager));
    }

    // ///////////////////////////////////////////////////////////////////////
    // isFixturesInstalled
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Implementation looks for the {@link #IS_FIXTURES_INSTALLED_KEY} in the
     * {@link #getConfiguration() configuration}.
     * <p>
     * By default this is not expected to be there, but utilities can add in on
     * the fly during bootstrapping if required.
     */
    public boolean isFixturesInstalled() {
        return getConfiguration().getBoolean(IS_FIXTURES_INSTALLED_KEY, IS_FIXTURES_INSTALLED_DEFAULT);
    }


    // ///////////////////////////////////////////////////////////////////////
    // reset
    // ///////////////////////////////////////////////////////////////////////

    public void reset() {
        // does nothing.
    }

    /**
     * Non-API.
     */
    public Connection getConnection() {
        OpenJPAEntityManager ojem = (OpenJPAEntityManager) entityManager;
        return (Connection) ojem.getConnection();
    }

    // ///////////////////////////////////////////////////////////////////////
    // TransactionMode (not API)
    // ///////////////////////////////////////////////////////////////////////

    public TransactionMode getTransactionMode() {
        return transactionMode;
    }

    public void setTransactionMode(final TransactionMode transactionMode) {
        ensureNotInTransaction();
        this.transactionMode = transactionMode;
    }

    // ///////////////////////////////////////////////////////////////////////
    // Transactions
    // ///////////////////////////////////////////////////////////////////////

    public void startTransaction() {
        beginJpaTransaction();
    }

    public void endTransaction() {
        commitJpaTransaction();
    }

    public void abortTransaction() {
        rollbackJpaTransaction();
    }

    private void beginJpaTransaction() {
        final EntityTransaction transaction = getEntityManager().getTransaction();
        if (transaction.isActive()) {
            throw new IllegalStateException("Transaction already active");
        }
        transaction.begin();
    }

    private void commitJpaTransaction() {
        final EntityTransaction transaction = getEntityManager().getTransaction();
        if (transaction.isActive()) {
            transaction.commit();
        }
    }

    private void rollbackJpaTransaction() {
        final EntityTransaction transaction = getEntityManager().getTransaction();
        if (transaction.isActive()) {
            transaction.rollback();
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // Command Factory
    // ///////////////////////////////////////////////////////////////////////

    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter adapter) {
        ensureOpened();
        ensureInSession();

        if (LOG.isDebugEnabled()) {
            LOG.debug("create object - creating command for: " + adapter);
        }
        if (adapter.representsPersistent()) {
            throw new IllegalArgumentException("Adapter is persistent; adapter: " + adapter);
        }
        return new JpaCreateObjectCommand(adapter, getEntityManager());
    }

    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter adapter) {
        ensureOpened();
        ensureInSession();

        if (!adapter.representsPersistent()) {
            throw new IllegalArgumentException("Adapter is not persistent; adapter: " + adapter);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("save object - creating command for: " + adapter);
        }
        return new JpaUpdateObjectCommand(adapter, getEntityManager());
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
        return new JpaDeleteObjectCommand(adapter, getEntityManager());
    }

    // ///////////////////////////////////////////////////////////////////////
    // Execute
    // ///////////////////////////////////////////////////////////////////////

    public void execute(final List<PersistenceCommand> commands) {
        ensureOpened();
        ensureInTransaction();

        if (LOG.isDebugEnabled()) {
            LOG.debug("execute " + commands.size() + " commands");
        }

        if (commands.size() <= 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("no commands");
            }
            return;
        }

        executeCommands(commands);
    }

    private void executeCommands(final List<PersistenceCommand> commands) {
        try {
            for (final PersistenceCommand command : commands) {
                command.execute(null);
            }
            getEntityManager().flush();
        } catch (final RuntimeException e) {
            LOG.warn("Failure during execution", e);
            throw e;
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // getObject, resolveImmediately, resolveField
    // ///////////////////////////////////////////////////////////////////////

    public ObjectAdapter getObject(final TypedOid oid) {
        ensureOpened();
        ensureInTransaction();

        if (LOG.isDebugEnabled()) {
            LOG.debug("getObject; oid=" + oid);
        }

        if(oid instanceof AggregatedOid) {
            // does it make sense to get these directly?  not sure, so for now have decided to fail fast. 
            throw new UnsupportedOperationException("Cannot retrieve aggregated objects directly, oid: " + oid.enString());
        }
        final RootOid rootOid = (RootOid) oid;
        Object result = null;
        try {
            final Class<?> cls = clsOf(rootOid);
            final Object idPropValue = idValueOf(rootOid);
            result = getEntityManager().find(cls, idPropValue);
        } catch (final RuntimeException e) {
            throw e;
        }

        if (result == null) {
            throw new ObjectNotFoundException(oid);
        }
        final ObjectAdapter adapter = hydrator.recreateAdapter(oid, result);
        
        //TODO: loadPostProcessor.loaded(adapter);
        return adapter;
    }

    /**
     * Will do nothing if object is already resolved or if object is transient.
     * <p>
     * TODO:
     * The final {@link ResolveState} of the adapter is set using
     * {@link NakedLoadPostEventListener#onPostLoad(org.hibernate.event.PostLoadEvent)}
     * Note: this is the same behaviour as MemoryObjectStore, XmlObjectStore
     * and HibernateObjectStore.
     * <p>
     * REVIEW: if the initial state is RESOLVING_PART, then the
     * {@link ResolveState} is not changed. Is this right?
     */
    public void resolveImmediately(final ObjectAdapter adapter) {
        ensureOpened();
        ensureInTransaction();

        if (LOG.isDebugEnabled()) {
            LOG.debug("resolveImmediately; oid=" + adapter.getOid().enString());
        }

        if (adapter.isResolved()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("; already resolved - ignoring");
            }
            return;
        }
        if (!adapter.representsPersistent()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("; not persistent - ignoring");
            }
            return;
        }

        final Oid oid = adapter.getOid();
        if (oid instanceof AggregatedOid) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("; aggregated - resolving parent");
            }
            final AggregatedOid aggregatedOid = (AggregatedOid) oid;
            final TypedOid parentOid = aggregatedOid.getParentOid();
            final ObjectAdapter parentAdapter = this.getObject(parentOid);
            resolveImmediately(parentAdapter);
            return;
        }

        JpaPropertyUtils.setPropertyIdFromOid(adapter, getAdapterFactory());
        try {
            final Object domainObject = adapter.getObject();

            getEntityManager().refresh(domainObject);
        } catch (final RuntimeException e) {
            throw new ObjectNotFoundException(adapter.getOid(), e);
        }

        if (adapter.getObject() == null) {
            throw new ObjectNotFoundException(adapter.getOid());
        }

        // possibly redundant because also called in the post-load event
        // listener,
        // but is required if we were ever to get an eager left-outer-join as
        // the result of a refresh (sounds possible).
        
        //TODO: loadPostProcessor.loaded(adapter);
    }

    /**
     * Walking the graph.
     */
    public void resolveField(final ObjectAdapter object, final ObjectAssociation association) {
        ensureOpened();
        ensureInTransaction();

        final ObjectAdapter referencedCollectionAdapter = association.get(object);

        // if a proxy collection, then force it to initialize.
        if (association.isOneToManyAssociation()) {
            ensureThatState(referencedCollectionAdapter, is(notNullValue()));

            final Object referencedCollection = referencedCollectionAdapter.getObject();
            ensureThatState(referencedCollection, is(notNullValue()));

            // just 'touching' the object is sufficient.
            referencedCollection.hashCode();
        }

        if (referencedCollectionAdapter != null) {
            // this works and seems to be sufficient (is also called from
            // NakedPostLoadEventListener for direct retrievals rather than
            // walking the
            // graph).
            
            // TODO: loadPostProcessor.loaded(referencedCollectionAdapter);
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // getInstances, hasInstances
    // ///////////////////////////////////////////////////////////////////////

    public List<ObjectAdapter> getInstances(final PersistenceQuery persistenceQuery) {
        ensureOpened();
        ensureInTransaction();

        final PersistenceQueryProcessor<? extends PersistenceQuery> processor = persistenceQueryProcessorByClass.get(persistenceQuery.getClass());
        if (processor == null) {
            throw new UnsupportedFindException(MessageFormat.format("Unsupported criteria type: {0}", persistenceQuery.getClass().getName()));
        }
        return processPersistenceQuery(processor, persistenceQuery);
    }

    @SuppressWarnings("unchecked")
    private <Q extends PersistenceQuery> List<ObjectAdapter> processPersistenceQuery(final PersistenceQueryProcessor<Q> persistenceQueryProcessor, final PersistenceQuery persistenceQuery) {
        return persistenceQueryProcessor.process((Q)persistenceQuery);
    }

    public boolean hasInstances(final ObjectSpecification specification) {
        ensureOpened();
        ensureInTransaction();

        if (LOG.isDebugEnabled()) {
            LOG.debug("hasInstances: class=" + specification.getFullIdentifier());
        }

        if (!specification.persistability().isPersistable()) {
            LOG.warn("hasInstances: trying to run for non-persistent class " + specification);
            return false;
        }

        final Query query = QueryUtil.createQuery(getEntityManager(), "o", "select o.id", specification, null);
        query.setMaxResults(1);
        return !query.getResultList().isEmpty();
    }

    // ///////////////////////////////////////////////////////////////////////
    // Helpers (loadObjects)
    // ///////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unused")
    private List<ObjectAdapter> loadObjects(final ObjectSpecification specification, final List<?> listOfPojs, final AdapterManager adapterManager) {
        final List<ObjectAdapter> adapters = Lists.newArrayList();
        int i = 0;
        for (final Object pojo : listOfPojs) {
            // REVIEW: cannot just load adapter for object - if Naked Objects
            // has
            // already loaded the object
            // then object won't match it (e.g. if getInstances has been called
            // and an instance has
            // been loaded) - so need to use Hibernate session to get an Oid to
            // do a lookup in that case
            adapters.add(adapterManager.getAdapterFor(pojo));
        }
        return adapters;
    }

    // ///////////////////////////////////////////////////////////////////////
    // Services
    // ///////////////////////////////////////////////////////////////////////

    @Override
    public void registerService(RootOid rootOid) {
        ensureOpened();
        this.registeredServices.put(rootOid.getObjectSpecId(), rootOid);
    }

    @Override
    public RootOid getOidForService(ObjectSpecification serviceSpec) {
        ensureOpened();
        return this.registeredServices.get(serviceSpec.getSpecId());
    }

    // ///////////////////////////////////////////////////////////////////////
    // Helpers: ensure*
    // ///////////////////////////////////////////////////////////////////////

    private void ensureNotYetOpen() {
        ensureStateIs(State.NOT_YET_OPEN);
    }

    private void ensureOpened() {
        ensureStateIs(State.OPEN);
    }

    private void ensureInSession() {
        ensureThatContext(IsisContext.inSession(), is(true));
    }

    private void ensureNotInTransaction() {
        ensureInSession();
        ensureThatContext(IsisContext.inTransaction(), is(false));
    }

    private void ensureInTransaction() {
        if (transactionMode == TransactionMode.UNCHAINED) {
            ensureThatContext(IsisContext.inTransaction(), is(true));
            ensureInHibernateTransaction();
        } else {
            ensureInSession();
            if (IsisContext.inTransaction()) {
                ensureInHibernateTransaction();
            } else {
                getTransactionManager().startTransaction();
            }
        }
    }

    private void ensureInHibernateTransaction() {
        ensureThatState(getEntityManager().getTransaction(), is(notNullValue()));
        ensureThatState(getEntityManager().getTransaction().isActive(), is(true));
    }

    private void ensureStateIs(final State stateRequired) {
        if (state == stateRequired) {
            return;
        }
        throw new IllegalStateException("State is: " + state + "; should be: " + stateRequired);
    }

    // ///////////////////////////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////////////////////////

    public void debugData(final DebugBuilder debug) {
        throw new NotYetImplementedException();
    }

    public String debugTitle() {
        throw new NotYetImplementedException();
    }

    // ///////////////////////////////////////////////////////////////////////
    // non-API
    // ///////////////////////////////////////////////////////////////////////

    public NamedQuery getNamedQuery(String queryName) {
        return applicationComponents.getNamedQuery(queryName);
    }

    /**
     * For testing purposes, to allow fixtures to use JPA to initialize the
     * database without triggering the objectstore.
     * 
     * @see #resumeListener()
     */
    public void suspendListener() {
        applicationComponents.suspendListener();
    }

    /**
     * For testing purposes, to allow fixtures to use JPA to initialize the
     * database without triggering the objectstore.
     * 
     * @see #suspendListener()
     */
    public void resumeListener() {
        applicationComponents.resumeListener();
    }


    // ///////////////////////////////////////////////////////////////////////
    // Helpers
    // ///////////////////////////////////////////////////////////////////////

    private Class<?> clsOf(final TypedOid oid) {
        final ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecId(oid.getObjectSpecId());
        return objectSpec.getCorrespondingClass();
    }

    private Object idValueOf(final RootOid oid) {
        final ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecId(oid.getObjectSpecId());
        return JpaPropertyUtils.idValueOf(oid, objectSpec);
    }

    
    /**
     * Intended for internal and test use only.
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    // ///////////////////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // ///////////////////////////////////////////////////////////////////////

    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    public ObjectAdapterFactory getAdapterFactory() {
        return adapterFactory;
    }

    /**
     * @see #setAdapterManager(AdapterManager)
     */
    public AdapterManager getAdapterManager() {
        return adapterManager;
    }

    // ///////////////////////////////////////////////////////////////////////
    // Dependencies (injected)
    // ///////////////////////////////////////////////////////////////////////

    private void ensureDependenciesInjected() {
        ensureThatState(specificationLoader, is(notNullValue()));
        ensureThatState(adapterManager, is(notNullValue()));
        ensureThatState(hydrator, is(notNullValue()));
        //TODO: ensureThatState(hibernateApplicationComponents, is(notNullValue()));
        ensureThatState(transactionManager, is(notNullValue()));
    }

    /**
     * @see #setSpecificationLoader(SpecificationLoader)
     */
    public SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    /**
     * Injected prior to {@link #open()}ing.
     * <p>
     * Injected by owning {@link PersistenceSessionObjectStore} (see
     * {@link PersistenceSessionObjectStore#open()}) by virtue of fact that this
     * implementation is {@link SpecificationLoaderAware aware} of the
     * {@link SpecificationLoader}.
     */
    public void setSpecificationLoader(final SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }

    /**
     * @see #setHydrator(PersistenceSessionHydrator)
     */
    public PersistenceSessionHydrator getHydrator() {
        return hydrator;
    }

    /**
     * Injected prior to {@link #open()}ing.
     * <p>
     * Injected by owning {@link PersistenceSessionObjectStore} (see
     * {@link PersistenceSessionObjectStore#open()}) by virtue of fact that this
     * implementation is {@link PersistenceSessionHydratorAware aware} of the
     * {@link PersistenceSessionHydrator}.
     */
    public void setHydrator(final PersistenceSessionHydrator hydrator) {
        this.hydrator = hydrator;
    }

    /**
     * @see #getTransactionManager()
     */
    public IsisTransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Injected prior to {@link #open()}ing.
     * <p>
     * Injected by owning {@link PersistenceSessionObjectStore} (see
     * {@link PersistenceSessionObjectStore#open()}) by virtue of fact that this
     * implementation is {@link NakedObjectTransactionManagerAware aware} of the
     * {@link NakedObjectTransactionManager}.
     */
    public void setTransactionManager(final IsisTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }




//    /**
//     * @see #setHibernateApplicationComponents(HibernateMetaDataComponents)
//     */
//    public HibernateApplicationComponents getHibernateApplicationComponents() {
//        return hibernateApplicationComponents;
//    }
//
//    /**
//     * Injected prior to {@link #open()}ing.
//     * <p>
//     * Injected by owning {@link JpaPersistenceSession} (see
//     * {@link JpaPersistenceSession#doOpen()); hard-coded into implementation.
//     */
//    public void setHibernateApplicationComponents(final HibernateApplicationComponents hibernateApplicationComponents) {
//        this.hibernateApplicationComponents = hibernateApplicationComponents;
//    }

}
