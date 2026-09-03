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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

final class VueSecmanAuthenticationContextFilter extends OncePerRequestFilter {

    private final VueSecmanPaths paths;

    VueSecmanAuthenticationContextFilter(final VueSecmanPaths paths) {
        this.paths = paths;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        final var path = request.getRequestURI().substring(request.getContextPath().length());
        if (!"GET".equals(request.getMethod()) || !path.equals(paths.authenticationContextPath())) {
            filterChain.doFilter(request, response);
            return;
        }
        final var csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || csrfToken == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"username":"%s","csrfHeaderName":"%s","csrfParameterName":"%s","csrfToken":"%s","loginPath":"%s","logoutPath":"%s"}
                """.formatted(
                        json(authentication.getName()),
                        json(csrfToken.getHeaderName()),
                        json(csrfToken.getParameterName()),
                        json(csrfToken.getToken()),
                        json(request.getContextPath() + paths.loginPath()),
                        json(request.getContextPath() + paths.logoutPath())));
    }

    private static String json(final String value) {
        final var escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            final char character = value.charAt(i);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append("\\u%04x".formatted((int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
