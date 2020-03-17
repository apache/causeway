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
package org.apache.isis.testdomain.ldap;

import java.util.Hashtable;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runners.model.InitializationError;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.Incubating;
import org.apache.isis.testdomain.Smoketest;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = {LdapServerService.class}
)
@Disabled // not sure why...
@Incubating("teardown issues?")
@TestPropertySource(IsisPresets.UseLog4j2Test)
class UnstableLdapEmbeddedServerTest {

    @Inject LdapServerService ldapServerService;

    @Test
    void authenticate_Sven() throws InitializationError, InterruptedException {

        val env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:" + LdapConstants.PORT);

        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, LdapConstants.SVEN_PRINCIPAL);
        env.put(Context.SECURITY_CREDENTIALS, "pass");

        try {
            val ctx = new InitialContext(env);
            val namingEnumeration = ctx.list("");

            int entryCount = 0;
            while (namingEnumeration.hasMore()) {
                namingEnumeration.next();
                ++entryCount;
            }
            assertEquals(3, entryCount);

            namingEnumeration.close();
            ctx.close();
        } catch (NamingException e) {
            fail(e.getMessage());
        } 
    }

    @Test
    void authenticate_Admin() throws InitializationError, InterruptedException {

        val env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:" + LdapConstants.PORT);

        env.put( Context.SECURITY_AUTHENTICATION, "simple" );
        env.put( Context.SECURITY_PRINCIPAL, "uid=admin,ou=system" );
        env.put( Context.SECURITY_CREDENTIALS, "secret" );

        try {
            val ctx = new InitialContext(env);
            val namingEnumeration = ctx.list("");

            int entryCount = 0;
            while (namingEnumeration.hasMore()) {
                namingEnumeration.next();
                ++entryCount;
            }
            assertEquals(3, entryCount);

            namingEnumeration.close();
            ctx.close();
        } catch (NamingException e) {
            fail(e.getMessage());
        } 

    }

}