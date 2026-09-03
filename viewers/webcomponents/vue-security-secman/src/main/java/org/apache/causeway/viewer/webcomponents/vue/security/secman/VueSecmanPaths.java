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

import java.net.URI;

final class VueSecmanPaths {

    private final String viewerBasePath;
    private final String graphQlPath;
    private final String loginPath;
    private final String logoutPath;
    private final String authenticationContextPath;

    VueSecmanPaths(final VueSecmanSecurityProperties properties) {
        viewerBasePath = properties.getBasePath();
        graphQlPath = properties.getGraphQlPath();
        loginPath = viewerBasePath + properties.getLoginPath();
        logoutPath = viewerBasePath + properties.getLogoutPath();
        authenticationContextPath = viewerBasePath + properties.getAuthenticationContextPath();
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

    String authenticationContextPath() {
        return authenticationContextPath;
    }

    String loginStylesheetPath() {
        return loginPath + ".css";
    }

    String assetsPath() {
        return viewerBasePath + "/assets/**";
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
                    && !path.equals(authenticationContextPath)
                    && !path.equals(graphQlPath)
                    && !path.startsWith(viewerBasePath + "/assets/");
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
