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
package demoapp.web;

import java.util.Optional;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.extensions.cors.impl.CausewayModuleExtCors;
import org.apache.causeway.extensions.secman.encryption.spring.CausewayModuleExtSecmanEncryptionSpring;
import org.apache.causeway.extensions.secman.integration.CausewayModuleExtSecmanIntegration;
import org.apache.causeway.extensions.secman.integration.authenticator.AuthenticatorSecmanAutoConfiguration;
import org.apache.causeway.testing.h2console.ui.CausewayModuleTestingH2ConsoleUi;
import org.apache.causeway.viewer.restfulobjects.viewer.CausewayModuleViewerRestfulObjectsViewer;

import lombok.extern.log4j.Log4j2;

import demoapp.dom._infra.fixtures.DemoFixtureScript;
import demoapp.web.security.PrototypeActionsVisibilityAdvisor;

/**
 * Makes the integral parts of the 'demo' web application.
 */
@Configuration
@Import({
    // Security Manager Extension (secman)
    CausewayModuleExtSecmanIntegration.class,
    CausewayModuleExtSecmanEncryptionSpring.class,
    AuthenticatorSecmanAutoConfiguration.class,

    // REST
    CausewayModuleViewerRestfulObjectsViewer.class,

    // CORS
    CausewayModuleExtCors.class,

    // H2 - PROTOTYPING
    CausewayModuleTestingH2ConsoleUi.class, // enables the H2 console menu item

    // services
    PrototypeActionsVisibilityAdvisor.class,

    // fixtures
    DemoFixtureScript.class,

})
@Log4j2
public class ReferenceAppManifestCommon {

    /**
     * If available from {@code System.getProperty("ContextPath")}
     * or {@code System.getenv("ContextPath")},
     * sets the context path for the web server. The context should start with a "/" character
     * but not end with a "/" character. The default context path can be
     * specified using an empty string.
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            var contextPath = Optional
                    .ofNullable(System.getProperty("ContextPath"))
                    .orElse(System.getenv("ContextPath")); // fallback
            if(contextPath!=null) {
                factory.setContextPath(contextPath);
                log.info("Setting context path to '{}'", contextPath);
            }
        };
    }

}
