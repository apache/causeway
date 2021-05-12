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
package org.apache.isis.extensions.secman.api.user.dom.mixins;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_delete.DomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        associateWith = "username",
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE
)
@ActionLayout(
        position = ActionLayout.Position.PANEL,
        sequence = "2"
)
@RequiredArgsConstructor
public class ApplicationUser_delete {

    public static class DomainEvent
            extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationUser_delete> {}

    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private RepositoryService repository;

    private final ApplicationUser target;

    @MemberSupport
    public Collection<ApplicationUser> act() {
        repository.removeAndFlush(target);
        return applicationUserRepository.allUsers();
    }

    @MemberSupport
    public String disableAct() {
        return applicationUserRepository.isAdminUser(target)? "Cannot delete the admin user": null;
    }

}
