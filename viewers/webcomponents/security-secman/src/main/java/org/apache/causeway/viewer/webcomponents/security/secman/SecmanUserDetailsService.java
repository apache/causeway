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

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;

final class SecmanUserDetailsService implements UserDetailsService {

    private static final String GENERIC_FAILURE = "Invalid username or password";

    private final ApplicationUserRepository applicationUserRepository;
    private final InteractionService interactionService;

    SecmanUserDetailsService(
            final ApplicationUserRepository applicationUserRepository,
            final InteractionService interactionService) {
        this.applicationUserRepository = applicationUserRepository;
        this.interactionService = interactionService;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        try {
            return interactionService.callAnonymous(() -> applicationUserRepository.findByUsername(username)
                    .map(applicationUser -> {
                        final var encryptedPassword = applicationUser.getEncryptedPassword();
                        if (encryptedPassword == null || encryptedPassword.isBlank()) {
                            throw new UsernameNotFoundException(GENERIC_FAILURE);
                        }
                        return new SecmanUserDetails(
                                applicationUser.getUsername(),
                                encryptedPassword,
                                ApplicationUserStatus.isUnlocked(applicationUser.getStatus()),
                                applicationUser.getRoles().stream().map(role -> role.getName()).toList(),
                                applicationUser.getAtPath(),
                                applicationUser.getLanguage(),
                                applicationUser.getNumberFormat(),
                                applicationUser.getTimeFormat());
                    })
                    .orElseThrow(() -> new UsernameNotFoundException(GENERIC_FAILURE)));
        } catch (UsernameNotFoundException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new InternalAuthenticationServiceException("Local authentication is temporarily unavailable", ex);
        }
    }
}
