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

package org.apache.causeway.extensions.secman.delegated.springoauth2.dom;

import java.util.Collection;
import java.util.List;

import javax.annotation.Priority;
import javax.inject.Inject;

import org.apache.causeway.applib.annotation.PriorityPrecedence;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;
import org.apache.causeway.extensions.secman.applib.user.dom.mixins.ApplicationUser_addRole;
import org.apache.causeway.extensions.secman.applib.user.dom.mixins.ApplicationUser_updateEmailAddress;

import lombok.RequiredArgsConstructor;

/**
 * This service automatically creates an {@link ApplicationUser} if an end-user successfully logged in via Oauth.
 *
 * <p>
 *     The initial set of rules are as per {@link CausewayConfiguration.Extensions.Secman.DelegatedUsers.AutoCreatePolicy}
 * </p>
 *
 * @since 2.0 {@index}
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Priority(PriorityPrecedence.MIDPOINT)
public class ApplicationUserAutoCreationService {

    private final ApplicationUserRepository applicationUserRepository;
    private final ApplicationRoleRepository applicationRoleRepository;
    private final InteractionService interactionService;
    private final CausewayConfiguration causewayConfiguration;
    private final FactoryService factoryService;

    @Order(PriorityPrecedence.MIDPOINT)
    @EventListener(InteractiveAuthenticationSuccessEvent.class)
    public void onApplicationEvent(final InteractiveAuthenticationSuccessEvent event) {

        var authentication = event.getAuthentication();
        var principal = authentication.getPrincipal();
        if (!(principal instanceof OidcUser)) {
            return;
        }

        var oidcUser = (OidcUser) principal;
        var username = oidcUser.getPreferredUsername();
        var email = oidcUser.getEmail();

        var secmanConfig = causewayConfiguration.getExtensions().getSecman().getDelegatedUsers();
        switch (secmanConfig.getAutoCreatePolicy()) {
            case DO_NOT_AUTO_CREATE:
                break;
            case AUTO_CREATE_AS_LOCKED:
                create(username, email, secmanConfig.getInitialRoleNames(), ApplicationUserStatus.LOCKED);
                break;
            case AUTO_CREATE_AS_UNLOCKED:
                create(username, email, secmanConfig.getInitialRoleNames(), ApplicationUserStatus.UNLOCKED);
                break;
        }
    }

    private void create(String username, String email, List<String> initialRoleNames, ApplicationUserStatus userStatus) {
        interactionService.runAnonymous(() -> {
            var userIfAny = applicationUserRepository.findByUsername(username);
            if (userIfAny.isEmpty()) {
                var applicationUser = applicationUserRepository.newDelegateUser(username, userStatus);
                factoryService.mixin(ApplicationUser_updateEmailAddress.class, applicationUser).act(email);

                if (notEmpty(initialRoleNames)) {
                    for (String initialRoleName : initialRoleNames) {
                        addRoleIfExists(applicationUser, initialRoleName);
                    }
                }
            }
        });
    }

    private void addRoleIfExists(ApplicationUser applicationUser, String initialRoleName) {
        applicationRoleRepository.findByName(initialRoleName).ifPresent(role -> {
            factoryService.mixin(ApplicationUser_addRole.class, applicationUser).act(role);
        });
    }

    private static boolean notEmpty(List<String> initialRoleNames) {
        return !isEmpty(initialRoleNames);
    }

    private static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

}
