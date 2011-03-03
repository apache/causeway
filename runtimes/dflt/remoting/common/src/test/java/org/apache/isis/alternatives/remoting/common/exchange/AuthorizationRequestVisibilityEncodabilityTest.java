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


package org.apache.isis.alternatives.remoting.common.exchange;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.isis.alternatives.remoting.common.data.common.IdentityData;
import org.apache.isis.alternatives.remoting.common.exchange.AuthorizationRequestVisibility;
import org.apache.isis.core.commons.encoding.EncodabilityContractTest;
import org.apache.isis.core.commons.encoding.Encodable;
import org.junit.Ignore;
import org.junit.Test;

public class AuthorizationRequestVisibilityEncodabilityTest extends EncodabilityContractTest {

	protected IdentityData mockTargetData;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		mockTargetData = context.mock(IdentityData.class);
	}
	
	protected Encodable createEncodable() {
		return new AuthorizationRequestVisibility(mockAuthSession, mockTargetData, "foobar");
	}
	
	
	@Override
	@Ignore
	@Test
	public void shouldRoundTrip() throws IOException {
		super.shouldRoundTrip();
	}

	@Override
	protected void assertRoundtripped(
			Object decodedEncodable,
			Object originalEncodable) {
		AuthorizationRequestVisibility decoded = (AuthorizationRequestVisibility) decodedEncodable;
		AuthorizationRequestVisibility original = (AuthorizationRequestVisibility) originalEncodable;
		
		// TODO: to complete, may need to setup mock expectations
		assertThat(decoded.getId(), is(equalTo(original.getId())));
	}

}
