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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Hashtable;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.junit.jupiter.api.Test;
import org.junit.runners.model.InitializationError;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		classes = {LdapServerService.class},
		properties = {
	        "logging.config=log4j2-test.xml",
	        "logging.level.org.apache.directory.api.ldap.model.entry.Value=OFF",
})
class LdapEmbeddedServerTest {
	
	@Inject LdapServerService ldapServerService;

    @Test
    void authenticateAgainstLdap() throws InitializationError, InterruptedException {
    	
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:" + LdapEmbeddedServer.PORT);

        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn=Sven Tester,ou=Users,dc=myorg,dc=com");
        env.put(Context.SECURITY_CREDENTIALS, "pass");
        
        try {
            Context ctx = new InitialContext(env);
            NamingEnumeration<NameClassPair> enm = ctx.list("");

            assertTrue(enm.hasMore());
            
            while (enm.hasMore()) {
                System.out.println(enm.next());
            }

            enm.close();
            ctx.close();
        } catch (NamingException e) {
            fail(e.getMessage());
        } 
    	
    }
    
}