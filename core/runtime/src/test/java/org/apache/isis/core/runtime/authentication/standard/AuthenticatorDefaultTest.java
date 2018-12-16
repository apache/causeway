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

package org.apache.isis.core.runtime.authentication.standard;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.standard.AuthenticatorAbstract;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JMock.class)
public class AuthenticatorDefaultTest {

    private final Mockery mockery = new JUnit4Mockery();

    private AuthenticatorAbstract authenticator;

    @Before
    public void setUp() {
        
        authenticator = new AuthenticatorAbstract() {

            @Override
            public boolean isValid(final AuthenticationRequest request) {
                return false;
            }

            @Override
            public boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
                return false;
            }
        };
    }

    @Test
    public void getConfiguration() throws Exception {
        //assertThat(authenticator.getConfiguration(), is(mockConfiguration));
    }


}
