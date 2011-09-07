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

package org.apache.isis.runtimes.dflt.webapp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.webapp.auth.AuthenticationSessionLookupStrategy;
import org.apache.isis.runtimes.dflt.webapp.auth.AuthenticationSessionLookupStrategyDefault;

public class IsisSessionFilter implements Filter {

    /**
     * Init parameter key to lookup implementation of {@link AuthenticationSessionLookupStrategy}.
     */
    public static final String AUTHENTICATION_SESSION_LOOKUP_STRATEGY_KEY = "authenticationSessionLookupStrategy";

    /**
     * Default value for {@link #AUTHENTICATION_SESSION_LOOKUP_STRATEGY_KEY} if not specified.
     */
    public static final String AUTHENTICATION_SESSION_LOOKUP_STRATEGY_DEFAULT =
        AuthenticationSessionLookupStrategyDefault.class.getName();

    /**
     * Init parameter key for (typically, a logon) page to redirect to if the {@link AuthenticationSession} cannot be
     * found or is invalid.
     */
    public static final String LOGON_PAGE_KEY = "logonPage";

    private AuthenticationSessionLookupStrategy authSessionLookupStrategy;
    private String logonPageIfNoSession;

    // /////////////////////////////////////////////////////////////////
    // init, destroy
    // /////////////////////////////////////////////////////////////////

    @Override
    public void init(final FilterConfig config) throws ServletException {
        lookupAuthenticationSessionLookupStrategy(config);
        lookupRedirectIfNoSessionKey(config);
    }

    private void lookupAuthenticationSessionLookupStrategy(final FilterConfig config) {
        String authLookupStrategyClassName = config.getInitParameter(AUTHENTICATION_SESSION_LOOKUP_STRATEGY_KEY);
        if (authLookupStrategyClassName == null) {
            authLookupStrategyClassName = AUTHENTICATION_SESSION_LOOKUP_STRATEGY_DEFAULT;
        }
        authSessionLookupStrategy =
            (AuthenticationSessionLookupStrategy) InstanceUtil.createInstance(authLookupStrategyClassName);
    }

    private void lookupRedirectIfNoSessionKey(final FilterConfig config) {
        logonPageIfNoSession = config.getInitParameter(LOGON_PAGE_KEY);
    }

    @Override
    public void destroy() {
    }

    // /////////////////////////////////////////////////////////////////
    // doFilter
    // /////////////////////////////////////////////////////////////////

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        // forward/redirect as required
        final AuthenticationSession authSession = authSessionLookupStrategy.lookup(request, response);
        if (!isValid(authSession)) {
            if (logonPageIfNoSession != null
                && !logonPageIfNoSession.equals(httpRequest.getServletPath())) {
                httpResponse.sendRedirect(logonPageIfNoSession);
            } else {
                // the destination servlet is expected to know that there
                // will be no open context
                try {
                    chain.doFilter(request, response);
                } finally {
                    // nothing to do
                }
            }
        } else {
            // else, is authenticated so open session
            authSessionLookupStrategy.bind(request, response, authSession);

            IsisContext.openSession(authSession);
            try {
                chain.doFilter(request, response);
            } finally {
                IsisContext.closeSession();
            }
        }
    }

    private boolean isValid(final AuthenticationSession authSession) {
        return authSession != null && getAuthenticationManager().isSessionValid(authSession);
    }

    // /////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////////////////

    private static AuthenticationManager getAuthenticationManager() {
        return IsisContext.getAuthenticationManager();
    }

}
