package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatContext;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.spi.PersistenceCapable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.log4j.Logger;
import org.datanucleus.identity.OIDImpl;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpiAware;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.FrameworkSynchronizer;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.FrameworkSynchronizer.CalledFrom;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.IsisLifecycleListener;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.commands.DataNucleusCreateObjectCommand;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.commands.DataNucleusDeleteObjectCommand;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.commands.DataNucleusUpdateObjectCommand;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries.PersistenceQueryFindAllInstancesProcessor;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries.PersistenceQueryFindByPatternProcessor;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries.PersistenceQueryFindByTitleProcessor;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries.PersistenceQueryFindUsingApplibQueryProcessor;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries.PersistenceQueryProcessor;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries.QueryUtil;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.spi.JdoObjectIdSerializer;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.query.JdoNamedQuery;
import org.apache.isis.runtimes.dflt.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.runtimes.dflt.runtime.persistence.UnsupportedFindException;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStoreSpi;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByPattern;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManagerSpi;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransaction;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;

public class DataNucleusObjectStore implements ObjectStoreSpi {

    private static final Logger LOG = Logger.getLogger(DataNucleusObjectStore.class);

    static enum State {
        NOT_YET_OPEN, OPEN, CLOSED;
    }

    /**
     * @see #isFixturesInstalled()
     */
    public static final String INSTALL_FIXTURES_KEY = ConfigurationConstants.ROOT + "persistor.datanucleus.install-fixtures";
    public static final boolean INSTALL_FIXTURES_DEFAULT = false;

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

    private final ObjectAdapterFactory adapterFactory;
    private final DataNucleusApplicationComponents applicationComponents;
    
    private final Map<ObjectSpecId, RootOid> registeredServices = Maps.newHashMap();

    private PersistenceManager persistenceManager;

    private final Map<Class<?>, PersistenceQueryProcessor<?>> persistenceQueryProcessorByClass = Maps.newHashMap();
    // private final LoadPostProcessor loadPostProcessor;

    private State state;
    private TransactionMode transactionMode;

    
    private final FrameworkSynchronizer frameworkSynchronizer;

    public DataNucleusObjectStore(ObjectAdapterFactory adapterFactory, DataNucleusApplicationComponents applicationComponents) {
        ensureThatArg(adapterFactory, is(notNullValue()));
        ensureThatArg(applicationComponents, is(notNullValue()));

        this.state = State.NOT_YET_OPEN;
        this.transactionMode = TransactionMode.UNCHAINED;

        this.adapterFactory = adapterFactory;
        this.applicationComponents = applicationComponents;
        this.frameworkSynchronizer = applicationComponents.getFrameworkSynchronizer();
    }

    @Override
    public String name() {
        return "datanucleus";
    }

    // ///////////////////////////////////////////////////////////////////////
    // open, close
    // ///////////////////////////////////////////////////////////////////////

    public void open() {
        ensureNotYetOpen();

        openSession();
        ensureThatState(persistenceManager, is(notNullValue()));

        addPersistenceQueryProcessors(persistenceManager);

        state = State.OPEN;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Automatically {@link IsisTransactionManager#endTransaction() ends
     * (commits)} the current (Isis) {@link Transaction}. This in turn
     * {@link DataNucleusObjectStore#commitJdoTransaction() commits the underlying
     * JPA transaction}.
     * <p>
     * The corresponding DataNucleus {@link Entity} is then
     * {@link EntityManager#close() close}d.
     */
    public void close() {
        ensureOpened();
        ensureThatState(persistenceManager, is(notNullValue()));

        final IsisTransaction currentTransaction = getTransactionManager().getTransaction();
        if (currentTransaction != null && currentTransaction.getState().canCommit()) {
            getTransactionManager().endTransaction();
        }

        persistenceManager.close();
        state = State.CLOSED;
    }

    private PersistenceManager openSession() {
        this.persistenceManager = applicationComponents.createPersistenceManager();
        return this.persistenceManager;
    }

    private void addPersistenceQueryProcessors(final PersistenceManager persistenceManager) {
        persistenceQueryProcessorByClass.put(PersistenceQueryFindAllInstances.class, new PersistenceQueryFindAllInstancesProcessor(persistenceManager, frameworkSynchronizer));
        persistenceQueryProcessorByClass.put(PersistenceQueryFindByTitle.class, new PersistenceQueryFindByTitleProcessor(persistenceManager, frameworkSynchronizer));
        persistenceQueryProcessorByClass.put(PersistenceQueryFindByPattern.class, new PersistenceQueryFindByPatternProcessor(persistenceManager, frameworkSynchronizer));
        persistenceQueryProcessorByClass.put(PersistenceQueryFindUsingApplibQueryDefault.class, new PersistenceQueryFindUsingApplibQueryProcessor(persistenceManager, frameworkSynchronizer));
    }

    // ///////////////////////////////////////////////////////////////////////
    // isFixturesInstalled
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Implementation looks for the {@link #INSTALL_FIXTURES_KEY} in the
     * {@link #getConfiguration() configuration}.
     * <p>
     * By default this is not expected to be there, but utilities can add in on
     * the fly during bootstrapping if required.
     */
    public boolean isFixturesInstalled() {
        return ! getConfiguration().getBoolean(INSTALL_FIXTURES_KEY, INSTALL_FIXTURES_DEFAULT);
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
    public Connection getJavaSqlConnection() {
        return (Connection) persistenceManager.getDataStoreConnection().getNativeConnection();
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
        beginJdoTransaction();
    }

    public void endTransaction() {
        commitJdoTransaction();
    }

    public void abortTransaction() {
        rollbackJpaTransaction();
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

    private void rollbackJpaTransaction() {
        final javax.jdo.Transaction transaction = getPersistenceManager().currentTransaction();
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
        return new DataNucleusCreateObjectCommand(adapter, getPersistenceManager());
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
        return new DataNucleusUpdateObjectCommand(adapter, getPersistenceManager());
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
            getPersistenceManager().flush();
        } catch (final RuntimeException e) {
            LOG.warn("Failure during execution", e);
            throw e;
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // loadMappedObject, resolveImmediately, resolveField
    // ///////////////////////////////////////////////////////////////////////

    public ObjectAdapter loadInstanceAndAdapt(final TypedOid oid) {
        ensureOpened();
        ensureInTransaction();

        if (LOG.isDebugEnabled()) {
            LOG.debug("getObject; oid=" + oid);
        }

        final Object pojo = loadPojo(oid);
        return getPersistenceSession().mapRecreatedPojo(oid, pojo);
    }

    
    
    /////////////////////////////////////////////////////////////
    // delegated to by PojoRecreator
    /////////////////////////////////////////////////////////////

    public Object loadPojo(final TypedOid oid) {
    	
        // REVIEW: does it make sense to get these directly?  not sure, so for now have decided to fail fast. 
        if(oid instanceof AggregatedOid) {
            throw new UnsupportedOperationException("Cannot retrieve aggregated objects directly, oid: " + oid.enString(getOidMarshaller()));
        }
        
        final RootOid rootOid = (RootOid) oid;
        
        Object result = null;
        try {
            final Class<?> cls = clsOf(rootOid);
            final Object jdoObjectId = JdoObjectIdSerializer.toJdoObjectId(rootOid);
            result = getPersistenceManager().getObjectById(cls, jdoObjectId);
        } catch (final RuntimeException e) {
            throw e;
        }

        if (result == null) {
            throw new ObjectNotFoundException(oid);
        }
        return result;
    }

    
    public ObjectAdapter lazilyLoaded(Object pojo) {
        if(!(pojo instanceof PersistenceCapable)) {
            return null;
        } 
        final PersistenceCapable persistenceCapable = (PersistenceCapable) pojo;
        return frameworkSynchronizer.lazilyLoaded(persistenceCapable, CalledFrom.OS_LAZILYLOADED);
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
            LOG.debug("resolveImmediately; oid=" + adapter.getOid().enString(getOidMarshaller()));
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
            final ObjectAdapter parentAdapter = loadInstanceAndAdapt(parentOid);
            resolveImmediately(parentAdapter);
            return;
        }

        // REVIEW: is this possible?
        final Object domainObject = adapter.getObject();
		if (domainObject == null) {
            throw new ObjectNotFoundException(adapter.getOid());
        }

        try {
            getPersistenceManager().refresh(domainObject);
        } catch (final RuntimeException e) {
            throw new ObjectNotFoundException(adapter.getOid(), e);
        }

        // possibly redundant because also called in the post-load event
        // listener, but (with JPA impl) found it was required if we were ever to 
        // get an eager left-outer-join as the result of a refresh (sounds possible).
        
        frameworkSynchronizer.postLoadProcessingFor((PersistenceCapable) domainObject, CalledFrom.OS_RESOLVE);
    }

    /**
     * Walking the graph.
     */
    public void resolveField(final ObjectAdapter object, final ObjectAssociation association) {
        ensureOpened();
        ensureInTransaction();

        final ObjectAdapter referencedCollectionAdapter = association.get(object);

        // this code originally brought in from the JPA impl, but seems reasonable.
        if (association.isOneToManyAssociation()) {
            ensureThatState(referencedCollectionAdapter, is(notNullValue()));

            final Object referencedCollection = referencedCollectionAdapter.getObject();
            ensureThatState(referencedCollection, is(notNullValue()));

            // if a proxy collection, then force it to initialize.  just 'touching' the object is sufficient.
            // REVIEW: I wonder if this is actually needed; does JDO use proxy collections?
            referencedCollection.hashCode();
        }

        // the JPA impl used to also call its lifecycle listener on the referenced collection object, eg List,
        // itself.  I don't think this makes sense to do for JDO (the collection is not a PersistenceCapable).
    }

    
    // ///////////////////////////////////////////////////////////////////////
    // getInstances, hasInstances
    // ///////////////////////////////////////////////////////////////////////

    public List<ObjectAdapter> loadInstancesAndAdapt(final PersistenceQuery persistenceQuery) {
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

        final Query query = QueryUtil.createQuery(getPersistenceManager(), "o", "select o.id", specification, null);
        throw new NotYetImplementedException();
        //query.set.setMaxResults(1);
        //return !query.getResultList().isEmpty();
    }

    // ///////////////////////////////////////////////////////////////////////
    // Helpers (loadObjects)
    // ///////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unused")
    private List<ObjectAdapter> loadObjects(final ObjectSpecification specification, final List<?> listOfPojs, final AdapterManagerSpi adapterManager) {
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

    public JdoNamedQuery getNamedQuery(String queryName) {
        return applicationComponents.getNamedQuery(queryName);
    }

    /**
     * For testing purposes, to allow fixtures to use JDO to initialize the
     * database without triggering the objectstore.
     * 
     * @see #resumeListener()
     */
    public void suspendListener() {
        applicationComponents.suspendListener();
    }

    /**
     * For testing purposes, to allow fixtures to use JDO to initialize the
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

    /**
     * Intended for internal and test use only.
     */
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    // ///////////////////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // ///////////////////////////////////////////////////////////////////////

    public ObjectAdapterFactory getAdapterFactory() {
        return adapterFactory;
    }


    // ///////////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////////////////////

    public IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }
    
    protected IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }


}
