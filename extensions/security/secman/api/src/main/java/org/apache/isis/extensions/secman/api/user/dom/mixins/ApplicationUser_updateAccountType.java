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
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.user.dom.AccountType;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_updateAccountType.DomainEvent;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

@Action(
        associateWith = "accountType",
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        promptStyle = PromptStyle.INLINE_AS_IF_EDIT,
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationUser_updateAccountType {

    public static class DomainEvent
            extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationUser_updateAccountType> {}

    @Inject private ApplicationUserRepository applicationUserRepository;

    private final ApplicationUser target;

    @MemberSupport
    public ApplicationUser act(
            final AccountType accountType) {
        target.setAccountType(accountType);
        return target;
    }

    @MemberSupport
    public String disableAct() {
        return applicationUserRepository.isAdminUser(target)
                ? "Cannot change account type for admin user"
                        : null;
    }

    @MemberSupport
    public AccountType default0Act() {
        return target.getAccountType();
    }

}
