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


package org.apache.isis.common.jmock;

import org.hamcrest.Description;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.api.Expectation;
import org.jmock.api.ExpectationErrorTranslator;
import org.jmock.api.Imposteriser;
import org.jmock.api.MockObjectNamingScheme;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.internal.ExpectationBuilder;

/**
 * Simply delegates to underlying {@link Mockery}.
 *
 * <p>
 * Designed to make it easy to write custom {@link Mockery} implementations, while
 * delegating most of the actual work to an existing {@link Mockery} implementation.
 */
public class DelegatingMockery extends ConvenienceMockery {
	
	private Mockery underlying;
	
	public DelegatingMockery(Mockery underlying) {
		this.underlying = underlying; 
	}
	
	public DelegatingMockery() {
		this(new JUnit4Mockery()); 
	}
	
	@Override
	public void addExpectation(Expectation expectation) {
		underlying.addExpectation(expectation);
	}
	
	@Override
	public void assertIsSatisfied() {
		underlying.assertIsSatisfied();
	}
	
	@Override
	public void checking(ExpectationBuilder expectations) {
		underlying.checking(expectations);
	}
	
	@Override
	public void describeTo(Description description) {
		underlying.describeTo(description);
	}

	@Override
	public <T> T mock(Class<T> typeToMock) {
		return underlying.mock(typeToMock);
	}
	
	@Override
	public <T> T mock(Class<T> typeToMock, String name) {
		return underlying.mock(typeToMock, name);
	}
	
	@Override
	public Sequence sequence(String name) {
		return underlying.sequence(name);
	}
	
	@Override
	public void setDefaultResultForType(Class<?> type, Object result) {
		underlying.setDefaultResultForType(type, result);
	}
	
	@Override
	public void setExpectationErrorTranslator(
			ExpectationErrorTranslator expectationErrorTranslator) {
		underlying.setExpectationErrorTranslator(expectationErrorTranslator);
	}
	
	@Override
	public void setImposteriser(Imposteriser imposteriser) {
		underlying.setImposteriser(imposteriser);
	}
	
	@Override
	public void setNamingScheme(MockObjectNamingScheme namingScheme) {
		underlying.setNamingScheme(namingScheme);
	}
	
	@Override
	public States states(String name) {
		return underlying.states(name);
	}
	
	
}
