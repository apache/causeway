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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserStatus;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_lock.DomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        associateWith = "status",
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        named="Disable",
        sequence = "2"
)
@RequiredArgsConstructor
public class ApplicationUser_lock {

    public static class DomainEvent
            extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationUser_lock> {}

    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private SecmanConfiguration configBean;

    private final ApplicationUser target;

    @MemberSupport
    public ApplicationUser act() {
        target.setStatus(ApplicationUserStatus.DISABLED);
        return target;
    }

    @MemberSupport
    public String disableAct() {
        if(applicationUserRepository.isAdminUser(target)) {
            return "Cannot disable the '" + configBean.getAdminUserName() + "' user.";
        }
        return target.getStatus() == ApplicationUserStatus.DISABLED ? "Status is already set to DISABLE": null;
    }

}
