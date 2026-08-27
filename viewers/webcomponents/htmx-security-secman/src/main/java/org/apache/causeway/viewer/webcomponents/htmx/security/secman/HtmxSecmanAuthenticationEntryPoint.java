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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

final class HtmxSecmanAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HtmxSecmanPaths paths;
    private final LoginUrlAuthenticationEntryPoint loginEntryPoint;

    HtmxSecmanAuthenticationEntryPoint(final HtmxSecmanPaths paths) {
        this.paths = paths;
        loginEntryPoint = new LoginUrlAuthenticationEntryPoint(paths.loginPath());
    }

    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException) throws IOException, ServletException {
        final var requestPath = request.getRequestURI().substring(request.getContextPath().length());
        if (requestPath.equals(paths.graphQlPath())
                || requestPath.startsWith(paths.graphQlPath() + "/")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if ("true".equalsIgnoreCase(request.getHeader("HX-Request"))) {
            final var candidate = "GET".equals(request.getMethod())
                    ? requestPath + (request.getQueryString() == null ? "" : "?" + request.getQueryString())
                    : null;
            final var continueParameter = paths.isSafeViewerDestination(candidate)
                    ? "?continue=" + URLEncoder.encode(candidate, StandardCharsets.UTF_8)
                    : "";
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("HX-Redirect", request.getContextPath() + paths.loginPath() + continueParameter);
            return;
        }
        loginEntryPoint.commence(request, response, authException);
    }
}
