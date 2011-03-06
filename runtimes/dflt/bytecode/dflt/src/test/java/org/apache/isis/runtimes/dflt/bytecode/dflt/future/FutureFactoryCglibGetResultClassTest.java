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


package org.apache.isis.runtimes.dflt.bytecode.dflt.future;

import java.io.InputStream;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.core.commons.futures.FutureResultFactory;
import org.apache.isis.runtimes.dflt.bytecode.dflt.future.FutureFactoryCglib;


@RunWith(JMock.class)
public class FutureFactoryCglibGetResultClassTest {

	private Mockery context = new JUnit4Mockery();
	
	private FutureFactoryCglib futureFactory;

	private FutureResultFactory<?> mockResultFactory;
	

	@Before
	public void setUp() {
		futureFactory = new FutureFactoryCglib();
		mockResultFactory = context.mock(FutureResultFactory.class);
	}

	@After
	public void tearDown() {
		futureFactory = null;
	}
	

	@Test
	public void canCreateFutureForInterface() throws Exception {
		context.checking(new Expectations() {
			{
				one(mockResultFactory).getResultClass();
				will(returnValue(Comparable.class));
			}
		});
		
		futureFactory.createFuture(mockResultFactory);
	}

	@Test
	public void canCreateFutureForAbstractClass() throws Exception {
		context.checking(new Expectations() {
			{
				one(mockResultFactory).getResultClass();
				will(returnValue(InputStream.class));
			}
		});
		futureFactory.createFuture(mockResultFactory);
	}

	@Test(expected=IllegalArgumentException.class)
	public void cannotCreateLazyForConcreteClass() throws Exception {
		context.checking(new Expectations() {
			{
				one(mockResultFactory).getResultClass();
				will(returnValue(String.class));
			}
		});
		futureFactory.createFuture(mockResultFactory);
	}


}
