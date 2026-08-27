/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.htmx.security.secman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.viewer.webcomponents.htmx.HtmxAuthenticationShell;
import org.apache.causeway.viewer.webcomponents.htmx.HtmxViewerProperties;

@Configuration
class HtmxSecmanSecurityConfiguration {

    @Bean
    HtmxSecmanPaths htmxSecmanPaths(
            final HtmxViewerProperties viewerProperties,
            final HtmxSecmanSecurityProperties securityProperties) {
        return new HtmxSecmanPaths(viewerProperties, securityProperties);
    }

    @Bean
    HtmxSecmanSecurityValidator htmxSecmanSecurityValidator(
            final CausewayConfiguration causewayConfiguration,
            final HtmxSecmanSecurityProperties securityProperties,
            final HtmxSecmanPaths paths) {
        return new HtmxSecmanSecurityValidator(causewayConfiguration, securityProperties, paths);
    }

    @Bean
    SecmanUserDetailsService secmanUserDetailsService(
            final ApplicationUserRepository applicationUserRepository,
            final InteractionService interactionService) {
        return new SecmanUserDetailsService(applicationUserRepository, interactionService);
    }

    @Bean
    AuthenticationConverterOfSecmanUserDetails authenticationConverterOfSecmanUserDetails() {
        return new AuthenticationConverterOfSecmanUserDetails();
    }

    @Bean
    HtmxAuthenticationShell secmanHtmxAuthenticationShell(
            final HtmxViewerProperties viewerProperties,
            final HtmxSecmanSecurityProperties securityProperties) {
        return new SecmanHtmxAuthenticationShell(viewerProperties, securityProperties);
    }

    @Bean
    RequestCache htmxSecmanRequestCache(final HtmxSecmanPaths paths) {
        final var requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);
        requestCache.setRequestMatcher(request -> {
            if (!"GET".equals(request.getMethod()) || "true".equalsIgnoreCase(request.getHeader("HX-Request"))) {
                return false;
            }
            final var requestPath = request.getRequestURI().substring(request.getContextPath().length());
            final var candidate = requestPath
                    + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
            return paths.isSafeViewerDestination(candidate);
        });
        return requestCache;
    }

    @Bean
    DaoAuthenticationProvider htmxSecmanAuthenticationProvider(
            final SecmanUserDetailsService userDetailsService,
            final @Qualifier("Secman") PasswordEncoder passwordEncoder) {
        final var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    @Bean
    SecurityFilterChain htmxSecmanSecurityFilterChain(
            final HttpSecurity http,
            final HtmxSecmanPaths paths,
            final HtmxSecmanSecurityProperties securityProperties,
            final HtmxViewerProperties viewerProperties,
            final DaoAuthenticationProvider authenticationProvider,
            final RequestCache requestCache) throws Exception {
        final var requestMatcher = PathPatternRequestMatcher.withDefaults();
        final var viewerRoute = new OrRequestMatcher(
                requestMatcher.matcher(paths.viewerBasePath()),
                requestMatcher.matcher(paths.viewerBasePath() + "/**"));
        final var publicAssetMatchers = new ArrayList<RequestMatcher>(List.of(
                requestMatcher.matcher("/causeway-htmx/**"),
                requestMatcher.matcher("/causeway-webcomponents/**"),
                requestMatcher.matcher("/webjars/htmx.org/**")));
        final var applicationStylesheet = viewerProperties.getApplicationStylesheet();
        if (applicationStylesheet != null && applicationStylesheet.startsWith("/")) {
            publicAssetMatchers.add(requestMatcher.matcher(applicationStylesheet));
        }
        final var publicViewerAssets = new OrRequestMatcher(publicAssetMatchers);
        final var graphQlRoute = new OrRequestMatcher(
                requestMatcher.matcher(paths.graphQlPath()),
                requestMatcher.matcher(paths.graphQlPath() + "/**"));
        http.securityMatcher(new OrRequestMatcher(
                        viewerRoute,
                        graphQlRoute,
                        publicViewerAssets))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                requestMatcher.matcher(HttpMethod.GET, paths.loginPath()),
                                requestMatcher.matcher(HttpMethod.POST, paths.loginPath()),
                                requestMatcher.matcher(HttpMethod.GET, paths.loginStylesheetPath()),
                                publicViewerAssets)
                        .permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider)
                .csrf(csrf -> csrf.requireCsrfProtectionMatcher(request -> {
                    if ("GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod())
                            || "OPTIONS".equals(request.getMethod()) || "TRACE".equals(request.getMethod())) {
                        return false;
                    }
                    final var requestPath = request.getRequestURI().substring(request.getContextPath().length());
                    if (requestPath.equals(paths.loginPath())) {
                        return true;
                    }
                    final var authentication = SecurityContextHolder.getContext().getAuthentication();
                    return authentication != null && authentication.isAuthenticated();
                }))
                .requestCache(cache -> cache.requestCache(requestCache))
                .sessionManagement(session -> session.sessionFixation(fixation -> fixation.migrateSession()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HtmxSecmanAuthenticationEntryPoint(paths)))
                .formLogin(form -> form
                        .loginPage(paths.loginPath())
                        .loginProcessingUrl(paths.loginPath())
                        .failureHandler(new HtmxSecmanAuthenticationFailureHandler(paths))
                        .successHandler(new HtmxSecmanAuthenticationSuccessHandler(paths, requestCache))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl(paths.logoutPath())
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies(securityProperties.getCookiesToDelete().toArray(String[]::new))
                        .logoutSuccessHandler((request, response, authentication) ->
                                logoutSucceeded(request, response, paths)))
                .addFilterAfter(new HtmxSecmanLoginPageFilter(paths, viewerProperties), CsrfFilter.class);
        return new OrderedSecurityFilterChain(http.build(), securityProperties.getFilterChainOrder());
    }

    private static void logoutSucceeded(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final HtmxSecmanPaths paths) throws IOException {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0");
        response.sendRedirect(response.encodeRedirectURL(
                request.getContextPath() + paths.loginPath() + "?logout=true"));
    }

    private record OrderedSecurityFilterChain(SecurityFilterChain delegate, int order)
    implements SecurityFilterChain, Ordered {
        @Override
        public boolean matches(final HttpServletRequest request) {
            return delegate.matches(request);
        }

        @Override
        public List<Filter> getFilters() {
            return delegate.getFilters();
        }

        @Override
        public int getOrder() {
            return order;
        }
    }
}
