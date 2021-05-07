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
package org.apache.isis.extensions.secman.api.user.mixins;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.extensions.secman.api.user.AccountType;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUser.UpdateAccountTypeDomainEvent;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = UpdateAccountTypeDomainEvent.class,
        associateWith = "accountType")
@ActionLayout(sequence = "1")
@RequiredArgsConstructor
public class ApplicationUser_updateAccountType {

    @Inject private ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;

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
