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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.util.ThreadContext;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ShiroAuthenticatorOrAuthorizorTest_authenticate {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private IsisConfiguration mockConfiguration;

    private ShiroAuthenticatorOrAuthorizor authOrAuth;

    @Before
    public void setUp() throws Exception {
        
    	context.checking(new Expectations() {{
            allowing(mockConfiguration).getBoolean("isis.authentication.shiro.autoLogoutIfAlreadyAuthenticated", false);
            will(returnValue(false));
        }});
    	
   		authOrAuth = new ShiroAuthenticatorOrAuthorizor(mockConfiguration);
    	authOrAuth.init(DeploymentCategory.PRODUCTION);
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

        assertThat(authOrAuth.canAuthenticate(AuthenticationRequestPassword.class), is(false));
        assertThat(authOrAuth.authenticate(new AuthenticationRequestPassword("dummy", "dummy"), "unused"), is(nullValue()));
    }

    @Test
    public void happyCase() throws Exception {

        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);

        
        assertThat(authOrAuth.canAuthenticate(AuthenticationRequestPassword.class), is(true));

        AuthenticationRequest ar = new AuthenticationRequestPassword("lonestarr", "vespa");
        AuthenticationSession isisAuthSession = authOrAuth.authenticate(ar, null);

        assertThat(isisAuthSession, is(not(nullValue())));
        assertThat(isisAuthSession.getUserName(), is("lonestarr"));
        assertThat(isisAuthSession.getValidationCode(), is(nullValue()));

        Identifier changeAddressIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Customer", "changeAddress", String.class, String.class);
        assertThat(authOrAuth.isVisibleInAnyRole(changeAddressIdentifier), is(true));

        Identifier changeEmailIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Customer", "changeEmail", String.class);
        assertThat(authOrAuth.isVisibleInAnyRole(changeEmailIdentifier), is(true));

        Identifier submitOrderIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Order", "submit");
        assertThat(authOrAuth.isVisibleInAnyRole(submitOrderIdentifier), is(true));

        Identifier cancelOrderIdentifier = Identifier.actionIdentifier("com.mycompany.myapp.Order", "cancel");
        assertThat(authOrAuth.isVisibleInAnyRole(cancelOrderIdentifier), is(false));
    }


}
