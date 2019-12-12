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

package org.apache.isis.viewer.wicket.viewer.integration;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.session.SessionLoggingService;
import org.apache.isis.runtime.session.IsisSessionFactory;
import org.apache.isis.security.api.authentication.AuthenticationRequestPassword;
import org.apache.isis.security.api.authentication.manager.AuthenticationManager;
import org.apache.isis.security.api.authentication.standard.Authenticator;
import org.apache.isis.security.api.authentication.standard.RandomCodeGeneratorDefault;
import org.apache.isis.security.api.authentication.standard.SimpleSession;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.webapp.context.IsisWebAppCommonContext;
import org.apache.wicket.request.Request;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.security.api.authentication.AuthenticationRequest;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class AuthenticatedWebSessionForIsis_Authenticate {


    @Rule
    public final JUnitRuleMockery2 context =
            JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    protected Request mockRequest;
    private AuthenticationManager authMgr;
    @Mock protected Authenticator mockAuthenticator;
    @Mock protected IsisWebAppCommonContext mockCommonContext;
    @Mock protected IsisSessionFactory mockIsisSessionFactory;
    @Mock protected ServiceRegistry mockServiceRegistry;

    protected AuthenticatedWebSessionForIsis webSession;

    @Before
    public void setUp() throws Exception {
        authMgr = new AuthenticationManager(Collections.singletonList(mockAuthenticator), new RandomCodeGeneratorDefault());

        context.checking(new Expectations() {
            {
                allowing(mockCommonContext).getServiceRegistry();
                will(returnValue(mockServiceRegistry));

                allowing(mockServiceRegistry).lookupService(SessionLoggingService.class);
                will(returnValue(Optional.empty()));

                allowing(mockCommonContext).lookupServiceElseFail(IsisSessionFactory.class);
                will(returnValue(mockIsisSessionFactory));

                allowing(mockIsisSessionFactory).doInSession(with(any(Runnable.class)));
                // ignore

                // must provide explicit expectation, since Locale is final.
                allowing(mockRequest).getLocale();
                will(returnValue(Locale.getDefault()));

                // stub everything else out
                ignoring(mockRequest);
            }
        });

    }

    protected void setupWebSession() {
        webSession = new AuthenticatedWebSessionForIsis(mockRequest) {
            private static final long serialVersionUID = 1L;

            {
                commonContext = mockCommonContext;
            }

            @Override
            protected AuthenticationManager getAuthenticationManager() {
                return authMgr;
            }
        };
    }



    @Test
    public void delegatesToAuthenticationManagerAndCachesAuthSessionIfOk() {

        context.checking(new Expectations() {
            {
                oneOf(mockAuthenticator).canAuthenticate(AuthenticationRequestPassword.class);
                will(returnValue(true));
                oneOf(mockAuthenticator).authenticate(with(any(AuthenticationRequest.class)), with(any(String.class)));
                will(returnValue(new SimpleSession(null, null)));
            }
        });

        setupWebSession();

        assertThat(webSession.authenticate("jsmith", "secret"), is(true));
        assertThat(webSession.getAuthenticationSession(), is(not(nullValue())));
    }

    @Test
    public void delegatesToAuthenticationManagerAndHandlesIfNotAuthenticated() {
        context.checking(new Expectations() {
            {
                oneOf(mockAuthenticator).canAuthenticate(AuthenticationRequestPassword.class);
                will(returnValue(true));
                oneOf(mockAuthenticator).authenticate(with(any(AuthenticationRequest.class)), with(any(String.class)));
                will(returnValue(null));
            }
        });
        
        setupWebSession();
        
        assertThat(webSession.authenticate("jsmith", "secret"), is(false));
        assertThat(webSession.getAuthenticationSession(), is(nullValue()));
    }

}
