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

package org.apache.isis.security.ldap.authentication;

import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class LdapAuthenticatorTester {

    public static void main(final String[] args) {
        final LdapAuthenticator auth = new LdapAuthenticator(IsisContext.getConfiguration());

        AuthenticationRequestPassword req = new AuthenticationRequestPassword("unauth", "pass");
        try {
            System.out.println("unauth auth=" + auth.isValid(req));
        } catch (final Exception e) {
            System.out.println("unauth failed authentication!");
            e.printStackTrace();
        }
        req = new AuthenticationRequestPassword("joe", "pass");
        try {
            System.out.println("joe auth=" + auth.isValid(req));
        } catch (final Exception e) {
            System.out.println("joe auth failed!!");
            e.printStackTrace();
        }
        req = new AuthenticationRequestPassword("joe", "wrongpass");
        try {
            System.out.println("joe wrongpass auth=" + auth.isValid(req));
        } catch (final Exception e) {
            System.out.println("joe wrongpass auth failed!!");
            e.printStackTrace();
        }
    }

}
