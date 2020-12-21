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
package demoapp.dom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;
import org.apache.isis.extensions.modelannotation.metamodel.IsisModuleExtModelAnnotation;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationServiceAllowBeatsVeto;
import org.apache.isis.legacy.jdo.datanucleus.IsisModuleJdoDataNucleus5;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;

@Configuration
@Import({
    IsisModuleCoreRuntimeServices.class,
    
//    IsisModuleJdoIntegration.class,
//    IsisModuleJdoSpring.class,
//    IsisModuleJdoProviderDataNucleus.class,
    
    IsisModuleJdoDataNucleus5.class,

    IsisModuleTestingFixturesApplib.class,

    IsisModuleExtModelAnnotation.class, // @Model support

    IsisModuleExtCommandLogImpl.class,

})
@PropertySources({
    @PropertySource(IsisPresets.H2InMemory),
    @PropertySource(IsisPresets.NoTranslations),
    @PropertySource(IsisPresets.SilenceWicket),
    @PropertySource(IsisPresets.DataNucleusAutoCreate),
})
@ComponentScan(
        basePackageClasses= {
                DemoModule.class
        })
public class DemoModule {
    
    @Bean
    public SecurityModuleConfig securityModuleConfigBean() {
        return SecurityModuleConfig.builder()
                .adminUserName("sven")
                .adminAdditionalPackagePermission("demoapp")
                .adminAdditionalPackagePermission("org.apache.isis")
                .build();
    }

    @Bean
    public PermissionsEvaluationService permissionsEvaluationService() {
        return new PermissionsEvaluationServiceAllowBeatsVeto();
    }
    
}
