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
package org.apache.isis.extensions.secman.jpa.seed.scripts;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.value.Password;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.secman.api.user.AccountType;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;
import org.apache.isis.extensions.secman.jpa.dom.role.ApplicationRole;
import org.apache.isis.extensions.secman.jpa.dom.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUser;
import org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUserRepository;
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
    private final List<String> roleNames;
    
    /**
     * The {@link org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser} 
     * updated/created by the fixture.
     */
    @Getter private ApplicationUser applicationUser;

    public AbstractUserAndRolesFixtureScript(
            final String username,
            final String password,
            final AccountType accountType, 
            final List<String> roleNames) {
        this(username, password, null, null, accountType, roleNames);
    }

    public AbstractUserAndRolesFixtureScript(
            final String username,
            final String password,
            final String emailAddress,
            final String tenancyPath,
            final AccountType accountType,
            final List<String> roleNames) {
        
        this.username = username;
        this.password = password;
        this.emailAddress = emailAddress;
        this.tenancyPath = tenancyPath;
        this.accountType = accountType;
        this.roleNames = Collections.unmodifiableList(_Lists.newArrayList(roleNames));
    }

    @Override
    protected void execute(final ExecutionContext executionContext) {

        // create user if does not exist, and assign to the role
        applicationUser = applicationUserRepository.findByUsername(username)
                .orElse(null);
        if(applicationUser == null) {
            
            switch (accountType) {
            case DELEGATED:
                applicationUser = (ApplicationUser) applicationUserRepository
                    .newDelegateUser(username, ApplicationUserStatus.ENABLED);
                break;
            case LOCAL:
                final Password pwd = new Password(password);
                applicationUser = (ApplicationUser) applicationUserRepository
                        .newLocalUser(username, pwd, ApplicationUserStatus.ENABLED);
                applicationUser.setEmailAddress(emailAddress);
            }

            if(applicationUser == null) {
                throw _Exceptions.unrecoverableFormatted("failed to create user '%s'", username);
            }
            
            // update tenancy (repository checks for null)
            applicationUser.setAtPath(tenancyPath);
            
            for (final String roleName : roleNames) {
                final ApplicationRole securityRole = applicationRoleRepository.findByName(roleName)
                        .orElse(null);

                if(securityRole!=null) {
                    applicationRoleRepository.addRoleToUser(securityRole, applicationUser);
                } else {
                    throw _Exceptions.unrecoverable("role not found by name: "+roleName);
                }

            }
            
        }
    }




}
