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

import javax.inject.Singleton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import org.apache.isis.config.presets.IsisPresets;
import org.apache.isis.testing.fixtures.applib.IsisModuleTstFixturesApplib;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationServiceAllowBeatsVeto;
import org.apache.isis.extensions.secman.encryption.jbcrypt.IsisModuleSecmanEncryptionJbcrypt;
import org.apache.isis.extensions.secman.jdo.IsisModuleSecmanPersistenceJdo;
import org.apache.isis.extensions.secman.model.IsisModuleSecmanModel;
import org.apache.isis.extensions.secman.shiro.IsisModuleSecmanRealmShiro;
import org.apache.isis.extensions.sse.applib.IsisModuleValSseApplib;
import org.apache.isis.incubator.model.metamodel.IsisModuleIncModelMetaModel;
import org.apache.isis.persistence.jdo.datanucleus5.IsisModuleJdoDataNucleus5;
import org.apache.isis.security.shiro.IsisModuleSecurityShiro;
import org.apache.isis.valuetypes.asciidoc.ui.IsisModuleValAsciidocUi;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleRestfulObjectsJaxrsResteasy4;
import org.apache.isis.viewer.restfulobjects.viewer.IsisModuleRestfulObjectsViewer;
import org.apache.isis.viewer.wicket.viewer.IsisModuleWicketViewer;
import org.apache.isis.webboot.springboot.IsisModuleSpringBoot;

import demoapp.dom.DemoModule;
import demoapp.utils.LibraryPreloadingService;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    DemoApp.AppManifest.class,
})
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
        IsisModuleSpringBoot.class,
        IsisModuleSecurityShiro.class,
        IsisModuleJdoDataNucleus5.class,
        IsisModuleWicketViewer.class,
        IsisModuleValSseApplib.class, // server sent events
        IsisModuleValAsciidocUi.class, // ascii-doc rendering support
        
        // REST
        IsisModuleRestfulObjectsViewer.class,
        IsisModuleRestfulObjectsJaxrsResteasy4.class,

        // Security Manager Extension (secman)
        IsisModuleSecmanModel.class,
        IsisModuleSecmanRealmShiro.class,
        IsisModuleSecmanPersistenceJdo.class,
        IsisModuleSecmanEncryptionJbcrypt.class,

        IsisModuleTstFixturesApplib.class,

        IsisModuleIncModelMetaModel.class, // @Model support (incubator)
        
        LibraryPreloadingService.class // just a performance enhancement

    })
    @ComponentScan(
            basePackageClasses= {
                    DemoModule.class
            }
    )
    public static class AppManifest {

        @Bean @Singleton
        public SecurityModuleConfig securityModuleConfigBean() {
            return SecurityModuleConfig.builder()
                    .adminUserName("sven")
                    .adminAdditionalPackagePermission("demoapp.dom")
                    .adminAdditionalPackagePermission("org.apache.isis")
                    .build();
        }

        @Bean @Singleton
        public PermissionsEvaluationService permissionsEvaluationService() {
            return new PermissionsEvaluationServiceAllowBeatsVeto();
        }
    }

}
