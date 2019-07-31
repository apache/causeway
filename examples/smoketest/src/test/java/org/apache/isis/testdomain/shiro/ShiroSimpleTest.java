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
package org.apache.isis.testdomain.shiro;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.val;

class ShiroSimpleTest extends AbstractShiroTest {

    @BeforeAll
    public static void beforeClass() {
        //0.  Build and set the SecurityManager used to build Subject instances used in your tests
        //    This typically only needs to be done once per class if your shiro.ini doesn't change,
        //    otherwise, you'll need to do this logic in each test that is different
        val factory = new IniSecurityManagerFactory("classpath:shiro-simple.ini");
        setSecurityManager(factory.getInstance());
    }
    
    @AfterAll
    public static void tearDownSubject() {
        tearDownShiro();
    }
    
    @BeforeEach
    public void setUp() {
        //1.  Build the Subject instance for the test to run:
        val subjectUnderTest = new Subject.Builder(getSecurityManager()).buildSubject();
        //2. Bind the subject to the current thread:
        setSubject(subjectUnderTest);
    }
    
    @AfterEach
    public void cleanUp() {
        //3. Unbind the subject from the current thread:
        clearSubject();    	
    }
    

    @Test
    public void loginLogoutRoundtrip() {
    	
        val secMan = SecurityUtils.getSecurityManager();
        assertNotNull(secMan);

        val subject = SecurityUtils.getSubject(); 
        assertNotNull(subject);
        assertFalse(subject.isAuthenticated());
        
        val token = (AuthenticationToken) new UsernamePasswordToken("sven", "pass");
		subject.login(token);
		assertTrue(subject.isAuthenticated());
		
		subject.logout();
		assertFalse(subject.isAuthenticated());
        
        
    }

    

}
