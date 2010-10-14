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


package org.apache.isis.extensions.headless.embedded;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.extensions.headless.embedded.EmbeddedContext;
import org.apache.isis.extensions.headless.embedded.IsisMetaModel;
import org.apache.isis.extensions.headless.embedded.dom.claim.ClaimRepositoryImpl;
import org.apache.isis.extensions.headless.embedded.dom.employee.EmployeeRepositoryImpl;


@RunWith(JMock.class)
public class GivenMetaModelWhenInitialized {
	
	private Mockery mockery = new JUnit4Mockery();
	
	private EmbeddedContext mockContext;
	
	private IsisMetaModel metaModel;
	
	
	@Before
	public void setUp() {
		mockContext = mockery.mock(EmbeddedContext.class);

		metaModel = new IsisMetaModel(mockContext, EmployeeRepositoryImpl.class, ClaimRepositoryImpl.class);
		metaModel.init();
	}
	
	@Test
	public void shouldBeAbleToGetViewer() {
		assertThat(metaModel.getViewer(), is(notNullValue()));
	}

}
