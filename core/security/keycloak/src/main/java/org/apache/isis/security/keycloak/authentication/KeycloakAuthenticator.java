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

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.security.authentication.AuthenticationRequest;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.standard.Authenticator;
import org.apache.isis.security.keycloak.WebModuleKeycloak;
import org.apache.isis.webapp.wormhole.AuthenticationSessionWormhole;

@Log4j2 @NoArgsConstructor
public class KeycloakAuthenticator implements Authenticator {

    @Inject private IsisConfiguration configuration;


    // -- init, shutdown

    public void init() {
    }


    public void shutdown() {
    }

    // -- Authenticator API

    @Override
    public final boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
        return true;
    }

    @Override
    public AuthenticationSession authenticate(final AuthenticationRequest request, final String code) {
        return AuthenticationSessionWormhole.sessionByThread.get();
    }

    @Override
    public void logout(final AuthenticationSession session) {
    }


}
