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
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.security.shiro.authentication.ShiroAuthenticator;
import org.apache.isis.security.shiro.authorization.ShiroAuthorizor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ShiroAuthenticatorOrAuthorizorTest_isVisibleInAnyRole {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private IsisConfiguration mockConfiguration;
    
    private ShiroAuthenticator authenticator;
    private ShiroAuthorizor authorizor;

    @Before
    public void setUp() throws Exception {
        
        // PRODUCTION

        _Config.clear();
        _Config.put("isis.authentication.shiro.autoLogoutIfAlreadyAuthenticated", false);
        
        authenticator = new ShiroAuthenticator();
        authorizor = new ShiroAuthorizor();
        
        authenticator.init();
        authorizor.init();
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
    public void vetoing() throws Exception {
        // given
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);

        AuthenticationRequest ar = new AuthenticationRequestPassword("darkhelmet", "ludicrousspeed");
        authenticator.authenticate(ar, null);

        // when, then
        Identifier changeAddressIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Customer", "changeAddress", String.class, String.class);
        assertThat(authorizor.isVisibleInAnyRole(changeAddressIdentifier), is(true));

    }

    
    @Test
    public void vetoingOverridden() throws Exception {
        // given
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);

        AuthenticationRequest ar = new AuthenticationRequestPassword("lonestarr", "vespa");
        authenticator.authenticate(ar, null);
        
        // when, then
        Identifier removeCustomerIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Customer", "remove");
        assertThat(authorizor.isVisibleInAnyRole(removeCustomerIdentifier), is(true));
    }


}
