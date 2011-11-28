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

package org.apache.isis.security.dflt.authentication.inmemory;

import java.util.Map;

import org.apache.isis.core.commons.components.Noop;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.authentication.RegistrationDetails;
import org.apache.isis.core.runtime.authentication.standard.AuthenticatorAbstract;
import org.apache.isis.core.runtime.authentication.standard.Registrar;
import org.apache.isis.core.runtime.authentication.standard.RegistrationDetailsPassword;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class AuthenticatorInMemory extends AuthenticatorAbstract implements Registrar {
    
    private Map<String,String> users = Maps.newHashMap();

    public AuthenticatorInMemory(final IsisConfiguration configuration) {
        super(configuration);
    }

    @Override
    public boolean canAuthenticate(final AuthenticationRequest request) {
        return request instanceof AuthenticationRequestPassword;
    }

    @Override
    public boolean isValid(final AuthenticationRequest request) {
        final AuthenticationRequestPassword requestPassword = (AuthenticationRequestPassword) request;
        final String password = users.get(requestPassword.getName());
        return Objects.equal(requestPassword.getPassword(), password);
    }

    @Override
    public boolean canRegister(final RegistrationDetails registrationDetails) {
        return registrationDetails instanceof RegistrationDetailsPassword;
    }

    @Override
    public boolean register(final RegistrationDetails registrationDetails) {
        final RegistrationDetailsPassword registration = (RegistrationDetailsPassword) registrationDetails;
        final String user = registration.getUser();
        final String existingPassword = users.get(user);
        if(existingPassword != null) { 
            return false;
        }
        final String password = registration.getPassword();
        users.put(user, password);
        return true;
    }

}
