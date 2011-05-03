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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.testsupport.jmock.FixtureMockery;
import org.apache.isis.viewer.wicket.viewer.Fixture_AuthenticationManager_AuthenticateOk;
import org.apache.isis.viewer.wicket.viewer.Fixture_Request_Stub;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.AuthenticatedWebSessionForIsis;
import org.apache.wicket.Request;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class AuthenticatedWebSessionForIsis_Authenticate {

    private final FixtureMockery context = new FixtureMockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private AuthenticatedWebSessionForIsis webSession;
    private Request stubRequest;

    @Before
    public void setUp() throws Exception {
        stubRequest = context.fixture(Fixture_Request_Stub.class).object();
    }

    @Test
    public void delegatesToAuthenticationManagerAndCachesAuthSessionIfOk() {
        final AuthenticationManager mockAuthMgr =
            context.fixture(Fixture_AuthenticationManager_AuthenticateOk.class).object();
        webSession = new AuthenticatedWebSessionForIsis(stubRequest) {
            private static final long serialVersionUID = 1L;

            @Override
            protected AuthenticationManager getAuthenticationManager() {
                return mockAuthMgr;
            }
        };
        assertThat(webSession.authenticate("jsmith", "secret"), is(true));
        assertThat(webSession.getAuthenticationSession(), is(not(nullValue())));
    }

    @Test
    public void delegatesToAuthenticationManagerAndHandlesIfNotAuthenticated() {
        final AuthenticationManager mockAuthMgr = context.mock(AuthenticationManager.class);
        context.checking(new Expectations() {
            {
                one(mockAuthMgr).authenticate(with(any(AuthenticationRequest.class)));
                will(returnValue(null));
            }
        });
        webSession = new AuthenticatedWebSessionForIsis(stubRequest) {
            private static final long serialVersionUID = 1L;

            @Override
            protected AuthenticationManager getAuthenticationManager() {
                return mockAuthMgr;
            }
        };
        assertThat(webSession.authenticate("jsmith", "secret"), is(false));
        assertThat(webSession.getAuthenticationSession(), is(nullValue()));
    }

}
