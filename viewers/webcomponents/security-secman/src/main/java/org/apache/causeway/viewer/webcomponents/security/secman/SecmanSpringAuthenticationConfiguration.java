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
package org.apache.causeway.viewer.webcomponents.security.secman;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;

@Configuration
public class SecmanSpringAuthenticationConfiguration {

    static final String AUTHENTICATION_PROVIDER = "webcomponentsSecmanAuthenticationProvider";

    @Bean
    SecmanUserDetailsService webcomponentsSecmanUserDetailsService(
            final ApplicationUserRepository applicationUserRepository,
            final InteractionService interactionService) {
        return new SecmanUserDetailsService(applicationUserRepository, interactionService);
    }

    @Bean
    AuthenticationConverterOfSecmanUserDetails webcomponentsAuthenticationConverterOfSecmanUserDetails() {
        return new AuthenticationConverterOfSecmanUserDetails();
    }

    @Bean(AUTHENTICATION_PROVIDER)
    DaoAuthenticationProvider webcomponentsSecmanAuthenticationProvider(
            final SecmanUserDetailsService userDetailsService,
            final @Qualifier("Secman") PasswordEncoder passwordEncoder) {
        final var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }
}
