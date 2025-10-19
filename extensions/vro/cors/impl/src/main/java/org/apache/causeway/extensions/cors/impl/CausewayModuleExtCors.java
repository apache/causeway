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
package org.apache.causeway.extensions.cors.impl;

import java.util.Collections;

import jakarta.servlet.Filter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.applib.RestfulPathProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Qualifier("CORS")
@Slf4j
public class CausewayModuleExtCors {

    @Bean
    public FilterRegistrationBean<Filter> createCorsFilterRegistration(
            final CausewayConfiguration causewayConfiguration) {

        var restfulPathProvider = new RestfulPathProvider(causewayConfiguration);

        String restfulBase = restfulPathProvider.getRestfulPath().orElse("");
        if(!restfulBase.endsWith("/*")) {
            restfulBase = restfulBase + "/*";
        }
        log.info("Setting up CORS to filter RO base path at '{}' with {}",
                restfulBase,
                causewayConfiguration.extensions().cors());

        final FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(createCorsFilter(causewayConfiguration));
        filterRegistrationBean.setUrlPatterns(Collections.singletonList(restfulBase));
        filterRegistrationBean.setOrder(PriorityPrecedence.EARLY - 100);

        return filterRegistrationBean;
    }

    private CorsFilter createCorsFilter(final CausewayConfiguration configuration) {

        var causewayCorsConfig = configuration.extensions().cors();

        var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(causewayCorsConfig.allowCredentials());
        corsConfiguration.setAllowedHeaders(causewayCorsConfig.allowedHeaders());
        corsConfiguration.setAllowedMethods(causewayCorsConfig.allowedMethods());
        corsConfiguration.setAllowedOrigins(causewayCorsConfig.allowedOrigins());
        corsConfiguration.setExposedHeaders(causewayCorsConfig.exposedHeaders());

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }

}
