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

import java.util.Locale;

import org.apache.wicket.request.Request;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.security.authentication.AuthenticationRequest;
import org.apache.isis.security.authentication.manager.AuthenticationManager;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.AuthenticatedWebSessionForIsis;

public class AuthenticatedWebSessionForIsis_Authenticate {

    @Rule
    public final JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private AuthenticatedWebSessionForIsis webSession;
    @Mock
    private Request stubRequest;
    @Mock
    private AuthenticationManager mockAuthMgr;

    @Before
    public void setUp() throws Exception {
        context.checking(new Expectations() {
            {
                // must provide explicit expectation, since Locale is final.
                allowing(stubRequest).getLocale();
                will(returnValue(Locale.getDefault()));

                // stub everything else out
                ignoring(stubRequest);
            }
        });

    }

    @Test
    public void delegatesToAuthenticationManagerAndCachesAuthSessionIfOk() {

        context.checking(new Expectations() {
            {
            	oneOf(mockAuthMgr).authenticate(with(any(AuthenticationRequest.class)));
            }
        });

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
        context.checking(new Expectations() {
            {
            	oneOf(mockAuthMgr).authenticate(with(any(AuthenticationRequest.class)));
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
