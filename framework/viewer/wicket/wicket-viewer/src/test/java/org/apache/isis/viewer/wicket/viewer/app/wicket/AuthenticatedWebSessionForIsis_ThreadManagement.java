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

import java.util.Locale;

import org.apache.wicket.request.Request;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.AuthenticatedWebSessionForIsis;

public class AuthenticatedWebSessionForIsis_ThreadManagement {

    @Rule
    public final JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private AuthenticatedWebSessionForIsis webSession;

    @Mock
    private Request stubRequest;

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

        webSession = new AuthenticatedWebSessionForIsis(stubRequest);
    }

    @Test
    public void testRegisterUseByThread() {
        assertThat(webSession.getThreadUsage(), is(0));
        webSession.registerUseByThread();
        assertThat(webSession.getThreadUsage(), is(1));
    }

    @Test
    public void testDeregisterUseByThread() {
        webSession.registerUseByThread();
        assertThat(webSession.getThreadUsage(), is(1));
        webSession.deregisterUseByThread();
        assertThat(webSession.getThreadUsage(), is(0));
    }
}
