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
package org.apache.causeway.security.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.security.shiro.authentication.AuthenticatorShiro;
import org.apache.causeway.security.shiro.authorization.AuthorizorShiro;

import lombok.val;

class ShiroAuthenticatorOrAuthorizorTest_authenticate {

    private AuthenticatorShiro authenticator;
    private AuthorizorShiro authorizor;

    @BeforeEach
    public void setUp() throws Exception {

         val configuration = new CausewayConfiguration(null);
        configuration.getSecurity().getShiro().setAutoLogoutIfAlreadyAuthenticated(false);

        authenticator = new AuthenticatorShiro(configuration);
        authorizor = new AuthorizorShiro();
    }

    @AfterEach
    public void tearDown() throws Exception {
        Subject subject = ThreadContext.getSubject();
        if(subject != null) {
            subject.logout();
        }
        SecurityUtils.setSecurityManager(null);
    }

    @Test
    public void cannotAuthenticateIfShiroEnvironmentNotInitialized() throws Exception {

        assertThat(authenticator.canAuthenticate(AuthenticationRequestPassword.class), is(false));
        assertThat(authenticator.authenticate(new AuthenticationRequestPassword("dummy", "dummy"), "unused"), is(nullValue()));
    }

    @Test
    public void happyCase() throws Exception {

        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);


        assertThat(authenticator.canAuthenticate(AuthenticationRequestPassword.class), is(true));

        AuthenticationRequest ar = new AuthenticationRequestPassword("lonestarr", "vespa");
        val authentication = authenticator.authenticate(ar, "test code");

        assertThat(authentication, is(not(nullValue())));
        assertThat(authentication.getUser().getName(), is("lonestarr"));
        assertThat(authentication.getUser().getAuthenticationCode(), is("test code"));

        Identifier changeAddressIdentifier = Identifier.actionIdentifier(
                TypeIdentifierTestFactory.customer(), "changeAddress", String.class, String.class);
        assertThat(authorizor.isVisible(authentication, changeAddressIdentifier), is(true));

        Identifier changeEmailIdentifier = Identifier.actionIdentifier(
                TypeIdentifierTestFactory.customer(), "changeEmail", String.class);
        assertThat(authorizor.isVisible(authentication, changeEmailIdentifier), is(true));

        Identifier submitOrderIdentifier = Identifier.actionIdentifier(
                TypeIdentifierTestFactory.order(), "submit");
        assertThat(authorizor.isVisible(authentication, submitOrderIdentifier), is(true));

        Identifier cancelOrderIdentifier = Identifier.actionIdentifier(
                TypeIdentifierTestFactory.order(), "cancel");
        assertThat(authorizor.isVisible(authentication, cancelOrderIdentifier), is(false));
    }



}
