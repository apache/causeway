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

import org.springframework.beans.factory.InitializingBean;

import org.apache.causeway.core.config.CausewayConfiguration;

final class HtmxSecmanSecurityValidator implements InitializingBean {

    static final String CSRF_PROPERTY = "causeway.security.spring.allow-csrf-filters";

    private final CausewayConfiguration causewayConfiguration;
    private final HtmxSecmanSecurityProperties securityProperties;
    private final HtmxSecmanPaths paths;

    HtmxSecmanSecurityValidator(
            final CausewayConfiguration causewayConfiguration,
            final HtmxSecmanSecurityProperties securityProperties,
            final HtmxSecmanPaths paths) {
        this.causewayConfiguration = causewayConfiguration;
        this.securityProperties = securityProperties;
        this.paths = paths;
    }

    @Override
    public void afterPropertiesSet() {
        if (!causewayConfiguration.security().spring().allowCsrfFilters()) {
            throw new IllegalStateException(
                    "Local HTMX SecMan authentication requires " + CSRF_PROPERTY + "=true.");
        }
        if (securityProperties.getFilterChainOrder() < -1_000
                || securityProperties.getFilterChainOrder() > 1_000) {
            throw new IllegalStateException("HTMX SecMan filter-chain order must be between -1000 and 1000.");
        }
        if (paths.loginPath().equals(paths.logoutPath())
                || paths.graphQlPath().equals(paths.loginPath())
                || paths.graphQlPath().equals(paths.logoutPath())) {
            throw new IllegalStateException("HTMX SecMan login, logout, and GraphQL paths must be distinct.");
        }
        for (final var cookieName : securityProperties.getCookiesToDelete()) {
            if (cookieName == null || !cookieName.matches("[!#$%&'*+.^_`|~0-9A-Za-z-]+")) {
                throw new IllegalStateException("HTMX SecMan cookie cleanup contains an invalid cookie name.");
            }
        }
    }
}
