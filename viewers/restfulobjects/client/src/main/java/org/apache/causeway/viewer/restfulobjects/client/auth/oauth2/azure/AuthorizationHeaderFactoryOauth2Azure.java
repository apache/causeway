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
package org.apache.causeway.viewer.restfulobjects.client.auth.oauth2.azure;

import org.apache.causeway.viewer.restfulobjects.client.AuthenticationMode;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClientConfig;
import org.apache.causeway.viewer.restfulobjects.client.auth.AuthorizationHeaderFactory;
import org.apache.causeway.viewer.restfulobjects.client.auth.oauth2.Oauth2Creds;

import lombok.SneakyThrows;

public class AuthorizationHeaderFactoryOauth2Azure implements AuthorizationHeaderFactory {

    private final TokenCache tokenCache;

    public AuthorizationHeaderFactoryOauth2Azure(final RestfulClientConfig restfulClientConfig) {
        if (restfulClientConfig.getAuthenticationMode() != AuthenticationMode.OAUTH2_AZURE) {
            throw new IllegalArgumentException(String.format("config.authenticationMode must be '%s'", AuthenticationMode.OAUTH2_AZURE));
        }
        final var oauthCreds = oauthCredsFrom(restfulClientConfig);
        tokenCache = new TokenCache(oauthCreds);
    }

    private static Oauth2Creds oauthCredsFrom(final RestfulClientConfig restfulClientConfig) {
        if (restfulClientConfig.getOauthTenantId() == null) {
            throw new IllegalArgumentException("config.oauthTenantId must be set");
        }
        if (restfulClientConfig.getOauthClientId() == null) {
            throw new IllegalArgumentException("config.oauthClientId must be set");
        }
        if (restfulClientConfig.getOauthClientSecret() == null) {
            throw new IllegalArgumentException("config.oauthClientSecret must be set");
        }
        return Oauth2Creds.builder()
                .tenantId(restfulClientConfig.getOauthTenantId())
                .clientId(restfulClientConfig.getOauthClientId())
                .clientSecret(restfulClientConfig.getOauthClientSecret())
                .build();
    }

    @SneakyThrows
    @Override
    public String create() {
        var tokenResult = tokenCache.getToken();
        if (tokenResult.isFailure()) {
            // TODO: this will cause the invocation to fail; but should we fail more permanently somehow if a JWT token could not be obtained?
            throw tokenResult.getFailureElseFail();
        }
        var token = tokenResult.getSuccessElseFail();
        return "Bearer " + token;
    }
}
