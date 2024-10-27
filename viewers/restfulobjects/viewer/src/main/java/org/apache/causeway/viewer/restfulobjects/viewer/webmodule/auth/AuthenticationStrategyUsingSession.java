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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;

/**
 * Checks that an already-present {@link InteractionContext authentication} (obtained from the {@link HttpSession}) is
 * still {@link org.apache.causeway.core.security.authentication.manager.AuthenticationManager#isSessionValid(InteractionContext) valid},
 * and re-binds the {@link InteractionContext authentication} onto the {@link HttpSession}.
 *
 * <p>
 *     Note that this implementation is not particularly &quot;restful&quot;; normally REST APIs are expected to be
 *     stateless whereas this implementation requires a session to obtain the {@link InteractionContext}.
 *     Typically it would be combined with Shiro, whose default behaviour (not suppressed by this filter) is indeed to
 *     store the {@link InteractionContext authentication} on the session.
 * </p>
 *
 * <p>
 * The session is looked-up from the {@link HttpSession} using the value
 * {@link AuthenticationStrategyUsingSession#HTTP_SESSION_AUTHENTICATION_SESSION_KEY}</li>
 * </ul>
 *
 * @since 2.0 {@index}
 */
public class AuthenticationStrategyUsingSession
extends AuthenticationStrategyAbstract {

    public static final String HTTP_SESSION_AUTHENTICATION_SESSION_KEY = AuthenticationStrategyUsingSession.class.getPackage().getName() + ".authentication";

    @Override
    public InteractionContext lookupValid(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse) {

        var authenticationManager = super.getAuthenticationManager(httpServletRequest);
        var httpSession = getHttpSession(httpServletRequest);

        // use previously authenticated session if available
        var authentication = (InteractionContext)
                httpSession.getAttribute(HTTP_SESSION_AUTHENTICATION_SESSION_KEY);
        if (authentication != null) {
            var sessionValid = authenticationManager.isSessionValid(authentication);
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

        var httpSession = getHttpSession(httpServletRequest);
        if(authentication != null) {
            httpSession.setAttribute(
                    HTTP_SESSION_AUTHENTICATION_SESSION_KEY, authentication);
        } else {
            httpSession.removeAttribute(HTTP_SESSION_AUTHENTICATION_SESSION_KEY);
        }
    }
}
