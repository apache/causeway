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


package org.apache.isis.extensions.wicket.viewer;

import org.apache.isis.commons.jmock.MockFixture;
import org.apache.isis.commons.jmock.MockFixtureAdapter;
import org.apache.isis.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtime.authentication.AuthenticationRequest;
import org.jmock.Expectations;
import org.jmock.Mockery;

/**
 * Stubs a Isis {@link AuthenticationManager}.
 */
public class Fixture_AuthenticationManager_AuthenticateOk extends MockFixtureAdapter<AuthenticationManager> {

	public void setUp(MockFixture.Context fixtureContext) {
		final Mockery mockery = fixtureContext.getMockery();
		final AuthenticationManager mock = createMock(fixtureContext, AuthenticationManager.class);
		mockery.checking(new Expectations() {
			{
				one(mock).authenticate(with(any(AuthenticationRequest.class)));
			}
		});
	}
}