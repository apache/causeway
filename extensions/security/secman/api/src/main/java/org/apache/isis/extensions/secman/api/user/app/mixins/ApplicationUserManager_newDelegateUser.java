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
package org.apache.isis.extensions.secman.api.user.app.mixins;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.SecurityRealmCharacteristic;
import org.apache.isis.extensions.secman.api.SecurityRealmService;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.app.ApplicationUserManager;
import org.apache.isis.extensions.secman.api.user.app.mixins.ApplicationUserManager_newDelegateUser.DomainEvent;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserStatus;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        domainEvent = DomainEvent.class
)
@ActionLayout(
        associateWith = "allUsers",
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationUserManager_newDelegateUser {

    public static class DomainEvent
            extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationUserManager_newDelegateUser> {}

    @Inject private ApplicationRoleRepository applicationRoleRepository;
    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private SecmanConfiguration configBean;
    @Inject private RepositoryService repository;
    @Inject private SecurityRealmService securityRealmService;

    private final ApplicationUserManager target;

    @MemberSupport
    public ApplicationUserManager act(

          @Parameter(maxLength = ApplicationUser.Username.MAX_LENGTH)
          @ParameterLayout(named = "Name")
          final String username,

          @Parameter(optionality = Optionality.OPTIONAL)
          @ParameterLayout(named = "Initial role")
          final ApplicationRole initialRole,

          @Parameter(optionality = Optionality.OPTIONAL)
          @ParameterLayout(named = "Enabled?")
          final Boolean enabled

            ) {

        final ApplicationUser user = applicationUserRepository
                .newDelegateUser(username, ApplicationUserStatus.parse(enabled));

        if (initialRole != null) {
            applicationRoleRepository.addRoleToUser(initialRole, user);
        }
        repository.persist(user);
        return target;
    }

    @MemberSupport
    public boolean hideAct() {
        return hasNoDelegateAuthenticationRealm();
    }

    @MemberSupport
    public ApplicationRole default1Act() {
        return applicationRoleRepository
                .findByNameCached(configBean.getRegularUserRoleName())
                .orElse(null);
    }


    // -- HELPER

    private boolean hasNoDelegateAuthenticationRealm() {
        val realm = securityRealmService.getCurrentRealm();
        return realm == null
                || !realm.getCharacteristics()
                .contains(SecurityRealmCharacteristic.DELEGATING);
    }

}
