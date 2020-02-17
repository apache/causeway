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
package org.apache.isis.extensions.secman.model.dom.user;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Password;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUser.NewLocalUserDomainEvent;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;

import lombok.RequiredArgsConstructor;

@Action(domainEvent = NewLocalUserDomainEvent.class, associateWith = "allUsers")
@RequiredArgsConstructor
public class ApplicationUserManager_newLocalUser {
    
    @Inject private ApplicationRoleRepository applicationRoleRepository;
    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private FactoryService factory;
    @Inject private RepositoryService repository;
    
    @SuppressWarnings("unused")
    private final ApplicationUserManager holder;

    @Model
    public ApplicationUser act(
            final String username,
            final Password password,
            final Password passwordRepeat,
            final org.apache.isis.extensions.secman.api.role.ApplicationRole initialRole,
            final Boolean enabled,
            final String emailAddress) {
        
        ApplicationUser user = applicationUserRepository.findByUsername(username);
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
        return user;
    }

    @Model
    public String validateAct(
            final String username,
            final Password password,
            final Password passwordRepeat,
            final org.apache.isis.extensions.secman.api.role.ApplicationRole initialRole,
            final Boolean enabled,
            final String emailAddress) {
        
        final ApplicationUser user = factory.get(ApplicationUser.class);
        
        return factory.mixin(ApplicationUser_resetPassword.class, user)
        .validateAct(password, passwordRepeat);
    }
    

}
