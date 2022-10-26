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
package org.apache.causeway.extensions.secman.applib.user.fixtures;

import java.util.function.Supplier;

import javax.inject.Inject;

import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.value.Password;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.AccountType;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;

import lombok.Getter;
import lombok.val;

/**
 * Convenience fixture script intended to be easily subclassed in order to set up an
 * {@link org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser} with associated roles.
 *
 * @since 2.x {@index}
 */
public abstract class AbstractUserAndRolesFixtureScript extends FixtureScript {

    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private ApplicationRoleRepository applicationRoleRepository;
    @Inject private ServiceInjector serviceInjector;

    private final Supplier<String> usernameSupplier;
    private final Supplier<String> passwordSupplier;
    private final Supplier<String> emailAddressSupplier;
    private final Supplier<String> tenancyPathSupplier;
    private final Supplier<AccountType> accountTypeSupplier;
    private final Supplier<Can<String>> roleNamesSupplier;

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
            final Supplier<String> usernameSupplier,
            final Supplier<String> passwordSupplier,
            final Supplier<AccountType> accountTypeSupplier,
            final Supplier<Can<String>> roleNamesSupplier) {
        this(usernameSupplier, passwordSupplier, () -> null, () -> null, accountTypeSupplier, roleNamesSupplier);
    }

    public AbstractUserAndRolesFixtureScript(
            final String username,
            final String password,
            final String emailAddress,
            final String tenancyPath,
            final AccountType accountType,
            final Can<String> roleNames) {
        this(() -> username, () -> password, () -> emailAddress, () -> tenancyPath, () -> accountType, () -> roleNames);
    }

    public AbstractUserAndRolesFixtureScript(
            final Supplier<String> usernameSupplier,
            final Supplier<String> passwordSupplier,
            final Supplier<String> emailAddressSupplier,
            final Supplier<String> tenancyPathSupplier,
            final Supplier<AccountType> accountTypeSupplier,
            final Supplier<Can<String>> roleNamesSupplier) {

        this.usernameSupplier = nullSafe(usernameSupplier);
        this.passwordSupplier = nullSafe(passwordSupplier);
        this.emailAddressSupplier = nullSafe(emailAddressSupplier);
        this.tenancyPathSupplier = nullSafe(tenancyPathSupplier);
        this.accountTypeSupplier = nullSafe(accountTypeSupplier);
        this.roleNamesSupplier = nullSafe(roleNamesSupplier);
    }

    protected final String getUsername() {
        return usernameSupplier.get();
    }

    protected String getPassword() {
        return passwordSupplier.get();
    }

    protected final String getEmailAddress() {
        return emailAddressSupplier.get();
    }

    protected final String getTenancyPath() {
        return tenancyPathSupplier.get();
    }

    protected final AccountType getAccountType() {
        return accountTypeSupplier.get();
    }

    protected final Can<String> getRoleNames() {
        return roleNamesSupplier.get();
    }


    @Override
    protected void execute(final ExecutionContext executionContext) {

        serviceInjector.injectServicesInto(this.usernameSupplier);
        serviceInjector.injectServicesInto(this.passwordSupplier);
        serviceInjector.injectServicesInto(this.emailAddressSupplier);
        serviceInjector.injectServicesInto(this.tenancyPathSupplier);
        serviceInjector.injectServicesInto(this.accountTypeSupplier);
        serviceInjector.injectServicesInto(this.roleNamesSupplier);

        // create user if does not exist, and assign to the role
        val username = getUsername();
        applicationUser = applicationUserRepository.findByUsername(username)
                .orElse(null);
        if(applicationUser == null) {

            switch (getAccountType()) {
            case DELEGATED:
                applicationUser = applicationUserRepository
                    .newDelegateUser(username, ApplicationUserStatus.UNLOCKED);
                break;
            case LOCAL:
                final Password pwd = new Password(getPassword());
                applicationUser = applicationUserRepository
                        .newLocalUser(username, pwd, ApplicationUserStatus.UNLOCKED);
                applicationUser.setEmailAddress(getEmailAddress());
            }

            if(applicationUser == null) {
                throw _Exceptions.unrecoverable("failed to create user '%s'", username);
            }

            // update tenancy (repository checks for null)
            applicationUser.setAtPath(getTenancyPath());
        }

        for (final String roleName : getRoleNames()) {
            applicationRoleRepository.findByName(roleName)
            .map(securityRole->{
                applicationRoleRepository.addRoleToUser(securityRole, applicationUser);
                return Boolean.TRUE;
            })
            .orElseThrow(()->_Exceptions.unrecoverable("role not found by name: " + roleName));
        }

    }

    private static <T> Supplier<T> nullSafe(final Supplier<T> supplier) {
        return supplier != null ? supplier : () -> null;
    }

}
