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
package demoapp.application;

import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.io.ClassPathResource;

import org.apache.isis.config.IsisPresets;
import org.apache.isis.config.beans.WebAppConfigBean;
import org.apache.isis.extensions.fixtures.ExtFixturesModule;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationServiceAllowBeatsVeto;
import org.apache.isis.extensions.secman.encryption.jbcrypt.IsisBootSecmanEncryptionJbcrypt;
import org.apache.isis.extensions.secman.jdo.IsisBootSecmanPersistenceJdo;
import org.apache.isis.extensions.secman.model.IsisBootSecmanModel;
import org.apache.isis.extensions.secman.shiro.IsisBootSecmanRealmShiro;
import org.apache.isis.extensions.sse.IsisBootSse;
import org.apache.isis.jdo.IsisBootDataNucleus;
import org.apache.isis.runtime.spring.IsisBoot;
import org.apache.isis.security.shiro.IsisBootSecurityShiro;
import org.apache.isis.viewer.restfulobjects.IsisBootViewerRestfulObjects;
import org.apache.isis.viewer.wicket.viewer.IsisBootViewerWicket;

import demoapp.dom.DemoModule;
import demoapp.utils.LibraryPreloadingService;

/**
 * Makes the integral parts of the 'demo' web application.
 */
@Configuration
@PropertySources({
    @PropertySource("classpath:/isis-non-changing.properties"),
    @PropertySource(IsisPresets.HsqlDbInMemory),
    @PropertySource(IsisPresets.NoTranslations),
    @PropertySource(IsisPresets.SilenceWicket),
})
@Import({
    IsisBoot.class,
    IsisBootSecurityShiro.class,
    IsisBootDataNucleus.class,
    IsisBootViewerWicket.class,
    IsisBootSse.class, // server sent events
    IsisBootViewerRestfulObjects.class,

    // Security Manager Extension (secman)
    IsisBootSecmanModel.class,
    IsisBootSecmanRealmShiro.class,
    IsisBootSecmanPersistenceJdo.class,
    IsisBootSecmanEncryptionJbcrypt.class,

    ExtFixturesModule.class,
    
    LibraryPreloadingService.class // just a performance enhancement

})
@ComponentScan(
        basePackageClasses= {
                DemoModule.class
        }
)
public class DemoAppManifest {

    @Bean @Singleton
    public WebAppConfigBean webAppConfigBean() {
        
        return WebAppConfigBean.builder()
                .applicationName("Isis Demo App")
                .menubarsLayoutXml(new ClassPathResource("menubars.layout.xml", this.getClass()))
                .brandLogoHeader("/images/gift_48.png")
                .applicationCss("css/application.css")
                .applicationJs("scripts/application.js")
                .faviconUrl("/images/favicon.png")
                .build();
    }

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
