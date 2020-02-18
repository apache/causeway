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

import java.util.Collection;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

//@Action(
//        domainEvent = RemoveRoleDomainEvent.class, 
//        associateWith = "roles",
//        associateWithSequence = "2")
//@ActionLayout(
//        named="Remove"
//        )
@Deprecated
@RequiredArgsConstructor
public class ApplicationUser_removeRole {
    
    @Inject private ApplicationRoleRepository<? extends ApplicationRole> applicationRoleRepository;
    @Inject private ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;
    
    private final ApplicationUser holder;

    @Model
    public ApplicationUser act(final ApplicationRole role) {
        applicationRoleRepository.removeRoleFromUser(role, holder);
        return holder;
    }

    @Model
    public String disableAct() {
        return holder.getRoles().isEmpty()? "No roles to remove": null;
    }

    @Model
    public Collection<? extends ApplicationRole> choices0Act() {
        return applicationRoleRepository.getRoles(holder);
    }

    @Model
    // duplicated in ApplicationRole_removeUser mixin
    public String validateAct(
            final ApplicationRole applicationRole) {
        if(applicationUserRepository.isAdminUser(holder) 
                && applicationRoleRepository.isAdminRole(applicationRole)) {
            return "Cannot remove admin user from the admin role.";
        }
        return null;
    }
}
