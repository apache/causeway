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

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "causeway.viewer.webcomponents.htmx.security.secman")
public class HtmxSecmanSecurityProperties {

    public static final int DEFAULT_FILTER_CHAIN_ORDER = 20;

    private String loginPath = "/login";
    private String logoutPath = "/logout";
    private int filterChainOrder = DEFAULT_FILTER_CHAIN_ORDER;
    private List<String> cookiesToDelete = List.of("JSESSIONID");

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(final String loginPath) {
        this.loginPath = normalizePath(loginPath, "Login path");
    }

    public String getLogoutPath() {
        return logoutPath;
    }

    public void setLogoutPath(final String logoutPath) {
        this.logoutPath = normalizePath(logoutPath, "Logout path");
    }

    public int getFilterChainOrder() {
        return filterChainOrder;
    }

    public void setFilterChainOrder(final int filterChainOrder) {
        this.filterChainOrder = filterChainOrder;
    }

    public List<String> getCookiesToDelete() {
        return cookiesToDelete;
    }

    public void setCookiesToDelete(final List<String> cookiesToDelete) {
        this.cookiesToDelete = cookiesToDelete == null ? List.of() : List.copyOf(cookiesToDelete);
    }

    private static String normalizePath(final String value, final String label) {
        final var path = value == null ? "" : value.trim();
        if (!path.startsWith("/") || path.startsWith("//") || path.endsWith("/")
                || path.contains("?") || path.contains("#") || path.contains("..")) {
            throw new IllegalArgumentException(label + " must be a non-root application-relative path.");
        }
        return path;
    }
}
