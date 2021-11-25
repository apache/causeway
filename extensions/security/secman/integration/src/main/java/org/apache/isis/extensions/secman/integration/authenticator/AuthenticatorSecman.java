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
package org.apache.isis.extensions.secman.integration.authenticator;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.security.authentication.Authenticator;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * {@link Authenticator} implementation that authenticates the
 * {@link ApplicationUser}, first that the user exists and secondly that the
 * provided password matches the
 * {@link ApplicationUser#getEncryptedPassword() encrypted password} of the user.
 *
 * <p>
 *     This Authenticator is a fallback and is only used if there is no other
 *     implementation available.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Log4j2
//@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AuthenticatorSecman implements Authenticator {

    private final ApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Inject
    public AuthenticatorSecman(
            final ApplicationUserRepository applicationUserRepository,
            final @Qualifier("secman") PasswordEncoder passwordEncoder) {
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
    public void logout(final InteractionContext context) {
        // nothing needs to be done.  On logout the top-level AuthenticationManager
        // will invalidate the validation code held in the Authentication
        // object; this will cause the next http request made by the user to
        // be re-authenticated.
    }



}
