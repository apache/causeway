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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.webapp.auth.AuthenticationSessionStrategyAbstract;

/**
 * Implements the HTTP Basic Auth protocol; does not bind the
 * {@link AuthenticationSession} onto the {@link HttpSession}.
 */
public class AuthenticationSessionStrategyBasicAuth extends AuthenticationSessionStrategyAbstract {

    private static Pattern USER_AND_PASSWORD_REGEX = Pattern.compile("^(.+):(.+)$");

    @Override
    public AuthenticationSession lookupValid(final ServletRequest servletRequest, final ServletResponse servletResponse) {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final String authStr = httpServletRequest.getHeader("Authorization");

        // value should be in the form:
        // Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
        if (authStr == null || !authStr.startsWith("Basic ")) {
            return null;
        }
        final String digest = authStr.substring(6);

        final String userAndPassword = new String(new Base64().decode(digest.getBytes()));
        final Matcher matcher = USER_AND_PASSWORD_REGEX.matcher(userAndPassword);
        if (!matcher.matches()) {
            return null;
        }

        final String user = matcher.group(1);
        final String password = matcher.group(2);

        final AuthenticationSession authSession = getAuthenticationManager().authenticate(new AuthenticationRequestPassword(user, password));
        return authSession;
    }

    // //////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////

    protected AuthenticationManager getAuthenticationManager() {
        return IsisContext.getAuthenticationManager();
    }

}
