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


package org.apache.isis.core.commons.guice;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

public class ScopeAction implements Action {

	public static ScopeAction actLike(Scope scope) {
		return new ScopeAction(scope);
	}

	protected final Scope scope;
	
	private ScopeAction(Scope scope) {
		this.scope =scope;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(Invocation invocation) throws Throwable {
		return scope.scope((Key)invocation.getParameter(0), (Provider)invocation.getParameter(1));
	}

	@Override
	public void describeTo(Description arg0) {
		arg0.appendText(scope.toString());
	}

}