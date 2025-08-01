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
package org.apache.causeway.security.spring;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.servlet.Filter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.security.spring.authconverters.AuthenticationConverterOfAuthenticatedPrincipal;
import org.apache.causeway.security.spring.authconverters.AuthenticationConverterOfStringPrincipal;
import org.apache.causeway.security.spring.authconverters.AuthenticationConverterOfUserDetailsPrincipal;
import org.apache.causeway.security.spring.authentication.AuthenticatorSpring;
import org.apache.causeway.security.spring.webmodule.WebModuleSpringSecurity;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration Bean to support authentication using Spring Security.
 *
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // modules
        CausewayModuleCoreRuntimeServices.class,

        // @Component's
        AuthenticationConverterOfAuthenticatedPrincipal.class,
        AuthenticationConverterOfStringPrincipal.class,
        AuthenticationConverterOfUserDetailsPrincipal.class,

        // @Service's
        AuthenticatorSpring.class,
        WebModuleSpringSecurity.class,

})
@Slf4j
public class CausewayModuleSecuritySpring {

    public static final String NAMESPACE = "causeway.security.spring";

    @Qualifier("springSecurityFilterChain")
    @Inject private Filter springSecurityFilterChain;
    @Inject private CausewayConfiguration causewayConfiguration;

    @PostConstruct
    public void disableCsrf() {
        if(causewayConfiguration.security().spring().allowCsrfFilters()) {
            return; // don't interfere
        }
        log.debug("About to disable any CSRF filters.");
        FilterChainProxy filterChainProxy = (FilterChainProxy) springSecurityFilterChain;
        List<SecurityFilterChain> list = filterChainProxy.getFilterChains();
        list.stream()
          .flatMap(chain -> chain.getFilters().stream())
          .filter(filter->filter instanceof CsrfFilter)
          .map(CsrfFilter.class::cast)
          .forEach(this::disable);
    }

    private void disable(final CsrfFilter csrfFilter) {
        log.info("disabling {}", csrfFilter.getClass().getName());
        // render the csrfFilter ineffective: filter never gets applied as matcher never matches
        csrfFilter.setRequireCsrfProtectionMatcher(request->false);
    }

}
