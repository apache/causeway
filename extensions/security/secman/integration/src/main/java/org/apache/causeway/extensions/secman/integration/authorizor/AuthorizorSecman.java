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
package org.apache.causeway.extensions.secman.integration.authorizor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.InteractionScope;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.security.authentication.logout.LogoutMenu;
import org.apache.causeway.core.security.authorization.Authorizor;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValueSet;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;

import org.jspecify.annotations.NonNull;

/**
 * <p>
 * Note that this service has an earlier precedence than <code>AuthorizorShiro</code>.
 * Conversely, <code>AuthenticatorShiro</code> overrides
 * {@link org.apache.causeway.extensions.secman.integration.authenticator.AuthenticatorSecman}.
 * </p>
 *
 * <p>
 * Therefore if both shiro and secman are configured, then shiro will be used for authentication, while secman will be
 * used for authorization.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleExtSecmanApplib.NAMESPACE + ".AuthorizorSecman")
@jakarta.annotation.Priority(PriorityPrecedence.EARLY - 10)   //
@Qualifier("Secman")
public class AuthorizorSecman implements Authorizor {

    @Inject ApplicationUserRepository applicationUserRepository;
    @Inject Provider<PermissionCache> cache;
    @Inject MetaModelService metaModelService;

    private _Lazy<Identifier> logoutIdentifier = _Lazy.threadSafe(this::logoutIdentifier);

    private Identifier logoutIdentifier() {
        return Identifier.actionIdentifier(metaModelService.lookupLogicalTypeByClass(LogoutMenu.class).orElseThrow(), "logout");
    }

    @Override
    public boolean isVisible(final InteractionContext authentication, final Identifier identifier) {
        if (this.logoutIdentifier.get().equals(identifier)) {
            return true;
        }
        return grants(authentication, identifier, ApplicationPermissionMode.VIEWING);
    }

    @Override
    public boolean isUsable(final InteractionContext authentication, final Identifier identifier) {
        if (this.logoutIdentifier.get().equals(identifier)) {
            return true;
        }
        return grants(authentication, identifier, ApplicationPermissionMode.CHANGING);
    }

    // -- HELPER

    private boolean grants(
            final InteractionContext authentication,
            final Identifier identifier,
            final ApplicationPermissionMode permissionMode) {

        var userName = authentication.getUser().name();
        var permissionSetIfAny = cache.get()
                .computeIfAbsent(userName, ()->
                    applicationUserRepository
                    .findByUsername(userName)
                    .map(ApplicationUser::getPermissionSet));

        return permissionSetIfAny
        .map(permissionSet->permissionSet.grants(
                ApplicationFeatureId.fromIdentifier(identifier),
                permissionMode))
        .orElse(false);
    }

    @Component
    @Named(CausewayModuleExtSecmanApplib.NAMESPACE + ".AuthorizorSecman.PermissionCache")
    @InteractionScope
    static class PermissionCache implements DisposableBean {

        private final Map<String, Optional<ApplicationPermissionValueSet>> permissionsByUsername = _Maps.newHashMap();

        @Override
        public void destroy() {
            permissionsByUsername.clear();
        }

        Optional<ApplicationPermissionValueSet> computeIfAbsent(
                final @NonNull String userName,
                final Supplier<Optional<ApplicationPermissionValueSet>> lookup) {

            return permissionsByUsername.computeIfAbsent(userName, __->lookup.get());
        }

    }

}
