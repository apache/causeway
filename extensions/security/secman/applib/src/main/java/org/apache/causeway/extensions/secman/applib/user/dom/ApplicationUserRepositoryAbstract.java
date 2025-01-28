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
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.eventbus.EventBusService;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy;
import org.apache.causeway.extensions.secman.applib.user.dom.mixins.ApplicationUser_lock;
import org.apache.causeway.extensions.secman.applib.user.dom.mixins.ApplicationUser_unlock;
import org.apache.causeway.extensions.secman.applib.user.events.UserCreatedEvent;
import org.apache.causeway.extensions.secman.applib.util.RegexReplacer;

import org.jspecify.annotations.NonNull;

/**
 *
 * @since 2.0 {@index}
 */
public class ApplicationUserRepositoryAbstract<U extends ApplicationUser>
implements ApplicationUserRepository {

    @Inject private FactoryService factoryService;
    @Inject private RepositoryService repository;
	@Inject protected CausewayConfiguration config;
    @Inject private EventBusService eventBusService;
    @Inject private RegexReplacer regexReplacer;
    @Inject private Provider<QueryResultsCache> queryResultsCacheProvider;

    // empty if no candidate is available
    @Autowired(required = false) @Qualifier("Secman") PasswordEncoder passwordEncoder;

    private final Class<U> applicationUserClass;

    protected ApplicationUserRepositoryAbstract(final Class<U> applicationUserClass) {
        this.applicationUserClass = applicationUserClass;
    }

    @Override
    public ApplicationUser newApplicationUser() {
        return factoryService.detachedEntity(this.applicationUserClass);
    }

    // -- findOrCreateUserByUsername (programmatic)

    /**
     * Uses the {@link QueryResultsCache} in order to support
     * multiple lookups from <code>org.apache.causeway.extensions.secman.jdo.app.user.UserPermissionViewModel</code>.
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
            ApplicationUserRepositoryAbstract.class, "findOrCreateUserByUsername", username);
    }

    // -- findByUsername

    public Optional<ApplicationUser> findByUsernameCached(final String username) {
        return queryResultsCacheProvider.get().execute(this::findByUsername,
                ApplicationUserRepositoryAbstract.class, "findByUsernameCached", username);
    }

    @Override
    public Optional<ApplicationUser> findByUsername(final String username) {
        return _Casts.uncheckedCast(
                repository.uniqueMatch(Query.named(this.applicationUserClass, ApplicationUser.Nq.FIND_BY_USERNAME)
                .withParameter("username", username))
        );
    }

    // -- findByEmailAddress (programmatic)

    public Optional<ApplicationUser> findByEmailAddressCached(final String emailAddress) {
        return queryResultsCacheProvider.get().execute(this::findByEmailAddress,
                ApplicationUserRepositoryAbstract.class, "findByEmailAddressCached", emailAddress);
    }

    @Override
    public Optional<ApplicationUser> findByEmailAddress(final String emailAddress) {
        return _Casts.uncheckedCast(
                repository.uniqueMatch(Query.named(this.applicationUserClass, ApplicationUser.Nq.FIND_BY_EMAIL_ADDRESS)
                .withParameter("emailAddress", emailAddress))
        );
    }

    // -- findByName

    @Override
    public Collection<ApplicationUser> find(final @Nullable String _search) {
        var regex = regexReplacer.asRegex(_search);
        return repository.allMatches(Query.named(this.applicationUserClass, ApplicationUser.Nq.FIND)
                .withParameter("regex", regex))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    // -- allUsers

    @Override
    public Collection<ApplicationUser> findByAtPath(final String atPath) {
        return repository.allMatches(Query.named(this.applicationUserClass, ApplicationUser.Nq.FIND_BY_ATPATH)
                .withParameter("atPath", atPath))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override
    public Collection<ApplicationUser> findByRole(final ApplicationRole role) {

        return _NullSafe.stream(role.getUsers())
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override
    public Collection<ApplicationUser> findByTenancy(
            final @NonNull ApplicationTenancy genericTenancy) {
        return findByAtPath(genericTenancy.getPath())
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    // -- allUsers

    @Override
    public Collection<ApplicationUser> allUsers() {
        return repository.allInstances(this.applicationUserClass)
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
    public void enable(final ApplicationUser user) {
        if(ApplicationUserStatus.canUnlock(user.getStatus())) {
             factoryService.mixin(ApplicationUser_unlock.class, user)
             .act();
        }
    }

    @Override
    public void disable(final ApplicationUser user) {
        if(ApplicationUserStatus.canLock(user.getStatus())) {
            factoryService.mixin(ApplicationUser_lock.class, user)
            .act();
        }
    }

    @Override
    public boolean isAdminUser(final ApplicationUser user) {
        return Objects.equals(
                config.getExtensions().getSecman().getSeed().getAdmin().getUserName(),
                user.getName());
    }

    @Override
    public ApplicationUser newUser(
            final @NonNull String username,
            @Nullable final AccountType accountType,
            final Consumer<ApplicationUser> beforePersist) {

        var user = newApplicationUser();
        user.setUsername(username);
        user.setAccountType(accountType);
        beforePersist.accept(user);
        if(user.getAccountType().equals(AccountType.LOCAL)) {
        	// keep null when is set for status in accept() call above
        } else {
			user.setStatus(ApplicationUserStatus.LOCKED);
        }
        repository.persistAndFlush(user);
        eventBusService.post(UserCreatedEvent.of(user));
        return user;
    }

    @Override
    public boolean updatePassword(
            final ApplicationUser user,
            final String password) {
        // in case called programmatically
        if(!isPasswordFeatureEnabled(user)) {
            return false;
        }
        user.setEncryptedPassword(passwordEncoder.encode(password));
        repository.persistAndFlush(user);
        return true;
    }

    @Override
    public boolean isPasswordFeatureEnabled(final ApplicationUser user) {
        return user.isLocalAccount()
                && passwordEncoder != null;
    }

}
