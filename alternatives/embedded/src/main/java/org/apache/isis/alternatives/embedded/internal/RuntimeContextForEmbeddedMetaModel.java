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


package org.apache.isis.alternatives.embedded.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.alternatives.embedded.EmbeddedContext;
import org.apache.isis.alternatives.embedded.internal.PersistenceState;
import org.apache.isis.alternatives.embedded.internal.ServiceAdapter;
import org.apache.isis.alternatives.embedded.internal.StandaloneAdapter;
import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.runtimecontext.ObjectInstantiationException;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecification.CreationMode;
import org.apache.isis.core.metamodel.spec.identifier.Identified;

/**
 * Acts as a bridge between the {@link RuntimeContext} (as used internally
 * within the meta-model) and the {@link EmbeddedContext} 
 * provided by the embedder (which deals only with pojos).
 */
public class RuntimeContextForEmbeddedMetaModel extends RuntimeContextAbstract implements ApplicationScopedComponent {

	private final EmbeddedContext context;
	private final List<Object> services;
	private List<ObjectAdapter> serviceAdapters;
	private ServicesInjector servicesInjector;

	public RuntimeContextForEmbeddedMetaModel(
			final EmbeddedContext context, 
			final List<Object> services) {
		this.context = context;
		this.services = services;
	}


	/////////////////////////////////////////////
	// init, shutdown
	/////////////////////////////////////////////

	public void init() {
		this.serviceAdapters = adaptersFor(services);
		
		servicesInjector = new ServicesInjectorDefault();
		servicesInjector.setContainer(getContainer());
		servicesInjector.setServices(services);
	}
	

	public void shutdown() {
		// does nothing
	}


	private List<ObjectAdapter> adaptersFor(List<Object> services) {
		List<ObjectAdapter> serviceAdapters = new ArrayList<ObjectAdapter>();
		for(Object service: services) {
			ObjectSpecification spec = getSpecificationLoader().loadSpecification(service.getClass());
			serviceAdapters.add(new ServiceAdapter(spec, service));
		}
		return Collections.unmodifiableList(serviceAdapters);
	}


	/////////////////////////////////////////////
	// AuthenticationSession
	/////////////////////////////////////////////
	
	public AuthenticationSession getAuthenticationSession() {
		return context.getAuthenticationSession();
	}



	/////////////////////////////////////////////
	// getAdapterFor, adapterFor
	/////////////////////////////////////////////

	
	public ObjectAdapter adapterFor(Object domainObject) {
		ObjectSpecification domainObjectSpec = getSpecificationLoader().loadSpecification(domainObject.getClass());
		PersistenceState persistenceState = context.getPersistenceState(domainObject);
		return new StandaloneAdapter(domainObjectSpec, domainObject, persistenceState);
	}

	public ObjectAdapter adapterFor(Object domainObject, ObjectAdapter ownerAdapter, Identified identified) {
		return adapterFor(domainObject);
	}

	public ObjectAdapter getAdapterFor(Object domainObject) {
		return adapterFor(domainObject);
	}

	public ObjectAdapter getAdapterFor(Oid oid) {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}
	
	
	/////////////////////////////////////////////
	// createTransientInstance, instantiate
	/////////////////////////////////////////////
	
	public ObjectAdapter createTransientInstance(ObjectSpecification spec) {
		Object domainObject = spec.createObject(CreationMode.INITIALIZE);
		return adapterFor(domainObject);
	}

	public Object instantiate(Class<?> type) throws ObjectInstantiationException {
		return context.instantiate(type);
	}

	
	/////////////////////////////////////////////
	// resolve, objectChanged
	/////////////////////////////////////////////

	public void resolve(Object parent) {
		context.resolve(parent);
	}

	public void resolve(Object parent, Object field) {
		context.resolve(parent, field);
	}

	public void objectChanged(ObjectAdapter adapter) {
		context.objectChanged(adapter.getObject());
	}

	public void objectChanged(Object object) {
		context.objectChanged(object);
	}

	
	/////////////////////////////////////////////
	// makePersistent, remove
	/////////////////////////////////////////////


	public void makePersistent(ObjectAdapter adapter) {
		context.makePersistent(adapter.getObject());
	}

	public void remove(ObjectAdapter adapter) {
		context.remove(adapter.getObject());
	}
	
	
	/////////////////////////////////////////////
	// flush, commit
	/////////////////////////////////////////////

	public boolean flush() {
		return context.flush();
	}
	
	public void commit() {
		context.commit();
	}

	
	/////////////////////////////////////////////
	// allMatchingQuery, firstMatchingQuery
	/////////////////////////////////////////////

	public <T> List<ObjectAdapter> allMatchingQuery(Query<T> query) {
		return wrap(context.allMatchingQuery(query));
	}

	public <T> ObjectAdapter firstMatchingQuery(Query<T> query) {
		return adapterFor(context.firstMatchingQuery(query));
	}

	private List<ObjectAdapter> wrap(List<?> pojos) {
		List<ObjectAdapter> adapters = new ArrayList<ObjectAdapter>();
		for(Object pojo: pojos) {
			adapters.add(adapterFor(pojo));
		}
		return adapters;
	}


    ////////////////////////////////////////////////////////////////////
    // info, warn, error messages
    ////////////////////////////////////////////////////////////////////

	public void informUser(String message) {
		context.informUser(message);
	}

	public void warnUser(String message) {
		context.warnUser(message);
	}

	public void raiseError(String message) {
		context.raiseError(message);
	}
	

	/////////////////////////////////////////////
	// getServices, injectDependenciesInto
	/////////////////////////////////////////////
	
	/**
	 * Unmodifiable. 
	 */
	public List<ObjectAdapter> getServices() {
		return serviceAdapters;
	}

	public void injectDependenciesInto(Object domainObject) {
		if (servicesInjector == null) {
			throw new IllegalStateException("must setContainer before using this method");
		}
		servicesInjector.injectDependencies(domainObject);
	}


	public ServicesInjector getServicesInjector() {
		return servicesInjector;
	}



}
