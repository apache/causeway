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
package org.apache.isis.testsecurity.ldap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.junit.jupiter.api.Test;
import org.junit.runners.model.InitializationError;

import lombok.val;

class LdapEmbeddedServerTest {

    @Test
    void authenticateAgainstLdap() throws InitializationError, InterruptedException {
    	
    	val latch = LdapEmbeddedServer.run();
    	
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:10389");

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
        } finally {
        	latch.countDown(); // release the ldap-server	
        }
        
        
    	
    }
        
    
}