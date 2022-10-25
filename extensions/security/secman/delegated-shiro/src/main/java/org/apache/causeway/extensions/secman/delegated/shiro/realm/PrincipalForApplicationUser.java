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
 *
 */
package org.apache.causeway.extensions.secman.delegated.shiro.realm;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;

import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValueSet;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.user.dom.AccountType;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;


/**
 * Acts as the Principal for the {@link CausewayModuleExtSecmanShiroRealm}, meaning that it is returned from
 * {@link CausewayModuleExtSecmanShiroRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken) authentication}, and passed into
 * {@link CausewayModuleExtSecmanShiroRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection) authorization}.
 *
 * <p>
 *     To minimize database lookups, holds the user, corresponding roles and the full set of permissions
 *     (all as value objects).  The permissions are eagerly looked up during
 *     {@link CausewayModuleExtSecmanShiroRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken) authentication} and so the
 *     {@link CausewayModuleExtSecmanShiroRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection) authorization} merely involves
 *     creating an adapter object for the appropriate Shiro API.
 * </p>
 *
 * TODO: this should probably implement java.security.Principal so that it doesn't get wrapped in a
 * ShiroHttpServletRequest.ObjectPrincipal.
 * Such a change would need some testing to avoid regressions, though.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class PrincipalForApplicationUser implements AuthorizationInfo {

    private static final long serialVersionUID = 1L;

    public static PrincipalForApplicationUser from(final ApplicationUser applicationUser) {
        if(applicationUser == null) {
            return null;
        }
        val username = applicationUser.getUsername();
        val encryptedPassword = applicationUser.getEncryptedPassword();
        val accountType = applicationUser.getAccountType();
        val roles = applicationUser.getRoles()
                .stream()
                .map(ApplicationRole::getName)
                .collect(Collectors.toCollection(TreeSet::new));
        val permissionSet = applicationUser.getPermissionSet();

        return new PrincipalForApplicationUser(username, encryptedPassword, accountType,
                applicationUser.getStatus(), roles, permissionSet);
    }

    @Getter(value = AccessLevel.PACKAGE) private final String username;
    @Getter(value = AccessLevel.PACKAGE) private final String encryptedPassword;
    @Getter(value = AccessLevel.PUBLIC)  private final AccountType accountType;
    @Getter(value = AccessLevel.PACKAGE) private final ApplicationUserStatus status;
    @Getter(value = AccessLevel.PUBLIC)  private final Set<String> roles;
    @Getter(value = AccessLevel.PACKAGE) private final ApplicationPermissionValueSet permissionSet;

    public boolean isLocked() {
        return getStatus() == ApplicationUserStatus.LOCKED;
    }

    @Override
    public Collection<String> getStringPermissions() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Permission> getObjectPermissions() {
        return objectPermissions.get();
    }

    /**
     * When wrapped by ShiroHttpServletRequest.ObjectPrincipal, the principal's name is derived by calling toString().
     */
    @Override
    public String toString() {
        return getUsername();
    }

    // -- HELPER

    private final _Lazy<Collection<Permission>> objectPermissions =
            _Lazy.threadSafe(this::createObjectPermissions);

    private Collection<Permission> createObjectPermissions() {
        val permission = Permission_backedByPermissionSet.of(getPermissionSet());
        return Collections.singleton(permission);
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class Permission_backedByPermissionSet implements Permission {

        @NonNull private final ApplicationPermissionValueSet permissionSet;

        @Override
        public boolean implies(final Permission p) {
            if (!(p instanceof PermissionForMember)) {
                return false;
            }
            val permissionForMember = (PermissionForMember) p;
            return permissionSet.grants(
                    permissionForMember.getFeatureId(),
                    permissionForMember.getMode());
        }
    }

}
