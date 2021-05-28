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

import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.security.IsisModuleCoreSecurity;
import org.apache.isis.extensions.secman.applib.role.seed.IsisExtH2ConsoleRoleAndPermissions;

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
 * @since 2.0 {@index}
 */
@Builder
public class SecmanConfiguration {

    // -- ADMIN

    /**
     * The name of the security super user.
     *
     * <p>
     * This user is automatically made a member of the
     * {@link #getAdminRoleName() admin role}, from which it is granted
     * permissions to administer other users.
     * </p>
     *
     * <p>
     * The password for this user is set in {@link #getAdminPassword()}.
     * </p>
     *
     * @see #getAdminPassword()
     * @see #getAdminRoleName()
     */
    @Getter
    @Builder.Default
    @NonNull
    final String adminUserName = "secman-admin";

    // sonar-ignore-on (detects potential security risk, which we are aware of)
    /**
     * The corresponding password for {@link #getAdminUserName() admin user}.
     *
     * @see #getAdminUserName()
     */
    @Getter
    @Builder.Default
    @NonNull
    final String adminPassword = "pass";
    // sonar-ignore-off

    /**
     * The name of security admin role.
     *
     * <p>
     * Users with this role (in particular, the default
     * {@link #getAdminUserName() admin user} are granted access to a set of
     * namespaces ({@link #getAdminStickyNamespacePermissions()} and
     * {@link #getAdminAdditionalNamespacePermissions()}) which are intended to
     * be sufficient to allow users with this admin role to be able to
     * administer the security module itself, for example to manage users and
     * roles.
     * </p>
     *
     * @see #getAdminUserName()
     * @see #getAdminStickyNamespacePermissions()
     * @see #getAdminAdditionalNamespacePermissions()
     */
    @Getter
    @Builder.Default
    @NonNull
    final String adminRoleName = ADMIN_ROLE_DEFAULT_NAME;

    public static String ADMIN_ROLE_DEFAULT_NAME = "isis-ext-secman-admin";

    /**
     * The set of namespaces to which the {@link #getAdminRoleName() admin role}
     * is granted.
     *
     * <p>
     * These namespaces are intended to be sufficient to allow users with
     * this admin role to be able to administer the security module itself,
     * for example to manage users and roles.  The security user is not
     * necessarily able to use the main business logic within the domain
     * application itself, though.
     * </p>
     *
     * <p>
     * These roles cannot be removed via user interface
     * </p>
     *
     * <p>
     * WARNING: normally these should not be overridden.  Instead, specify
     * additional namespaces using
     * {@link #getAdminAdditionalNamespacePermissions()}.
     * </p>
     *
     * @see #getAdminAdditionalNamespacePermissions()
     */
    @Getter
    @Builder.Default
    @NonNull
    final String[] adminStickyNamespacePermissions = new String[]{
            IsisModuleCoreSecurity.NAMESPACE,
            IsisModuleApplib.NAMESPACE_SUDO,
            IsisModuleApplib.NAMESPACE_CONF,
            IsisModuleApplib.NAMESPACE_FEAT,
            IsisExtH2ConsoleRoleAndPermissions.NAMESPACE,
            IsisModuleExtSecmanApplib.NAMESPACE
    };

    /**
     * An (optional) additional set of namespaces that the
     * {@link #getAdminRoleName() admin role} is granted.
     *
     * <p>
     * These are in addition to the main
     * {@link #getAdminStickyNamespacePermissions() namespaces} granted.
     * </p>
     *
     * @see #getAdminStickyNamespacePermissions()
     */
    @Getter
    @Singular
    final Set<String> adminAdditionalNamespacePermissions;


    // -- REGULAR USER

    /**
     * The role name for regular users of the application, granting them access
     * to basic security features.
     *
     * <p>
     *     The exact set of permissions is hard-wired in the
     *     <code>IsisExtSecmanRegularUserRoleAndPermissions</code> fixture.
     * </p>
     */
    @Getter
    @Builder.Default
    @NonNull
    final String regularUserRoleName = REGULAR_USER_ROLE_DEFAULT_NAME;

    public static String REGULAR_USER_ROLE_DEFAULT_NAME = "isis-ext-secman-user";


    /**
     * Delegated users, on first successful logon, are auto-created but locked (by default).
     * <p>
     * This option allows to override this behavior, such that authenticated
     * users are also auto-unlocked.
     * <p>
     *
     * <p>
     * BE AWARE THAT if any users are auto-created unlocked, then the set of roles that
     * they are given should be highly restricted !!!
     * </p>
     */
    @Getter
    @Builder.Default
    final boolean autoUnlockIfDelegatedAndAuthenticated = false;


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

}
