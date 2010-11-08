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

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.common.jmock.AbstractJMockForInterfacesTest;
import org.apache.isis.core.commons.guice.ScopeBindingModule;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Scopes;


public class GuiceTest extends AbstractJMockForInterfacesTest {

	private Scope mockSessionScope;

	@Before
	public void setUp() throws Exception {
		mockSessionScope = context.mock(Scope.class, "sessionScope");
	}

	@Test
	public void canCreateGlobalComponent() throws Exception {
		Injector injector = Guice.createInjector(new GlobalModule());
		SomeSingletonComponent singletonComponent = injector.getInstance(SomeSingletonComponent.class);
		assertThat(singletonComponent, is(notNullValue()));
	}

	@Test
	public void singletonComponentReused() throws Exception {
		Injector injector = Guice.createInjector(new GlobalModule());
		SomeSingletonComponent singletonComponent = injector.getInstance(SomeSingletonComponent.class);
		assertThat(singletonComponent, is(notNullValue()));
		SomeSingletonComponent singletonComponent2 = injector.getInstance(SomeSingletonComponent.class);
		assertThat(singletonComponent2, is(singletonComponent));
	}

	@Test(expected=CreationException.class)
	public void cannotUseScopedComponentIfItsScopeIsNotBound() throws Exception {
		expectSessionScopeToActLike(Scopes.NO_SCOPE);
		Guice.createInjector(new GlobalModule(), new SomeSessionComponentModule());
	}

	@Test
	public void sessionComponentWhenNotInScope() throws Exception {
		expectSessionScopeToActLike(Scopes.NO_SCOPE);
		Injector injector = Guice.createInjector(new GlobalModule(), new ScopeBindingModule(mockSessionScope), new SomeSessionComponentModule());
		SomeSessionComponent sessionComponent = injector.getInstance(SomeSessionComponent.class);
		assertThat(sessionComponent, is(notNullValue()));
	}

	@Test
	public void sessionComponentWhenInScope() throws Exception {
		expectSessionScopeToActLike(Scopes.SINGLETON);
		Injector injector = Guice.createInjector(new GlobalModule(), new ScopeBindingModule(mockSessionScope), new SomeSessionComponentModule());
		SomeSessionComponent sessionComponent = injector.getInstance(SomeSessionComponent.class);
		assertThat(sessionComponent, is(notNullValue()));

		SomeSessionComponent sessionComponent2 = injector.getInstance(SomeSessionComponent.class);
		assertThat(sessionComponent2, is(sameInstance(sessionComponent)));
	}

	@Test
	public void sessionComponentInjectedWithGlobalComponent() throws Exception {
		expectSessionScopeToActLike(Scopes.SINGLETON);
		Injector injector = Guice.createInjector(new GlobalModule(), new ScopeBindingModule(mockSessionScope), new SomeSessionComponentModule());

		SomeSessionComponent sessionComponent = injector.getInstance(SomeSessionComponent.class);
		assertThat(sessionComponent, is(instanceOf(SomeSessionComponentImpl.class)));

		SomeSessionComponentImpl sessionComponentImpl = (SomeSessionComponentImpl) sessionComponent;

		assertThat(sessionComponentImpl.getSomeSingletonComponent(), is(notNullValue()));
	}

	@Test(expected=CreationException.class)
	public void mustBindScopeIfThereIsAComponentDefinedForThatScope() throws Exception {
		Injector injector = Guice.createInjector(new GlobalModule(), new SomeSessionComponentModule());

		// should throw exception
		injector.getInstance(SomeSessionComponent.class);
	}

	@Test(expected=CreationException.class)
	public void mustBindGlobalComponentIfThereIsADependency() throws Exception {
		expectSessionScopeToActLike(Scopes.SINGLETON);
		Injector injector = Guice.createInjector(new ScopeBindingModule(mockSessionScope), new SomeSessionComponentModule());

		// should throw exception
		injector.getInstance(SomeSessionComponent.class);
	}

	@SuppressWarnings("unused")
	private void ignoringSessionScope() {
		context.checking(new Expectations() {
			{
				ignoring(mockSessionScope);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void expectSessionScopeToActLike(final Scope actLike) {
		context.checking(new Expectations() {
			{
				allowing(mockSessionScope).scope(with(any(Key.class)), with(any(Provider.class)));
				will(ScopeAction.actLike(actLike));
			}
		});
	}

}

