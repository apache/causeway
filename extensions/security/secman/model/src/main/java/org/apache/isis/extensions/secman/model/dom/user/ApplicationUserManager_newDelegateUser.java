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
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUser.NewDelegateUserDomainEvent;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = NewDelegateUserDomainEvent.class, 
        associateWith = "allUsers")
@RequiredArgsConstructor
public class ApplicationUserManager_newDelegateUser {
    
    @Inject private ApplicationRoleRepository<? extends ApplicationRole> applicationRoleRepository;
    @Inject private ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;
    @Inject private RepositoryService repository;

    @SuppressWarnings("unused")
    private final ApplicationUserManager target;
    
    @Model
    public ApplicationUser act(
            final String username,
            final ApplicationRole initialRole,
            final Boolean enabled) {
        
        final ApplicationUser user = applicationUserRepository
                .newDelegateUser(username, ApplicationUserStatus.parse(enabled));

        if (initialRole != null) {
            applicationRoleRepository.addRoleToUser(initialRole, user);
        }
        repository.persist(user);
        return user;
    }

}
