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
package org.apache.causeway.core.webapp.modules.observation;

import jakarta.servlet.DispatcherType;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;

import org.apache.causeway.core.config.observation.CausewaySemanticTraceNamer;
import org.apache.causeway.core.config.observation.CausewayTraceClassifier;

@Configuration(proxyBeanMethods = false)
@Profile("observation")
public class WebObservationConfiguration {
    @Bean
    public FilterRegistrationBean<CausewayForegroundTraceFilter> causewayForegroundTraceFilter(
            CausewayTraceClassifier classifier, CausewaySemanticTraceNamer semanticTraceNamer) {
        var registration = new FilterRegistrationBean<>(new CausewayForegroundTraceFilter(classifier, semanticTraceNamer));
        registration.setName("CausewayForegroundTraceFilter");
        registration.addUrlPatterns("/*");
        // Boot's HTTP observation filter runs at HIGHEST_PRECEDENCE + 1.
        // Agent servlet instrumentation surrounds the whole filter chain.
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setAsyncSupported(true);
        return registration;
    }
}
