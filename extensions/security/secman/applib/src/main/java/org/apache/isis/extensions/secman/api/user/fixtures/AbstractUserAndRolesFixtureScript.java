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
package org.apache.isis.extensions.secman.api.user.fixtures;

import javax.inject.Inject;

import org.apache.isis.applib.value.Password;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.dom.AccountType;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserStatus;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import lombok.Getter;

public class AbstractUserAndRolesFixtureScript extends FixtureScript {

    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private ApplicationRoleRepository applicationRoleRepository;

    private final String username;
    private final String password;
    private final String emailAddress;
    private final String tenancyPath;
    private final AccountType accountType;
    private final Can<String> roleNames;

    /**
     * The {@link ApplicationUser}
     * updated/created by the fixture.
     */
    @Getter private ApplicationUser applicationUser;

    public AbstractUserAndRolesFixtureScript(
            final String username,
            final String password,
            final AccountType accountType,
            final Can<String> roleNames) {
        this(username, password, null, null, accountType, roleNames);
    }

    public AbstractUserAndRolesFixtureScript(
            final String username,
            final String password,
            final String emailAddress,
            final String tenancyPath,
            final AccountType accountType,
            final Can<String> roleNames) {

        this.username = username;
        this.password = password;
        this.emailAddress = emailAddress;
        this.tenancyPath = tenancyPath;
        this.accountType = accountType;
        this.roleNames = roleNames;
    }

    @Override
    protected void execute(final ExecutionContext executionContext) {

        // create user if does not exist, and assign to the role
        applicationUser = applicationUserRepository.findByUsername(username)
                .orElse(null);
        if(applicationUser == null) {

            switch (accountType) {
            case DELEGATED:
                applicationUser = applicationUserRepository
                    .newDelegateUser(username, ApplicationUserStatus.UNLOCKED);
                break;
            case LOCAL:
                final Password pwd = new Password(password);
                applicationUser = applicationUserRepository
                        .newLocalUser(username, pwd, ApplicationUserStatus.UNLOCKED);
                applicationUser.setEmailAddress(emailAddress);
            }

            if(applicationUser == null) {
                throw _Exceptions.unrecoverableFormatted("failed to create user '%s'", username);
            }

            // update tenancy (repository checks for null)
            applicationUser.setAtPath(tenancyPath);
        }

        for (final String roleName : roleNames) {
            applicationRoleRepository.findByName(roleName)
            .map(securityRole->{
                applicationRoleRepository.addRoleToUser(securityRole, applicationUser);
                return Boolean.TRUE;
            })
            .orElseThrow(()->_Exceptions.unrecoverable("role not found by name: "+roleName));
        }

    }

}
