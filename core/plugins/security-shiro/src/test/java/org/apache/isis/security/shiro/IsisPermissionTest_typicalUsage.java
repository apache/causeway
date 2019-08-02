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

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.security.shiro.authorization.IsisPermission;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class IsisPermissionTest_typicalUsage {


    @Before
    public void setUp() throws Exception {
        IsisPermission.resetVetoedPermissions();
    }
    
    @After
    public void tearDown() throws Exception {
        IsisPermission.resetVetoedPermissions();
    }



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
        assertThat(viewCustomerChangeAddress, permittedBy("com.mycompany.myapp:*:*"));
        assertThat(viewCustomerChangeAddress, permittedBy("com.mycompany.myapp:*"));
        assertThat(viewCustomerChangeAddress, permittedBy("com.mycompany.myapp"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:*:*:r"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:*:*:*"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:*:*"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:*"));
        assertThat(viewCustomerChangeAddress, permittedBy("*"));
        assertThat(viewCustomerChangeAddress, permittedBy("*:Customer:*:r"));


        assertThat(useCustomerChangeAddress, permittedBy("com.mycompany.myapp:Customer:changeAddress:w"));
        assertThat(useCustomerChangeAddress, permittedBy("com.mycompany.myapp:Customer:changeAddress:*"));

        // and these are some counterexamples
        assertThat(viewCustomerChangeAddress, not(permittedBy("com.mycompany"))); // packages are NOT recursive
        assertThat(viewCustomerChangeAddress, not(permittedBy("com.mycompany.*"))); // can't use regex wildcards for packages either
        
        assertThat(viewCustomerChangeAddress, not(permittedBy("com.mycompany.myapp:Customer:changeAddress:w")));
        assertThat(useCustomerChangeAddress, not(permittedBy("com.mycompany.myapp:Customer:changeAddress:r")));

        assertThat(viewCustomerChangeAddress, not(permittedBy("com.mycompany.myapp:Customer:changePhoneNumber:r")));
        assertThat(viewCustomerChangeAddress, not(permittedBy("com.mycompany.myapp:Order:changeAddress:r")));
        assertThat(viewCustomerChangeAddress, not(permittedBy("xxx.mycompany.myapp:Customer:changeAddress:r")));
        assertThat(viewCustomerChangeAddress, not(permittedBy("*:*:xxx")));
        assertThat(viewCustomerChangeAddress, not(permittedBy("*:xxx")));
        assertThat(viewCustomerChangeAddress, not(permittedBy("xxx")));
        
        assertThat(viewCustomerChangeAddress, not(permittedBy("!foo/com.mycompany.myapp:Customer:changeAddress:r")));
        assertThat(useCustomerChangeAddress, not(permittedBy("!foo/com.mycompany.myapp:Customer:changeAddress:w")));
        
        // and check that two wrongs don't make a right (ie the ! means veto, rather than "not") 
        assertThat(useCustomerChangeAddress, not(permittedBy("!foo/com.mycompany.myapp:Customer:changeAddress:r")));
    }


    @Test
    public void vetoableDomains() throws Exception {
        
        // these are the permissions that Isis will check
        WildcardPermission viewCustomerChangeAddress = new WildcardPermission("com.mycompany.myapp:Customer:changeAddress:r");

        // normally this would be permitted...
        assertThat(viewCustomerChangeAddress, permittedBy("foo/com.mycompany.myapp:Customer:*"));
        
        // but if there's a veto
        assertThat(viewCustomerChangeAddress, not(permittedBy("!foo/com.mycompany.myapp:Customer:changeAddress:r")));
        // then no longer permitted if in the same vetoable domain
        assertThat(viewCustomerChangeAddress, not(permittedBy("foo/com.mycompany.myapp:Customer:*")));
        // though the same permission in another vetoable domain will permit
        assertThat(viewCustomerChangeAddress, permittedBy("bar/com.mycompany.myapp:Customer:*"));
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
        return permittedBy(new IsisPermission(permissionString));
    }

    private static Matcher<? super Permission> permittedBy(final IsisPermission wp) {
        return new TypeSafeMatcher<Permission>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("permitted by " + wp.toString());
            }

            @Override
            protected boolean matchesSafely(Permission item) {
                return wp.implies(item);
            }
        };
    }

}
