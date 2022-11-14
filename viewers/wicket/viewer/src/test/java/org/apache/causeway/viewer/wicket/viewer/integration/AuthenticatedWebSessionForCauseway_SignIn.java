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

import java.util.Locale;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import org.apache.wicket.request.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.security._testing.InteractionService_forTesting;
import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.core.security.authentication.Authenticator;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.core.security.authentication.standard.RandomCodeGeneratorDefault;

class AuthenticatedWebSessionForCauseway_SignIn {

    private AuthenticationManager authMgr;

    protected Request mockRequest = Mockito.mock(Request.class);
    protected Authenticator mockAuthenticator = Mockito.mock(Authenticator.class);
    protected InteractionService mockInteractionService = Mockito.mock(InteractionService.class);
    protected ServiceRegistry mockServiceRegistry = Mockito.mock(ServiceRegistry.class);

    protected AuthenticatedWebSessionForCauseway webSession;
    private MetaModelContext mmc;

    @BeforeEach
    void setUp() throws Exception {
        mmc = MetaModelContext_forTesting.builder()
                .singleton(mockInteractionService)
                .build();

        authMgr = new AuthenticationManager(
                singletonList(mockAuthenticator),
                new InteractionService_forTesting(),
                new RandomCodeGeneratorDefault(),
                Optional.empty(),
                emptyList());
    }

    @Test
    void signInJustDelegatesToAuthenticateAndSavesState() {

        Mockito
        // must provide explicit expectation, since Locale is final.
        .when(mockRequest.getLocale())
        .thenReturn(Locale.getDefault());

        Mockito
        .when(mockAuthenticator.canAuthenticate(AuthenticationRequestPassword.class))
        .thenReturn(true);

        Mockito
        .when(mockAuthenticator.authenticate(
                Mockito.any(AuthenticationRequest.class),
                Mockito.any(String.class)))
        .thenReturn(InteractionContextFactory.testing());

        webSession = new AuthenticatedWebSessionForCauseway(mockRequest) {
            private static final long serialVersionUID = 1L;

            {
                metaModelContext = mmc;
            }

            @Override
            public AuthenticationManager getAuthenticationManager() {
                return authMgr;
            }
        };


        // when
        webSession.signIn("john", "secret");

        // then
        assertThat(webSession.isSignedIn(), is(true));
    }
}
