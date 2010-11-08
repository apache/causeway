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


package org.apache.isis.metamodel.runtimecontext;

import java.util.List;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.identifier.Identified;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

/**
 * Decouples the metamodel from a runtime.
 * 
 */
public interface RuntimeContext extends Injectable {


	/////////////////////////////////////////////
	// SpecificationLoader
	/////////////////////////////////////////////

	public SpecificationLoader getSpecificationLoader();


	/////////////////////////////////////////////
	// AuthenticationSession
	/////////////////////////////////////////////

	/**
	 * Provided by <tt>AuthenticationManager</tt> when used by framework.
	 */
	AuthenticationSession getAuthenticationSession();

	
	
	/////////////////////////////////////////////
	// getAdapterFor, adapterFor
	/////////////////////////////////////////////

	/**
	 * Provided by the <tt>AdapterManager</tt> when used by framework.
	 */
	ObjectAdapter getAdapterFor(Oid oid);

	/**
	 * Provided by the <tt>AdapterManager</tt> when used by framework.
	 */
	ObjectAdapter getAdapterFor(Object domainObject);

	/**
	 * Provided by the <tt>AdapterManager</tt> when used by framework.
	 */
	ObjectAdapter adapterFor(Object domainObject);

	/**
	 * Provided by the <tt>AdapterManager</tt> when used by framework.
	 */
	ObjectAdapter adapterFor(Object domainObject, ObjectAdapter ownerAdapter, Identified identified);


	/////////////////////////////////////////////
	// createTransientInstance, instantiate
	/////////////////////////////////////////////
	
	/**
	 * Provided by the <tt>PersistenceSession</tt> when used by framework.
	 */
	ObjectAdapter createTransientInstance(ObjectSpecification spec);
	
	/**
	 * Provided by the <tt>ObjectFactory</tt> when used by framework.
	 */
	Object instantiate(Class<?> cls) throws ObjectInstantiationException;
	

	/////////////////////////////////////////////
	// resolve, objectChanged
	/////////////////////////////////////////////

	/**
	 * Provided by <tt>PersistenceSession</tt> when used by framework.
	 */
	void resolve(Object parent);

	/**
	 * Provided by <tt>PersistenceSession</tt> when used by framework.
	 */
	void resolve(Object parent, Object field);

	/**
	 * Provided by <tt>PersistenceSession</tt> when used by framework.
	 */
	void objectChanged(ObjectAdapter adapter);

	/**
	 * TODO: combined with {@link #objectChanged(ObjectAdapter)}.
	 */
	void objectChanged(Object object);

	
	/////////////////////////////////////////////
	// makePersistent, remove
	/////////////////////////////////////////////

	/**
	 * Provided by the <tt>PersistenceSession</tt> when used by framework.
	 */
	void makePersistent(ObjectAdapter adapter);

	/**
	 * Provided by <tt>UpdateNotifier</tt> and <tt>PersistenceSession</tt>
	 * when used by framework.
	 */
	void remove(ObjectAdapter adapter);


	/////////////////////////////////////////////
	// flush, commit
	/////////////////////////////////////////////

	/**
	 * Provided by <tt>TransactionManager</tt> when used by framework.
	 */
	boolean flush();

	/**
	 * Provided by <tt>TransactionManager</tt> when used by framework.
	 */
	void commit();



	/////////////////////////////////////////////
	// *MatchingQuery
	/////////////////////////////////////////////

	/**
	 * Provided by <tt>PersistenceSession</tt> when used by framework.
	 */
	public <T> List<ObjectAdapter> allMatchingQuery(Query<T> query);


	/**
	 * Provided by <tt>PersistenceSession</tt> when used by framework.
	 */
	public <T> ObjectAdapter firstMatchingQuery(Query<T> query);
	

    ////////////////////////////////////////////////////////////////////
    // info, warn, error messages
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * Provided by <tt>MessageBroker</tt> when used by framework.
	 */
	void informUser(String message);

	/**
	 * Provided by <tt>MessageBroker</tt> when used by framework.
	 */
	void warnUser(String message);

	void raiseError(String message);

	
	
	/////////////////////////////////////////////
	// getServices, injectDependenciesInto
	/////////////////////////////////////////////

	/**
	 * Provided by <tt>PersistenceSession</tt> when used by framework.
	 */
	List<ObjectAdapter> getServices();

	/**
	 * Provided by the <tt>ServicesInjectorDefault</tt> when used by framework.
	 */
	void injectDependenciesInto(final Object domainObject);


	void setContainer(DomainObjectContainer container);


    ////////////////////////////////////////////////////////////////////
    // properties
    ////////////////////////////////////////////////////////////////////
	
    String getProperty(String name);


    List<String> getPropertyNames();






}
