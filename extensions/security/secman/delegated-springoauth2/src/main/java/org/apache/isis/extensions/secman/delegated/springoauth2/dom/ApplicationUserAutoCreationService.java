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

package org.apache.isis.extensions.secman.delegated.springoauth2.dom;

import javax.inject.Inject;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserStatus;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationUserAutoCreationService
        implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

    private final ApplicationUserRepository applicationUserRepository;
    private final InteractionService interactionService;

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
        val applicationUser = interactionService.callAnonymous(() -> applicationUserRepository.findOrCreateUserByUsername(username));
        applicationUser.setEmailAddress(email);
        applicationUser.setStatus(ApplicationUserStatus.UNLOCKED);  // locking not supported for keycloak
    }
}
