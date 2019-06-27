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

package org.apache.isis.webapp.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.isis.config.WebAppConstants;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.manager.AuthenticationManager;

/**
 * Returns a valid {@link AuthenticationSession} through a number of mechanisms;
 * supports caching of the {@link AuthenticationSession} onto the
 * {@link HttpSession}.
 *
 * <p>
 * The session is looked-up as follows:
 * <ul>
 * <li>it looks up from the {@link HttpSession} using the value
 * {@link WebAppConstants#HTTP_SESSION_AUTHENTICATION_SESSION_KEY}</li>
 * <li>failing that, if a {@link LogonFixture} has been provided and not already
 * used, will provide an session for that fixture. The {@link HttpSession} also
 * stores the value
 * {@link WebAppConstants#HTTP_SESSION_LOGGED_ON_PREVIOUSLY_USING_LOGON_FIXTURE_KEY}
 * in the session to track whether this has been done</li>
 * </ul>
 * <p>
 */
public class AuthenticationSessionStrategyDefault extends AuthenticationSessionStrategyAbstract {

    @Override
    public AuthenticationSession lookupValid(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) {

        final AuthenticationManager authenticationManager = authenticationManagerFrom(httpServletRequest);
        final HttpSession httpSession = getHttpSession(httpServletRequest);

        // use previously authenticated session if available
        AuthenticationSession authSession = (AuthenticationSession) httpSession.getAttribute(WebAppConstants.HTTP_SESSION_AUTHENTICATION_SESSION_KEY);
        if (authSession != null) {
            final boolean sessionValid = authenticationManager.isSessionValid(authSession);
            if (sessionValid) {
                return authSession;
            }
        }

        return null;
    }

    @Override
    public void bind(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final AuthenticationSession authSession) {
        final HttpSession httpSession = getHttpSession(httpServletRequest);
        if(authSession != null) {
            httpSession.setAttribute(WebAppConstants.HTTP_SESSION_AUTHENTICATION_SESSION_KEY, authSession);
        } else {
            httpSession.removeAttribute(WebAppConstants.HTTP_SESSION_AUTHENTICATION_SESSION_KEY);
        }
    }




}
