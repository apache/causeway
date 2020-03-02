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
package org.apache.isis.security.keycloak.authentication;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionTracker;
import org.apache.isis.core.security.authentication.standard.Authenticator;

@Service
@Named("isisSecurityKeycloak.AuthenticatorKeycloak")
@Order(OrderPrecedence.EARLY)
@Qualifier("Keycloak")
@Singleton
public class AuthenticatorKeycloak implements Authenticator {
    
    @Inject private AuthenticationSessionTracker authenticationSessionTracker;

    @Override
    public final boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
        return true;
    }

    @Override
    public AuthenticationSession authenticate(final AuthenticationRequest request, final String code) {
        // HTTP request filters should already have taken care of AuthenticationSession creation    
        return authenticationSessionTracker.currentAuthenticationSession().orElse(null);
    }

    @Override
    public void logout(final AuthenticationSession session) {
    }

}
