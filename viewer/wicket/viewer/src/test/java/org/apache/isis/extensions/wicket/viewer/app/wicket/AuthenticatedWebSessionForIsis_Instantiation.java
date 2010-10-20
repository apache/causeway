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

import org.apache.isis.commons.jmock.FixtureMockery;
import org.apache.isis.extensions.wicket.viewer.Fixture_Request_Stub;
import org.apache.isis.extensions.wicket.viewer.integration.wicket.AuthenticatedWebSessionForIsis;
import org.apache.wicket.Request;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class AuthenticatedWebSessionForIsis_Instantiation {

	private FixtureMockery context = new FixtureMockery() {{
		setImposteriser(ClassImposteriser.INSTANCE);
	}};
	
	@Test
	public void canInstantiateIfProvideRequest() {
		final Request stubRequest = context.fixture(Fixture_Request_Stub.class).object();
		new AuthenticatedWebSessionForIsis(stubRequest);
	}

	@Test(expected=IllegalArgumentException.class)
	public void requestMustBeProvided() {
		new AuthenticatedWebSessionForIsis(null);
	}

}
