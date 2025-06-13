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
package org.apache.causeway.extensions.secman.applib.user.dom;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.value.Password;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy;

import org.jspecify.annotations.NonNull;

/**
 * @since 2.0 {@index}
 */
public interface ApplicationUserRepository {

    /**
     * @return detached entity
     */
    ApplicationUser newApplicationUser();

    Optional<ApplicationUser> findByUsername(String username);
    ApplicationUser findOrCreateUserByUsername(String username);

    Collection<ApplicationUser> allUsers();
    Collection<ApplicationUser> find(String search);
    Collection<ApplicationUser> findByAtPath(String atPath);
    Collection<ApplicationUser> findByRole(ApplicationRole role);
    Collection<ApplicationUser> findByTenancy(ApplicationTenancy tenancy);
    Optional<ApplicationUser> findByEmailAddress(String emailAddress);

    /**
     * auto-complete support
     * @param search
     */
    Collection<ApplicationUser> findMatching(String search);

    void enable(ApplicationUser user);
    void disable(ApplicationUser user);

    boolean isAdminUser(ApplicationUser user);
    boolean isPasswordFeatureEnabled(ApplicationUser holder);

    boolean updatePassword(ApplicationUser user, String password);

    ApplicationUser newUser(String username, AccountType accountType, Consumer<ApplicationUser> beforePersist);

    default ApplicationUser upsertLocal(
            @NonNull final String username,
            @Nullable final Password password,
            @NonNull final ApplicationUserStatus status) {
        return findByUsername(username)
                .orElseGet(() -> newLocalUser(username, password, status));
    }

    default ApplicationUser newLocalUser(
            @NonNull final String username,
            @Nullable final Password password,
            @NonNull final ApplicationUserStatus status) {

        return newUser(username, AccountType.LOCAL, user->{

            user.setStatus(status);

            if (password != null) {
                updatePassword(user, password.password());
            }

        });
    }

    default ApplicationUser newDelegateUser(
            final String username,
            final ApplicationUserStatus status) {

        return newUser(username, AccountType.DELEGATED, user->{
            user.setStatus(status);
        });

    }

}
