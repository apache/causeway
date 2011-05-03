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

package org.apache.isis.runtimes.dflt.remoting.common.client.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.remoting.common.data.Data;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.IdentityData;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.ObjectData;
import org.apache.isis.runtimes.dflt.remoting.common.data.query.PersistenceQueryData;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.FindInstancesRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.FindInstancesResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.GetObjectRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.GetObjectResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.HasInstancesRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.HasInstancesResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.OidForServiceRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.OidForServiceResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResolveFieldRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResolveFieldResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResolveObjectRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResolveObjectResponse;
import org.apache.isis.runtimes.dflt.remoting.common.facade.ServerFacade;
import org.apache.isis.runtimes.dflt.remoting.common.protocol.ObjectEncoderDecoder;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSessionAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.PersistenceSessionObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryBuiltIn;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.ObjectFactory;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.UpdateNotifier;
import org.apache.isis.runtimes.dflt.runtime.transaction.TransactionalClosureAbstract;
import org.apache.isis.runtimes.dflt.runtime.transaction.TransactionalClosureWithReturnAbstract;
import org.apache.log4j.Logger;

public class PersistenceSessionProxy extends PersistenceSessionAbstract {

    final static Logger LOG = Logger.getLogger(PersistenceSessionProxy.class);

    private final ServerFacade serverFacade;
    private final ObjectEncoderDecoder encoderDecoder;

    /**
     * Keyed on an adapter wrapping a <tt>java.util.List</tt> (or equiv), ie with a {@link CollectionFacet}.
     */
    private final Map<ObjectSpecification, ObjectAdapter> cache = new HashMap<ObjectSpecification, ObjectAdapter>();
    private final HashMap<String, Oid> serviceOidByNameCache = new HashMap<String, Oid>();

    // ////////////////////////////////////////////////////////////////
    // Constructor
    // ////////////////////////////////////////////////////////////////

    public PersistenceSessionProxy(final PersistenceSessionFactory persistenceSessionFactory,
        final ObjectAdapterFactory adapterFactory, final ObjectFactory objectFactory,
        final ServicesInjector containerInjector, final OidGenerator oidGenerator,
        final AdapterManagerExtended adapterManager, final ServerFacade serverFacade, final ObjectEncoderDecoder encoder) {
        super(persistenceSessionFactory, adapterFactory, objectFactory, containerInjector, oidGenerator, adapterManager);
        this.serverFacade = serverFacade;
        this.encoderDecoder = encoder;
    }

    // ////////////////////////////////////////////////////////////////
    // init, shutdown, reset, isInitialized
    // ////////////////////////////////////////////////////////////////

    /**
     * TODO: mismatch between {@link SessionScopedComponent} (open) and {@link ApplicationScopedComponent} (init).
     */
    @Override
    public void doOpen() {
        serverFacade.init();
    }

    /**
     * TODO: mismatch between {@link SessionScopedComponent} (open) and {@link ApplicationScopedComponent} (init).
     */
    @Override
    public void doClose() {
        serverFacade.shutdown();
    }

    /**
     * No need to install fixtures, rely on server-side to do the right thing.
     */
    @Override
    public boolean isFixturesInstalled() {
        return true;
    }

    // ////////////////////////////////////////////////////////////////
    // loadObject, reload
    // ////////////////////////////////////////////////////////////////

    @Override
    public synchronized ObjectAdapter loadObject(final Oid oid, final ObjectSpecification hintSpec) {
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        if (adapter != null) {
            return adapter;
        }
        return loadObjectFromPersistenceLayer(oid, hintSpec);
    }

    private ObjectAdapter loadObjectFromPersistenceLayer(final Oid oid, final ObjectSpecification specHint) {
        return getTransactionManager().executeWithinTransaction(
            new TransactionalClosureWithReturnAbstract<ObjectAdapter>() {
                @Override
                public ObjectAdapter execute() {
                    // NB: I think that the auth session must be null because we may not yet have logged in if
                    // retrieving the services.
                    final GetObjectRequest request = new GetObjectRequest(null, oid, specHint.getFullIdentifier());
                    final GetObjectResponse response = serverFacade.getObject(request);
                    final ObjectData data = response.getObjectData();
                    return encoderDecoder.decode(data);
                }
            });
    }

    @Override
    public void reload(final ObjectAdapter object) {
        final IdentityData identityData = encoderDecoder.encodeIdentityData(object);
        reloadFromPersistenceLayer(identityData);
    }

    private void reloadFromPersistenceLayer(final IdentityData identityData) {
        getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void execute() {
                final ResolveObjectRequest request = new ResolveObjectRequest(getAuthenticationSession(), identityData);
                final ResolveObjectResponse response = serverFacade.resolveImmediately(request);
                final ObjectData update = response.getObjectData();
                encoderDecoder.decode(update);
            }
        });
    }

    // ////////////////////////////////////////////////////////////////
    // resolveImmediately, resolveField
    // ////////////////////////////////////////////////////////////////

    @Override
    public synchronized void resolveImmediately(final ObjectAdapter adapter) {
        final ResolveState resolveState = adapter.getResolveState();
        if (!resolveState.canChangeTo(ResolveState.RESOLVING)) {
            return;
        }
        final Oid oid = adapter.getOid();
        if (LOG.isDebugEnabled()) {
            LOG.debug("resolve object (remotely from server)" + oid);
        }

        resolveImmediatelyFromPersistenceLayer(adapter);
    }

    private void resolveImmediatelyFromPersistenceLayer(final ObjectAdapter adapter) {
        final IdentityData adapterData = encoderDecoder.encodeIdentityData(adapter);
        getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void execute() {
                final ResolveObjectRequest request = new ResolveObjectRequest(getAuthenticationSession(), adapterData);
                // unlike the server-side implementation we don't invoke the callbacks
                // for loading and loaded (they will already have been called in the server)
                final ResolveObjectResponse response = serverFacade.resolveImmediately(request);
                final ObjectData data = response.getObjectData();
                encoderDecoder.decode(data);
            }
        });
    }

    @Override
    public void resolveField(final ObjectAdapter adapter, final ObjectAssociation field) {
        if (field.getSpecification().isCollectionOrIsAggregated()) {
            return;
        }
        final ObjectAdapter referenceAdapter = field.get(adapter);
        if (referenceAdapter != null && referenceAdapter.getResolveState().isResolved()) {
            return;
        }
        if (referenceAdapter == null || !referenceAdapter.isPersistent()) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("resolveField on server: " + adapter + "/" + field.getId());
        }
        resolveFieldFromPersistenceLayer(adapter, field);
    }

    private void resolveFieldFromPersistenceLayer(final ObjectAdapter adapter, final ObjectAssociation field) {
        final IdentityData adapterData = encoderDecoder.encodeIdentityData(adapter);
        final String fieldId = field.getId();
        getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void execute() {
                final ResolveFieldRequest request =
                    new ResolveFieldRequest(getAuthenticationSession(), adapterData, fieldId);
                final ResolveFieldResponse response = serverFacade.resolveField(request);
                final Data data = response.getData();
                encoderDecoder.decode(data);
            }
        });
    }

    // ////////////////////////////////////////////////////////////////
    // makePersistent
    // ////////////////////////////////////////////////////////////////

    /**
     * REVIEW: we should perhaps have a little more symmetry here, and have the {@link ServerFacade} callback to the
     * {@link PersistenceSession} (the <tt>PersistenceSessionPersist</tt> API) to handle remapping of adapters.
     */
    @Override
    public synchronized void makePersistent(final ObjectAdapter object) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("makePersistent " + object);
        }
        // TODO: the PSOS has more checks; should we do the same?
        makePersistentInPersistenceLayer(object);
    }

    protected void makePersistentInPersistenceLayer(final ObjectAdapter object) {
        getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void execute() {

                // the two implementations (proxy vs object store) vary here.
                // the object store does not make this call directly, it
                // instead delegates to the PersistAlgorithm that makes a
                // callback to the PersistenceSessionPersist API,
                // which in turn calls remaps the adapters.
                //
                // the proxy persistor on the other hand does nothing here.
                // instead we remap the adapter in distribution code,
                // processing the handling of the returned results.
                //
                // (see REVIEW comment above)

                getTransactionManager().addMakePersistent(object);
            }
        });
    }

    // ////////////////////////////////////////////////////////////////
    // objectChanged
    // ////////////////////////////////////////////////////////////////

    @Override
    public void objectChanged(final ObjectAdapter adapter) {
        if (adapter.isTransient()) {
            addObjectChangedToPresentationLayer(adapter);
            return;
        }
        if (adapter.getResolveState().respondToChangesInPersistentObjects()) {
            if (isImmutable(adapter)) {
                return;
            }
            addObjectChangedToPersistenceLayer(adapter);
        }
    }

    private void addObjectChangedToPresentationLayer(final ObjectAdapter adapter) {
        getUpdateNotifier().addChangedObject(adapter);
    }

    private void addObjectChangedToPersistenceLayer(final ObjectAdapter adapter) {
        getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void execute() {
                getTransactionManager().addObjectChanged(adapter);
            }
        });
    }

    // ////////////////////////////////////////////////////////////////
    // destroy
    // ////////////////////////////////////////////////////////////////

    @Override
    public synchronized void destroyObject(final ObjectAdapter object) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroyObject " + object);
        }
        destroyObjectInPersistenceLayer(object);

        // TODO need to do garbage collection instead
        // Isis.getObjectLoader().unloaded(object);
    }

    protected void destroyObjectInPersistenceLayer(final ObjectAdapter object) {
        getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
            @Override
            public void execute() {
                getTransactionManager().addDestroyObject(object);
            }
        });
    }

    // ////////////////////////////////////////////////////////////////
    // getInstances
    // ////////////////////////////////////////////////////////////////

    @Override
    protected ObjectAdapter[] getInstances(final PersistenceQuery persistenceQuery) {
        final ObjectSpecification noSpec = persistenceQuery.getSpecification();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstances of " + noSpec + " with " + persistenceQuery);
        }

        if (canSatisfyFromCache(persistenceQuery)) {
            return getInstancesFromCache(persistenceQuery);
        }

        return findInstancesFromPersistenceLayer(persistenceQuery);
    }

    private boolean canSatisfyFromCache(final PersistenceQuery persistenceQuery) {
        final ObjectSpecification noSpec = persistenceQuery.getSpecification();
        return cache.containsKey(noSpec) && persistenceQuery instanceof PersistenceQueryBuiltIn;
    }

    /**
     * TODO: this code is not currently in use because there is no way to set up the cache. We may want to change what
     * the cache is keyed on.
     */
    private ObjectAdapter[] getInstancesFromCache(final PersistenceQuery persistenceQuery) {
        final ObjectSpecification noSpec = persistenceQuery.getSpecification();
        final PersistenceQueryBuiltIn builtIn = (PersistenceQueryBuiltIn) persistenceQuery;
        final ObjectAdapter collection = cache.get(noSpec);
        if (!collection.getSpecification().isCollection()) {
            return new ObjectAdapter[0];
        }

        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
        final List<ObjectAdapter> instances = new ArrayList<ObjectAdapter>();
        for (final ObjectAdapter instance : facet.iterable(collection)) {
            if (builtIn.matches(instance)) {
                instances.add(instance);
            }
        }
        return instances.toArray(new ObjectAdapter[instances.size()]);
    }

    private ObjectAdapter[] findInstancesFromPersistenceLayer(final PersistenceQuery persistenceQuery) {
        final PersistenceQueryData criteriaData = encoderDecoder.encodePersistenceQuery(persistenceQuery);
        return getTransactionManager().executeWithinTransaction(
            new TransactionalClosureWithReturnAbstract<ObjectAdapter[]>() {
                @Override
                public ObjectAdapter[] execute() {
                    final FindInstancesRequest request =
                        new FindInstancesRequest(getAuthenticationSession(), criteriaData);
                    final FindInstancesResponse response = serverFacade.findInstances(request);
                    final ObjectData[] instancesAsObjectData = response.getInstances();
                    final ObjectAdapter[] instances = new ObjectAdapter[instancesAsObjectData.length];
                    for (int i = 0; i < instancesAsObjectData.length; i++) {
                        instances[i] = encoderDecoder.decode(instancesAsObjectData[i]);
                    }
                    return instances;
                }

                @Override
                public void onSuccess() {
                    clearAllDirty();
                }
            });
    }

    // ////////////////////////////////////////////////////////////////
    // hasInstances
    // ////////////////////////////////////////////////////////////////

    @Override
    public boolean hasInstances(final ObjectSpecification specification) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasInstances of " + specification);
        }
        if (cache.containsKey(specification)) {
            return hasInstancesFromCache(specification);
        }
        return hasInstancesFromPersistenceLayer(specification);
    }

    private boolean hasInstancesFromCache(final ObjectSpecification specification) {
        final ObjectAdapter collection = cache.get(specification);
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
        if (facet == null) {
            return false;
        }
        return facet.size(collection) > 0;
    }

    private boolean hasInstancesFromPersistenceLayer(final ObjectSpecification specification) {
        final String specFullName = specification.getFullIdentifier();
        return getTransactionManager().executeWithinTransaction(new TransactionalClosureWithReturnAbstract<Boolean>() {
            @Override
            public Boolean execute() {
                final HasInstancesRequest request = new HasInstancesRequest(getAuthenticationSession(), specFullName);
                final HasInstancesResponse response = serverFacade.hasInstances(request);
                return response.hasInstances();
            }
        });
    }

    // ////////////////////////////////////////////////////////////////
    // Services
    // ////////////////////////////////////////////////////////////////

    /**
     * TODO: not symmetric with {@link PersistenceSessionObjectStore}; we have a cache but it does not?
     */
    @Override
    public Oid getOidForService(final String name) {
        Oid oid = serviceOidByNameCache.get(name);
        if (oid == null) {
            oid = getOidForServiceFromPersistenceLayer(name);
            registerService(name, oid);
        }
        return oid;
    }

    private Oid getOidForServiceFromPersistenceLayer(final String serviceId) {
        final OidForServiceRequest request = new OidForServiceRequest(getAuthenticationSession(), serviceId);
        final OidForServiceResponse response = serverFacade.oidForService(request);
        final IdentityData data = response.getOidData();
        return data.getOid();
    }

    /**
     * TODO: not symmetric with {@link PersistenceSessionObjectStore}; we have a cache but it does not?
     */
    @Override
    public void registerService(final String name, final Oid oid) {
        serviceOidByNameCache.put(name, oid);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TransactionManager
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Just downcasts.
     */
    @Override
    public ClientSideTransactionManager getTransactionManager() {
        return (ClientSideTransactionManager) super.getTransactionManager();
    }

    // ////////////////////////////////////////////////////////////////
    // Debugging
    // ////////////////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugBuilder debug) {
        super.debugData(debug);
        debug.appendln("Server Facade", serverFacade);
    }

    @Override
    public String debugTitle() {
        return "Proxy Persistence Sessino";
    }

    // ////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ////////////////////////////////////////////////////////////////

    private static AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    private static UpdateNotifier getUpdateNotifier() {
        return IsisContext.getUpdateNotifier();
    }

}
