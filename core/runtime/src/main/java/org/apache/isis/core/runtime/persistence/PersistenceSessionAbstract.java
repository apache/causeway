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


package org.apache.isis.core.runtime.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectList;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByPattern;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByTitle;
import org.apache.isis.core.metamodel.spec.Dirtiable;
import org.apache.isis.core.metamodel.spec.IntrospectableSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationFacets;
import org.apache.isis.core.metamodel.spec.ObjectSpecification.CreationMode;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.persistence.objectfactory.ObjectFactory;
import org.apache.isis.core.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.core.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindByPattern;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindUsingApplibQuerySerializable;
import org.apache.isis.core.runtime.persistence.services.ServiceUtil;
import org.apache.isis.core.runtime.transaction.IsisTransactionManager;


public abstract class PersistenceSessionAbstract implements PersistenceSession {
    private static final Logger LOG = Logger.getLogger(PersistenceSessionAbstract.class);

    private final PersistenceSessionFactory persistenceSessionFactory;
    private final AdapterFactory adapterFactory;
    private final ObjectFactory objectFactory;
    private final ServicesInjector servicesInjector;
    private final OidGenerator oidGenerator;
    private final AdapterManagerExtended adapterManager;

    private boolean dirtiableSupport;

    /**
     * Injected using setter-based injection.
     */
    private SpecificationLoader specificationLoader;
    /**
     * Injected using setter-based injection.
     */
    private IsisTransactionManager transactionManager;

    
    private static enum State {
        NOT_INITIALIZED,
        OPEN,
        CLOSED
    }
    private State state;
    
    public PersistenceSessionAbstract(
            final PersistenceSessionFactory persistenceSessionFactory, 
            final AdapterFactory adapterFactory, 
            final ObjectFactory objectFactory, 
            final ServicesInjector servicesInjector, 
            final OidGenerator oidGenerator, 
            final AdapterManagerExtended identityMap) {

        ensureThatArg(persistenceSessionFactory, is(not(nullValue())), "persistence session factory required");
        
        ensureThatArg(adapterFactory, is(not(nullValue())), "adapter factory required");
        ensureThatArg(objectFactory, is(not(nullValue())), "object factory required");
        ensureThatArg(servicesInjector, is(not(nullValue())), "services injector required");
        ensureThatArg(oidGenerator, is(not(nullValue())), "OID generator required");
        ensureThatArg(identityMap, is(not(nullValue())), "identity map required");

        // owning, application scope
        this.persistenceSessionFactory = persistenceSessionFactory;
        
        // session scope
        this.adapterFactory = adapterFactory;
        this.objectFactory = objectFactory;
        this.servicesInjector = servicesInjector;
        this.oidGenerator = oidGenerator;
        this.adapterManager = identityMap;

        setState(State.NOT_INITIALIZED);
    }


    
    // ///////////////////////////////////////////////////////////////////////////
    // PersistenceSessionFactory
    // ///////////////////////////////////////////////////////////////////////////
    
    public PersistenceSessionFactory getPersistenceSessionFactory() {
        return persistenceSessionFactory;
    }

    
    // ///////////////////////////////////////////////////////////////////////////
    // open, close
    // ///////////////////////////////////////////////////////////////////////////


    /**
     * Injects components, calls {@link #doOpen()}, and then creates service adapters.
     * 
     * @see #doOpen()
     */
    public final void open() {
        ensureNotOpened();
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("opening " + this);
        }
        
        // injected via setters
        ensureThatState(specificationLoader, is(not(nullValue())), "SpecificationLoader missing");
        ensureThatState(transactionManager, is(not(nullValue())), "TransactionManager missing");
        
        // inject any required dependencies into object factory
        this.injectInto(objectFactory);
        specificationLoader.injectInto(objectFactory);
        servicesInjector.injectInto(objectFactory);

        // wire dependencies into identityMap
        adapterFactory.injectInto(adapterManager);
        specificationLoader.injectInto(adapterManager);
        oidGenerator.injectInto(adapterManager);
        servicesInjector.injectInto(adapterManager);

        // wire dependencies into oid generator
        specificationLoader.injectInto(oidGenerator);
        
        servicesInjector.open();
        adapterFactory.open();
        objectFactory.open();
        adapterManager.open();
        oidGenerator.open();

        doOpen();
        createServiceAdapters();
        
        setState(State.OPEN);
        doOpened();
    }


    /**
     * Calls {@link #doClose()}, then closes all components.
     * 
     * @see #doClose()
     */
    public final void close() {
        if (getState() == State.CLOSED) {
            // nothing to do
            return;
        }
       
        if (LOG.isInfoEnabled()) {
            LOG.info("closing " + this);
        }

        doClose();
        
        adapterManager.close();
        servicesInjector.close();
        objectFactory.close();
        adapterFactory.close();
        oidGenerator.close();
        
        setState(State.CLOSED);
    }



    /**
     * Optional hook method called prior to creating service adapters for subclass 
     * to initialize its components.
     */
    protected void doOpen() {}

    /**
     * Optional hook method for any final processing from {@link #open()}.
     */
    protected void doOpened() {}


    /**
     * Optional hook method to close subclass' components.
     */
    protected void doClose() {}






    /**
     * Creates (or recreates following a {@link #testReset()}) {@link ObjectAdapter adapters}
     * for the {@link #serviceList}.
     */
    private void createServiceAdapters() {
        getTransactionManager().startTransaction();
        for (final Object service : servicesInjector.getRegisteredServices()) {
            ObjectSpecification serviceNoSpec = specificationLoader.loadSpecification(service.getClass());
            if (serviceNoSpec instanceof IntrospectableSpecification) {
                IntrospectableSpecification introspectableSpecification = (IntrospectableSpecification) serviceNoSpec;
                introspectableSpecification.markAsService();
            }
            final String serviceId = ServiceUtil.id(service);
            final Oid existingOid = getOidForService(serviceId);
            ObjectAdapter adapter;
            if (existingOid == null) {
                adapter = getAdapterManager().adapterFor(service);
            } else {
                adapter = getAdapterManager().recreateRootAdapter(existingOid, service);
            }
            
            if (adapter.getOid().isTransient()) {
            	adapterManager.remapAsPersistent(adapter);
            }
            
            if (adapter.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
            	adapter.changeState(ResolveState.RESOLVING);
            	adapter.changeState(ResolveState.RESOLVED);
            }
            if (existingOid == null) {
                final Oid persistentOid = adapter.getOid();
                registerService(serviceId, persistentOid);
            }
            
        }
        getTransactionManager().endTransaction();
    }

    
    private State getState() {
        return state;
    }
    private void setState(State state) {
        this.state = state;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // State Management
    // ///////////////////////////////////////////////////////////////////////////

    protected void ensureNotOpened() {
        if (getState() != State.NOT_INITIALIZED) {
            throw new IllegalStateException("Persistence session has already been initialized");
        }
    }
    
    protected void ensureOpen() {
        if (getState() != State.OPEN) {
            throw new IllegalStateException("Persistence session is not open");
        }
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // shutdown, reset
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * For testing purposes only.
     */
    public void testReset() {
    }
    




    // ///////////////////////////////////////////////////////////////////////////
    // Factory (for transient instance)
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a root or standalone {@link ObjectAdapter adapter}.
     * 
     * <p>
     * The returned object will be initialied (had the relevant callback lifecycle methods 
     * invoked).
     * 
     * <p>
     * TODO: this is the same as {@link RuntimeContextFromSession#createTransientInstance(ObjectSpecification)}; could it be unified?
     */
    public ObjectAdapter createInstance(final ObjectSpecification specification) {
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("creating transient instance of " + specification);
    	}
        final Object pojo = specification.createObject(CreationMode.INITIALIZE);
        return getAdapterManager().adapterFor(pojo);
    }

    public ObjectAdapter recreateAdapter(final Oid oid, final ObjectSpecification specification) {
        final ObjectAdapter adapterLookedUpByOid = getAdapterManager().getAdapterFor(oid);
        if (adapterLookedUpByOid != null) {
            return adapterLookedUpByOid;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("recreating adapter for Oid: " + oid + " of type " + specification);
        }
        final Object pojo = specification.createObject(CreationMode.NO_INITIALIZE);
        
        return getAdapterManager().recreateRootAdapter(oid, pojo);
    }


    public ObjectAdapter recreateAdapter(final Oid oid, final Object pojo) {
        final ObjectAdapter adapterLookedUpByOid = getAdapterManager().getAdapterFor(oid);
        if (adapterLookedUpByOid != null) {
            return adapterLookedUpByOid;
        }

        final ObjectAdapter adapterLookedUpByPojo = getAdapterManager().getAdapterFor(pojo);
        if (adapterLookedUpByPojo != null) {
            return adapterLookedUpByPojo;
        }

        if (LOG.isDebugEnabled()) {
            // don't touch pojo in case cause it to resolve.
            LOG.debug("recreating adapter for Oid: " + oid + " for provided pojo ");
        }
        return getAdapterManager().recreateRootAdapter(oid, pojo);
    }



    
    // ///////////////////////////////////////////////////////////////////////////
    // reload
    // ///////////////////////////////////////////////////////////////////////////

    public ObjectAdapter reload(Oid oid) {
        ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        reload(adapter);
        return adapter;
    }
    

    public abstract void reload(ObjectAdapter adapter);
    
    
    // ///////////////////////////////////////////////////////////////////////////
    // findInstances, getInstances
    // ///////////////////////////////////////////////////////////////////////////

    public <T> ObjectAdapter findInstances(Query<T> query, QueryCardinality cardinality) {
    	final PersistenceQuery persistenceQuery = createPersistenceQueryFor(query, cardinality);
        if (persistenceQuery == null) {
            throw new IllegalArgumentException("Unknown query type: " + query.getDescription());
        }
        return findInstances(persistenceQuery);
    }

    public ObjectAdapter findInstances(PersistenceQuery persistenceQuery) {
        final ObjectAdapter[] instances = getInstances(persistenceQuery);
        final ObjectSpecification specification = persistenceQuery.getSpecification();
        final ObjectList results = new ObjectList(specification, instances);
        return getAdapterManager().adapterFor(results);
    }

    /**
     * Converts the {@link Query applib representation of a query} into the
     * {@link PersistenceQuery NOF-internal representation}. 
     */
	protected final PersistenceQuery createPersistenceQueryFor(Query<?> query, QueryCardinality cardinality) {
		LOG.info("createPersistenceQueryFor: " + query.getDescription());
		ObjectSpecification noSpec = specFor(query);
	    if (query instanceof QueryFindAllInstances) {
			return new PersistenceQueryFindAllInstances(noSpec);
	    }
	    if (query instanceof QueryFindByTitle) {
			QueryFindByTitle<?> queryByTitle = (QueryFindByTitle<?>) query;
	    	String title = queryByTitle.getTitle();
			return new PersistenceQueryFindByTitle(noSpec, title);
	    }
	    if (query instanceof QueryFindByPattern) {
			QueryFindByPattern<?> queryByPattern = (QueryFindByPattern<?>) query;
			Object pattern = queryByPattern.getPattern();
			ObjectAdapter patternAdapter = getAdapterManager().adapterFor(pattern);
			return new PersistenceQueryFindByPattern(noSpec, patternAdapter);
	    }
	    if (query instanceof QueryDefault) {
			QueryDefault<?> queryDefault = (QueryDefault<?>) query;
			String queryName = queryDefault.getQueryName();
			Map<String, ObjectAdapter> argumentsAdaptersByParameterName = wrap(queryDefault.getArgumentsByParameterName());
			return new PersistenceQueryFindUsingApplibQueryDefault(noSpec, queryName, argumentsAdaptersByParameterName, cardinality);
	    }
	    // fallback; generic serializable applib query.
	    return new PersistenceQueryFindUsingApplibQuerySerializable(noSpec, query, cardinality);
	}

	private ObjectSpecification specFor(Query<?> query) {
		return getSpecificationLoader().loadSpecification(query.getResultType());
	}

	/**
	 * Converts a map of pojos keyed by string to a map of adapters keyed by the
	 * same strings.
	 */
	private Map<String, ObjectAdapter> wrap(
			Map<String, Object> argumentsByParameterName) {
		Map<String, ObjectAdapter> argumentsAdaptersByParameterName = new HashMap<String, ObjectAdapter>();
		for (Map.Entry<String,Object> entry: argumentsByParameterName.entrySet()) {
		    String parameterName = entry.getKey();
			Object argument = argumentsByParameterName.get(parameterName);
			ObjectAdapter argumentAdapter = argument != null? getAdapterManager().adapterFor(argument): null;
			argumentsAdaptersByParameterName.put(parameterName, argumentAdapter);
		}
		return argumentsAdaptersByParameterName;
	}


	protected abstract ObjectAdapter[] getInstances(final PersistenceQuery persistenceQuery);

    
    // ///////////////////////////////////////////////////////////////////////////
    // Manual dirtying support
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * @see #setDirtiableSupport(boolean)
     */
    public boolean isCheckObjectsForDirtyFlag() {
        return dirtiableSupport;
    }

    /**
     * Whether to notice {@link Dirtiable manually-dirtied} objects.
     */
    public void setDirtiableSupport(final boolean checkObjectsForDirtyFlag) {
        this.dirtiableSupport = checkObjectsForDirtyFlag;
    }

    /**
     * If {@link #isCheckObjectsForDirtyFlag() enabled}, will mark as {@link #objectChanged(ObjectAdapter) changed}
     * any {@link Dirtiable} objects that have manually been {@link Dirtiable#markDirty(ObjectAdapter) marked as dirty}. 
     */
    public void objectChangedAllDirty() {
        if (!dirtiableSupport) {
            return;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("marking as changed any objects that have been manually set as dirty");
        }
        for (final ObjectAdapter adapter: getAdapterManager()) {
            if (adapter.getSpecification().isDirty(adapter)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  found dirty object " + adapter);
                }
                objectChanged(adapter);
                adapter.getSpecification().clearDirty(adapter);
            }
        }
    }

    /**
     * Set as {@link Dirtiable#clearDirty(ObjectAdapter) clean} any {@link Dirtiable} objects.
     */
    public synchronized void clearAllDirty() {
        if (!isCheckObjectsForDirtyFlag()) {
            return;
        } 
        if (LOG.isDebugEnabled()) {
            LOG.debug("cleaning any manually dirtied objects");
        }
        
        for (final ObjectAdapter object: getAdapterManager()) {
            if (object.getSpecification().isDirty(object)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  found dirty object " + object);
                }
                object.getSpecification().clearDirty(object);
            }
        }
        
    }

    // ///////////////////////////////////////////////////////////////////////////
    // AdaptedServiceManager
    // ///////////////////////////////////////////////////////////////////////////


    /**
     * Returns the OID for the adapted service. This allows a service object to be given the same OID that it
     * had when it was created in a different session.
     */
    protected abstract Oid getOidForService(String name);

    /**
     * Registers the specified service as having the specified OID.
     */
    protected abstract void registerService(String name, Oid oid);

    public ObjectAdapter getService(final String id) {
        for (final Object service : servicesInjector.getRegisteredServices()) {
            // TODO this (ServiceUtil) uses reflection to access the service object; it should use the
            // reflector, ie call allServices first and use the returned array
            if (id.equals(ServiceUtil.id(service))) {
                return getService(service);
            }
        }
        return null;
    }

    // REVIEW why does this get called multiple times when starting up 
    public List<ObjectAdapter> getServices() {
        List<Object> services = servicesInjector.getRegisteredServices();
        List<ObjectAdapter> serviceAdapters = new ArrayList<ObjectAdapter>();
        for (Object service : services) {
            serviceAdapters.add(getService(service));
        }
        return serviceAdapters;
    }

    private ObjectAdapter getService(final Object service) {
        final Oid oid = getOidForService(ServiceUtil.id(service));
        return recreateAdapterForExistingService(oid, service);
    }

    /**
     * Has any services.
     */
    public boolean hasServices() {
        return servicesInjector.getRegisteredServices().size() > 0;
    }

    private ObjectAdapter recreateAdapterForExistingService(final Oid oid, final Object service) {
        final ObjectAdapter adapter = getAdapterManager().recreateRootAdapter(oid, service);
        if (adapter.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
            adapter.changeState(ResolveState.RESOLVING);
            adapter.changeState(ResolveState.RESOLVED);
        }
        return adapter;
    }


    // ////////////////////////////////////////////////////////////////////
    // Helpers
    // ////////////////////////////////////////////////////////////////////
    
	protected boolean isImmutable(final ObjectAdapter adapter) {
		final ObjectSpecification noSpec = adapter.getSpecification();
		return SpecificationFacets.isAlwaysImmutable(noSpec) || 
			(SpecificationFacets.isImmutableOncePersisted(noSpec) && 
			 adapter.isPersistent());
	}


    // ////////////////////////////////////////////////////////////////////
    // injectInto
    // ////////////////////////////////////////////////////////////////////

    public void injectInto(Object candidate) {
        if (PersistenceSessionAware.class.isAssignableFrom(candidate.getClass())) {
            PersistenceSessionAware cast = PersistenceSessionAware.class.cast(candidate);
            cast.setPersistenceSession(this);
        }
        if (PersistenceSessionHydratorAware.class.isAssignableFrom(candidate.getClass())) {
            PersistenceSessionHydratorAware cast = PersistenceSessionHydratorAware.class.cast(candidate);
            cast.setHydrator(this);
        }
    }

    
    // ///////////////////////////////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////////////////////////////

    public void debugData(final DebugString debug) {
        debug.appendTitle(getClass().getName());
        debug.appendln("container", servicesInjector);
        debug.appendln();

        adapterManager.debugData(debug);
        debug.appendln();

        debug.appendln("manually dirtiable support (isDirty flag)?", dirtiableSupport);

        debug.appendTitle("OID Generator");
        oidGenerator.debugData(debug);
        debug.appendln();

        debug.appendTitle("Services");
        for (final Object service : servicesInjector.getRegisteredServices()) {
            final String id = ServiceUtil.id(service);
            final String serviceClassName = service.getClass().getName();
            Oid oidForService = getOidForService(id);
            String serviceId = id + (id.equals(serviceClassName) ? "" : " (" + serviceClassName + ")");
            debug.appendln(oidForService != null? oidForService.toString(): "[NULL]", serviceId);
        }
        debug.appendln();

    }


    // ///////////////////////////////////////////////////////////////////////////
    // Dependencies (injected in constructor, possibly implicitly)
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Injected in constructor.
     */
    public final AdapterFactory getAdapterFactory() {
        return adapterFactory;
    }
    
    /**
     * Injected in constructor.
     */
    public final OidGenerator getOidGenerator() {
        return oidGenerator;
    }

    /**
     * Injected in constructor.
     */
    public final AdapterManagerExtended getAdapterManager() {
        return adapterManager;
    }


    /**
     * The {@link ServicesInjector}. 
     */
    public ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

    /**
     * Obtained indirectly from the injected reflector.
     */
    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }


    // ///////////////////////////////////////////////////////////////////////////
    // Dependencies (injected)
    // ///////////////////////////////////////////////////////////////////////////

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    
    /**
     * Injects the {@link SpecificationLoader}
     */
    public void setSpecificationLoader(final SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }




    public void setTransactionManager(final IsisTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public IsisTransactionManager getTransactionManager() {
        return transactionManager;
    }



}
