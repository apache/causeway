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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.extensions.cors.impl.IsisModuleExtCorsImpl;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationServiceAllowBeatsVeto;
import org.apache.isis.extensions.secman.encryption.jbcrypt.IsisModuleExtSecmanEncryptionJbcrypt;
import org.apache.isis.extensions.secman.jdo.IsisModuleExtSecmanPersistenceJdo;
import org.apache.isis.extensions.secman.model.IsisModuleExtSecmanModel;
import org.apache.isis.extensions.secman.shiro.IsisModuleExtSecmanRealmShiro;
import org.apache.isis.extensions.viewer.wicket.exceldownload.ui.IsisModuleExtExcelDownloadUi;
import org.apache.isis.incubator.model.metamodel.IsisModuleIncModelMetaModel;
import org.apache.isis.incubator.viewer.vaadin.viewer.IsisModuleIncViewerVaadinViewer;
import org.apache.isis.persistence.jdo.datanucleus5.IsisModuleJdoDataNucleus5;
import org.apache.isis.security.shiro.IsisModuleSecurityShiro;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;
import org.apache.isis.valuetypes.asciidoc.ui.IsisModuleValAsciidocUi;
import org.apache.isis.valuetypes.sse.ui.IsisModuleValSseUi;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;
import org.apache.isis.viewer.restfulobjects.viewer.IsisModuleViewerRestfulObjectsViewer;
import org.apache.isis.viewer.wicket.viewer.IsisModuleViewerWicketViewer;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.DemoModule;
import demoapp.utils.LibraryPreloadingService;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    DemoApp.AppManifest.class,
})
@Log4j2
public class DemoApp extends SpringBootServletInitializer {

    /**
     * 
     * @param args
     * @implNote this is to support the <em>Spring Boot Maven Plugin</em>, which auto-detects an 
     * entry point by searching for classes having a {@code main(...)}
     */
    public static void main(String[] args) {
        //IsisPresets.prototyping();
        SpringApplication.run(new Class[] { DemoApp.class }, args);
    }

    /**
     * Makes the integral parts of the 'demo' web application.
     */
    @Configuration
    @PropertySources({
        @PropertySource(IsisPresets.HsqlDbInMemory),
        @PropertySource(IsisPresets.NoTranslations),
        @PropertySource(IsisPresets.SilenceWicket),
        @PropertySource(IsisPresets.DataNucleusAutoCreate),
    })
    @Import({
        IsisModuleCoreRuntimeServices.class,
        IsisModuleSecurityShiro.class,
        IsisModuleJdoDataNucleus5.class,
        IsisModuleViewerWicketViewer.class, // wicket viewer
        IsisModuleValSseUi.class, // server sent events
        IsisModuleValAsciidocUi.class, // ascii-doc rendering support

        // EXPERIMENTAL
        IsisModuleIncViewerVaadinViewer.class, // vaadin viewer
        
        // REST
        IsisModuleViewerRestfulObjectsViewer.class,
        IsisModuleViewerRestfulObjectsJaxrsResteasy4.class,
        
        // CORS
        IsisModuleExtCorsImpl.class,

        // Security Manager Extension (secman)
        IsisModuleExtSecmanModel.class,
        IsisModuleExtSecmanRealmShiro.class,
        IsisModuleExtSecmanPersistenceJdo.class,
        IsisModuleExtSecmanEncryptionJbcrypt.class,

        IsisModuleTestingFixturesApplib.class,

        IsisModuleIncModelMetaModel.class, // @Model support (incubator)
        IsisModuleExtExcelDownloadUi.class, // allows for collection download as excel
        
        LibraryPreloadingService.class // just a performance enhancement

    })
    @ComponentScan(
            basePackageClasses= {
                    DemoModule.class
            }
    )
    public static class AppManifest {

        @Bean
        public SecurityModuleConfig securityModuleConfigBean() {
            return SecurityModuleConfig.builder()
                    .adminUserName("sven")
                    .adminAdditionalPackagePermission("demoapp.dom")
                    .adminAdditionalPackagePermission("org.apache.isis")
                    .build();
        }

        @Bean
        public PermissionsEvaluationService permissionsEvaluationService() {
            return new PermissionsEvaluationServiceAllowBeatsVeto();
        }
        
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

}
