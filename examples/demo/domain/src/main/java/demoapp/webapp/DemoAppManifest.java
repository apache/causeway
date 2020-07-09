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
package demoapp.webapp;

import java.util.Optional;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.secman.encryption.jbcrypt.IsisModuleExtSecmanEncryptionJbcrypt;
import org.apache.isis.extensions.secman.jdo.IsisModuleExtSecmanPersistenceJdo;
import org.apache.isis.extensions.secman.model.IsisModuleExtSecmanModel;
import org.apache.isis.extensions.secman.shiro.IsisModuleExtSecmanRealmShiro;
import org.apache.isis.extensions.viewer.wicket.exceldownload.ui.IsisModuleExtExcelDownloadUi;
import org.apache.isis.security.shiro.IsisModuleSecurityShiro;
import org.apache.isis.testing.h2console.ui.IsisModuleTestingH2ConsoleUi;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;
import org.apache.isis.viewer.restfulobjects.viewer.IsisModuleViewerRestfulObjectsViewer;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.DemoModule;
import demoapp.dom._infra.fixtures.DemoFixtureScript;

/**
 * Makes the integral parts of the 'demo' web application.
 */
@Configuration
@Import({
    DemoModule.class, // shared demo core module
    
    // SECURITY
    IsisModuleSecurityShiro.class,

    // Security Manager Extension (secman)
    IsisModuleExtSecmanModel.class,
    IsisModuleExtSecmanRealmShiro.class,
    IsisModuleExtSecmanPersistenceJdo.class,
    IsisModuleExtSecmanEncryptionJbcrypt.class,
    
    // REST
    IsisModuleViewerRestfulObjectsViewer.class,
    IsisModuleViewerRestfulObjectsJaxrsResteasy4.class,

    // CORS
    //IsisModuleExtCorsImpl.class, // currently breaks Wicket
    
    IsisModuleTestingH2ConsoleUi.class,
    IsisModuleExtExcelDownloadUi.class, // allows for collection download as excel

    // fixtures
    DemoFixtureScript.class,

})
@Log4j2
public class DemoAppManifest {

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
            val contextPath = Optional
                    .ofNullable(System.getProperty("ContextPath"))
                    .orElse(System.getenv("ContextPath")); // fallback
            if(contextPath!=null) {
                factory.setContextPath(contextPath);
                log.info("Setting context path to '{}'", contextPath);
            }
        };
    }

}
