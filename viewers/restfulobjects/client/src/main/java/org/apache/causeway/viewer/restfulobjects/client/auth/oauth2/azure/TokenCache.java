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

import java.time.ZonedDateTime;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.causeway.commons.functional.Railway;
import org.apache.causeway.viewer.restfulobjects.client.auth.oauth2.Oauth2Creds;

public class TokenCache {

    private final Oauth2Creds creds;
    private final ClientBuilder clientBuilder;

    public TokenCache(final Oauth2Creds creds) {
        this.creds = creds;
        this.clientBuilder = ClientBuilder.newBuilder();
    }

    private String jwtToken;
    private ZonedDateTime jwtTokenExpiresAt;

    public Railway<Exception, String> getToken() {

        if (isTokenValid()) {
            return Railway.success(jwtToken);
        }

        var client = clientBuilder.build();
        var webTarget = client.target(UriBuilder.fromUri(String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", creds.getTenantId())));
        var invocationBuilder = webTarget.request()
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .header(HttpHeaders.ACCEPT, "application/json")
                ;
        var form = new Form().param("scope", String.format("%s/.default", creds.getClientId()))
                .param("client_id", creds.getClientId())
                .param("client_secret", creds.getClientSecret())
                .param("grant_type", "client_credentials");

        var invocation = invocationBuilder.buildPost(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        var response = invocation.invoke();

        var entity = response.readEntity(String.class);

        if (response.getStatus() != 200) {

            this.jwtToken = null;
            this.jwtTokenExpiresAt = null;

            return Railway.failure(new RuntimeException(entity));
        }

        var result = new TokenParser().parseTokenEntity(entity);
        if (result.isFailure()) {
            this.jwtToken = null;
            this.jwtTokenExpiresAt = null;

            return Railway.failure(result.getFailureElseFail());
        }

        var tsr = result.getSuccessElseFail();

        int expiresIn = tsr.getExpires_in();
        this.jwtToken = tsr.getAccess_token();
        this.jwtTokenExpiresAt = now().plusMinutes(expiresIn);

        return Railway.success(jwtToken);
    }

    private boolean isTokenValid() {
        if (jwtTokenExpiresAt == null) {
            return false;
        }

        // token must remain valid for at least 2 additional minutes from now.
        var inFiveMinutesTime = now().plusMinutes(2);
        return jwtTokenExpiresAt.isBefore(inFiveMinutesTime);
    }

    private static ZonedDateTime now() {
        return ZonedDateTime.now();
    }

}
