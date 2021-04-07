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
package org.apache.isis.extensions.secman.model.dom.role;

import java.util.Collection;
import java.util.List;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRole.AddUserDomainEvent;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = AddUserDomainEvent.class, 
        associateWith = "users")
@ActionLayout(named="Add", sequence = "1")
@RequiredArgsConstructor
public class ApplicationRole_addUser {
    
    @Inject private ApplicationRoleRepository<? extends ApplicationRole> applicationRoleRepository;
    @Inject private ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;
    
    private final ApplicationRole target;

    @Model
    public ApplicationRole act(final ApplicationUser applicationUser) {
        applicationRoleRepository.addRoleToUser(target, applicationUser);
        return target;
    }

    @Model
    public List<? extends ApplicationUser> autoComplete0Act(final String search) {
        final Collection<? extends ApplicationUser> matchingSearch = applicationUserRepository.find(search);
        final List<? extends ApplicationUser> list = _Lists.newArrayList(matchingSearch);
        list.removeAll(applicationUserRepository.findByRole(target));
        return list;
    }
}
