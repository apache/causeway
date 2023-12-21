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
package org.apache.causeway.extensions.proxy.impl;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import jakarta.servlet.Filter;

/**
 * @since 3.0 {@index}
 */
@Configuration
@Qualifier("Proxy")
@Log4j2
public class CausewayModuleExtProxy {

    @Bean
    public FilterRegistrationBean<Filter> createCorsFilterRegistration(
            final CausewayConfiguration causewayConfiguration) {

        String proxyBase = "/proxy/";//restEasyConfiguration.getJaxrs().getDefaultPath();
        if (!proxyBase.endsWith("/*")) {
            proxyBase = proxyBase + "/*";
        }
        log.info("Setting up Proxy to allow base at '{}' with {}",
                proxyBase,
                causewayConfiguration.getExtensions().getProxy());

        final FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(createProxyFilter(causewayConfiguration));
        filterRegistrationBean.setUrlPatterns(Collections.singletonList(proxyBase));
        filterRegistrationBean.setOrder(PriorityPrecedence.EARLY - 100);

        return filterRegistrationBean;
    }

    private ProxyFilter createProxyFilter(CausewayConfiguration configuration) {

        val causewayProxyConfig = configuration.getExtensions().getProxy();

        //       corsConfiguration.setAllowCredentials(causewayCorsConfig.isAllowCredentials());

        val source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", causewayProxyConfig);

        final ProxyServer proxyServer = new ProxyServer();
        return new ProxyFilter(proxyServer);
    }

}
