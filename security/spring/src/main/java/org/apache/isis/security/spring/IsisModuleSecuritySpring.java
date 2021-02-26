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
package org.apache.isis.security.spring;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.security.spring.authentication.AuthenticatorSpring;
import org.apache.isis.security.spring.authorization.AuthorizorSpring;
import org.apache.isis.security.spring.webmodule.WebModuleSpringSecurity;

import lombok.extern.log4j.Log4j2;

/**
 * Configuration Bean to support authentication using Spring Security.
 *
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // modules
        IsisModuleCoreRuntimeServices.class,

        // @Service's
        AuthenticatorSpring.class,
        AuthorizorSpring.class,
        WebModuleSpringSecurity.class,

})
@Log4j2
public class IsisModuleSecuritySpring {

    @Qualifier("springSecurityFilterChain")
    @Inject private Filter springSecurityFilterChain;
    @Inject private IsisConfiguration isisConfiguration;
    
    @PostConstruct
    public void disableCsrf() {
        if(isisConfiguration.getSecurity().getSpring().isAllowCsrfFilters()) {
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
    
    private void disable(CsrfFilter csrfFilter) {
        log.info("disabling {}", csrfFilter.getClass().getName());
        // render the csrfFilter ineffective: filter never gets applied as matcher never matches
        csrfFilter.setRequireCsrfProtectionMatcher(request->false); 
    }
    
}
