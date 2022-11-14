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

import org.apache.wicket.request.Request;
import org.mockito.Mockito;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.session.SessionSubscriber;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;

public abstract class AuthenticatedWebSessionForCauseway_TestAbstract {

    protected Request mockRequest = Mockito.mock(Request.class);
    protected AuthenticationManager mockAuthMgr = Mockito.mock(AuthenticationManager.class);
    protected MetaModelContext mockCommonContext = Mockito.mock(MetaModelContext.class);
    protected InteractionService mockInteractionService = Mockito.mock(InteractionService.class);
    protected ServiceRegistry mockServiceRegistry = Mockito.mock(ServiceRegistry.class);

    protected AuthenticatedWebSessionForCauseway webSession;

    protected void setUp() throws Exception {

        Mockito
        // must provide explicit expectation, since Locale is final.
        .when(mockRequest.getLocale())
        .thenReturn(Locale.getDefault());

        Mockito
        .when(mockCommonContext.getServiceRegistry())
        .thenReturn(mockServiceRegistry);

        Mockito
        .when(mockServiceRegistry.lookupService(SessionSubscriber.class))
        .thenReturn(Optional.empty());

        Mockito
        .when(mockServiceRegistry.lookupServiceElseFail(InteractionService.class))
        .thenReturn(mockInteractionService);

    }

    protected void setupWebSession() {
        webSession = new AuthenticatedWebSessionForCauseway(mockRequest) {
            private static final long serialVersionUID = 1L;

            {
                metaModelContext = mockCommonContext;
            }

            @Override
            public AuthenticationManager getAuthenticationManager() {
                return mockAuthMgr;
            }
        };
    }


}
