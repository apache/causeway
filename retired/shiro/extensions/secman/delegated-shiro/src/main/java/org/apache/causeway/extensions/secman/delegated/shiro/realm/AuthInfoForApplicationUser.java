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

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.extensions.secman.delegated.shiro.util.ShiroUtils;

import lombok.AccessLevel;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class AuthInfoForApplicationUser implements AuthenticationInfo, AuthorizationInfo {

    private static final long serialVersionUID = 1L;

    static AuthenticationInfo of(
            final PrincipalForApplicationUser principal,
            final String realmName,
            final Object credentials) {

        return new AuthInfoForApplicationUser(principal, realmName, credentials);
    }

    @NonNull private final PrincipalForApplicationUser principal;
    @NonNull private final String realmName;
    @NonNull @Getter private final Object credentials;

    @Override
    public PrincipalCollection getPrincipals() {
        return principalCollection.get();
    }

    @Override
    public Collection<String> getRoles() {
        return principal.getRoles();
    }

    @Override
    public Collection<String> getStringPermissions() {
        return principal.getStringPermissions();
    }

    @Override
    public Collection<Permission> getObjectPermissions() {
        return principal.getObjectPermissions();
    }

    // -- HELPER

    private final _Lazy<PrincipalCollection> principalCollection =
            _Lazy.threadSafe(this::createPrincipalCollection);

    private PrincipalCollection createPrincipalCollection() {
        return ShiroUtils.isSingleRealm()
                ? PrincipalCollectionForApplicationUserOnSingleRealm.of(principal, realmName)
                : new SimplePrincipalCollection(principal, realmName);
    }

}
