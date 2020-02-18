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
package org.apache.isis.extensions.secman.api.user;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.isis.applib.value.Password;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy;

import lombok.NonNull;

public interface ApplicationUserRepository<U extends ApplicationUser> {

    /**
     * @return detached entity
     */
    U newApplicationUser();

    Optional<U> findByUsername(String username);
    U findOrCreateUserByUsername(String username);

    Collection<U> allUsers();
    Collection<U> find(String search);
    Collection<U> findByAtPath(String atPath);
    Collection<U> findByRole(ApplicationRole role);
    Collection<U> findByTenancy(ApplicationTenancy tenancy);
    
    /**
     * auto-complete support
     * @param search
     */
    Collection<U> findMatching(String search);

    void enable(ApplicationUser user);
    void disable(ApplicationUser user);

    boolean isAdminUser(ApplicationUser user);
    boolean isPasswordFeatureEnabled(ApplicationUser holder);

    boolean updatePassword(ApplicationUser user, String password);
    
    U newUser(String username, AccountType accountType, Consumer<U> beforePersist);

    default U newLocalUser(
            @NonNull String username, 
            @Nullable Password password,
            @NonNull ApplicationUserStatus status) {

        return newUser(username, AccountType.LOCAL, user->{

            user.setStatus(status);

            if (password != null) {
                updatePassword(user, password.getPassword());
            }

        });
    }

    default U newDelegateUser(
            String username,
            ApplicationUserStatus status) {

        return newUser(username, AccountType.DELEGATED, user->{
            user.setStatus(status);
        });

    }

    


}
