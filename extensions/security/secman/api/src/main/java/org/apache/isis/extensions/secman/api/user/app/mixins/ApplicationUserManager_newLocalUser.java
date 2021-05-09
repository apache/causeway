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

import java.util.Objects;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Password;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.app.mixins.ApplicationUserManager_newLocalUser.DomainEvent;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserStatus;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_updateEmailAddress;
import org.apache.isis.extensions.secman.api.user.app.ApplicationUserManager;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = DomainEvent.class,
        associateWith = "allUsers")
@RequiredArgsConstructor
public class ApplicationUserManager_newLocalUser
extends ApplicationUserManager_newLocalUserAbstract {

    public static class DomainEvent
            extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationUserManager_newLocalUser> {}

    @Inject private ApplicationRoleRepository applicationRoleRepository;
    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private SecmanConfiguration configBean;
    @Inject private FactoryService factory;
    @Inject private RepositoryService repository;

    private final ApplicationUserManager target;

    @MemberSupport
    public ApplicationUserManager act(
          @Parameter(maxLength = ApplicationUser.MAX_LENGTH_USERNAME)
          @ParameterLayout(named = "Name")
          final String username,

          @Parameter(optionality = Optionality.OPTIONAL)
          @ParameterLayout(named = "Password")
          final Password password,

          @Parameter(optionality = Optionality.OPTIONAL)
          @ParameterLayout(named = "Repeat password")
          final Password passwordRepeat,

          @Parameter(optionality = Optionality.OPTIONAL)
          @ParameterLayout(named = "Initial role")
          final ApplicationRole initialRole,

          @Parameter(optionality = Optionality.OPTIONAL)
          @ParameterLayout(named = "Enabled?")
          final Boolean enabled,

          @Parameter(optionality = Optionality.OPTIONAL)
          @ParameterLayout(named = "Email Address")
          final String emailAddress) {

        ApplicationUser user = applicationUserRepository.findByUsername(username).orElse(null);
        if (user == null) {
            user = applicationUserRepository
                    .newLocalUser(username, password, ApplicationUserStatus.parse(enabled));
        }
        if (initialRole != null) {
            applicationRoleRepository.addRoleToUser(initialRole, user);
        }
        if (emailAddress != null) {
            factory.mixin(ApplicationUser_updateEmailAddress.class, user)
                    .act(emailAddress);
        }
        repository.persist(user);
        return target;
    }

    @MemberSupport
    public String validateAct(
            final String username,
            final Password newPassword,
            final Password newPasswordRepeat,
            final ApplicationRole initialRole,
            final Boolean enabled,
            final String emailAddress) {

        if (!Objects.equals(newPassword, newPasswordRepeat)) {
            return "Passwords do not match";
        }

        return null;
    }

    @MemberSupport
    public ApplicationRole default3Act() {
        return applicationRoleRepository
                .findByNameCached(configBean.getRegularUserRoleName())
                .orElse(null);
    }


}
