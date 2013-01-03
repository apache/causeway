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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.unittestsupport.jmock.auto.Mock;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class WildcardPermissionTest {


    @Test
    public void typicalUsageWithinIsis() throws Exception {
        
        // these are the permissions that Isis will check
        WildcardPermission viewCustomerChangeAddress = new WildcardPermission("com.mycompany.myapp:Customer:changeAddress:r");
        WildcardPermission useCustomerChangeAddress = new WildcardPermission("com.mycompany.myapp:Customer:changeAddress:w");

        // and these are examples of permissions that will be associated with a user
        assertThat(viewCustomerChangeAddress, permittedBy("com.mycompany.myapp:Customer:changeAddress:r"));
        assertThat(viewCustomerChangeAddress, permittedBy("com.mycompany.myapp:Customer:changeAddress:*"));
        assertThat(viewCustomerChangeAddress, permittedBy("com.mycompany.myapp:Customer:*:r"));
        assertThat(viewCustomerChangeAddress, permittedBy("com.mycompany.myapp:*:*:r"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:*:*:r"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:*:*:*"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:*:*"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:*"));
        assertThat(viewCustomerChangeAddress, permittedBy("*"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:Customer:*:r"));

        assertThat(useCustomerChangeAddress, permittedBy("com.mycompany.myapp:Customer:changeAddress:w"));
        assertThat(useCustomerChangeAddress, permittedBy("com.mycompany.myapp:Customer:changeAddress:*"));

        // and these are some counterexamples
        assertThat(viewCustomerChangeAddress, not(permittedBy("com.mycompany.myapp:Customer:changeAddress:w")));
        assertThat(useCustomerChangeAddress, not(permittedBy("com.mycompany.myapp:Customer:changeAddress:r")));

        assertThat(viewCustomerChangeAddress, not(permittedBy("com.mycompany.myapp:Customer:changePhoneNumber:r")));
        assertThat(viewCustomerChangeAddress, not(permittedBy("com.mycompany.myapp:Order:changeAddress:r")));
        assertThat(viewCustomerChangeAddress, not(permittedBy("xxx.mycompany.myapp:Customer:changeAddress:r")));
        assertThat(viewCustomerChangeAddress, not(permittedBy("*:*:xxx")));
        assertThat(viewCustomerChangeAddress, not(permittedBy("*:xxx")));
        assertThat(viewCustomerChangeAddress, not(permittedBy("xxx")));
    }

    
    @Test
    public void defaultPackage() throws Exception {
        
        // these are the permissions that Isis will check
        WildcardPermission viewCustomerChangeAddress = new WildcardPermission(":Customer:changeAddress:r");

        // and these are examples of permissions that will be associated with a user
        assertThat(viewCustomerChangeAddress, permittedBy(":Customer:changeAddress:r"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:Customer:changeAddress:r"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:Customer:changeAddress:*"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:Customer:changeAddress"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:Customer:*"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:Customer"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:*"));
        assertThat(viewCustomerChangeAddress, permittedBy("*"));
    }
    
    private static Matcher<? super Permission> permittedBy(final String permissionString) {
        return implies(new WildcardPermission(permissionString));
    }

    private static Matcher<? super Permission> implies(final WildcardPermission wp) {
        return new TypeSafeMatcher<Permission>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("implies " + wp.toString());
            }

            @Override
            protected boolean matchesSafely(Permission item) {
                return wp.implies(item);
            }
        };
    }

}
