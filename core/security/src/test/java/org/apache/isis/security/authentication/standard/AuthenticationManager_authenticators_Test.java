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

package org.apache.isis.security.authentication.standard;

import java.util.Collections;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.security.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authentication.standard.Authenticator;
import org.apache.isis.core.security.authentication.standard.NoAuthenticatorException;
import org.apache.isis.core.security.authentication.standard.RandomCodeGeneratorDefault;

@RunWith(JMock.class)
public class AuthenticationManager_authenticators_Test {

    private final Mockery mockery = new JUnit4Mockery();

    private AuthenticationManager authenticationManager;
    private Authenticator mockAuthenticator;

    @Before
    public void setUp() throws Exception {
        mockAuthenticator = mockery.mock(Authenticator.class);
    }

    @Test(expected = NoAuthenticatorException.class)
    public void shouldNotBeAbleToAuthenticateWithNoAuthenticators() throws Exception {

        authenticationManager = new AuthenticationManager(Collections.emptyList(), new RandomCodeGeneratorDefault());
        authenticationManager.authenticate(new AuthenticationRequestPassword("foo", "bar"));
    }

    @Test
    public void shouldBeAbleToUseAuthenticators() throws Exception {

        authenticationManager = new AuthenticationManager(Collections.singletonList(mockAuthenticator), new RandomCodeGeneratorDefault());
        assertThat(authenticationManager.getAuthenticators().size(), is(1));
        assertThat(authenticationManager.getAuthenticators().get(0), is(sameInstance(mockAuthenticator)));
    }

}
