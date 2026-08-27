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

import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.csrf.CsrfToken;

import org.apache.causeway.viewer.webcomponents.htmx.HtmxAuthenticationShell;
import org.apache.causeway.viewer.webcomponents.htmx.HtmxViewerProperties;

final class SecmanHtmxAuthenticationShell implements HtmxAuthenticationShell {

    static final ActionIdentity FRAMEWORK_LOGOUT =
            new ActionIdentity("causeway.security.LogoutMenu", "logout");

    private final HtmxViewerProperties viewerProperties;
    private final HtmxSecmanSecurityProperties securityProperties;

    SecmanHtmxAuthenticationShell(
            final HtmxViewerProperties viewerProperties,
            final HtmxSecmanSecurityProperties securityProperties) {
        this.viewerProperties = viewerProperties;
        this.securityProperties = securityProperties;
    }

    @Override
    public Optional<State> state(final HttpServletRequest request) {
        final var principal = request.getUserPrincipal();
        final var csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (principal == null || csrfToken == null) {
            return Optional.empty();
        }
        final var viewerBase = request.getContextPath() + normalizedBasePath();
        return Optional.of(new State(
                principal.getName(),
                viewerBase + securityProperties.getLoginPath(),
                viewerBase + securityProperties.getLogoutPath(),
                csrfToken.getHeaderName(),
                csrfToken.getParameterName(),
                csrfToken.getToken(),
                Set.of(FRAMEWORK_LOGOUT)));
    }

    private String normalizedBasePath() {
        final var value = viewerProperties.getBasePath();
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
