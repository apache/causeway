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

import static org.junit.Assert.fail;

import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.jmock.auto.Mock;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.WebRequestCycleForIsis;

public class WebRequestCycleForIsisTest {

    @Rule
    public final JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);

    @Mock
    private WebRequest mockRequest;
    @Mock
    private Response mockResponse;

    @Mock
    private WebApplication mockApplication;

    @Ignore
    @Test
    public void testOnBeginRequest() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testOnEndRequest() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testIsisWebRequestCycle() {
        context.ignoring(mockApplication);
        new WebRequestCycleForIsis(mockApplication, mockRequest, mockResponse);
    }

    @Ignore("downcast")
    @Test
    public void testGetWebSession() {
    }

    @Ignore
    @Test
    public void testGetIsisContext() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testNewClientInfo() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testGetTransactionManager() {
        fail("Not yet implemented");
    }

}
