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

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.persistence.query.PersistenceQueryBuiltIn;

import com.google.common.collect.Lists;

/**
 * Tested in style of <i>Working Effectively with Legacy Code</i> (Feathers)
 * and <i>Growing Object-Oriented Software</i> (Freeman &amp; Pryce).
 */
@RunWith(JMock.class)
public class ObjectStoreInstances_findInstancesAndAdd {

	private ObjectStoreInstances instances;

	private Mockery context = new JUnit4Mockery();

	private ObjectSpecification mockSpec;
	private PersistenceQueryBuiltIn mockPersistenceQueryBuiltIn;
//	private ObjectAdapter mockAdapter;
	private AuthenticationSession mockAuthSession;

	@Before
	public void setUp() throws Exception {
		mockSpec = context.mock(ObjectSpecification.class);
		mockPersistenceQueryBuiltIn = context.mock(PersistenceQueryBuiltIn.class);
		// mockAdapter = context.mock(ObjectAdapter.class);
		mockAuthSession = context.mock(AuthenticationSession.class);
		instances = new ObjectStoreInstances(mockSpec) {
			@Override
			protected AuthenticationSession getAuthenticationSession() {
				return mockAuthSession;
			}
		};
		ignoreAuthenticationSession();
	}

	private void ignoreAuthenticationSession() {
		context.checking(new Expectations() {
			{
				ignoring(mockAuthSession);
			}
		});
	}



	@Test
	public void findInstancesAndAddWhenEmpty() throws Exception {
		neverInteractsWithPersistenceQueryBuiltIn();
		List<ObjectAdapter> foundInstances = Lists.newArrayList();
		instances.findInstancesAndAdd(mockPersistenceQueryBuiltIn, foundInstances);
	}

	@Ignore // not yet implemented
	@Test
	public void findInstancesAndNotEmpty() throws Exception {

	}

	private void neverInteractsWithPersistenceQueryBuiltIn() {
		context.checking(new Expectations() {
			{
				never(mockPersistenceQueryBuiltIn);
			}
		});
	}


}
