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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;

import lombok.val;

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

        // Basic auth should never create sessions!
        // However, telling this Shiro here, is a fragile approach.
        //TODO[2156] do this somewhere else (more coupled with shiro)
        httpServletRequest.setAttribute(
                "org.apache.shiro.subject.support.DefaultSubjectContext.SESSION_CREATION_ENABLED",
                Boolean.FALSE);


        val digest = getBasicAuthDigest(httpServletRequest);
        if (digest == null) {
            return null;
        }

        val userAndPassword = unencoded(digest);
        val matcher = USER_AND_PASSWORD_REGEX.matcher(userAndPassword);
        if (!matcher.matches()) {
            return null;
        }

        val user = matcher.group(1);
        val password = matcher.group(2);

        val authenticationRequestPwd = new AuthenticationRequestPassword(user, password);
        val authenticationManager = super.getAuthenticationManager(httpServletRequest);
        val authentication = authenticationManager.authenticate(authenticationRequestPwd);
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
        val authStr = httpServletRequest.getHeader(HEADER_AUTHORIZATION);
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
