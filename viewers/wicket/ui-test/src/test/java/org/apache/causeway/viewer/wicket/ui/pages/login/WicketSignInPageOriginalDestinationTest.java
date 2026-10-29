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
package org.apache.causeway.viewer.wicket.ui.pages.login;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Uses stub pages to model the request sequence of an externally authenticated user (CAUSEWAY-4078):
 * the first request for a protected page is intercepted to the sign-in page because the Wicket session
 * is not yet signed in; by the time the sign-in page is constructed, the session has been signed in.
 */
class WicketSignInPageOriginalDestinationTest {

    private static boolean signedIn;
    private static boolean reportingException;
    private static boolean clearOriginalDestination;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        signedIn = false;
        reportingException = false;
        clearOriginalDestination = false;
        tester = new WicketTester();
    }

    @AfterEach
    void tearDown() {
        tester.destroy();
    }

    @Test
    void deepLinkContinuesToOriginalDestination() {
        tester.startPage(ProtectedPage.class);

        tester.assertRenderedPage(ProtectedPage.class);
    }

    @Test
    void withoutOriginalDestinationRestartsAtHome() {
        signedIn = true;

        tester.startPage(StubSignInPage.class);

        tester.assertRenderedPage(StubHomePage.class);
    }

    @Test
    void whenClearOriginalDestinationConfiguredRestartsAtHome() {
        clearOriginalDestination = true;

        tester.startPage(ProtectedPage.class);

        tester.assertRenderedPage(StubHomePage.class);
    }

    @Test
    void whenReportingExceptionDoesNotContinueToOriginalDestination() {
        reportingException = true;

        tester.startPage(ProtectedPage.class);

        tester.assertRenderedPage(StubHomePage.class);
    }

    // -- STUB PAGES

    abstract static class StubPage extends WebPage implements IMarkupResourceStreamProvider {
        private static final long serialVersionUID = 1L;

        @Override
        public IResourceStream getMarkupResourceStream(
                final MarkupContainer container,
                final Class<?> containerClass) {
            return new StringResourceStream("<html><body></body></html>");
        }
    }

    /**
     * Analogous to an {@code @AuthorizeInstantiation}-protected page such as the entity page.
     */
    public static class ProtectedPage extends StubPage {
        private static final long serialVersionUID = 1L;

        public ProtectedPage() {
            if (!signedIn) {
                throw new RestartResponseAtInterceptPageException(StubSignInPage.class);
            }
        }
    }

    /**
     * Analogous to {@link WicketSignInPage}, where the session has meanwhile been signed in.
     */
    public static class StubSignInPage extends StubPage {
        private static final long serialVersionUID = 1L;

        public StubSignInPage() {
            signedIn = true;
            WicketSignInPage.continueToOriginalDestinationIfAllowed(
                    this, reportingException, clearOriginalDestination);
            throw new RestartResponseException(StubHomePage.class);
        }
    }

    public static class StubHomePage extends StubPage {
        private static final long serialVersionUID = 1L;
    }

}
