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

package org.apache.isis.runtimes.dflt.webapp.auth;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtimes.dflt.runtime.authentication.exploration.AuthenticationRequestExploration;
import org.apache.isis.runtimes.dflt.runtime.fixtures.authentication.AuthenticationRequestLogonFixture;
import org.apache.isis.runtimes.dflt.runtime.system.IsisSystem;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.webapp.WebAppConstants;

/**
 * Looks up from the {@link HttpSession}, failing that attempts to logon using a {@link LogonFixture} (if available and
 * not already used) or in exploration mode.
 * 
 * <p>
 * The {@link AuthenticationSession} is looked up from the {@link HttpSession} using the value
 * {@value WebAppConstants#HTTP_SESSION_AUTHENTICATION_SESSION_KEY}. Because we only want to use the
 * {@link LogonFixture} once, the {@link HttpSession} also stores the value
 * {@value WebAppConstants#HTTP_SESSION_LOGGED_ON_PREVIOUSLY_USING_LOGON_FIXTURE_KEY} once a logon has occurred
 * implicitly.
 */
public class AuthenticationSessionLookupStrategyDefault extends AuthenticationSessionLookupStrategyAbstract {

    @Override
    public AuthenticationSession lookup(final ServletRequest servletRequest, final ServletResponse servletResponse) {

        // use previously authenticated session if available.
        final HttpSession httpSession = getHttpSession(servletRequest);
        AuthenticationSession authSession =
            (AuthenticationSession) httpSession.getAttribute(WebAppConstants.HTTP_SESSION_AUTHENTICATION_SESSION_KEY);
        if (authSession != null) {
            final boolean sessionValid = getAuthenticationManager().isSessionValid(authSession);
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
            authSession = getAuthenticationManager().authenticate(new AuthenticationRequestExploration(logonFixture));
            if (authSession != null) {
                return authSession;
            }
        }

        final boolean loggedInUsingLogonFixture =
            httpSession.getAttribute(WebAppConstants.HTTP_SESSION_LOGGED_ON_PREVIOUSLY_USING_LOGON_FIXTURE_KEY) != null;
        if (logonFixture != null && !loggedInUsingLogonFixture) {
            httpSession.setAttribute(WebAppConstants.HTTP_SESSION_LOGGED_ON_PREVIOUSLY_USING_LOGON_FIXTURE_KEY, true);
            return getAuthenticationManager().authenticate(new AuthenticationRequestLogonFixture(logonFixture));
        }

        return null;
    }

    @Override
    public void bind(final ServletRequest servletRequest, final ServletResponse servletResponse,
        final AuthenticationSession authSession) {
        final HttpSession httpSession = getHttpSession(servletRequest);
        httpSession.setAttribute(WebAppConstants.HTTP_SESSION_AUTHENTICATION_SESSION_KEY, authSession);
    }

    // //////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////

    private static AuthenticationManager getAuthenticationManager() {
        return IsisContext.getAuthenticationManager();
    }

}
