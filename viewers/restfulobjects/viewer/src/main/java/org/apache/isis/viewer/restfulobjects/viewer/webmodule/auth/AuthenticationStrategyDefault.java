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

import org.apache.isis.applib.services.iactnlayer.InteractionContext;

import lombok.val;

/**
 * Returns a valid {@link InteractionContext} through a number of mechanisms;
 * supports caching of the {@link InteractionContext} onto the
 * {@link HttpSession}.
 *
 * <p>
 * The session is looked-up as follows:
 * <ul>
 * <li>it looks up from the {@link HttpSession} using the value
 * {@link AuthenticationStrategyDefault#HTTP_SESSION_AUTHENTICATION_SESSION_KEY}</li>
 * </ul>
 *
 * @since 2.0 {@index}
 */
public class AuthenticationStrategyDefault
extends AuthenticationStrategyAbstract {

    public static final String HTTP_SESSION_AUTHENTICATION_SESSION_KEY = AuthenticationStrategyDefault.class.getPackage().getName() + ".authentication";

    @Override
    public InteractionContext lookupValid(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse) {

        val authenticationManager = super.getAuthenticationManager(httpServletRequest);
        val httpSession = getHttpSession(httpServletRequest);

        // use previously authenticated session if available
        val authentication = (InteractionContext)
                httpSession.getAttribute(HTTP_SESSION_AUTHENTICATION_SESSION_KEY);
        if (authentication != null) {
            val sessionValid = authenticationManager.isSessionValid(authentication);
            if (sessionValid) {
                return authentication;
            }
        }

        return null;
    }

    @Override
    public void bind(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final InteractionContext authentication) {

        val httpSession = getHttpSession(httpServletRequest);
        if(authentication != null) {
            httpSession.setAttribute(
                    HTTP_SESSION_AUTHENTICATION_SESSION_KEY, authentication);
        } else {
            httpSession.removeAttribute(HTTP_SESSION_AUTHENTICATION_SESSION_KEY);
        }
    }




}
