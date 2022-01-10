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
package org.apache.isis.extensions.secman.integration.authorizor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.InteractionScope;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.security.authorization.Authorizor;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;

import lombok.NonNull;
import lombok.val;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.ext.secman.AuthorizorSecman")
@javax.annotation.Priority(PriorityPrecedence.EARLY - 10)
@Qualifier("Secman")
public class AuthorizorSecman implements Authorizor {

    @Inject ApplicationUserRepository applicationUserRepository;
    @Inject Provider<PermissionCache> cache;

    @Override
    public boolean isVisible(final InteractionContext authentication, final Identifier identifier) {
        return grants(authentication, identifier, ApplicationPermissionMode.VIEWING);
    }

    @Override
    public boolean isUsable(final InteractionContext authentication, final Identifier identifier) {
        return grants(authentication, identifier, ApplicationPermissionMode.CHANGING);
    }

    // -- HELPER

    private boolean grants(
            final InteractionContext authentication,
            final Identifier identifier,
            final ApplicationPermissionMode permissionMode) {

        val userName = authentication.getUser().getName();
        val permissionSetIfAny = cache.get()
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
    @Named("isis.ext.secman.AuthorizorSecman.PermissionCache")
    @InteractionScope
    static class PermissionCache implements DisposableBean {

        private Map<String, Optional<ApplicationPermissionValueSet>> permissionsByUsername;

        @Override
        public void destroy() throws Exception {
            permissionsByUsername = null;
        }

        Optional<ApplicationPermissionValueSet> computeIfAbsent(
                final @NonNull String userName,
                final Supplier<Optional<ApplicationPermissionValueSet>> lookup) {

            if(permissionsByUsername==null) {
                permissionsByUsername = _Maps.newHashMap();
            }

            return permissionsByUsername.computeIfAbsent(userName, __->lookup.get());
        }

    }

}
