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
package org.apache.isis.viewer.restfulobjects.server.authentication;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.security.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.webapp.auth.AuthenticationSessionStrategyAbstract;

/**
 * Implements the HTTP Basic Auth protocol; does not bind the
 * {@link AuthenticationSession} onto the {@link HttpSession}.
 */
public class AuthenticationSessionStrategyBasicAuth extends AuthenticationSessionStrategyAbstract {

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String BASIC_AUTH_PREFIX = "Basic ";

    private static Pattern USER_AND_PASSWORD_REGEX = Pattern.compile("^(.+):(.+)$");

    @Override
    public AuthenticationSession lookupValid(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) {

        final String digest = getBasicAuthDigest(httpServletRequest);
        if (digest == null) {
            return null;
        }

        final String userAndPassword = unencoded(digest);
        final Matcher matcher = USER_AND_PASSWORD_REGEX.matcher(userAndPassword);
        if (!matcher.matches()) {
            return null;
        }

        final String user = matcher.group(1);
        final String password = matcher.group(2);

        final AuthenticationRequestPassword request = new AuthenticationRequestPassword(user, password);
        final AuthenticationSession authSession =
                authenticationManagerFrom(httpServletRequest).authenticate(request);
        return authSession;
    }

    // value should be in the form:
    // Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    String getBasicAuthDigest(final HttpServletRequest httpServletRequest) {
        final String authStr = httpServletRequest.getHeader(HEADER_AUTHORIZATION);
        return authStr != null &&
                authStr.startsWith(BASIC_AUTH_PREFIX)
                ? authStr.substring(BASIC_AUTH_PREFIX.length())
                        : null;
    }


    protected String unencoded(final String encodedDigest) {
        return _Strings.ofBytes(_Bytes.decodeBase64(Base64.getUrlDecoder(), encodedDigest.getBytes()), StandardCharsets.UTF_8);
    }


}
