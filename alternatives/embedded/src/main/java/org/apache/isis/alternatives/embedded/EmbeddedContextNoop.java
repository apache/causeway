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


package org.apache.isis.alternatives.embedded;

import java.util.List;

import org.apache.isis.alternatives.embedded.internal.PersistenceState;
import org.apache.isis.applib.query.Query;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;

public class EmbeddedContextNoop implements EmbeddedContext {
	
	public AuthenticationSession getAuthenticationSession() {
		return null;
	}

	public PersistenceState getPersistenceState(Object object) {
		return null;
	}

	public Object instantiate(Class<?> type) {
		return null;
	}
	public void makePersistent(Object object) {
	}
	public void remove(Object object) {
	}


	public void resolve(Object parent) {
	}
	public void resolve(Object parent, Object field) {
	}
	public void objectChanged(Object object) {
	}

	public <T> List<T> allMatchingQuery(Query<T> query) {
		return null;
	}
	public <T> T firstMatchingQuery(Query<T> query) {
		return null;
	}

	
	public void commit() {
	}
	public boolean flush() {
		return false;
	}
	
	public void informUser(String message) {
	}
	public void warnUser(String message) {
	}
	public void raiseError(String message) {
	}

}
