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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUser.AddRoleDomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(domainEvent = AddRoleDomainEvent.class, associateWith = "roles")
@ActionLayout(named="Add")
@RequiredArgsConstructor
public class ApplicationUser_addRole {
    
    @Inject private ApplicationRoleRepository<? extends ApplicationRole> applicationRoleRepository;
    
    private final ApplicationUser holder;

    @MemberOrder(sequence = "1")
    public ApplicationUser act(final ApplicationRole role) {
        applicationRoleRepository.addRoleToUser(role, holder);
        return holder;
    }

    public Collection<? extends ApplicationRole> choices0Act() {
        val allRoles = applicationRoleRepository.allRoles();
        val applicationRoles = _Sets.newTreeSet(allRoles);
        applicationRoles.removeAll(holder.getRoles());
        return applicationRoles;
    }

    public String disableAct() {
        return choices0Act().isEmpty()? "All roles added": null;
    }

}
