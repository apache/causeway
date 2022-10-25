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
package org.apache.causeway.security.keycloak.handler;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.apache.causeway.core.security.authentication.logout.LogoutHandler;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Propagates logouts to Keycloak.
 *
 * <p>
 * Necessary because Spring Security 5 (currently) doesn't support
 * end-session-endpoints.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogoutHandlerForKeycloak implements LogoutHandler {

    private final RestTemplate restTemplate;

    public LogoutHandlerForKeycloak() {
        this(new RestTemplate());
    }

    @Override public void logout() {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            propagateLogoutToKeycloak((OidcUser) authentication.getPrincipal());
        }

    }
    private void propagateLogoutToKeycloak(OidcUser user) {

        val endSessionEndpoint = String.format("%s/protocol/openid-connect/logout", user.getIssuer());

        val builder = UriComponentsBuilder
                .fromUriString(endSessionEndpoint)
                .queryParam("id_token_hint", user.getIdToken().getTokenValue());

        val logoutResponse = restTemplate.getForEntity(builder.toUriString(), String.class);
        if (logoutResponse.getStatusCode().is2xxSuccessful()) {
            log.info("Successfully logged out in Keycloak");
        } else {
            log.info("Could not propagate logout to Keycloak");
        }
    }

    @Override public boolean isHandlingCurrentThread() {
        return true;
    }
}
