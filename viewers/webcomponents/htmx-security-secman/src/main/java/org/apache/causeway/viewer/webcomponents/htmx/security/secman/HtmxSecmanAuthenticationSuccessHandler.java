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
import java.net.URI;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;

final class HtmxSecmanAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final HtmxSecmanPaths paths;
    private final RequestCache requestCache;

    HtmxSecmanAuthenticationSuccessHandler(final HtmxSecmanPaths paths, final RequestCache requestCache) {
        this.paths = paths;
        this.requestCache = requestCache;
    }

    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication) throws IOException, ServletException {
        final var requested = request.getParameter("continue");
        var destination = paths.isSafeViewerDestination(requested) ? requested : null;
        final var savedRequest = requestCache.getRequest(request, response);
        if (destination == null && savedRequest != null && "GET".equals(savedRequest.getMethod())) {
            destination = relativeDestination(savedRequest.getRedirectUrl(), request.getContextPath());
        }
        if (!paths.isSafeViewerDestination(destination)) {
            destination = paths.viewerBasePath();
        }
        requestCache.removeRequest(request, response);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0");
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + destination));
    }

    private String relativeDestination(final String redirectUrl, final String contextPath) {
        try {
            final var uri = URI.create(redirectUrl);
            var path = uri.getRawPath();
            if (!contextPath.isEmpty() && path.startsWith(contextPath + "/")) {
                path = path.substring(contextPath.length());
            }
            return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
