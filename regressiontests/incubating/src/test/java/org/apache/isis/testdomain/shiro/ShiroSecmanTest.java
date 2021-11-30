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
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.extensions.secman.encryption.spring.IsisModuleExtSecmanEncryptionSpring;
import org.apache.isis.extensions.secman.integration.IsisModuleExtSecmanIntegration;
import org.apache.isis.extensions.secman.jdo.IsisModuleExtSecmanPersistenceJdo;
import org.apache.isis.extensions.secman.shiro.IsisModuleExtSecmanRealmShiro;
import org.apache.isis.testdomain.conf.Configuration_usingJdoAndShiro;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdoAndShiro.class,
        })
@Import({
    // Security Manager Extension (secman)
    IsisModuleExtSecmanIntegration.class,
    IsisModuleExtSecmanRealmShiro.class,
    IsisModuleExtSecmanPersistenceJdo.class,
    IsisModuleExtSecmanEncryptionSpring.class,
})
@TestPropertySource(IsisPresets.UseLog4j2Test)
class ShiroSecmanTest extends AbstractShiroTest {

    @Inject IsisConfiguration isisConfig;
    @Inject ServiceInjector serviceInjector;

    @BeforeEach
    void beforeEach() {
        setSecurityManager(serviceInjector, "classpath:shiro-secman-ldap.ini");
    }

    @AfterEach
    void afterEach() {
        tearDownShiro();
    }

    @Test
    void loginLogoutRoundtrip() {

        val secMan = getSecurityManager();
        assertNotNull(secMan);

        val subject = SecurityUtils.getSubject();
        assertNotNull(subject);
        assertFalse(subject.isAuthenticated());

        val token = new UsernamePasswordToken(
                isisConfig.getExtensions().getSecman().getSeed().getAdmin().getUserName(),
                isisConfig.getExtensions().getSecman().getSeed().getAdmin().getPassword());

        subject.login(token);
        assertTrue(subject.isAuthenticated());

        subject.logout();
        assertFalse(subject.isAuthenticated());

    }

    @Test
    void login_withInvalidPassword() {

        val secMan = SecurityUtils.getSecurityManager();
        assertNotNull(secMan);

        val subject = SecurityUtils.getSubject();
        assertNotNull(subject);
        assertFalse(subject.isAuthenticated());

        val token = new UsernamePasswordToken(
                isisConfig.getExtensions().getSecman().getSeed().getAdmin().getUserName(),
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

        val token = new UsernamePasswordToken(
                "non-existent-user",
                "invalid-pass");

        assertThrows(CredentialsException.class, ()->{
            subject.login(token);
        });

        assertFalse(subject.isAuthenticated());


    }


}
