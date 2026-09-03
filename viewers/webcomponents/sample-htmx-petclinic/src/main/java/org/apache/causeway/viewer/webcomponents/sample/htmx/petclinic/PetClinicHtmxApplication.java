/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.persistence.jpa.eclipselink.CausewayModulePersistenceJpaEclipselink;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.viewer.graphql.viewer.CausewayModuleViewerGraphqlViewer;
import org.apache.causeway.viewer.webcomponents.htmx.CausewayModuleViewerWebcomponentsHtmx;
import org.apache.causeway.viewer.webcomponents.sample.petclinic.domain.PetClinicDomainModule;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;

@SpringBootApplication
@Import({
        CausewayModuleCoreRuntimeServices.class,
        CausewayModuleSecurityBypass.class,
        CausewayModulePersistenceJpaEclipselink.class,
        CausewayModuleViewerGraphqlViewer.class,
        CausewayModuleViewerWebcomponentsHtmx.class,
        CausewayModuleViewerWicketViewer.class,
        PetClinicDomainModule.class
})
@PropertySources({
        @PropertySource(CausewayPresets.H2InMemory_withUniqueSchema),
        @PropertySource(CausewayPresets.SilenceMetaModel),
        @PropertySource(CausewayPresets.SilenceProgrammingModel)
})
public class PetClinicHtmxApplication extends SpringBootServletInitializer {

    public static void main(final String[] args) {
        SpringApplication.run(PetClinicHtmxApplication.class, args);
    }

    @Bean
    SecurityFilterChain petClinicPermissiveSecurityFilterChain(final HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .build();
    }
}
