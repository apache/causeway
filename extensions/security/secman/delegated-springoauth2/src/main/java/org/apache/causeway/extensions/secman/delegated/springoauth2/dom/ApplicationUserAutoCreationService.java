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
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.context.ApplicationListener;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
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
import lombok.val;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationUserAutoCreationService
        implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

    private final ApplicationUserRepository applicationUserRepository;
    private final ApplicationRoleRepository applicationRoleRepository;
    private final InteractionService interactionService;
    private final CausewayConfiguration causewayConfiguration;
    private final FactoryService factoryService;

    @Override
    public void onApplicationEvent(final InteractiveAuthenticationSuccessEvent event) {
        val authentication = event.getAuthentication();
        val principal = authentication.getPrincipal();
        if (!(principal instanceof DefaultOidcUser)) {
            return;
        }

        val oidcUser = (DefaultOidcUser) principal;
        val username = oidcUser.getIdToken().getPreferredUsername();
        val email = oidcUser.getIdToken().getEmail();
        interactionService.runAnonymous(() -> {
            Optional<ApplicationUser> userIfAny = applicationUserRepository.findByUsername(username);
            if (userIfAny.isEmpty()) {
                val status = ApplicationUserStatus.UNLOCKED;  // locking not supported for spring delegated accounts
                val applicationUser = applicationUserRepository.newDelegateUser(username, status);
                factoryService.mixin(ApplicationUser_updateEmailAddress.class, applicationUser).act(email);

                val initialRoleNames = causewayConfiguration.getExtensions().getSecman().getDelegatedUsers().getInitialRoleNames();
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
