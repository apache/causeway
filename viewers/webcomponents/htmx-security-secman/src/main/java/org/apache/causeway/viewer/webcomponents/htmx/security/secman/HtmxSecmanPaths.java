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

import java.net.URI;

import org.apache.causeway.viewer.webcomponents.htmx.HtmxViewerProperties;

final class HtmxSecmanPaths {

    private final String viewerBasePath;
    private final String graphQlPath;
    private final String loginPath;
    private final String logoutPath;

    HtmxSecmanPaths(
            final HtmxViewerProperties viewerProperties,
            final HtmxSecmanSecurityProperties securityProperties) {
        viewerBasePath = normalize(viewerProperties.getBasePath(), "HTMX base path");
        graphQlPath = normalize(viewerProperties.getGraphQlEndpoint(), "GraphQL endpoint");
        loginPath = viewerBasePath + securityProperties.getLoginPath();
        logoutPath = viewerBasePath + securityProperties.getLogoutPath();
    }

    String viewerBasePath() {
        return viewerBasePath;
    }

    String graphQlPath() {
        return graphQlPath;
    }

    String loginPath() {
        return loginPath;
    }

    String logoutPath() {
        return logoutPath;
    }

    String loginStylesheetPath() {
        return loginPath + ".css";
    }

    boolean isSafeViewerDestination(final String candidate) {
        if (candidate == null || candidate.isBlank() || candidate.indexOf('\\') >= 0
                || candidate.chars().anyMatch(Character::isISOControl)) {
            return false;
        }
        try {
            final var uri = URI.create(candidate);
            if (uri.isAbsolute() || uri.getRawAuthority() != null || uri.getRawFragment() != null) {
                return false;
            }
            final var path = uri.getPath();
            return path != null
                    && (path.equals(viewerBasePath) || path.startsWith(viewerBasePath + "/"))
                    && !path.equals(loginPath)
                    && !path.equals(logoutPath)
                    && !path.equals(graphQlPath);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static String normalize(final String value, final String label) {
        final var path = value == null ? "" : value.trim();
        if (path.length() < 2 || !path.startsWith("/") || path.startsWith("//")
                || path.endsWith("/") || path.contains("?") || path.contains("#")
                || path.contains("..") || path.indexOf('\\') >= 0
                || path.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalStateException(label + " must be a canonical non-root same-origin path.");
        }
        return path;
    }
}
