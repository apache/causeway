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
package org.apache.causeway.viewer.restfulobjects.viewer.webmodule.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;

/**
 * Implements the HTTP Basic Auth protocol; does not bind the
 * {@link InteractionContext} onto the {@link HttpSession}.
 *
 * @since 2.0 {@index}
 */
public class AuthenticationStrategyBasicAuth extends AuthenticationStrategyAbstract {

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String BASIC_AUTH_PREFIX = "Basic ";

    private static Pattern USER_AND_PASSWORD_REGEX = Pattern.compile("^(.+):(.+)$");

    @Override
    public InteractionContext lookupValid(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse) {

        var digest = getBasicAuthDigest(httpServletRequest);
        if (digest == null) {
            return null;
        }

        var userAndPassword = unencoded(digest);
        var matcher = USER_AND_PASSWORD_REGEX.matcher(userAndPassword);
        if (!matcher.matches()) {
            return null;
        }

        var user = matcher.group(1);
        var password = matcher.group(2);

        var authenticationRequestPwd = new AuthenticationRequestPassword(user, password);
        var authenticationManager = super.getAuthenticationManager(httpServletRequest);
        var authentication = authenticationManager.authenticate(authenticationRequestPwd);
        return authentication;
    }

    /**
     * This implementation is stateless and so does not support binding the {@link InteractionContext} (aka
     * authentication) into a store (eg a session); instead each request is authenticated afresh.
     */
    @Override
    public void bind(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            InteractionContext auth) {

    }

    // -- HELPER

    // value should be in the form:
    // Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    private String getBasicAuthDigest(final HttpServletRequest httpServletRequest) {
        var authStr = httpServletRequest.getHeader(HEADER_AUTHORIZATION);
        return authStr != null &&
                authStr.startsWith(BASIC_AUTH_PREFIX)
                ? authStr.substring(BASIC_AUTH_PREFIX.length())
                        : null;
    }

    protected String unencoded(final String encodedDigest) {
        return _Strings.ofBytes(
                _Bytes.decodeBase64(
                        Base64.getUrlDecoder(),
                        encodedDigest.getBytes()),
                StandardCharsets.UTF_8);
    }

}
