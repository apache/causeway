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

package org.apache.isis.examples.onlinedemo.auth;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.authentication.RegistrationDetails;
import org.apache.isis.core.runtime.authentication.standard.AuthenticatorAbstract;
import org.apache.isis.core.runtime.authentication.standard.Registrar;
import org.apache.isis.core.runtime.authentication.standard.RegistrationDetailsPassword;

public class AuthenticatorInMemory extends AuthenticatorAbstract implements Registrar {

    private static AuthenticatorInMemory instance;

    /**
     * Rather nasty, but is required to make available to the Users repository.
     */
    public static AuthenticatorInMemory getInstance() {
        return instance;
    }

    private final Map<String, String> passwordsByUser = Maps.newHashMap();

    public AuthenticatorInMemory(final IsisConfiguration configuration) {
        super(configuration);
        instance = this;
    }

    @Override
    public final boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
        return AuthenticationRequestPassword.class.isAssignableFrom(authenticationRequestClass);
    }

    @Override
    public boolean isValid(final AuthenticationRequest authRequest) {
        final AuthenticationRequestPassword request = (AuthenticationRequestPassword) authRequest;
        final String userId = request.getName();
        return Objects.equal(request.getPassword(), passwordsByUser.get(userId));
    }

    @Override
    public boolean canRegister(final Class<? extends RegistrationDetails> registrationDetailsClass) {
        return RegistrationDetailsPassword.class.isAssignableFrom(registrationDetailsClass);
    }

    @Override
    public boolean register(final RegistrationDetails registrationDetails) {
        final RegistrationDetailsPassword registration = (RegistrationDetailsPassword) registrationDetails;
        final String userId = registration.getUser();
        if (passwordsByUser.containsKey(userId)) {
            return false;
        }
        passwordsByUser.put(userId, registration.getPassword());
        return true;
    }

}
