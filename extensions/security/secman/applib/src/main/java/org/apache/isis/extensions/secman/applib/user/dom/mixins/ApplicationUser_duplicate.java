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

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserStatus;
import org.apache.isis.extensions.secman.applib.user.dom.mixins.ApplicationUser_duplicate.DomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.NON_IDEMPOTENT
)
@ActionLayout(
        associateWith = "username",
        position = ActionLayout.Position.PANEL,
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationUser_duplicate {

    public static class DomainEvent
            extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationUser_duplicate> {}

    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private ApplicationRoleRepository applicationRoleRepository;

    private final ApplicationUser target;

    @MemberSupport
    public ApplicationUser act(
            final String username,
            @Nullable
            final String emailAddress) {

        return applicationUserRepository
                .newUser(username, target.getAccountType(), user->{

                    user.setStatus(ApplicationUserStatus.LOCKED);
                    user.setEmailAddress(emailAddress);

                    for (ApplicationRole role : target.getRoles()) {
                        applicationRoleRepository.addRoleToUser(role, user);
                    }

                });

    }

}
