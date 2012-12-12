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

package org.apache.isis.core.webapp.auth;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.exploration.AuthenticationRequestExploration;
import org.apache.isis.core.runtime.fixtures.authentication.AuthenticationRequestLogonFixture;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.webapp.WebAppConstants;

/**
 * Returns a valid {@link AuthenticationSession} through a number of mechanisms;
 * supports caching of the {@link AuthenticationSession} onto the
 * {@link HttpSession}.
 * 
 * <p>
 * The session is looked-up as follows:
 * <ul>
 * <li>it looks up from the {@link HttpSession} using the value
 * {@value WebAppConstants#HTTP_SESSION_AUTHENTICATION_SESSION_KEY}</li>
 * <li>failing that, if in exploration mode, then returns an exploration session
 * </li>
 * <li>failing that, if a {@link LogonFixture} has been provided and not already
 * used, will provide an session for that fixture. The {@link HttpSession} also
 * stores the value
 * {@value WebAppConstants#HTTP_SESSION_LOGGED_ON_PREVIOUSLY_USING_LOGON_FIXTURE_KEY}
 * in the session to track whether this has been done</li>
 * </ul>
 * <p>
 */
public class AuthenticationSessionStrategyDefault extends AuthenticationSessionStrategyAbstract {

    @Override
    public AuthenticationSession lookupValid(final ServletRequest servletRequest, final ServletResponse servletResponse) {

        final AuthenticationManager authenticationManager = getAuthenticationManager();
        final HttpSession httpSession = getHttpSession(servletRequest);

        // use previously authenticated session if available
        AuthenticationSession authSession = (AuthenticationSession) httpSession.getAttribute(WebAppConstants.HTTP_SESSION_AUTHENTICATION_SESSION_KEY);
        if (authSession != null) {
            final boolean sessionValid = authenticationManager.isSessionValid(authSession);
            if (sessionValid) {
                return authSession;
            }
        }

        // otherwise, look for LogonFixture and try to authenticate
        final ServletContext servletContext = getServletContext(servletRequest);
        final IsisSystem system = (IsisSystem) servletContext.getAttribute(WebAppConstants.ISIS_SYSTEM_KEY);
        if (system == null) {
            // not expected to happen...
            return null;
        }
        final LogonFixture logonFixture = system.getLogonFixture();

        // see if exploration is supported
        if (system.getDeploymentType().isExploring()) {
            authSession = authenticationManager.authenticate(new AuthenticationRequestExploration(logonFixture));
            if (authSession != null) {
                return authSession;
            }
        }

        final boolean loggedInUsingLogonFixture = httpSession.getAttribute(WebAppConstants.HTTP_SESSION_LOGGED_ON_PREVIOUSLY_USING_LOGON_FIXTURE_KEY) != null;
        if (logonFixture != null && !loggedInUsingLogonFixture) {
            httpSession.setAttribute(WebAppConstants.HTTP_SESSION_LOGGED_ON_PREVIOUSLY_USING_LOGON_FIXTURE_KEY, true);
            return authenticationManager.authenticate(new AuthenticationRequestLogonFixture(logonFixture));
        }

        return null;
    }

    @Override
    public void bind(final ServletRequest servletRequest, final ServletResponse servletResponse, final AuthenticationSession authSession) {
        final HttpSession httpSession = getHttpSession(servletRequest);
        httpSession.setAttribute(WebAppConstants.HTTP_SESSION_AUTHENTICATION_SESSION_KEY, authSession);
    }

    // //////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////

    protected AuthenticationManager getAuthenticationManager() {
        return IsisContext.getAuthenticationManager();
    }

}
