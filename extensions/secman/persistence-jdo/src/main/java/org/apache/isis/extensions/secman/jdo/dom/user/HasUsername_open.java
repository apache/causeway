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
package org.apache.isis.extensions.secman.jdo.dom.user;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.HasUsername;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.extensions.secman.api.SecurityModule;

import lombok.RequiredArgsConstructor;

@Mixin(method = "exec") @RequiredArgsConstructor
public class HasUsername_open {

    private final HasUsername holder;

    public static class ActionDomainEvent extends SecurityModule.ActionDomainEvent<HasUsername_open> {
		private static final long serialVersionUID = 1L;}

    @Action(
            semantics = SemanticsOf.SAFE,
            domainEvent = ActionDomainEvent.class
    )
    @ActionLayout(
            contributed = Contributed.AS_ACTION
    )
    @MemberOrder(name = "User", sequence = "1") // associate with a 'User' property (if any)
    public ApplicationUser exec() {
        if (holder == null || holder.getUsername() == null) {
            return null;
        }
        return applicationUserRepository.findByUsername(holder.getUsername());
    }
    public boolean hideExec() {
        return holder instanceof ApplicationUser;
    }

    public TranslatableString disableExec() {
        if (holder == null || holder.getUsername() == null) {
            return TranslatableString.tr("No username");
        }
        return null;
    }

    @Inject
    private ApplicationUserRepository applicationUserRepository;

}
