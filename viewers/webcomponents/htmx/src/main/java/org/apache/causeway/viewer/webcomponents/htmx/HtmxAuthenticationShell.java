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
package org.apache.causeway.viewer.webcomponents.htmx;

import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.jspecify.annotations.NonNull;

/**
 * Optional host-owned authentication state for the generic HTMX shell.
 */
public interface HtmxAuthenticationShell {

    Optional<State> state(HttpServletRequest request);

    record State(
            @NonNull String username,
            @NonNull String loginPath,
            @NonNull String logoutPath,
            @NonNull String csrfHeaderName,
            @NonNull String csrfParameterName,
            @NonNull String csrfToken,
            @NonNull Set<ActionIdentity> excludedActions) {

        public State {
            excludedActions = Set.copyOf(excludedActions);
        }
    }

    record ActionIdentity(@NonNull String serviceLogicalTypeName, @NonNull String actionId) {
        public String externalForm() {
            return serviceLogicalTypeName + "#" + actionId;
        }
    }
}
