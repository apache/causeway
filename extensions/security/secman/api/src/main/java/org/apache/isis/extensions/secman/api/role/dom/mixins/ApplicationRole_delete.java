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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole.DeleteDomainEvent;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRoleRepository;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = DeleteDomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
@ActionLayout(sequence = "1")
@RequiredArgsConstructor
public class ApplicationRole_delete {

    @Inject private ApplicationRoleRepository<? extends ApplicationRole> applicationRoleRepository;

    private final ApplicationRole holder;

    @MemberSupport
    public Collection<? extends ApplicationRole> act() {
        applicationRoleRepository.deleteRole(holder);
        return applicationRoleRepository.allRoles();
    }

    @MemberSupport
    public String disableAct() {
        return applicationRoleRepository.isAdminRole(holder) ? "Cannot delete the admin role" : null;
    }

}
