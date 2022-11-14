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
package org.apache.causeway.viewer.wicket.viewer.integration;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.apache.wicket.request.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.user.ImpersonatedUserHolder;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.security._testing.InteractionService_forTesting;
import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.core.security.authentication.Authenticator;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.core.security.authentication.standard.RandomCodeGeneratorDefault;

class AuthenticatedWebSessionForCauseway_Authenticate {

    private AuthenticationManager authMgr;
    protected Request mockRequest = Mockito.mock(Request.class);
    protected Authenticator mockAuthenticator = Mockito.mock(Authenticator.class);
    protected InteractionService mockInteractionService = Mockito.mock(InteractionService.class);
    protected ImpersonatedUserHolder mockImpersonatedUserHolder = Mockito.mock(ImpersonatedUserHolder.class);
    protected InteractionLayerTracker mockInteractionLayerTracker = Mockito.mock(InteractionLayerTracker.class);

    protected AuthenticatedWebSessionForCauseway webSession;
    private MetaModelContext mmc;

    @BeforeEach
    void setUp() throws Exception {

        mmc = MetaModelContext_forTesting.builder()
                .singleton(mockInteractionService)
                .singleton(mockImpersonatedUserHolder)
                .build();

        authMgr = new AuthenticationManager(
                Collections.singletonList(mockAuthenticator),
                new InteractionService_forTesting(),
                new RandomCodeGeneratorDefault(),
                Optional.empty(),
                Collections.emptyList());

        Mockito
        .when(mockInteractionLayerTracker.currentInteractionContext())
        .thenReturn(Optional.of(InteractionContextFactory.testing()));

        Mockito
        // must provide explicit expectation, since Locale is final.
        .when(mockRequest.getLocale())
        .thenReturn(Locale.getDefault());
    }

    protected void setupWebSession() {
        webSession = new AuthenticatedWebSessionForCauseway(mockRequest) {
            private static final long serialVersionUID = 1L;
            { metaModelContext = mmc; }
            @Override public AuthenticationManager getAuthenticationManager() {
                return authMgr;
            }
        };
    }

    @Test
    void delegatesToAuthenticationManagerAndCachesAuthSessionIfOk() {

        Mockito
        .when(mockImpersonatedUserHolder.getUserMemento())
        .thenReturn(Optional.empty());

        Mockito
        .when(mockAuthenticator.canAuthenticate(AuthenticationRequestPassword.class))
        .thenReturn(true);

        Mockito
        .when(mockAuthenticator.authenticate(
                Mockito.any(AuthenticationRequest.class),
                Mockito.any(String.class)))
        .thenReturn(InteractionContextFactory.testing());

        setupWebSession();

        // when
        assertThat(webSession.authenticate("jsmith", "secret"), is(true));

        // then
        assertThat(webSession.getAuthentication(), is(not(nullValue())));
    }

    @Test
    void delegatesToAuthenticationManagerAndHandlesIfNotAuthenticated() {

        Mockito
        .when(mockAuthenticator.canAuthenticate(AuthenticationRequestPassword.class))
        .thenReturn(true);

        Mockito
        .when(mockAuthenticator.authenticate(
                Mockito.any(AuthenticationRequest.class),
                Mockito.any(String.class)))
        .thenReturn(null);

        setupWebSession();

        assertThat(webSession.authenticate("jsmith", "secret"), is(false));
        assertThat(webSession.getAuthentication(), is(nullValue()));
    }

}
