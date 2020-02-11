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
package org.apache.isis.security.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.security.shiro.authentication.AuthenticatorShiro;
import org.apache.isis.security.shiro.authorization.AuthorizorShiro;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;

import lombok.val;

public class ShiroAuthenticatorOrAuthorizorTest_authenticate {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private AuthenticatorShiro authenticator;
    private AuthorizorShiro authorizor;

    @Before
    public void setUp() throws Exception {

        // PRODUCTION

        val configuration = new IsisConfiguration(null);
        configuration.getSecurity().getShiro().setAutoLogoutIfAlreadyAuthenticated(false);
        
        authenticator = new AuthenticatorShiro(configuration);
        authorizor = new AuthorizorShiro();
    }

    @After
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
        AuthenticationSession isisAuthSession = authenticator.authenticate(ar, null);

        assertThat(isisAuthSession, is(not(nullValue())));
        assertThat(isisAuthSession.getUserName(), is("lonestarr"));
        assertThat(isisAuthSession.getValidationCode(), is(nullValue()));

        Identifier changeAddressIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Customer", "changeAddress", String.class, String.class);
        assertThat(authorizor.isVisibleInAnyRole(changeAddressIdentifier), is(true));

        Identifier changeEmailIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Customer", "changeEmail", String.class);
        assertThat(authorizor.isVisibleInAnyRole(changeEmailIdentifier), is(true));

        Identifier submitOrderIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Order", "submit");
        assertThat(authorizor.isVisibleInAnyRole(submitOrderIdentifier), is(true));

        Identifier cancelOrderIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Order", "cancel");
        assertThat(authorizor.isVisibleInAnyRole(cancelOrderIdentifier), is(false));
    }


}
