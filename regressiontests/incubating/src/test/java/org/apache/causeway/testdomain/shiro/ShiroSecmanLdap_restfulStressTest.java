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
package org.apache.causeway.testdomain.shiro;

import static java.time.Duration.ofMillis;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.extensions.secman.delegated.shiro.realm.CausewayModuleExtSecmanShiroRealm;
import org.apache.causeway.extensions.secman.encryption.spring.CausewayModuleExtSecmanEncryptionSpring;
import org.apache.causeway.extensions.secman.integration.CausewayModuleExtSecmanIntegration;
import org.apache.causeway.extensions.secman.jdo.CausewayModuleExtSecmanPersistenceJdo;
import org.apache.causeway.security.shiro.webmodule.WebModuleShiro;
import org.apache.causeway.testdomain.conf.Configuration_usingJdoAndShiro;
import org.apache.causeway.testdomain.jdo.JdoTestDomainPersona;
import org.apache.causeway.testdomain.ldap.LdapServerService;
import org.apache.causeway.testdomain.util.rest.RestEndpointService;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.causeway.viewer.restfulobjects.viewer.CausewayModuleViewerRestfulObjectsViewer;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdoAndShiro.class
        },
        properties = {
                //"logging.config=log4j2-test.xml",
                "logging.config=log4j2-debug-persistence.xml",
                "server.servlet.session.persistent=false", // defaults to false
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({

    // Restful server
    CausewayModuleViewerRestfulObjectsViewer.class,
    RestEndpointService.class,

    // Embedded LDAP server for testing
    LdapServerService.class,

    // Security Manager Extension (secman)
    CausewayModuleExtSecmanIntegration.class,
    CausewayModuleExtSecmanShiroRealm.class,
    CausewayModuleExtSecmanPersistenceJdo.class,
    CausewayModuleExtSecmanEncryptionSpring.class,
})
@PropertySources({
    @PropertySource(CausewayPresets.DatanucleusAutocreateNoValidate)
})
//XXX "does not work, when executed in sequence with other regressiontests
class ShiroSecmanLdap_restfulStressTest extends AbstractShiroTest {

    @Inject FixtureScripts fixtureScripts;
    @Inject RestEndpointService restService;
    @Inject LdapServerService ldapServerService;
//    @Inject ApplicationUserRepository applicationUserRepository;
//    @Inject ApplicationRoleRepository applicationRoleRepository;
//    @Inject SecmanConfiguration securityConfig;
    @Inject ServiceInjector serviceInjector;

    @BeforeAll
    static void setup() {
        WebModuleShiro.setShiroIniResource("classpath:shiro-secman-ldap.ini");
    }

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
    void stressTheRestEndpoint() {

        val useRequestDebugLogging = false;
        val restfulClient = restService.newClient(useRequestDebugLogging);

        assertTimeout(ofMillis(5000), ()->{

            for(int i=0; i<100; ++i) {
                val digest = restService.getHttpSessionInfo(restfulClient)
                        .ifFailure(Assertions::fail);

                val httpSessionInfo = digest.getValue().orElseThrow();

                assertNotNull(httpSessionInfo);

                // impersonation in UserService means that we _do_ now get
                // an httpSession as a side-effect
                //assertEquals("no http-session", httpSessionInfo);
                assertEquals("http-session attribute names: {}", httpSessionInfo);

            }

        });


    }

}
