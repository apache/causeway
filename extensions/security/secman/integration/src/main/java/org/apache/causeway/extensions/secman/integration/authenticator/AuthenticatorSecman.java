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
package org.apache.causeway.extensions.secman.integration.authenticator;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.core.security.authentication.Authenticator;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * {@link Authenticator} implementation that authenticates the {@link ApplicationUser}.
 * <p>
 * Verifies that
 * <ul>
 * <li>the user exists</li>
 * <li>the user is UNLOCKED</li>
 * <li>the user has a persisted {@link ApplicationUser#getEncryptedPassword() encrypted password}</li>
 * <li>the provided raw-password, when encrypted, matches the persisted one</li>
 * </ul>
 * <p>
 * This Authenticator is a fallback and is only used if there is no other
 * implementation available.
 *
 * @since 2.0 {@index}
 */
@Log4j2
public class AuthenticatorSecman implements Authenticator {

    private final ApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Inject
    public AuthenticatorSecman(
            final ApplicationUserRepository applicationUserRepository,
            final @Qualifier("Secman") PasswordEncoder passwordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public final boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
        return AuthenticationRequestPassword.class.isAssignableFrom(authenticationRequestClass);
    }

    @Override
    public InteractionContext authenticate(final AuthenticationRequest request, final String code) {
        val authRequest = (AuthenticationRequestPassword) request;
        val username = authRequest.getName();
        val rawPassword = authRequest.getPassword();
        if(username == null) {
            log.info("login failed: username is null");
            return null;
        }

        return applicationUserRepository.findByUsername(username)
                // if user is LOCKED, then veto
                .filter(appUser -> ApplicationUserStatus.isUnlocked(appUser.getStatus()))
                // if user has no encrypted password persisted, then veto
                .filter(appUser -> appUser.isHasPassword())
                .filter(appUser -> passwordEncoder.matches(rawPassword, appUser.getEncryptedPassword()))
                .map(appUser -> {
                    val roleNames = Stream.concat(
                            appUser.getRoles().stream().map(ApplicationRole::getName),
                            request.streamRoles());
                    val user = UserMemento.ofNameAndRoleNames(username, roleNames)
                            .withAuthenticationCode(code);
                    return InteractionContext.ofUserWithSystemDefaults(user);
                })
                .orElse(null);
    }

    @Override
    public void logout() {
        // nothing needs to be done.  On logout the top-level AuthenticationManager
        // will invalidate the validation code held in the Authentication
        // object; this will cause the next http request made by the user to
        // be re-authenticated.
    }

}
