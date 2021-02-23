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
package org.apache.isis.extensions.secman.jpa.dom.user;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.encryption.PasswordEncryptionService;
import org.apache.isis.extensions.secman.api.events.UserCreatedEvent;
import org.apache.isis.extensions.secman.api.user.AccountType;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;
import org.apache.isis.extensions.secman.jpa.dom.constants.NamedQueryNames;
import org.apache.isis.extensions.secman.jpa.dom.role.ApplicationRole;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_lock;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_unlock;

import lombok.NonNull;
import lombok.val;

@Service
@Named("isis.ext.secman.applicationUserRepository")
public class ApplicationUserRepository
implements org.apache.isis.extensions.secman.api.user.ApplicationUserRepository<ApplicationUser> {

    @Inject private FactoryService factoryService;
    @Inject private RepositoryService repository;
    @Inject private SecmanConfiguration configBean;
    @Inject private Optional<PasswordEncryptionService> passwordEncryptionService; // empty if no candidate is available
	@Inject protected IsisConfiguration isisConfiguration;
    @Inject private EventBusService eventBusService;  
 
    @Inject private javax.inject.Provider<QueryResultsCache> queryResultsCacheProvider;
    
    @Override
    public ApplicationUser newApplicationUser() {
        return factoryService.detachedEntity(new ApplicationUser());
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
        return queryResultsCacheProvider.get().execute(()->
            findByUsername(username).orElseGet(()->newDelegateUser(username, null)), 
            ApplicationUserRepository.class, "findOrCreateUserByUsername", username);
    }

    // -- findByUsername

    public Optional<ApplicationUser> findByUsernameCached(final String username) {
        return queryResultsCacheProvider.get().execute(this::findByUsername, 
                ApplicationUserRepository.class, "findByUsernameCached", username);
    }

    @Override
    public Optional<ApplicationUser> findByUsername(final String username) {
        return repository.uniqueMatch(Query.named(ApplicationUser.class, NamedQueryNames.USER_BY_USERNAME)
                .withParameter("username", username));
    }

    // -- findByEmailAddress (programmatic)

    public Optional<ApplicationUser> findByEmailAddressCached(final String emailAddress) {
        return queryResultsCacheProvider.get().execute(this::findByEmailAddress, 
                ApplicationUserRepository.class, "findByEmailAddressCached", emailAddress);
    }

    public Optional<ApplicationUser> findByEmailAddress(final String emailAddress) {
        return repository.uniqueMatch(Query.named(ApplicationUser.class, NamedQueryNames.USER_BY_EMAIL)
                .withParameter("emailAddress", emailAddress));
    }

    // -- findByName

    @Override
    public Collection<ApplicationUser> find(final @Nullable String _search) {
        val search = String.format("%s", _Strings.nullToEmpty(_search).replace("*", "%").replace("?", "_"));
        val regex = _Strings.suffix(_Strings.prefix(search, "%"), "%");
        return repository.allMatches(Query.named(ApplicationUser.class, NamedQueryNames.USER_FIND)
                .withParameter("regex", regex))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    // -- allUsers

    @Override
    public Collection<ApplicationUser> findByAtPath(final String atPath) {
        return repository.allMatches(Query.named(ApplicationUser.class, NamedQueryNames.USER_BY_ATPATH)
                .withParameter("atPath", atPath))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }
    
    @Override
    public Collection<ApplicationUser> findByRole(
            org.apache.isis.extensions.secman.api.role.ApplicationRole genericRole) {
        
        val role = _Casts.<ApplicationRole>uncheckedCast(genericRole);
        return _NullSafe.stream(role.getUsers())
                .collect(_Sets.toUnmodifiableSorted());
    }
    
    @Override
    public Collection<ApplicationUser> findByTenancy(
            @NonNull final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy genericTenancy) {
        return findByAtPath(genericTenancy.getPath()) 
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

    @Override
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
            @NonNull String username, 
            @Nullable AccountType accountType,
            Consumer<ApplicationUser> beforePersist) {
        
        val user = newApplicationUser();
        user.setUsername(username);
        user.setAccountType(accountType);
        beforePersist.accept(user);
        if(user.getAccountType().equals(AccountType.LOCAL)) {
        	// keep null when is set for status in accept() call above
        } else {
			user.setStatus(configBean.isAutoEnableIfDelegatedAndAuthenticated() 
			        ?  ApplicationUserStatus.ENABLED 
	                :  ApplicationUserStatus.DISABLED);
        }
        repository.persistAndFlush(user);
        eventBusService.post(UserCreatedEvent.of(user));
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
                /*sonar-ignore-on*/
                && passwordEncryptionService!=null // if for any reason injection fails
                /*sonar-ignore-off*/
                && passwordEncryptionService.isPresent();
    }

    
}
