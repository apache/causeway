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

package org.apache.isis.core.runtime.persistence.objectstore;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingCallbackFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.util.CallbackUtils;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.FixturesInstalledFlag;
import org.apache.isis.core.runtime.persistence.NotPersistableException;
import org.apache.isis.core.runtime.persistence.PersistenceSessionAbstract;
import org.apache.isis.core.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.core.runtime.persistence.objectfactory.ObjectFactory;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.ToPersistObjectSet;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.ObjectStoreTransactionManager;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.core.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.core.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.core.runtime.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.transaction.ObjectPersistenceException;
import org.apache.isis.core.runtime.transaction.TransactionalClosureAbstract;
import org.apache.isis.core.runtime.transaction.TransactionalClosureWithReturnAbstract;
import org.apache.isis.core.runtime.transaction.updatenotifier.UpdateNotifier;
import org.apache.log4j.Logger;

public class PersistenceSessionObjectStore extends PersistenceSessionAbstract implements ToPersistObjectSet {
    private static final Logger LOG = Logger.getLogger(PersistenceSessionObjectStore.class);
    private final PersistAlgorithm persistAlgorithm;
    private final ObjectStorePersistence objectStore;
    private final Map<String, Oid> services = new HashMap<String, Oid>();

    /**
     * Initialize the object store so that calls to this object store access persisted objects and persist changes to
     * the object that are saved.
     */
    public PersistenceSessionObjectStore(final PersistenceSessionFactory persistenceSessionFactory,
        final AdapterFactory adapterFactory, final ObjectFactory objectFactory,
        final ServicesInjector servicesInjector, final OidGenerator oidGenerator,
        final AdapterManagerExtended identityMap, final PersistAlgorithm persistAlgorithm,
        final ObjectStorePersistence objectStore) {

        super(persistenceSessionFactory, adapterFactory, objectFactory, servicesInjector, oidGenerator, identityMap);
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + this);
        }

        ensureThatArg(persistAlgorithm, is(not(nullValue())), "persist algorithm required");
        ensureThatArg(objectStore, is(not(nullValue())), "object store required");

        this.persistAlgorithm = persistAlgorithm;
        this.objectStore = objectStore;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // init, shutdown
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doOpen() {

        ensureThatState(objectStore, is(notNullValue()), "object store required");
        ensureThatState(getTransactionManager(), is(notNullValue()), "transaction manager required");
        ensureThatState(persistAlgorithm, is(notNullValue()), "persist algorithm required");

        this.injectInto(objectStore); // as a hydrator
        getAdapterManager().injectInto(objectStore);
        getSpecificationLoader().injectInto(objectStore);
        getTransactionManager().injectInto(objectStore);

        getOidGenerator().injectInto(objectStore);

        objectStore.open();
    }

    /**
     * Returns the cached value of {@link ObjectStore#isFixturesInstalled() whether fixtures are installed} from the
     * {@link PersistenceSessionFactory} (provided it implements {@link FixturesInstalledFlag}), otherwise queries
     * {@link ObjectStore} directly.
     * <p>
     * This caching is important because if we've determined, for a given run, that fixtures are not installed, then we
     * don't want to change our mind by asking the object store again in another session.
     * 
     * @see FixturesInstalledFlag
     */
    @Override
    public boolean isFixturesInstalled() {
        PersistenceSessionFactory persistenceSessionFactory = getPersistenceSessionFactory();
        if (persistenceSessionFactory instanceof FixturesInstalledFlag) {
            FixturesInstalledFlag fixturesInstalledFlag = (FixturesInstalledFlag) persistenceSessionFactory;
            if (fixturesInstalledFlag.isFixturesInstalled() == null) {
                fixturesInstalledFlag.setFixturesInstalled(objectStore.isFixturesInstalled());
            }
            return fixturesInstalledFlag.isFixturesInstalled();
        } else {
            return objectStore.isFixturesInstalled();
        }
    }

    @Override
    protected void doClose() {
        objectStore.close();
    }

    @Override
    public void testReset() {
        objectStore.reset();
        getAdapterManager().reset();
        super.testReset();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing object manager");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // loadObject, reload
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter loadObject(final Oid oid, final ObjectSpecification hintSpec) {
        ensureThatArg(oid, is(notNullValue()));
        ensureThatArg(hintSpec, is(notNullValue()));

        ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        if (adapter != null) {
            return adapter;
        }

        return loadObjectFromPersistenceLayer(oid, hintSpec);
    }

    private ObjectAdapter loadObjectFromPersistenceLayer(final Oid oid, final ObjectSpecification hintSpec) {
        // the object store will map for us, using its hydrator (calls back
        // to #recreateAdapter)
        return getTransactionManager().executeWithinTransaction(
            new TransactionalClosureWithReturnAbstract<ObjectAdapter>() {
                @Override
                public ObjectAdapter execute() {
                    return objectStore.getObject(oid, hintSpec);
                }
            });
    }

    /**
     * Does nothing.
     */
    @Override
    public void reload(final ObjectAdapter object) {
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
            final ResolveState resolveState = adapter.getResolveState();
            if (!resolveState.canChangeTo(ResolveState.RESOLVING)) {
                return;
            }
            Assert.assertFalse("only resolve object that is not yet resolved", adapter, resolveState.isResolved());
            Assert.assertTrue("only resolve object that is persistent", adapter, adapter.isPersistent());
            resolveImmediatelyFromPersistenceLayer(adapter);
            if (LOG.isInfoEnabled()) {
                // don't log object - its toString() may use the unresolved field, or unresolved collection
                LOG.info("resolved: " + adapter.getSpecification().getShortName() + " " + resolveState.code() + " "
                    + adapter.getOid());
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
        if (field.isNotPersisted() || field.isOneToManyAssociation()
            || field.getSpecification().isCollectionOrIsAggregated()) {
            return;
        }
        final ObjectAdapter referenceAdapter = field.get(objectAdapter);
        if (referenceAdapter == null || referenceAdapter.getResolveState().isResolved()) {
            return;
        }
        if (!referenceAdapter.isPersistent()) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            // don't log object - it's toString() may use the unresolved field
            // or unresolved collection
            LOG.info("resolve field " + objectAdapter.getSpecification().getShortName() + "." + field.getId() + ": "
                + referenceAdapter.getSpecification().getShortName() + " " + referenceAdapter.getResolveState().code()
                + " " + referenceAdapter.getOid());
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
     * Makes an {@link ObjectAdapter} persistent. The specified object should be stored away via this object store's
     * persistence mechanism, and have an new and unique OID assigned to it. The object, should also be added to the
     * {@link AdapterManager} as the object is implicitly 'in use'.
     * 
     * <p>
     * If the object has any associations then each of these, where they aren't already persistent, should also be made
     * persistent by recursively calling this method.
     * 
     * <p>
     * If the object to be persisted is a collection, then each element of that collection, that is not already
     * persistent, should be made persistent by recursively calling this method.
     * 
     * @see #remapAsPersistent(ObjectAdapter)
     */
    @Override
    public void makePersistent(final ObjectAdapter adapter) {
        if (adapter.isPersistent()) {
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
                persistAlgorithm.makePersistent(adapter, PersistenceSessionObjectStore.this);
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

        if (adapter.isTransient()) {
            addObjectChangedForPresentationLayer(adapter);
            return;
        }

        if (adapter.getResolveState().respondToChangesInPersistentObjects()) {
            if (isImmutable(adapter)) {
                // previously used to throw
                // new ObjectPersistenceException("cannot change immutable object");
                // however, since the the bytecode enhancers effectively make
                // calling objectChanged() the responsibility of the framework,
                // we may as well now do the check here and ignore if doesn't apply.
                return;
            }

            addObjectChangedForPersistenceLayer(adapter);
            addObjectChangedForPresentationLayer(adapter);
        }
        if (adapter.getResolveState().respondToChangesInPersistentObjects() || adapter.isTransient()) {
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
                SaveObjectCommand saveObjectCommand = objectStore.createSaveObjectCommand(adapter);
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
     * Removes the specified object from the system. The specified object's data should be removed from the persistence
     * mechanism.
     */
    @Override
    public void destroyObject(final ObjectAdapter adapter) {
        if (LOG.isInfoEnabled()) {
            LOG.info("destroyObject " + adapter);
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
                    throw new ObjectPersistenceException("Object to be deleted does not have a version (maybe it should be reolved first): " + adapter);
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
     * Callback from the {@link PersistAlgorithm} (or equivalent; some object stores such as Hibernate will use
     * listeners instead) to indicate that the {@link ObjectAdapter adapter} is persisted, and the adapter maps should
     * be updated.
     * 
     * <p>
     * The object store is expected to have already updated the {@link Oid} state and the {@link ResolveState} . Some
     * object stores (again, we're thinking Hibernate here) might also have updated collections, both the Oid of the
     * collection and the pojo wrapped by the adapter.
     * 
     * <p>
     * The {@link PersistAlgorithm} is called from {@link #makePersistent(ObjectAdapter)}.
     * 
     * <p>
     * TODO: the <tt>PersistenceSessionProxy</tt> doesn't have this method; should document better why this is the case,
     * and where the equivalent functionality is (somewhere in the marshalling stuff, I think).
     * 
     * @see #remapAsPersistent(ObjectAdapter)
     */
    @Override
    public void remapAsPersistent(final ObjectAdapter adapter) {
        getAdapterManager().remapAsPersistent(adapter);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // getInstances
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    protected ObjectAdapter[] getInstances(final PersistenceQuery persistenceQuery) {
        if (LOG.isInfoEnabled()) {
            LOG.info("getInstances matching " + persistenceQuery);
        }
        return getInstancesFromPersistenceLayer(persistenceQuery);
    }

    private ObjectAdapter[] getInstancesFromPersistenceLayer(final PersistenceQuery persistenceQuery) {
        return getTransactionManager().executeWithinTransaction(
            new TransactionalClosureWithReturnAbstract<ObjectAdapter[]>() {
                @Override
                public ObjectAdapter[] execute() {
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
     * Checks whether there are any instances of the specified type. The object store should look for instances of the
     * type represented by <variable>type </variable> and return <code>true</code> if there are, or <code>false</code>
     * if there are not.
     */
    @Override
    public boolean hasInstances(final ObjectSpecification specification) {
        if (LOG.isInfoEnabled()) {
            LOG.info("hasInstances of " + specification.getShortName());
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

    @Override
    protected Oid getOidForService(final String name) {
        return getOidForServiceFromPersistenceLayer(name);
    }

    private Oid getOidForServiceFromPersistenceLayer(final String name) {
        Oid oid = services.get(name);
        if (oid == null) {
            oid = objectStore.getOidForService(name);
            services.put(name, oid);
        }
        return oid;
    }

    @Override
    protected void registerService(final String name, final Oid oid) {
        objectStore.registerService(name, oid);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TransactionManager
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Just downcasts.
     */
    @Override
    public ObjectStoreTransactionManager getTransactionManager() {
        return (ObjectStoreTransactionManager) super.getTransactionManager();
    }

    /**
     * Uses the {@link ObjectStore} to {@link ObjectStore#createCreateObjectCommand(ObjectAdapter) create} a
     * {@link CreateObjectCommand}, and adds to the {@link IsisTransactionManager}.
     */
    @Override
    public void addPersistedObject(final ObjectAdapter object) {
        getTransactionManager().addCommand(objectStore.createCreateObjectCommand(object));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugString debug) {
        super.debugData(debug);

        debug.appendTitle("Persistor");
        getTransactionManager().debugData(debug);
        debug.appendln("Persist Algorithm", persistAlgorithm);
        debug.appendln("Object Store", objectStore);
        debug.appendln();

        objectStore.debugData(debug);
    }

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
