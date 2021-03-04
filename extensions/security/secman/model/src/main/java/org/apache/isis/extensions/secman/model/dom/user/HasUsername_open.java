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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

@Action(
        semantics = SemanticsOf.SAFE,
        domainEvent = HasUsername_open.ActionDomainEvent.class
        )
@RequiredArgsConstructor
public class HasUsername_open {

    @Inject private ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;

    private final HasUsername target;

    public static class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<HasUsername_open> {}


    @MemberOrder(name = "User", sequence = "1") // associate with a 'User' property (if any)
    public ApplicationUser act() {
        if (target == null || target.getUsername() == null) {
            return null;
        }
        return applicationUserRepository.findByUsername(target.getUsername()).orElse(null);
    }

    public boolean hideAct() {
        return target instanceof ApplicationUser;
    }

    public TranslatableString disableAct() {
        if (target == null || target.getUsername() == null) {
            return TranslatableString.tr("No username");
        }
        return null;
    }


}
