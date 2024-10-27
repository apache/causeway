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
package org.apache.causeway.security.simple.authentication;

import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.core.security.authentication.Authenticator;
import org.apache.causeway.security.simple.CausewayModuleSecuritySimple;
import org.apache.causeway.security.simple.realm.SimpleRealm;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Simple in-memory {@link Authenticator} implementation.
 *
 * @since 2.x {@index}
 */
@Service
@Named(CausewayModuleSecuritySimple.NAMESPACE + ".SimpleAuthenticator")
@javax.annotation.Priority(PriorityPrecedence.LATE - 10) // ensure earlier than bypass
@Qualifier("Simple")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SimpleAuthenticator implements Authenticator {

    protected final SimpleRealm realm;
    protected final PasswordEncoder passwordEncoder;

    @Override
    public final boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
        return AuthenticationRequestPassword.class.isAssignableFrom(authenticationRequestClass);
    }

    @Override
    public final InteractionContext authenticate(
            final AuthenticationRequest request,
            final String validationCode) {

        if(request instanceof AuthenticationRequestPassword) {
            if (!isValid((AuthenticationRequestPassword) request)) {
                return null;
            }
        } else {
            return null; // request type not supported
        }

        var user = UserMemento.ofNameAndRoleNames(request.getName(), Stream.concat(
                    request.streamRoles(),
                    realm.lookupUserByName(request.getName()).orElseThrow()
                        .roles()
                        .stream()
                        .map(SimpleRealm.Role::name))
                )
                .withAuthenticationCode(validationCode);
        return InteractionContext.ofUserWithSystemDefaults(user);
    }

    @Override
    public final void logout() {
        // no-op
    }

    protected boolean isValid(final @NonNull AuthenticationRequestPassword request) {
        var plainPass = request.getPassword();
        return _Strings.isNullOrEmpty(plainPass)
            ? false
            : realm.lookupUserByName(request.getName())
                .map(SimpleRealm.User::encryptedPass)
                .map(encryptedPass->passwordEncoder.matches(request.getPassword(), encryptedPass))
                .orElse(false);
    }

}
