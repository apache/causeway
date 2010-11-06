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


package org.apache.isis.remoting.client.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.commons.components.ApplicationScopedComponent;
import org.apache.isis.commons.components.SessionScopedComponent;
import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.services.ServicesInjector;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.util.CollectionFacetUtils;
import org.apache.isis.remoting.data.Data;
import org.apache.isis.remoting.data.common.IdentityData;
import org.apache.isis.remoting.data.common.ObjectData;
import org.apache.isis.remoting.data.query.PersistenceQueryData;
import org.apache.isis.remoting.exchange.FindInstancesRequest;
import org.apache.isis.remoting.exchange.FindInstancesResponse;
import org.apache.isis.remoting.exchange.GetObjectRequest;
import org.apache.isis.remoting.exchange.GetObjectResponse;
import org.apache.isis.remoting.exchange.HasInstancesRequest;
import org.apache.isis.remoting.exchange.HasInstancesResponse;
import org.apache.isis.remoting.exchange.OidForServiceRequest;
import org.apache.isis.remoting.exchange.OidForServiceResponse;
import org.apache.isis.remoting.exchange.ResolveFieldRequest;
import org.apache.isis.remoting.exchange.ResolveFieldResponse;
import org.apache.isis.remoting.exchange.ResolveObjectRequest;
import org.apache.isis.remoting.exchange.ResolveObjectResponse;
import org.apache.isis.remoting.facade.ServerFacade;
import org.apache.isis.remoting.protocol.ObjectEncoderDecoder;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.PersistenceSessionAbstract;
import org.apache.isis.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtime.persistence.objectfactory.ObjectFactory;
import org.apache.isis.runtime.persistence.objectstore.PersistenceSessionObjectStore;
import org.apache.isis.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.runtime.persistence.query.PersistenceQueryBuiltIn;
import org.apache.isis.runtime.transaction.TransactionalClosureAbstract;
import org.apache.isis.runtime.transaction.TransactionalClosureWithReturnAbstract;
import org.apache.isis.runtime.transaction.updatenotifier.UpdateNotifier;
import org.apache.log4j.Logger;


public class PersistenceSessionProxy extends PersistenceSessionAbstract {

    final static Logger LOG = Logger.getLogger(PersistenceSessionProxy.class);

    private final ServerFacade serverFacade;
    private final ObjectEncoderDecoder encoderDecoder;

    /**
     * Keyed on an adapter wrapping a <tt>java.util.List</tt> (or equiv), ie with a {@link CollectionFacet}.
     */
    private final Map<ObjectSpecification, ObjectAdapter> cache = new HashMap<ObjectSpecification, ObjectAdapter>();
    private HashMap<String, Oid> serviceOidByNameCache = new HashMap<String, Oid>();


    //////////////////////////////////////////////////////////////////
    // Constructor
    //////////////////////////////////////////////////////////////////

    public PersistenceSessionProxy(
            final PersistenceSessionFactory persistenceSessionFactory,
            final AdapterFactory adapterFactory,
            final ObjectFactory objectFactory,
            final ServicesInjector containerInjector,
            final OidGenerator oidGenerator,
            final AdapterManagerExtended adapterManager,
            final ServerFacade serverFacade,
            final ObjectEncoderDecoder encoder) {
        super(persistenceSessionFactory, adapterFactory, objectFactory, containerInjector, oidGenerator, adapterManager);
        this.serverFacade = serverFacade;
        this.encoderDecoder = encoder;
    }


    //////////////////////////////////////////////////////////////////
    // init, shutdown, reset, isInitialized
    //////////////////////////////////////////////////////////////////

    /**
     * TODO: mismatch between {@link SessionScopedComponent} (open) and
     * {@link ApplicationScopedComponent} (init).
     */
    @Override
    public void doOpen() {
        serverFacade.init();
    }

    /**
     * TODO: mismatch between {@link SessionScopedComponent} (open) and
     * {@link ApplicationScopedComponent} (init).
     */
    public void doClose() {
        serverFacade.shutdown();
    }


    /**
     * No need to install fixtures, rely on server-side to do the right thing.
     */
    public boolean isFixturesInstalled() {
        return true;
    }


    //////////////////////////////////////////////////////////////////
    // loadObject, reload
    //////////////////////////////////////////////////////////////////

    public synchronized ObjectAdapter loadObject(
    		final Oid oid, final ObjectSpecification hintSpec) {
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        if (adapter != null) {
            return adapter;
        }
        return loadObjectFromPersistenceLayer(oid, hintSpec);
    }


	private ObjectAdapter loadObjectFromPersistenceLayer(final Oid oid,
			final ObjectSpecification specHint) {
		return getTransactionManager().executeWithinTransaction(
        		new TransactionalClosureWithReturnAbstract<ObjectAdapter>(){
			public ObjectAdapter execute() {
				// NB: I think that the auth session must be null because we may not yet have logged in if retrieving the services.
				GetObjectRequest request = new GetObjectRequest(null, oid, specHint.getFullName());
				GetObjectResponse response = serverFacade.getObject(request);
				final ObjectData data = response.getObjectData();
				return encoderDecoder.decode(data);
			}});
	}

    public void reload(final ObjectAdapter object) {
        final IdentityData identityData = encoderDecoder.encodeIdentityData(object);
        reloadFromPersistenceLayer(identityData);
    }


	private void reloadFromPersistenceLayer(final IdentityData identityData) {
		getTransactionManager().executeWithinTransaction(
    		new TransactionalClosureAbstract() {
				public void execute() {
					ResolveObjectRequest request = new ResolveObjectRequest(getAuthenticationSession(), identityData);
					ResolveObjectResponse response = serverFacade.resolveImmediately(request);
					final ObjectData update = response.getObjectData();
					encoderDecoder.decode(update);
				}});
	}


    //////////////////////////////////////////////////////////////////
    // resolveImmediately, resolveField
    //////////////////////////////////////////////////////////////////

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
		getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract(){
			public void execute() {
				ResolveObjectRequest request = new ResolveObjectRequest(
						getAuthenticationSession(), adapterData);
				// unlike the server-side implementation we don't invoke the callbacks
				// for loading and loaded (they will already have been called in the server)
				ResolveObjectResponse response = serverFacade.resolveImmediately(
						request);
				final ObjectData data = response.getObjectData();
				encoderDecoder.decode(data);
			}
			});
	}

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


	private void resolveFieldFromPersistenceLayer(final ObjectAdapter adapter,
			final ObjectAssociation field) {
		final IdentityData adapterData = encoderDecoder.encodeIdentityData(adapter);
		final String fieldId = field.getId();
		getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
			public void execute() {
				ResolveFieldRequest request = new ResolveFieldRequest(getAuthenticationSession(), adapterData, fieldId);
				ResolveFieldResponse response = serverFacade.resolveField(
						request);
				final Data data = response.getData();
				encoderDecoder.decode(data);
			}});
	}


    //////////////////////////////////////////////////////////////////
    // makePersistent
    //////////////////////////////////////////////////////////////////

    /**
     * REVIEW: we should perhaps have a little more symmetry here, and
     * have the {@link ServerFacade} callback to the {@link PersistenceSession}
     * (the <tt>PersistenceSessionPersist</tt> API) to handle remapping
     * of adapters.
     */
    public synchronized void makePersistent(final ObjectAdapter object) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("makePersistent " + object);
        }
        // TODO: the PSOS has more checks; should we do the same?
		makePersistentInPersistenceLayer(object);
    }

	protected void makePersistentInPersistenceLayer(final ObjectAdapter object) {
		getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract(){
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
			}});
	}


    //////////////////////////////////////////////////////////////////
    // objectChanged
    //////////////////////////////////////////////////////////////////

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
		getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract(){
			public void execute() {
				getTransactionManager().addObjectChanged(adapter);
			}});
	}


    //////////////////////////////////////////////////////////////////
    // destroy
    //////////////////////////////////////////////////////////////////

    public synchronized void destroyObject(final ObjectAdapter object) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroyObject " + object);
        }
        destroyObjectInPersistenceLayer(object);

        // TODO need to do garbage collection instead
        // Isis.getObjectLoader().unloaded(object);
    }


	protected void destroyObjectInPersistenceLayer(final ObjectAdapter object) {
		getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract(){
			public void execute() {
				getTransactionManager().addDestroyObject(object);
			}
		});
	}

    //////////////////////////////////////////////////////////////////
    // getInstances
    //////////////////////////////////////////////////////////////////

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
		return cache.containsKey(noSpec) &&
		       persistenceQuery instanceof PersistenceQueryBuiltIn;
	}

	/**
	 * TODO: this code is not currently in use because there is no way to
	 * set up the cache.  We may want to change what the cache is keyed on.
	 */
	private ObjectAdapter[] getInstancesFromCache(
			PersistenceQuery persistenceQuery) {
		final ObjectSpecification noSpec = persistenceQuery.getSpecification();
        PersistenceQueryBuiltIn builtIn = (PersistenceQueryBuiltIn) persistenceQuery;
		final ObjectAdapter collection = cache.get(noSpec);
        if (!collection.getSpecification().isCollection()) {
        	return new ObjectAdapter[0];
        }

        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
		final List<ObjectAdapter> instances = new ArrayList<ObjectAdapter>();
		for (ObjectAdapter instance : facet.iterable(collection)) {
		    if (builtIn.matches(instance)) {
		        instances.add(instance);
		    }
		}
		return (ObjectAdapter[]) instances.toArray(new ObjectAdapter[instances.size()]);
	}


	private ObjectAdapter[] findInstancesFromPersistenceLayer(
			final PersistenceQuery persistenceQuery) {
		final PersistenceQueryData criteriaData = encoderDecoder.encodePersistenceQuery(persistenceQuery);
        return getTransactionManager().executeWithinTransaction(
    		new TransactionalClosureWithReturnAbstract<ObjectAdapter[]>(){
				public ObjectAdapter[] execute() {
					FindInstancesRequest request = new FindInstancesRequest(getAuthenticationSession(), criteriaData);
					FindInstancesResponse response = serverFacade.findInstances(request);
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


    //////////////////////////////////////////////////////////////////
    // hasInstances
    //////////////////////////////////////////////////////////////////

    public boolean hasInstances(final ObjectSpecification specification) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasInstances of " + specification);
        }
        if (cache.containsKey(specification)) {
            return hasInstancesFromCache(specification);
        }
        return hasInstancesFromPersistenceLayer(specification);
    }


	private boolean hasInstancesFromCache(
			final ObjectSpecification specification) {
		final ObjectAdapter collection = (ObjectAdapter) cache.get(specification);
		final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
		if (facet == null) {
			return false;
		}
		return facet.size(collection) > 0;
	}


	private boolean hasInstancesFromPersistenceLayer(
			final ObjectSpecification specification) {
		final String specFullName = specification.getFullName();
		return getTransactionManager().executeWithinTransaction(
    		new TransactionalClosureWithReturnAbstract<Boolean>(){
				public Boolean execute() {
					HasInstancesRequest request = new HasInstancesRequest(getAuthenticationSession(), specFullName);
					HasInstancesResponse response = serverFacade.hasInstances(request);
					return response.hasInstances();
				}});
	}


    //////////////////////////////////////////////////////////////////
    // Services
    //////////////////////////////////////////////////////////////////

	/**
	 * TODO: not symmetric with {@link PersistenceSessionObjectStore}; we have
	 * a cache but it does not?
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
		OidForServiceRequest request = new OidForServiceRequest(getAuthenticationSession(), serviceId);
		OidForServiceResponse response = serverFacade.oidForService(request);
		final IdentityData data = response.getOidData();
		return data.getOid();
	}

	/**
	 * TODO: not symmetric with {@link PersistenceSessionObjectStore}; we have
	 * a cache but it does not?
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
    public ClientSideTransactionManager getTransactionManager() {
        return (ClientSideTransactionManager) super.getTransactionManager();
    }


    //////////////////////////////////////////////////////////////////
    // Debugging
    //////////////////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugString debug) {
        super.debugData(debug);
        debug.appendln("Server Facade", serverFacade);
    }

    public String debugTitle() {
        return "Proxy Persistence Sessino";
    }


    //////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    //////////////////////////////////////////////////////////////////

    private static AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    private static UpdateNotifier getUpdateNotifier() {
        return IsisContext.getUpdateNotifier();
    }



}
