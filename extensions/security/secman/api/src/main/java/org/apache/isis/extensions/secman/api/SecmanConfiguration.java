/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.extensions.secman.api;

import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._NullSafe;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

/**
 * @since 2.0 {@index}
 */
@Builder
public class SecmanConfiguration {

    // -- ROLES

    @Getter @Builder.Default @NonNull
    final String regularUserRoleName = "secman-regular-user";

    @Getter @Builder.Default @NonNull
    final String fixtureRoleName = "secman-fixtures";

    @Getter @Builder.Default @NonNull
    final String adminRoleName = "secman-admin";

    // -- ADMIN

    @Getter @Builder.Default @NonNull
    final String adminUserName = "secman-admin";

    // sonar-ignore-on (detects potential security risk, which we are aware of)
    @Getter @Builder.Default @NonNull
    final String adminPassword = "pass";
    // sonar-ignore-off

    /**
     * cannot be removed via user interface
     */
    @Getter @Builder.Default @NonNull
    final String[] adminStickyNamespacePermissions = new String[]{
            "isis.security",
            "isis.ext.secman"
    };

    @Getter @Singular
    final Set<String> adminAdditionalNamespacePermissions;

    /**
     * Delegated users, on first successful logon, are auto-created but disabled (by default).
     * <p>
     * This option allows to override this behavior, such that authenticated
     * users are also auto-enabled.
     * <p>
     * default: false
     *
     */
    @Getter @Builder.Default
    final boolean autoEnableIfDelegatedAndAuthenticated = false;

    // -- UTILITIES

    public Stream<String> streamAdminNamespacePermissions() {
        return Stream.concat(
                _NullSafe.stream(adminStickyNamespacePermissions),
                _NullSafe.stream(adminAdditionalNamespacePermissions));
    }

    public boolean isStickyAdminNamespace(String featureFqn) {
        return _NullSafe.stream(adminStickyNamespacePermissions)
        .anyMatch(stickyPackage->stickyPackage.equals(featureFqn));
    }


}
