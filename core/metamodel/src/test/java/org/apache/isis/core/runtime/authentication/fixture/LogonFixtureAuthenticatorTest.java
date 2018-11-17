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

package org.apache.isis.core.runtime.authentication.fixture;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestAbstract;
import org.apache.isis.core.runtime.fixtures.authentication.AuthenticationRequestLogonFixture;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JMock.class)
public class LogonFixtureAuthenticatorTest {

    private final Mockery mockery = new JUnit4Mockery();

    private IsisConfiguration mockConfiguration;
    private LogonFixtureAuthenticator authenticator;

    private AuthenticationRequestLogonFixture logonFixtureRequest;

    private SomeOtherAuthenticationRequest someOtherRequest;

    private static class SomeOtherAuthenticationRequest extends AuthenticationRequestAbstract {
        public SomeOtherAuthenticationRequest() {
            super("other");
        }
    }

    @Before
    public void setUp() {
        mockConfiguration = mockery.mock(IsisConfiguration.class);

        logonFixtureRequest = new AuthenticationRequestLogonFixture(new LogonFixture("joebloggs"));
        someOtherRequest = new SomeOtherAuthenticationRequest();
        authenticator = new LogonFixtureAuthenticator(mockConfiguration);
    }

    @Test
    public void canAuthenticateExplorationRequest() throws Exception {
        assertThat(authenticator.canAuthenticate(logonFixtureRequest.getClass()), is(true));
    }

    @Test
    public void canAuthenticateSomeOtherTypeOfRequest() throws Exception {
        assertThat(authenticator.canAuthenticate(someOtherRequest.getClass()), is(false));
    }

//    @Test
//    public void isValidLogonFixtureRequestWhenRunningInExplorationMode() throws Exception {
//        authenticator.init(DeploymentCategory.EXPLORING);
//        assertThat(authenticator.isValid(logonFixtureRequest), is(true));
//    }
//
//    @Test
//    public void isValidLogonFixtureRequestWhenRunningInPrototypeMode() throws Exception {
//        authenticator.init(DeploymentCategory.PROTOTYPING);
//        assertThat(authenticator.isValid(logonFixtureRequest), is(true));
//    }
//
//    @Test
//    public void isNotValidExplorationRequestWhenRunningInSomethingOtherThanExplorationOrPrototypeMode() throws Exception {
//        authenticator.init(DeploymentCategory.PRODUCTION);
//        assertThat(authenticator.isValid(logonFixtureRequest), is(false));
//    }
//
//    @Test
//    public void isValidSomeOtherTypeOfRequest() throws Exception {
//        authenticator.init(DeploymentCategory.EXPLORING);
//        assertThat(authenticator.canAuthenticate(SomeOtherAuthenticationRequest.class), is(false));
//    }

}
