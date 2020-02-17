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
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Repository;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Password;
import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.user.AccountType;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_lock;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_unlock;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_updatePassword;

import lombok.val;

@Repository
@Named("isisExtSecman.applicationUserRepository")
public class ApplicationUserRepository
implements org.apache.isis.extensions.secman.api.user.ApplicationUserRepository {

    @Inject private QueryResultsCache queryResultsCache;
    @Inject private FactoryService factoryService;
    @Inject private RepositoryService repository;
    @Inject private SecurityModuleConfig configBean;
    
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
    public Collection<org.apache.isis.extensions.secman.api.user.ApplicationUser> find(final String search) {
        final String regex = String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."));
        return repository.allMatches(new QueryDefault<>(
                ApplicationUser.class,
                "find", "regex", regex))
                .stream()
                .map(org.apache.isis.extensions.secman.api.user.ApplicationUser.class::cast)
                .collect(_Sets.toUnmodifiableSorted());
    }

    // -- allUsers

    @Override
    public List<ApplicationUser> findByAtPath(final String atPath) {
        return repository.allMatches(new QueryDefault<>(
                ApplicationUser.class,
                "findByAtPath", "atPath", atPath));
    }

    // -- allUsers

    @Override
    public Collection<org.apache.isis.extensions.secman.api.user.ApplicationUser> allUsers() {
        return repository.allInstances(ApplicationUser.class)
                .stream()
                .map(org.apache.isis.extensions.secman.api.user.ApplicationUser.class::cast)
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Collection<org.apache.isis.extensions.secman.api.user.ApplicationUser> findMatching(final String search) {
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
    public org.apache.isis.extensions.secman.api.user.ApplicationUser newUser(String username, AccountType accountType) {
        val user = factoryService.detachedEntity(ApplicationUser.class);
        user.setUsername(username);
        user.setAccountType(accountType);
        user.setStatus(ApplicationUserStatus.DISABLED);
        repository.persist(user);
        return user;
    }

    @Override
    public org.apache.isis.extensions.secman.api.user.ApplicationUser newLocalUser(
            String username, 
            Password password,
            ApplicationUserStatus status) {
        
        val user = newUser(username, AccountType.LOCAL);
        user.setStatus(status);
        
        if (password != null) {
            factoryService.mixin(ApplicationUser_updatePassword.class, user)
            .updatePassword(password.getPassword());
        }
        
        repository.persistAndFlush(user);
        return user;
    }
    
    @Override
    public org.apache.isis.extensions.secman.api.user.ApplicationUser newDelegateUser(
            String username,
            ApplicationUserStatus status) {
      
          val user = newUser(username, AccountType.DELEGATED);
          user.setStatus(status);
          repository.persistAndFlush(user);
          return user;
      }
    
}
