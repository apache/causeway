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


package org.apache.isis.viewer.wicket.viewer.app.wicket;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.runtimes.dflt.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.testsupport.jmock.FixtureMockery;
import org.apache.isis.viewer.wicket.viewer.Fixture_AuthenticationManager_AuthenticateOk;
import org.apache.isis.viewer.wicket.viewer.Fixture_Request_Stub;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.AuthenticatedWebSessionForIsis;
import org.apache.wicket.Request;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class AuthenticatedWebSessionForIsis_SignIn {

	private FixtureMockery context = new FixtureMockery() {{
		setImposteriser(ClassImposteriser.INSTANCE);
	}};
	
	private AuthenticatedWebSessionForIsis webSession;
	private Request stubRequest;
	
	@Before
	public void setUp() throws Exception {
		stubRequest = context.fixture(Fixture_Request_Stub.class).object();
	}
	
	@Test
	public void signInJustDelegatesToAuthenticateAndSavesState() {
		final AuthenticationManager mockAuthMgr = context.fixture(Fixture_AuthenticationManager_AuthenticateOk.class).object();
		webSession = new AuthenticatedWebSessionForIsis(stubRequest) {
			private static final long serialVersionUID = 1L;

			@Override
			protected AuthenticationManager getAuthenticationManager() {
				return mockAuthMgr;
			}
		};
		webSession.signIn("john", "secret");
		assertThat(webSession.isSignedIn(), is(true));
	}
}
