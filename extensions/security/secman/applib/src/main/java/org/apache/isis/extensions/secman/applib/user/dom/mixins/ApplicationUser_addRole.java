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
package org.apache.isis.extensions.secman.applib.user.dom.mixins;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.applib.user.dom.mixins.ApplicationUser_addRole.DomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "roles",
        named="Add",
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationUser_addRole {

    public static class DomainEvent
            extends IsisModuleExtSecmanApplib.ActionDomainEvent<ApplicationUser_addRole> {}

    @Inject private ApplicationRoleRepository applicationRoleRepository;

    private final ApplicationUser target;

    public ApplicationUser act(final ApplicationRole role) {
        applicationRoleRepository.addRoleToUser(role, target);
        return target;
    }

    public Collection<? extends ApplicationRole> choices0Act() {
        val allRoles = applicationRoleRepository.allRoles();
        val applicationRoles = _Sets.newTreeSet(allRoles);
        applicationRoles.removeAll(target.getRoles());
        return applicationRoles;
    }

    public String disableAct() {
        return choices0Act().isEmpty()? "All roles added": null;
    }

}
