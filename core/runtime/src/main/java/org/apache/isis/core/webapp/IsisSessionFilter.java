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

package org.apache.isis.core.webapp;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.webapp.auth.AuthenticationSessionStrategy;
import org.apache.isis.core.webapp.auth.AuthenticationSessionStrategyDefault;
import org.apache.isis.core.webapp.content.ResourceCachingFilter;

public class IsisSessionFilter implements Filter {

    /**
     * Recommended standard init parameter key for filters and servlets to
     * lookup an implementation of {@link AuthenticationSessionStrategy}.
     */
    public static final String AUTHENTICATION_SESSION_STRATEGY_KEY = "authenticationSessionStrategy";

    /**
     * Default value for
     * {@link AuthenticationSessionLookupStrategyConstants#AUTHENTICATION_SESSION_STRATEGY_KEY}
     * if not specified.
     */
    public static final String AUTHENTICATION_SESSION_STRATEGY_DEFAULT = AuthenticationSessionStrategyDefault.class.getName();

    /**
     * Init parameter key for backward compatibility; if logonPage set then
     * assume 'restricted' handling.
     */
    public static final String LOGON_PAGE_KEY = "logonPage";
    
    
    /**
     * Init parameter key for what should be done if no session was found.
     * 
     * <p>
     * Valid values are:
     * <ul>
     * <li>unauthorized - issue a 401 response.
     * <li>basicAuthChallenge - issue a basic auth 401 challenge. The idea here
     * is that the configured logon strategy should handle the next request
     * <li>restricted - allow access but only to a restricted (comma-separated)
     * list of paths. Access elsewhere should be redirected to the first of
     * these paths
     * <li>continue - allow the request to continue (eg if there is no security
     * requirements)
     * </ul>
     */
    public static final String WHEN_NO_SESSION_KEY = "whenNoSession";

    /**
     * Init parameter key to read the restricted list of paths (if
     * {@link #WHEN_NO_SESSION_KEY} is for {@link WhenNoSession#RESTRICTED}).
     * 
     * <p>
     * The servlets mapped to these paths are expected to be able to deal with
     * there being no session. Typically they will be logon pages.
     */
    public static final String RESTRICTED_KEY = "restricted";

    /**
     * Init parameter key to redirect to if an exception occurs.
     */
    public static final String REDIRECT_TO_ON_EXCEPTION_KEY = "redirectToOnException";

    /**
     * Init parameter key for which extensions should be ignored (typically,
     * mappings for other viewers within the webapp context).
     * 
     * <p>
     * It can also be used to specify ignored static resources (though putting
     * the {@link ResourceCachingFilter} first in the <tt>web.xml</tt>
     * accomplishes the same thing).
     * 
     * <p>
     * The value is expected as a comma separated list, for example:
     * 
     * <pre>
     * htmlviewer
     * </pre>
     */
    public static final String IGNORE_EXTENSIONS_KEY = "ignoreExtensions";

    private static final Function<String, Pattern> STRING_TO_PATTERN = new Function<String, Pattern>() {
        @Override
        public Pattern apply(final String input) {
            return Pattern.compile(".*\\." + input);
        }

    };

    static void redirect(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final String redirectTo) throws IOException {
        httpResponse.sendRedirect(StringExtensions.combinePath(httpRequest.getContextPath(), redirectTo));
    }

    public enum WhenNoSession {
        UNAUTHORIZED("unauthorized") {
            @Override
            public void handle(final IsisSessionFilter filter, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final FilterChain chain) throws IOException, ServletException {
                httpResponse.sendError(401);
            }
        },
        BASIC_AUTH_CHALLENGE("basicAuthChallenge") {
            @Override
            public void handle(final IsisSessionFilter filter, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final FilterChain chain) throws IOException, ServletException {
                httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"Apache Isis\"");
                httpResponse.sendError(401);
            }
        },
        /**
         * the destination servlet is expected to know that there will be no
         * open context
         */
        CONTINUE("continue") {
            @Override
            public void handle(final IsisSessionFilter filter, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final FilterChain chain) throws IOException, ServletException {
                chain.doFilter(httpRequest, httpResponse);
            }
        },
        /**
         * Allow access to a restricted list of URLs (else redirect to the first
         * of that list of URLs)
         */
        RESTRICTED("restricted") {
            @Override
            public void handle(final IsisSessionFilter filter, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final FilterChain chain) throws IOException, ServletException {

                if (filter.restrictedPaths.contains(httpRequest.getServletPath())) {
                    chain.doFilter(httpRequest, httpResponse);
                    return;
                }
                redirect(httpRequest, httpResponse, filter.restrictedPaths.get(0));
            }

        };
        private final String initParamValue;

        private WhenNoSession(final String initParamValue) {
            this.initParamValue = initParamValue;
        }

        public static WhenNoSession lookup(final String whenNoSessionStr) {
            for (final WhenNoSession wns : values()) {
                if (wns.initParamValue.equals(whenNoSessionStr)) {
                    return wns;
                }
            }
            throw new IllegalStateException("require an init-param of '" + WHEN_NO_SESSION_KEY + "', taking a value of " + WhenNoSession.values());
        }

        public abstract void handle(IsisSessionFilter filter, HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain) throws IOException, ServletException;
    }

    /**
     * Used as a flag on {@link HttpServletRequest} so that if there are two
     * {@link IsisSessionFilter}s in a request pipeline, then the second can
     * determine what processing has already been done (and is usually then just
     * a no-op).
     */
    private static final String SESSION_STATE_KEY = IsisSessionFilter.SessionState.class.getName();

    private AuthenticationSessionStrategy authSessionStrategy;
    private List<String> restrictedPaths;
    private WhenNoSession whenNoSession;
    private String redirectToOnException;
    private Collection<Pattern> ignoreExtensions;

    // /////////////////////////////////////////////////////////////////
    // init, destroy
    // /////////////////////////////////////////////////////////////////

    @Override
    public void init(final FilterConfig config) throws ServletException {
        authSessionStrategy = lookup(config.getInitParameter(AUTHENTICATION_SESSION_STRATEGY_KEY));
        lookupWhenNoSession(config);
        lookupRedirectToOnException(config);
        lookupIgnoreExtensions(config);
    }

    /**
     * Public visibility so can also be used by servlets.
     */
    public static AuthenticationSessionStrategy lookup(String authLookupStrategyClassName) {
        if (authLookupStrategyClassName == null) {
            authLookupStrategyClassName = AUTHENTICATION_SESSION_STRATEGY_DEFAULT;
        }
        return (AuthenticationSessionStrategy) InstanceUtil.createInstance(authLookupStrategyClassName);
    }

    private void lookupWhenNoSession(final FilterConfig config) {

        final String whenNoSessionStr = config.getInitParameter(WHEN_NO_SESSION_KEY);

        // backward compatibility
        final String logonPage = config.getInitParameter(LOGON_PAGE_KEY);
        if (logonPage != null) {
            if (whenNoSessionStr != null) {
                throw new IllegalStateException("The init-param '" + LOGON_PAGE_KEY + "' is only provided for backwards compatibility; remove if the init-param '" + WHEN_NO_SESSION_KEY + "' has been specified");
            }
            whenNoSession = WhenNoSession.RESTRICTED;
            this.restrictedPaths = Lists.newArrayList(logonPage);
            return;
        }

        whenNoSession = WhenNoSession.lookup(whenNoSessionStr);
        if (whenNoSession == WhenNoSession.RESTRICTED) {
            final String restrictedPathsStr = config.getInitParameter(RESTRICTED_KEY);
            if (restrictedPathsStr == null) {
                throw new IllegalStateException("Require an init-param of '" + RESTRICTED_KEY + "' key to be set.");
            }
            this.restrictedPaths = Lists.newArrayList(Splitter.on(",").split(restrictedPathsStr));
        }

    }

    private void lookupRedirectToOnException(final FilterConfig config) {
        redirectToOnException = config.getInitParameter(REDIRECT_TO_ON_EXCEPTION_KEY);
    }

    private void lookupIgnoreExtensions(final FilterConfig config) {
        ignoreExtensions = Collections.unmodifiableCollection(parseIgnorePatterns(config));
    }

    private static Collection<Pattern> parseIgnorePatterns(final FilterConfig config) {
        final String ignoreExtensionsStr = config.getInitParameter(IGNORE_EXTENSIONS_KEY);
        if (ignoreExtensionsStr != null) {
            final List<String> ignoreExtensions = Lists.newArrayList(Splitter.on(",").split(ignoreExtensionsStr));
            return Collections2.transform(ignoreExtensions, STRING_TO_PATTERN);
        }
        return Lists.newArrayList();
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
            public void handle(final IsisSessionFilter filter, final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

                final HttpServletRequest httpRequest = (HttpServletRequest) request;
                final HttpServletResponse httpResponse = (HttpServletResponse) response;

                if (requestIsIgnoreExtension(filter, httpRequest)) {
                    try {
                        chain.doFilter(request, response);
                        return;
                    } finally {
                        closeSession();
                    }
                }

                if (ResourceCachingFilter.isCachedResource(httpRequest)) {
                    try {
                        chain.doFilter(request, response);
                        return;
                    } finally {
                        closeSession();
                    }
                }

                // authenticate
                final AuthenticationSession validSession = filter.authSessionStrategy.lookupValid(request, response);
                if (validSession != null) {
                    filter.authSessionStrategy.bind(request, response, validSession);

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

                try {
                    NO_SESSION_SINCE_NOT_AUTHENTICATED.setOn(request);
                    filter.whenNoSession.handle(filter, httpRequest, httpResponse, chain);
                } catch (final RuntimeException ex) {
                    // in case the destination servlet cannot cope, but we've
                    // been told
                    // to redirect elsewhere
                    if (filter.redirectToOnException != null) {
                        redirect(httpRequest, httpResponse, filter.redirectToOnException);
                        return;
                    }
                    throw ex;
                } catch (final IOException ex) {
                    if (filter.redirectToOnException != null) {
                        redirect(httpRequest, httpResponse, filter.redirectToOnException);
                        return;
                    }
                    throw ex;
                } catch (final ServletException ex) {
                    // in case the destination servlet cannot cope, but we've
                    // been told
                    // to redirect elsewhere
                    if (filter.redirectToOnException != null) {
                        redirect(httpRequest, httpResponse, filter.redirectToOnException);
                        return;
                    }
                    throw ex;
                } finally {
                    UNDEFINED.setOn(request);
                    // nothing to do
                }

            }

            private boolean requestIsIgnoreExtension(final IsisSessionFilter filter, final HttpServletRequest httpRequest) {
                final String servletPath = httpRequest.getServletPath();
                for (final Pattern extension : filter.ignoreExtensions) {
                    if (extension.matcher(servletPath).matches()) {
                        return true;
                    }
                }
                return false;
            }
        },
        NO_SESSION_SINCE_REDIRECTING_TO_LOGON_PAGE, NO_SESSION_SINCE_NOT_AUTHENTICATED, SESSION_IN_PROGRESS;

        static SessionState lookup(final ServletRequest request) {
            final Object state = request.getAttribute(SESSION_STATE_KEY);
            return state != null ? (SessionState) state : SessionState.UNDEFINED;
        }

        boolean isValid(final AuthenticationSession authSession) {
            return authSession != null && getAuthenticationManager().isSessionValid(authSession);
        }

        void setOn(final ServletRequest request) {
            request.setAttribute(SESSION_STATE_KEY, this);
        }

        public void handle(final IsisSessionFilter filter, final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
            chain.doFilter(request, response);
        }

        AuthenticationManager getAuthenticationManager() {
            return IsisContext.getAuthenticationManager();
        }

        IsisSession openSession(final AuthenticationSession authSession) {
            return IsisContext.openSession(authSession);
        }

        void closeSession() {
            IsisContext.closeSession();
        }

    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

        final SessionState sessionState = SessionState.lookup(request);
        sessionState.handle(this, request, response, chain);
    }

    
    protected IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

}
