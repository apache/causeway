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

import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;
import org.apache.isis.extensions.secman.encryption.jbcrypt.IsisModuleSecmanEncryptionJbcrypt;
import org.apache.isis.extensions.secman.jdo.IsisModuleSecmanPersistenceJdo;
import org.apache.isis.extensions.secman.model.IsisModuleSecmanModel;
import org.apache.isis.extensions.secman.shiro.IsisModuleSecmanRealmShiro;
import org.apache.isis.testdomain.Incubating;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdoAndShiro;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.ldap.LdapConstants;
import org.apache.isis.testdomain.ldap.LdapServerService;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJdoAndShiro.class, 
        }, 
        properties = {
                //"logging.config=log4j2-test.xml",
                "logging.config=log4j2-debug-persistence.xml",
                //IsisPresets.DebugPersistence,
                "isis.persistor.datanucleus.impl.datanucleus.schema.autoCreateDatabase=true",
        })
@Import({

    // Embedded LDAP server for testing
    LdapServerService.class,

    // Security Manager Extension (secman)
    IsisModuleSecmanModel.class,
    IsisModuleSecmanRealmShiro.class,
    IsisModuleSecmanPersistenceJdo.class,
    IsisModuleSecmanEncryptionJbcrypt.class,
})
@Incubating("does not work with surefire")
class ShiroSecmanLdapTest extends AbstractShiroTest {

    @Inject FixtureScripts fixtureScripts;
    @Inject LdapServerService ldapServerService;
    @Inject ApplicationUserRepository applicationUserRepository;
    @Inject ApplicationRoleRepository applicationRoleRepository;
    @Inject SecurityModuleConfig securityConfig;
    @Inject ServiceInjector serviceInjector;

    @BeforeEach
    void beforeEach() {
        
        setSecurityManager(serviceInjector, "classpath:shiro-secman-ldap.ini");
        
        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.SvenApplicationUser);
    }
    
    @AfterEach
    void afterEach() {
        tearDownShiro();
    }

    @Test
    void loginLogoutRoundtrip() {

        val secMan = SecurityUtils.getSecurityManager();
        assertNotNull(secMan);

        val subject = SecurityUtils.getSubject(); 
        assertNotNull(subject);
        assertFalse(subject.isAuthenticated());

        val token = (AuthenticationToken) new UsernamePasswordToken(
                LdapConstants.SVEN_PRINCIPAL,
                "pass");

        subject.login(token);
        assertTrue(subject.isAuthenticated());

        subject.logout();
        assertFalse(subject.isAuthenticated());

    }

    @Test
    void login_withAccountOnlyKnownToLdap() {

        val secMan = getSecurityManager();
        assertNotNull(secMan);

        val subject = SecurityUtils.getSubject(); 
        assertNotNull(subject);
        assertFalse(subject.isAuthenticated());

        val username = LdapConstants.OLAF_PRINCIPAL;
        val token = (AuthenticationToken) new UsernamePasswordToken(
                username,
                "pass");

        // default behavior is to create the account within the DB but leave it disabled 
        assertThrows(DisabledAccountException.class, ()->{
            subject.login(token);
        });

        val olafUser = applicationUserRepository.findByUsername(username);
        assertNotNull(olafUser);
        assertNotNull(olafUser.getStatus());
        assertFalse(olafUser.getStatus().isEnabled());
    }

    @Test
    void login_withInvalidPassword() {

        val secMan = SecurityUtils.getSecurityManager();
        assertNotNull(secMan);
        
        val subject = SecurityUtils.getSubject(); 
        assertNotNull(subject);
        assertFalse(subject.isAuthenticated());

        val token = (AuthenticationToken) new UsernamePasswordToken(
                LdapConstants.SVEN_PRINCIPAL,
                "invalid-pass");

        assertThrows(CredentialsException.class, ()->{
            subject.login(token);
        });

        assertFalse(subject.isAuthenticated());

    }

    @Test
    void login_withNonExistentUser() {

        val secMan = SecurityUtils.getSecurityManager();
        assertNotNull(secMan);

        val subject = SecurityUtils.getSubject(); 
        assertNotNull(subject);
        assertFalse(subject.isAuthenticated());

        val username = "non-existent-user";
        val token = (AuthenticationToken) new UsernamePasswordToken(
                username,
                "invalid-pass");

        assertThrows(CredentialsException.class, ()->{
            subject.login(token);
        });

        assertFalse(subject.isAuthenticated());

        val nonUser = applicationUserRepository.findByUsername(username);
        assertNull(nonUser);

    }


}
