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
package org.apache.causeway.viewer.restfulobjects.viewer.webmodule;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.TransactionalException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.factory._InstanceUtil;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.causeway.core.webapp.modules.templresources.TemplateResourceCachingFilter;
import org.apache.causeway.viewer.restfulobjects.viewer.webmodule.auth.AuthenticationStrategy;
import org.apache.causeway.viewer.restfulobjects.viewer.webmodule.auth.AuthenticationStrategyUsingSession;

import lombok.Builder;

/**
 * Filter for RestfulObjects.
 *
 * authenticate user, set up an Causeway session
 */
//@WebFilter(
//        servletNames={"RestfulObjectsRestEasyDispatcher"}, // this is mapped to the entire application;
//            // however the CausewayRestfulObjectsInteractionFilter will
//            // "notice" if the interaction filter has already been
//            // executed for the request pipeline, and if so will do nothing
//        initParams={
//        @WebInitParam(
//                name="authenticationStrategy",
//                value="org.apache.causeway.viewer.restfulobjects.server.authentication.AuthenticationStrategyBasicAuth"), // authentication required for REST
//        @WebInitParam(
//                name="whenNoSession", // what to do if no session was found ...
//                value="auto"), // ... 401 and a basic authentication challenge if request originates from web browser
//        @WebInitParam(name="passThru", value="/restful/swagger,/restful/health") //TODO[CAUSEWAY-1895] the restful path is configured elsewhere
//})
//TODO[causeway-viewer-restfulobjects-viewer-CAUSEWAY-3892] experimental
@Component
public class CausewayRestfulObjectsInteractionFilter2 extends OncePerRequestFilter {

    public static final String AUTHENTICATION_SESSION_STRATEGY_DEFAULT = AuthenticationStrategyUsingSession.class.getName();

    /**
     * Somewhat hacky, add this to the query
     */
    public static final String CAUSEWAY_SESSION_FILTER_QUERY_STRING_FORCE_LOGOUT = "__causeway_force_logout";

    private static final Function<String, Pattern> STRING_TO_PATTERN = (final String input) -> {
        return Pattern.compile(".*\\." + input);
    };

    @Autowired private InteractionService interactionService;
    @Autowired private SpecificationLoader specificationLoader;
    @Autowired private TransactionService transactionService;
    @Autowired private CausewayConfiguration causewayConfiguration;
    @Value("${causeway.viewer.restfulobjects.base-path}") String restfulPath;

    @Builder
    record Config(
        /**
         * Recommended standard init parameter for filters and servlets to
         * lookup an implementation of {@link AuthenticationStrategy}.
         */
        String authenticationStrategyClassName,
        /**
         * What to do if no session was found.
         *
         * <p> Valid values are:
         * <ul>
         * <li>unauthorized - issue a 401 response.
         * <li>basicAuthChallenge - issue a basic auth 401 challenge. The idea here
         * is that the configured logon strategy should handle the next request
         * <li>restricted - allow access but only to a restricted (comma-separated)
         * list of paths. Access elsewhere should be redirected to the first of
         * these paths
         * <li>continue - allow the request to continue (eg if there is no security requirements)
         * </ul>
         */
        String whenNoSession,
        /**
         * Which URLs to ignore (eg <code>/restful/swagger</code> so that swagger specs can be accessed from the swagger-ui)
         */
        String passThru,
        //TODO[causeway-viewer-restfulobjects-viewer-CAUSEWAY-3892] not applied just yet
        String urlPattern) {

        /**
         * where to redirect to if an exception occurs
         */
        public String redirectToOnException() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * Init parameter key for which extensions should be ignored (typically,
         * mappings for other viewers within the webapp context).
         *
         * <p>It can also be used to specify ignored static resources (though putting
         * the {@link TemplateResourceCachingFilter} first in the <tt>web.xml</tt>
         * accomplishes the same thing).
         *
         * <p>The value is expected as a comma separated list.
         */
        public String ignoreExtensions() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * Init parameter to read the restricted list of paths (if
         * {@link #whenNoSession} is for {@link WhenNoSession#RESTRICTED}).
         *
         * <p>The servlets mapped to these paths are expected to be able to deal with
         * there being no session. Typically they will be logon pages.
         */
        public String restricted() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * Init parameter for backward compatibility; if logonPage set then
         * assume 'restricted' handling.
         */
        public String logonPage() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private List<String> passThruList = Collections.emptyList();

    static void redirect(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final String redirectTo) throws IOException {
        httpResponse.sendRedirect(_Resources.combinePath(httpRequest.getContextPath(), redirectTo));
    }

    public enum WhenNoSession {
        UNAUTHORIZED("unauthorized") {
            @Override
            public void handle(final CausewayRestfulObjectsInteractionFilter2 filter, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final FilterChain chain) throws IOException, ServletException {
                httpResponse.sendError(401);
            }
        },
        BASIC_AUTH_CHALLENGE("basicAuthChallenge") {
            @Override
            public void handle(final CausewayRestfulObjectsInteractionFilter2 filter, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final FilterChain chain) throws IOException, ServletException {
                httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"Apache Causeway\"");
                httpResponse.sendError(401);
            }
        },
        AUTO("auto") {
            @Override
            public void handle(final CausewayRestfulObjectsInteractionFilter2 filter, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final FilterChain chain) throws IOException, ServletException {
                if(fromWebBrowser(httpRequest)) {
                    httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"Apache Causeway\"");
                }
                httpResponse.sendError(401);
            }

            private boolean fromWebBrowser(final HttpServletRequest httpRequest) {
                String accept = httpRequest.getHeader("Accept");
                return accept.contains("text/html");
            }
        },
        /**
         * the destination servlet is expected to know that there will be no open session, and handle the case appropriately
         */
        CONTINUE("continue") {
            @Override
            public void handle(final CausewayRestfulObjectsInteractionFilter2 filter, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final FilterChain chain) throws IOException, ServletException {
                chain.doFilter(httpRequest, httpResponse);
            }
        },
        /**
         * Allow access to a restricted list of URLs (else redirect to the first of that list of URLs)
         */
        RESTRICTED("restricted") {
            @Override
            public void handle(final CausewayRestfulObjectsInteractionFilter2 filter, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final FilterChain chain) throws IOException, ServletException {

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
            throw new IllegalStateException("require an init-param of 'whenNoSession', "
                + "taking a value of " + Arrays.toString(WhenNoSession.values()));
        }

        public abstract void handle(CausewayRestfulObjectsInteractionFilter2 filter, HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain) throws IOException, ServletException;
    }

    private AuthenticationStrategy authStrategy;
    private List<String> restrictedPaths;
    private WhenNoSession whenNotAuthenticated;
    private String redirectToOnException;
    private Collection<Pattern> ignoreExtensions;

    @Override
    public void initFilterBean() throws ServletException {
        var config = createConfig();
        this.authStrategy = lookup(config.authenticationStrategyClassName());
        lookupWhenNoSession(config);
        lookupPassThru(config);
        lookupRedirectToOnException(config);
        lookupIgnoreExtensions(config);
    }

    private Config createConfig() {

        return new Config(
            causewayConfiguration.getViewer().getRestfulobjects().getAuthentication().getStrategyClassName(),
            "auto", // ... 401 and a basic authentication challenge if request originates from web browser
            String.join(",",
                this.restfulPath + "swagger",
                this.restfulPath + "health"),
            // this is mapped to the entire application;
            // however the CausewayRestfulObjectsInteractionFilter will
            // "notice" if the session filter has already been
            // executed for the request pipeline, and if so will do nothing
            this.restfulPath + "/*");
    }

    /**
     * Public visibility so can also be used by servlets.
     */
    public static AuthenticationStrategy lookup(String authLookupStrategyClassName) {
        if (authLookupStrategyClassName == null) {
            authLookupStrategyClassName = AUTHENTICATION_SESSION_STRATEGY_DEFAULT;
        }
        return (AuthenticationStrategy) _InstanceUtil.createInstance(authLookupStrategyClassName);
    }

    private void lookupWhenNoSession(final Config config) {

        final String whenNoSessionStr = config.whenNoSession();

        // backward compatibility
        final String logonPage = config.logonPage();
        if (logonPage != null) {
            if (whenNoSessionStr != null) {
                throw new IllegalStateException(String.format(
                        "The init-param '%s' is only provided for backwards compatibility; "
                                + "remove if the init-param '%s' has been specified", "logonPage", "whenNoSession"));
            } else {
                // default whenNotAuthenticated and allow access through to the logonPage
                whenNotAuthenticated = WhenNoSession.RESTRICTED;
                this.restrictedPaths = List.of(logonPage);
                return;
            }
        }

        whenNotAuthenticated = WhenNoSession.lookup(whenNoSessionStr);
        if (whenNotAuthenticated == WhenNoSession.RESTRICTED) {
            final String restrictedPathsStr = config.restricted();
            if (restrictedPathsStr == null) {
                throw new IllegalStateException(String.format("Require an init-param of '%s' key to be set.", "restricted"));
            }
            this.restrictedPaths =
                    _Strings.splitThenStream(restrictedPathsStr, ",")
                    .collect(Collectors.toList());

        }

    }

    void lookupPassThru(final Config config) {
        this.passThruList = lookupAndParsePassThru(config);
    }

    List<String> lookupAndParsePassThru(final Config config) {
        final String passThru = config.passThru();
        return passThru != null && !passThru.equals("")
                ? Arrays.asList(passThru.split(","))
                        : Collections.<String>emptyList();
    }

    private void lookupRedirectToOnException(final Config config) {
        redirectToOnException = config.redirectToOnException();
    }

    private void lookupIgnoreExtensions(final Config config) {
        ignoreExtensions = Collections.unmodifiableCollection(parseIgnorePatterns(config)
                .collect(Collectors.toList()));
    }

    private Stream<Pattern> parseIgnorePatterns(final Config config) {
        final String ignoreExtensionsStr = config.ignoreExtensions();
        if (ignoreExtensionsStr != null) {
            final Stream<String> ignoreExtensions = _Strings.splitThenStream(ignoreExtensionsStr, ",");
            return ignoreExtensions.map(STRING_TO_PATTERN);
        }
        return Stream.empty();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilterInternal(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final FilterChain chain) throws IOException, ServletException {

        Objects.requireNonNull(interactionService, "causewayInteractionFactory");
        Objects.requireNonNull(specificationLoader, "specificationLoader");

        // check that the app has bootstrapped; if it hasn't, then return a 503 (rather than a huge stack trace)
        try {
            ensureMetamodelIsValid(specificationLoader);
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        try {
            var queryString = httpServletRequest.getQueryString();
            if (queryString != null && queryString
                    .contains(CAUSEWAY_SESSION_FILTER_QUERY_STRING_FORCE_LOGOUT)) {

                authStrategy.invalidate(httpServletRequest, httpServletResponse);
                return;
            }

            if (requestIsIgnoreExtension(this, httpServletRequest) ||
                    TemplateResourceCachingFilter.isCachedResource(httpServletRequest)) {
                chain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            if(requestIsPassThru(httpServletRequest)) {
                chain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            // authenticate
            var authentication =
                    authStrategy.lookupValid(httpServletRequest, httpServletResponse);
            if (authentication != null) {

                authStrategy.bind(httpServletRequest, httpServletResponse, authentication);

                interactionService.run(
                        authentication,
                        ()->{
                            transactionService.runWithinCurrentTransactionElseCreateNew(()->
                                chain.doFilter(httpServletRequest, httpServletResponse))
                            .mapFailure(e->new TransactionalException("", e))
                            .ifFailureFail();
                        });

                return;
            }

            try {
                whenNotAuthenticated.handle(this, httpServletRequest, httpServletResponse, chain);
            } catch (final RuntimeException | IOException | ServletException ex) {
                // in case the destination servlet cannot cope, but we've
                // been told to redirect elsewhere
                if (redirectToOnException != null) {
                    redirect(httpServletRequest, httpServletResponse, redirectToOnException);
                    return;
                }
                throw ex;
            }

        } finally {
            interactionService.closeInteractionLayers();
        }

    }

    private static void ensureMetamodelIsValid(final SpecificationLoader specificationLoader) {
        // using side-effect free access to MM validation result
        var validationResult = specificationLoader.getValidationResult()
        .orElseThrow(()->_Exceptions.illegalState("Application is not fully initialized yet."));
        if(validationResult.hasFailures()) {
            throw new MetaModelInvalidException(validationResult.getAsLineNumberedString());
        }
    }

    protected boolean requestIsPassThru(final HttpServletRequest httpServletRequest) {
        final String requestURI = httpServletRequest.getRequestURI();
        for (final String passThru : passThruList) {
            if(requestURI.startsWith(passThru)) {
                return true;
            }
        }
        return false;
    }

    private boolean requestIsIgnoreExtension(
            final CausewayRestfulObjectsInteractionFilter2 filter,
            final HttpServletRequest httpRequest) {

        var servletPath = httpRequest.getServletPath();
        for (final Pattern extension : filter.ignoreExtensions) {
            if (extension.matcher(servletPath).matches()) {
                return true;
            }
        }
        return false;
    }

}
