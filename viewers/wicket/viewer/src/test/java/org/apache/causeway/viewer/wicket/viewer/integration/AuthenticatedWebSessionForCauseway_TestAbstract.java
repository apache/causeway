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
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.session.SessionSubscriber;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.causeway.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;

public abstract class AuthenticatedWebSessionForCauseway_TestAbstract {

    @Rule public final JUnitRuleMockery2 context =
            JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock protected Request mockRequest;
    @Mock protected AuthenticationManager mockAuthMgr;
    @Mock protected MetaModelContext mockCommonContext;
    @Mock protected InteractionService mockInteractionService;
    @Mock protected ServiceRegistry mockServiceRegistry;

    protected AuthenticatedWebSessionForCauseway webSession;

    protected void setUp() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(mockCommonContext).getServiceRegistry();
                will(returnValue(mockServiceRegistry));

                allowing(mockServiceRegistry).lookupService(SessionSubscriber.class);
                will(returnValue(Optional.empty()));

                allowing(mockCommonContext).lookupServiceElseFail(InteractionService.class);
                will(returnValue(mockInteractionService));

                allowing(mockInteractionService).run(
                        InteractionContextFactory.testing(),
                        with(any(ThrowingRunnable.class)));
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
