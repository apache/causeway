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


package org.apache.isis.runtime.objectstore.inmemory.internal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.core.metamodel.adapter.oid.Oid;

@RunWith(JMock.class)
public class ObjectStorePersistedObjectsDefault_services {

	private Mockery context = new JUnit4Mockery();
	
	private ObjectStorePersistedObjectsDefault persistedObjects;

	private Oid mockOidForFooService, mockOidForBarService;
	
	@Before
	public void setUp() throws Exception {
		persistedObjects = new ObjectStorePersistedObjectsDefault();
		mockOidForFooService = context.mock(Oid.class, "fooServiceOid");
		mockOidForBarService = context.mock(Oid.class, "barServiceOid");
	}
	
	@Test
	public void noServicesInitially() throws Exception {
		Oid service = persistedObjects.getService("fooService");
		assertThat(service, is(nullValue()));
	}

	@Test
	public void registerServicesMakesAvailable() throws Exception {
		persistedObjects.registerService("fooService", mockOidForFooService);
		
		Oid service = persistedObjects.getService("fooService");
		assertThat(service, is(mockOidForFooService));
	}

	@Test
	public void registerServicesWhenMoreThanOnePullsOutTheCorrectOne() throws Exception {
		persistedObjects.registerService("fooService", mockOidForFooService);
		persistedObjects.registerService("barService", mockOidForBarService);
		
		Oid service = persistedObjects.getService("fooService");
		assertThat(service, is(mockOidForFooService));
	}

}
