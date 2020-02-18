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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Repository;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.encryption.PasswordEncryptionService;
import org.apache.isis.extensions.secman.api.user.AccountType;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_lock;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_unlock;

import lombok.val;

@Repository
@Named("isisExtSecman.applicationUserRepository")
public class ApplicationUserRepository
implements org.apache.isis.extensions.secman.api.user.ApplicationUserRepository<ApplicationUser> {

    @Inject private QueryResultsCache queryResultsCache;
    @Inject private FactoryService factoryService;
    @Inject private RepositoryService repository;
    @Inject private SecurityModuleConfig configBean;
    @Inject private Optional<PasswordEncryptionService> passwordEncryptionService; // empty if no candidate is available
    
    @Override
    public ApplicationUser newApplicationUser() {
        return factoryService.detachedEntity(ApplicationUser.class);
    }
    
    // -- findOrCreateUserByUsername (programmatic)

    /**
     * Uses the {@link QueryResultsCache} in order to support
     * multiple lookups from <code>org.apache.isis.extensions.secman.jdo.app.user.UserPermissionViewModel</code>.
     * <p>
     * <p>
     * If the user does not exist, it will be automatically created.
     * </p>
     */
    @Override
    public ApplicationUser findOrCreateUserByUsername(
            final String username) {
        // slightly unusual to cache a function that modifies state, but safe because this is idempotent
        return queryResultsCache.execute(new Callable<ApplicationUser>() {
            @Override
            public ApplicationUser call() throws Exception {
                final ApplicationUser applicationUser = findByUsername(username);
                if (applicationUser != null) {
                    return applicationUser;
                }
                return (ApplicationUser) newDelegateUser(username, null);
            }
        }, ApplicationUserRepository.class, "findOrCreateUserByUsername", username);
    }

    // -- findByUsername

    public ApplicationUser findByUsernameCached(final String username) {
        return queryResultsCache.execute(new Callable<ApplicationUser>() {
            @Override public ApplicationUser call() throws Exception {
                return findByUsername(username);
            }
        }, ApplicationUserRepository.class, "findByUsernameCached", username);
    }

    @Override
    public ApplicationUser findByUsername(final String username) {
        return repository.uniqueMatch(new QueryDefault<>(
                ApplicationUser.class,
                "findByUsername", "username", username)).orElse(null);
    }

    // -- findByEmailAddress (programmatic)

    public ApplicationUser findByEmailAddressCached(final String emailAddress) {
        return queryResultsCache.execute(new Callable<ApplicationUser>() {
            @Override public ApplicationUser call() throws Exception {
                return findByEmailAddress(emailAddress);
            }
        }, ApplicationUserRepository.class, "findByEmailAddressCached", emailAddress);
    }

    public ApplicationUser findByEmailAddress(final String emailAddress) {
        return repository.uniqueMatch(new QueryDefault<>(
                ApplicationUser.class,
                "findByEmailAddress", "emailAddress", emailAddress))
                .orElse(null);
    }

    // -- findByName

    @Override
    public Collection<ApplicationUser> find(final String search) {
        final String regex = String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."));
        return repository.allMatches(new QueryDefault<>(
                ApplicationUser.class,
                "find", "regex", regex))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    // -- allUsers

    @Override
    public List<ApplicationUser> findByAtPath(final String atPath) {
        return repository.allMatches(new QueryDefault<>(
                ApplicationUser.class,
                "findByAtPath", "atPath", atPath));
    }
    
    @Override
    public Collection<ApplicationUser> findByRole(
            org.apache.isis.extensions.secman.api.role.ApplicationRole genericRole) {
        
        val role = _Casts.<ApplicationRole>uncheckedCast(genericRole);
        return role.getUsers()
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    // -- allUsers

    @Override
    public Collection<ApplicationUser> allUsers() {
        return repository.allInstances(ApplicationUser.class)
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Collection<ApplicationUser> findMatching(final String search) {
        if (search != null && search.length() > 0) {
            return find(search);
        }
        return Collections.emptySortedSet();
    }
    
    // -- UPDATE USER STATE
    
    @Override
    public void enable(org.apache.isis.extensions.secman.api.user.ApplicationUser user) {
        if(user.getStatus() != ApplicationUserStatus.ENABLED) {
             factoryService.mixin(ApplicationUser_unlock.class, user)
             .act();
        }
    }

    @Override
    public void disable(org.apache.isis.extensions.secman.api.user.ApplicationUser user) {
        if(user.getStatus() != ApplicationUserStatus.DISABLED) {
            factoryService.mixin(ApplicationUser_lock.class, user)
            .act();
        }
    }

    @Override
    public boolean isAdminUser(org.apache.isis.extensions.secman.api.user.ApplicationUser user) {
        return configBean.getAdminUserName().equals(user.getName());
    }

    @Override
    public ApplicationUser newUser(
            String username, 
            AccountType accountType,
            Consumer<ApplicationUser> beforePersist) {
        
        val user = newApplicationUser();
        user.setUsername(username);
        user.setAccountType(accountType);
        user.setStatus(ApplicationUserStatus.DISABLED);
        beforePersist.accept(user);
        repository.persistAndFlush(user);
        return user;
    }
    
    @Override
    public boolean updatePassword(
            final org.apache.isis.extensions.secman.api.user.ApplicationUser user, 
            final String password) {
        // in case called programmatically
        if(!isPasswordFeatureEnabled(user)) {
            return false;
        }
        val encrypter = passwordEncryptionService.orElseThrow(_Exceptions::unexpectedCodeReach);
        user.setEncryptedPassword(encrypter.encrypt(password));
        repository.persistAndFlush(user);
        return true;
    }
    
    @Override
    public boolean isPasswordFeatureEnabled(org.apache.isis.extensions.secman.api.user.ApplicationUser user) {
        return user.isLocalAccount() 
                && passwordEncryptionService!=null 
                && passwordEncryptionService.isPresent();
    }

    
}
