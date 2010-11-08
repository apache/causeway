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


package org.apache.isis.runtime.persistence.internal;

import java.util.List;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.query.Query;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.identifier.Identified;
import org.apache.isis.core.metamodel.util.CollectionFacetUtils;
import org.apache.isis.metamodel.runtimecontext.ObjectInstantiationException;
import org.apache.isis.metamodel.runtimecontext.RuntimeContextAbstract;
import org.apache.isis.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.persistence.container.DomainObjectContainerObjectChanged;
import org.apache.isis.runtime.persistence.container.DomainObjectContainerResolve;
import org.apache.isis.runtime.session.IsisSession;
import org.apache.isis.runtime.transaction.IsisTransactionManager;
import org.apache.isis.runtime.transaction.messagebroker.MessageBroker;
import org.apache.isis.runtime.transaction.updatenotifier.UpdateNotifier;

/**
 * Provides services to the metamodel based on the currently running
 * {@link IsisSession session} (primarily the {@link PersistenceSession}).
 */
public class RuntimeContextFromSession extends RuntimeContextAbstract {


    ////////////////////////////////////////////////////////////////////
	// AuthenticationSession
    ////////////////////////////////////////////////////////////////////

	public AuthenticationSession getAuthenticationSession() {
		return IsisContext.getAuthenticationSession();
	}

    ////////////////////////////////////////////////////////////////////
	// getAdapterFor, adapterFor
    ////////////////////////////////////////////////////////////////////

	public ObjectAdapter getAdapterFor(Object pojo) {
		return getAdapterManager().getAdapterFor(pojo);
	}

	public ObjectAdapter getAdapterFor(Oid oid) {
		return getAdapterManager().getAdapterFor(oid);
	}

	public ObjectAdapter adapterFor(Object pojo) {
		return getAdapterManager().adapterFor(pojo);
	}

	public ObjectAdapter adapterFor(Object pojo, ObjectAdapter ownerAdapter, Identified identified) {
		return getAdapterManager().adapterFor(pojo, ownerAdapter, identified);
	}


    ////////////////////////////////////////////////////////////////////
	// createTransientInstance, instantiate
    ////////////////////////////////////////////////////////////////////
	
	public ObjectAdapter createTransientInstance(ObjectSpecification spec) {
        return getPersistenceSession().createInstance(spec);
	}

	public Object instantiate(Class<?> cls) throws ObjectInstantiationException {
		return getPersistenceSession().getObjectFactory().instantiate(cls);
	}

	
    ////////////////////////////////////////////////////////////////////
	// resolve, objectChanged
    ////////////////////////////////////////////////////////////////////

	public void resolve(Object parent) {
        new DomainObjectContainerResolve().resolve(parent);
	}

	public void resolve(Object parent, Object field) {
        new DomainObjectContainerResolve().resolve(parent, field);
	}

	public void objectChanged(ObjectAdapter adapter) {
		getPersistenceSession().objectChanged(adapter);
	}

	public void objectChanged(Object object) {
        new DomainObjectContainerObjectChanged().objectChanged(object);
	}

    ////////////////////////////////////////////////////////////////////
	// makePersistent, remove
    ////////////////////////////////////////////////////////////////////

	public void makePersistent(ObjectAdapter adapter) {
		getPersistenceSession().makePersistent(adapter);
	}

	public void remove(ObjectAdapter adapter) {
        getUpdateNotifier().addDisposedObject(adapter);
        getPersistenceSession().destroyObject(adapter);
	}

	
    ////////////////////////////////////////////////////////////////////
	// flush, commit
    ////////////////////////////////////////////////////////////////////

	public boolean flush() {
        return getTransactionManager().flushTransaction();
	}

	public void commit() {
		getTransactionManager().endTransaction();
	}
	

    ////////////////////////////////////////////////////////////////////
	// allInstances, allMatching*, *MatchingQuery
    ////////////////////////////////////////////////////////////////////

	public <T> List<ObjectAdapter> allMatchingQuery(Query<T> query) {
		ObjectAdapter instances = getPersistenceSession().findInstances(query, QueryCardinality.MULTIPLE);
		return CollectionFacetUtils.convertToAdapterList(instances);
	}

	public <T> ObjectAdapter firstMatchingQuery(Query<T> query) {
		ObjectAdapter instances = getPersistenceSession().findInstances(query, QueryCardinality.SINGLE);
		List<ObjectAdapter> list = CollectionFacetUtils.convertToAdapterList(instances);
		return list.size() > 0? list.get(0): null;
	}


    ////////////////////////////////////////////////////////////////////
    // info, warn, error messages
    ////////////////////////////////////////////////////////////////////

	public void informUser(String message) {
		getMessageBroker().addMessage(message);		
	}

	public void warnUser(String message) {
		getMessageBroker().addWarning(message);
	}
	
	public void raiseError(String message) {
		throw new ApplicationException(message);
	}


	/////////////////////////////////////////////
	// getServices, injectDependenciesInto
	/////////////////////////////////////////////
	
	public List<ObjectAdapter> getServices() {
		return getPersistenceSession().getServices();
	}

	public void injectDependenciesInto(Object object) {
		getPersistenceSession().getServicesInjector().injectDependencies(object);
	}

	/////////////////////////////////////////////
	// Dependencies (from context)
	/////////////////////////////////////////////
	
	private static PersistenceSession getPersistenceSession() {
		return IsisContext.getPersistenceSession();
	}

	private static AdapterManager getAdapterManager() {
		return getPersistenceSession().getAdapterManager();
	}

	private static UpdateNotifier getUpdateNotifier() {
		return IsisContext.getUpdateNotifier();
	}

    private static IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    private static MessageBroker getMessageBroker() {
        return IsisContext.getMessageBroker();
    }



}
