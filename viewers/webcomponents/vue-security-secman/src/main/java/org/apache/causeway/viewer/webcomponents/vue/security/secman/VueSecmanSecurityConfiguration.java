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
package org.apache.causeway.viewer.webcomponents.vue.security.secman;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.Filter;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import org.apache.causeway.core.config.CausewayConfiguration;

@Configuration
class VueSecmanSecurityConfiguration {

    @Bean
    VueSecmanPaths vueSecmanPaths(final VueSecmanSecurityProperties securityProperties) {
        return new VueSecmanPaths(securityProperties);
    }

    @Bean
    VueSecmanSecurityValidator vueSecmanSecurityValidator(
            final CausewayConfiguration causewayConfiguration,
            final VueSecmanSecurityProperties securityProperties,
            final VueSecmanPaths paths) {
        return new VueSecmanSecurityValidator(causewayConfiguration, securityProperties, paths);
    }

    @Bean
    RequestCache vueSecmanRequestCache(final VueSecmanPaths paths) {
        final var requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);
        requestCache.setRequestMatcher(request -> {
            if (!"GET".equals(request.getMethod())) {
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
    SecurityFilterChain vueSecmanSecurityFilterChain(
            final HttpSecurity http,
            final VueSecmanPaths paths,
            final VueSecmanSecurityProperties securityProperties,
            final @Qualifier("webcomponentsSecmanAuthenticationProvider")
            DaoAuthenticationProvider authenticationProvider,
            final RequestCache requestCache) throws Exception {
        final var matcher = PathPatternRequestMatcher.withDefaults();
        final var viewerRoute = new OrRequestMatcher(
                matcher.matcher(paths.viewerBasePath()),
                matcher.matcher(paths.viewerBasePath() + "/**"));
        final var graphQlRoute = new OrRequestMatcher(
                matcher.matcher(paths.graphQlPath()),
                matcher.matcher(paths.graphQlPath() + "/**"));
        final var publicAssets = new OrRequestMatcher(
                matcher.matcher(paths.assetsPath()),
                matcher.matcher("/causeway-webcomponents/**"),
                matcher.matcher("/webjars/font-awesome/**"));
        http.securityMatcher(new OrRequestMatcher(viewerRoute, graphQlRoute, publicAssets))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                matcher.matcher(HttpMethod.GET, paths.loginPath()),
                                matcher.matcher(HttpMethod.POST, paths.loginPath()),
                                matcher.matcher(HttpMethod.GET, paths.loginStylesheetPath()),
                                publicAssets)
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
                        .authenticationEntryPoint(new VueSecmanAuthenticationEntryPoint(paths)))
                .formLogin(form -> form
                        .loginPage(paths.loginPath())
                        .loginProcessingUrl(paths.loginPath())
                        .failureHandler(new VueSecmanAuthenticationFailureHandler(paths))
                        .successHandler(new VueSecmanAuthenticationSuccessHandler(paths, requestCache))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl(paths.logoutPath())
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies(securityProperties.getCookiesToDelete().toArray(String[]::new))
                        .logoutSuccessHandler((request, response, authentication) ->
                                logoutSucceeded(request, response, paths)))
                .addFilterAfter(new VueSecmanLoginPageFilter(paths, securityProperties), CsrfFilter.class)
                .addFilterAfter(new VueSecmanAuthenticationContextFilter(paths), CsrfFilter.class);
        return new OrderedSecurityFilterChain(http.build(), securityProperties.getFilterChainOrder());
    }

    private static void logoutSucceeded(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final VueSecmanPaths paths) throws IOException {
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
