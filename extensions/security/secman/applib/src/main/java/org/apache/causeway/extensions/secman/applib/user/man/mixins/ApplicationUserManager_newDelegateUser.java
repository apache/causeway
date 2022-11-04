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
package org.apache.causeway.extensions.secman.applib.user.man.mixins;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;
import org.apache.causeway.extensions.secman.applib.user.man.ApplicationUserManager;
import org.apache.causeway.extensions.secman.applib.user.man.mixins.ApplicationUserManager_newDelegateUser.DomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 *
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = DomainEvent.class
)
@ActionLayout(
        associateWith = "allUsers",
        sequence = "2"
)
@RequiredArgsConstructor
public class ApplicationUserManager_newDelegateUser {

    public static class DomainEvent
            extends CausewayModuleExtSecmanApplib.ActionDomainEvent<ApplicationUserManager_newDelegateUser> {}

    @Inject private ApplicationRoleRepository applicationRoleRepository;
    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private CausewayConfiguration config;
    @Inject private RepositoryService repository;

    private final ApplicationUserManager target;

    @MemberSupport public ApplicationUserManager act(

          @Parameter(maxLength = ApplicationUser.Username.MAX_LENGTH)
          @ParameterLayout(named = "Name")
          final String username,

          @Parameter(optionality = Optionality.OPTIONAL)
          @ParameterLayout(named = "Initial role")
          final ApplicationRole initialRole,

          @Parameter(optionality = Optionality.OPTIONAL)
          @ParameterLayout(named = "Unlocked?")
          final Boolean unlocked

            ) {

        final ApplicationUser user = applicationUserRepository
                .newDelegateUser(username, ApplicationUserStatus.parse(unlocked));

        if (initialRole != null) {
            applicationRoleRepository.addRoleToUser(initialRole, user);
        }
        repository.persist(user);
        return target;
    }

    @MemberSupport public ApplicationRole defaultInitialRole() {
        val regularUserRoleName = config.getExtensions().getSecman().getSeed().getRegularUser().getRoleName();
        return applicationRoleRepository
                .findByNameCached(regularUserRoleName)
                .orElse(null);
    }



}
