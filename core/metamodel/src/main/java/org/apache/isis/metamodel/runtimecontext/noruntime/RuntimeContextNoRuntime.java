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


package org.apache.isis.metamodel.runtimecontext.noruntime;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.query.Query;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.runtimecontext.ObjectInstantiationException;
import org.apache.isis.metamodel.runtimecontext.RuntimeContextAbstract;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.identifier.Identified;

public class RuntimeContextNoRuntime extends RuntimeContextAbstract {

	public RuntimeContextNoRuntime() {
	}
	

	/////////////////////////////////////////////
	// AuthenticationSession
	/////////////////////////////////////////////

	public AuthenticationSession getAuthenticationSession() {
		return new AuthenticationSessionNoRuntime();
	}

	/////////////////////////////////////////////
	// getAdapterFor, adapterFor
	/////////////////////////////////////////////

	public ObjectAdapter getAdapterFor(Object pojo) {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}
	
	public ObjectAdapter getAdapterFor(Oid oid) {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}
	
	public ObjectAdapter adapterFor(Object pattern) {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}
	
	public ObjectAdapter adapterFor(Object pojo, ObjectAdapter ownerAdapter,
			Identified identified) {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}

	
	/////////////////////////////////////////////
	// createTransientInstance, instantiate
	/////////////////////////////////////////////

	public ObjectAdapter createTransientInstance(ObjectSpecification spec) {
		throw new UnsupportedOperationException(
			"Not supported by this implementation of RuntimeContext");
	}

	public Object instantiate(Class<?> cls) throws ObjectInstantiationException {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}

	
	/////////////////////////////////////////////
	// resolve, objectChanged
	/////////////////////////////////////////////

	public void resolve(Object parent) {
		throw new UnsupportedOperationException(
			"Not supported by this implementation of RuntimeContext");
	}

	public void resolve(Object parent, Object field) {
		throw new UnsupportedOperationException(
			"Not supported by this implementation of RuntimeContext");
	}

	public void objectChanged(ObjectAdapter inObject) {
		throw new UnsupportedOperationException(
				"Not supported by this implementation of RuntimeContext");
	}

	public void objectChanged(Object object) {
		throw new UnsupportedOperationException(
			"Not supported by this implementation of RuntimeContext");
	}

	
	/////////////////////////////////////////////
	// makePersistent, remove
	/////////////////////////////////////////////
	
	public void makePersistent(ObjectAdapter adapter) {
		throw new UnsupportedOperationException(
			"Not supported by this implementation of RuntimeContext");
	}

	public void remove(ObjectAdapter adapter) {
		throw new UnsupportedOperationException(
			"Not supported by this implementation of RuntimeContext");
	}

	/////////////////////////////////////////////
	// flush, commit
	/////////////////////////////////////////////
	
	public boolean flush() {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}

	public void commit() {
		throw new UnsupportedOperationException(
			"Not supported by this implementation of RuntimeContext");
	}

	
	/////////////////////////////////////////////
	// allInstances, allMatching*
	/////////////////////////////////////////////

	public List<ObjectAdapter> allInstances(ObjectSpecification noSpec) {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}

	public <T> List<ObjectAdapter> allMatchingQuery(Query<T> query) {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}

	public <T> ObjectAdapter firstMatchingQuery(Query<T> query) {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}

    ////////////////////////////////////////////////////////////////////
    // info, warn, error messages
    ////////////////////////////////////////////////////////////////////


	public void informUser(String message) {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}

	public void warnUser(String message) {
		throw new UnsupportedOperationException(
			"Not supported by this implementation of RuntimeContext");
	}

	public void raiseError(String message) {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}

	
	/////////////////////////////////////////////
	// getServices, injectDependenciesInto
	/////////////////////////////////////////////

	/**
	 * Just returns an empty array.
	 */
	public List<ObjectAdapter> getServices() {
		return new ArrayList<ObjectAdapter>();
	}


	/**
	 * Unlike most of the methods in this implementation, does nothing (because
	 * this will always be called, even in a no-runtime context).
	 */
	public void injectDependenciesInto(Object object) {
		// does nothing.
	}



}
