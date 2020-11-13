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
package org.apache.isis.extensions.cors.impl;

import java.util.Collections;
import java.util.Map;

import javax.inject.Named;
import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Configuration
@Named("isisMapCors.WebModuleServerCors")
@Qualifier("CORS")
@Log4j2
public class IsisModuleExtCorsImpl {
    
    @Bean
    public FilterRegistrationBean<Filter> createCorsFilterRegistration(IsisConfiguration configuration) {

        final Map<String, String> cfgMap = configuration.getAsMap();
        final String resteasyBase = cfgMap.getOrDefault("resteasy.jaxrs.defaultPath", "/restful/*");
        log.info("Setting up CORS to filter resteasy-base at '{}' with {}", 
                resteasyBase, 
                configuration.getExtensions().getCors());

        final FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(createCorsFilter(configuration));
        filterRegistrationBean.setUrlPatterns(Collections.singletonList(resteasyBase));
        filterRegistrationBean.setOrder(OrderPrecedence.EARLY - 100);
        return filterRegistrationBean;
    }

    private CorsFilter createCorsFilter(IsisConfiguration configuration) {
        
        val isisCorsConfig = configuration.getExtensions().getCors();
        
        val corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(isisCorsConfig.getAllowedHeaders());
        corsConfiguration.setAllowedMethods(isisCorsConfig.getAllowedMethods());
        corsConfiguration.setAllowedOrigins(isisCorsConfig.getAllowedOrigins());
        corsConfiguration.setExposedHeaders(isisCorsConfig.getExposedHeaders());
        
        val source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsFilter(source);
    }
    

}
