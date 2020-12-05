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

package org.apache.isis.viewer.restfulobjects.viewer.webmodule.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.isis.core.security.authentication.Authentication;

import lombok.val;

/**
 * Returns a valid {@link Authentication} through a number of mechanisms;
 * supports caching of the {@link Authentication} onto the
 * {@link HttpSession}.
 *
 * <p>
 * The session is looked-up as follows:
 * <ul>
 * <li>it looks up from the {@link HttpSession} using the value
 * {@link AuthenticationSessionStrategyDefault#HTTP_SESSION_AUTHENTICATION_SESSION_KEY}</li>
 * </ul>
 * 
 */
public class AuthenticationSessionStrategyDefault extends AuthenticationSessionStrategyAbstract {

    public static final String HTTP_SESSION_AUTHENTICATION_SESSION_KEY = AuthenticationSessionStrategyDefault.class.getPackage().getName() + ".authenticationSession";

    @Override
    public Authentication lookupValid(
            final HttpServletRequest httpServletRequest, 
            final HttpServletResponse httpServletResponse) {

        val authenticationManager = super.getAuthenticationManager(httpServletRequest);
        val httpSession = getHttpSession(httpServletRequest);

        // use previously authenticated session if available
        val authSession = (Authentication) 
                httpSession.getAttribute(HTTP_SESSION_AUTHENTICATION_SESSION_KEY);
        if (authSession != null) {
            val sessionValid = authenticationManager.isSessionValid(authSession);
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
            final Authentication authSession) {
        
        val httpSession = getHttpSession(httpServletRequest);
        if(authSession != null) {
            httpSession.setAttribute(
                    HTTP_SESSION_AUTHENTICATION_SESSION_KEY, authSession);
        } else {
            httpSession.removeAttribute(HTTP_SESSION_AUTHENTICATION_SESSION_KEY);
        }
    }




}
