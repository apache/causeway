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
import javax.servlet.http.HttpSession;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.webapp.auth.AuthenticationSessionLookupStrategy;
import org.apache.isis.runtimes.dflt.webapp.auth.AuthenticationSessionLookupStrategy.Caching;
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

    /**
     * Init parameter key for whether the authentication session may be cached on the HttpSession.
     */
    public static final String CACHE_AUTH_SESSION_ON_HTTP_SESSION_KEY = "cacheAuthSessionOnHttpSession";

    
    private AuthenticationSessionLookupStrategy authSessionLookupStrategy;
    private String logonPageIfNoSession;
    
    private Caching caching;

    // /////////////////////////////////////////////////////////////////
    // init, destroy
    // /////////////////////////////////////////////////////////////////

    @Override
    public void init(final FilterConfig config) throws ServletException {
        lookupAuthenticationSessionLookupStrategy(config);
        lookupLogonPageIfNoSessionKey(config);
        lookupCacheAuthSessionKey(config);
    }

    private void lookupAuthenticationSessionLookupStrategy(final FilterConfig config) {
        String authLookupStrategyClassName = config.getInitParameter(AUTHENTICATION_SESSION_LOOKUP_STRATEGY_KEY);
        if (authLookupStrategyClassName == null) {
            authLookupStrategyClassName = AUTHENTICATION_SESSION_LOOKUP_STRATEGY_DEFAULT;
        }
        authSessionLookupStrategy =
            (AuthenticationSessionLookupStrategy) InstanceUtil.createInstance(authLookupStrategyClassName);
    }

    private void lookupLogonPageIfNoSessionKey(final FilterConfig config) {
        logonPageIfNoSession = config.getInitParameter(LOGON_PAGE_KEY);
    }

    private void lookupCacheAuthSessionKey(final FilterConfig config) {
        caching = Caching.lookup(config.getInitParameter(CACHE_AUTH_SESSION_ON_HTTP_SESSION_KEY));
    }

    @Override
    public void destroy() {
    }

    // /////////////////////////////////////////////////////////////////
    // doFilter
    // /////////////////////////////////////////////////////////////////

    public enum SessionState {
        
        UNDEFINED {
            @Override
            public void handle(IsisSessionFilter filter, ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                
                final HttpServletRequest httpRequest = (HttpServletRequest) request;
                final HttpServletResponse httpResponse = (HttpServletResponse) response;

                // request is to logon page
                if (requestToLogonPage(filter, httpRequest)) {
                    NO_SESSION_SINCE_REDIRECTING_TO_LOGON_PAGE.setOn(request);
                    try {
                        chain.doFilter(request, response);
                        return;
                    } finally {
                        UNDEFINED.setOn(request);
                        closeSession();
                    }
                }

                // authenticate
                final AuthenticationSession validSession = filter.authSessionLookupStrategy.lookupValid(request, response, filter.caching);
                if (validSession != null) {
                    filter.authSessionLookupStrategy.bind(request, response, validSession, filter.caching);

                    openSession(validSession);
                    SESSION_IN_PROGRESS.setOn(request);
                    try {
                        chain.doFilter(request, response);
                    } finally {
                        UNDEFINED.setOn(request);
                        closeSession();
                    }
                    return;
                }
                
                // redirect to logon page (if there is one)
                if (filter.logonPageIfNoSession != null) {
                    // no need to set state, since not proceeding along the filter chain
                    httpResponse.sendRedirect(filter.logonPageIfNoSession);
                    return;
                }
                
                // the destination servlet is expected to know that there
                // will be no open context
                try {
                    NO_SESSION_SINCE_NOT_AUTHENTICATED.setOn(request);
                    chain.doFilter(request, response);
                } finally {
                    UNDEFINED.setOn(request);
                    // nothing to do
                }
            }

            private boolean requestToLogonPage(IsisSessionFilter filter, HttpServletRequest httpRequest) {
                return filter.logonPageIfNoSession != null &&
                       filter.logonPageIfNoSession.equals(httpRequest.getServletPath());
            }

        },
        NO_SESSION_SINCE_REDIRECTING_TO_LOGON_PAGE,
        NO_SESSION_SINCE_NOT_AUTHENTICATED,
        SESSION_IN_PROGRESS;

        static SessionState lookup(ServletRequest request) {
            final Object state = request.getAttribute(SESSION_STATE_KEY);
            return state != null ? (SessionState)state : SessionState.UNDEFINED;
        }


        boolean isValid(final AuthenticationSession authSession) {
            return authSession != null && getAuthenticationManager().isSessionValid(authSession);
        }

        void setOn(ServletRequest request) {
            request.setAttribute(SESSION_STATE_KEY, this);
        }

        public void handle(IsisSessionFilter filter, ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            chain.doFilter(request, response);
        }
    
        AuthenticationManager getAuthenticationManager() {
            return IsisContext.getAuthenticationManager();
        }

        void openSession(final AuthenticationSession authSession) {
            IsisContext.openSession(authSession);
        }

        void closeSession() {
            IsisContext.closeSession();
        }
    
    }
    
    static final String SESSION_STATE_KEY = IsisSessionFilter.SessionState.class.getName();

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException {
        
        SessionState sessionState = SessionState.lookup(request);
        sessionState.handle(this, request, response, chain);
    }
    

}
