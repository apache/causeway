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
package org.apache.isis.extensions.secman.applib;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

/**
 * Applications consuming secman must instantiate a
 * {@link org.springframework.context.annotation.Bean} of this type; secman
 * then uses this bean to configure itself.
 *
 * <p>
 * The typical place to create this bean is in the top-level
 * <code>AppManifest</code>
 * {@link org.springframework.context.annotation.Configuration} class.
 * </p>
 *
 * <p>
 * This class is implemented as a builder (courtesy of Lombok) but it
 * provides reasonable defaults.
 * </p>
 *
 * @deprecated - use <code>application.yml</code> config properties instead.
 *
 * @since 2.0 {@index}
 */
@Deprecated
@Builder
public class SecmanConfiguration {

    // -- ADMIN

    /**
     * @see IsisConfiguration.Extensions.Secman.Seed.Admin#getUserName()
     *
     * @deprecated
     */
    @Deprecated
    @Getter
    @Builder.Default
    @NonNull
    final String adminUserName = IsisConfiguration.Extensions.Secman.Seed.ADMIN_USER_NAME_DEFAULT;

    // sonar-ignore-on (detects potential security risk, which we are aware of)

    /**
     * @see IsisConfiguration.Extensions.Secman.Seed.Admin#getPassword()
     *
     * @deprecated
     */
    @Deprecated
    @Getter
    @Builder.Default
    @NonNull
    final String adminPassword = IsisConfiguration.Extensions.Secman.Seed.ADMIN_PASSWORD_DEFAULT;
    // sonar-ignore-off

    /**
     * @see IsisConfiguration.Extensions.Secman.Seed.Admin#getRoleName()
     *
     * @deprecated
     */
    @Deprecated
    @Getter
    @Builder.Default
    @NonNull
    final String adminRoleName = IsisConfiguration.Extensions.Secman.Seed.ADMIN_ROLE_NAME_DEFAULT;

    /**
     * @see IsisConfiguration.Extensions.Secman.Seed.Admin.NamespacePermissions#getSticky()
     *
     * @deprecated
     */
    @Deprecated
    @Getter
    @Builder.Default
    @NonNull
    final String[] adminStickyNamespacePermissions = arrayOf(IsisConfiguration.Extensions.Secman.Seed.ADMIN_STICKY_NAMESPACE_PERMISSIONS_DEFAULT);

    /**
     * @see IsisConfiguration.Extensions.Secman.Seed.Admin.NamespacePermissions#getAdditional()
     *
     * @deprecated
     */
    @Deprecated
    @Getter
    @Singular
    final Set<String> adminAdditionalNamespacePermissions;



    // -- REGULAR USER

    /**
     * @see IsisConfiguration.Extensions.Secman.Seed.RegularUser#getRoleName()
     *
     * @deprecated
     */
    @Deprecated
    @Getter
    @Builder.Default
    @NonNull
    final String regularUserRoleName = IsisConfiguration.Extensions.Secman.Seed.REGULAR_USER_ROLE_NAME_DEFAULT;

    /**
     * @see IsisConfiguration.Extensions.Secman.DelegatedUsers#getAutoCreatePolicy()
     *
     * @deprecated
     */
    @Deprecated
    @Getter
    @Builder.Default
    final boolean autoUnlockIfDelegatedAndAuthenticated = IsisConfiguration.Extensions.Secman.Seed.AUTO_UNLOCK_IF_DELEGATED_AND_AUTHENTICATED_DEFAULT;


    // -- UTILITIES

    public Stream<String> streamAdminNamespacePermissions() {
        return Stream.concat(
                _NullSafe.stream(adminStickyNamespacePermissions),
                _NullSafe.stream(adminAdditionalNamespacePermissions));
    }

    public boolean isStickyAdminNamespace(String featureFqn) {
        return _NullSafe.stream(adminStickyNamespacePermissions)
                .anyMatch(stickyPackage -> stickyPackage.equals(featureFqn));
    }

    private static Set<String> setOf(Collection<String> coll) {
        return _Sets.newTreeSet(coll);
    }
    private static String[] arrayOf(List<String> list) {
        return list.toArray(new String[]{});
    }

}
