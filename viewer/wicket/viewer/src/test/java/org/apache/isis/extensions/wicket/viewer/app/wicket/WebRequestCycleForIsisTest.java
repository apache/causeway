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


package org.apache.isis.extensions.wicket.viewer.app.wicket;

import static org.junit.Assert.fail;

import org.apache.isis.common.jmock.AbstractJMockForClassesTest;
import org.apache.isis.extensions.wicket.viewer.integration.wicket.WebRequestCycleForIsis;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.junit.Ignore;
import org.junit.Test;

public class WebRequestCycleForIsisTest extends AbstractJMockForClassesTest {

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

	@Test
	public void testIsisWebRequestCycle() {
		final WebApplication mockApplication = context.mockAndIgnoreAnyInteraction(WebApplication.class);
		final WebRequest mockRequest = context.mock(WebRequest.class);
		final Response mockResponse = context.mock(Response.class);
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
