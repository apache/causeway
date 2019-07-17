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

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Password;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.encryption.PasswordEncryptionService;
import org.apache.isis.extensions.secman.api.user.AccountType;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRoleRepository;

@DomainService(
        nature = NatureOfService.DOMAIN,
        repositoryFor = ApplicationUser.class
)
public class ApplicationUserRepository
implements org.apache.isis.extensions.secman.api.user.ApplicationUserRepository {

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
                return newDelegateUser(username, null, null);
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
                "findByUsername", "username", username));
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
                "findByEmailAddress", "emailAddress", emailAddress));
    }

    // -- findByName

    @Override
	public List<ApplicationUser> find(final String search) {
        final String regex = String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."));
        return repository.allMatches(new QueryDefault<>(
                ApplicationUser.class,
                "find", "regex", regex));
    }

    // -- newDelegateUser (action)

    @Override
    public ApplicationUser newDelegateUser(
            final String username,
            final org.apache.isis.extensions.secman.api.role.ApplicationRole initialRole,
            final Boolean enabled) {
        final ApplicationUser user = applicationUserFactory.newApplicationUser();
        user.setUsername(username);
        user.setStatus(ApplicationUserStatus.parse(enabled));
        user.setAccountType(AccountType.DELEGATED);
        if (initialRole != null) {
            user.addRole((ApplicationRole)initialRole);
        }
        repository.persist(user);
        return user;
    }

    // -- newLocalUser (action)

    @Override
	public ApplicationUser newLocalUser(
            final String username,
            final Password password,
            final Password passwordRepeat,
            final org.apache.isis.extensions.secman.api.role.ApplicationRole initialRole,
            final Boolean enabled,
            final String emailAddress) {
        ApplicationUser user = findByUsername(username);
        if (user == null) {
            user = applicationUserFactory.newApplicationUser();
            user.setUsername(username);
            user.setStatus(ApplicationUserStatus.parse(enabled));
            user.setAccountType(AccountType.LOCAL);
        }
        if (initialRole != null) {
            user.addRole((ApplicationRole)initialRole);
        }
        if (password != null) {
            user.updatePassword(password.getPassword());
        }
        if (emailAddress != null) {
            user.updateEmailAddress(emailAddress);
        }
        repository.persist(user);
        return user;
    }

    @Override
    public String validateNewLocalUser(
            final String username,
            final Password password,
            final Password passwordRepeat,
            final org.apache.isis.extensions.secman.api.role.ApplicationRole initialRole,
            final Boolean enabled,
            final String emailAddress) {
        final ApplicationUser user = applicationUserFactory.newApplicationUser();
        return user.validateResetPassword(password, passwordRepeat);
    }

    // -- newLocalUserBasedOn (action)

    public ApplicationUser newLocalUserBasedOn(
            final String username,
            final Password password,
            final Password passwordRepeat,
            final ApplicationUser userWhosRolesShouldBeCloned,
            final Boolean enabled,
            final String emailAddress) {
        final ApplicationUser user = this.newLocalUser(username, password, passwordRepeat, null, enabled, emailAddress);
        for (ApplicationRole role : userWhosRolesShouldBeCloned.getRoles()) {
            user.addToRoles(role);
        }
        return user;
    }

    // -- allUsers

    public List<ApplicationUser> findByAtPath(final String atPath) {
        return repository.allMatches(new QueryDefault<>(
                ApplicationUser.class,
                "findByAtPath", "atPath", atPath));
    }

    // -- allUsers

    @Override
	public List<ApplicationUser> allUsers() {
        return repository.allInstances(ApplicationUser.class);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public List<ApplicationUser> findMatching(final String search) {
        if (search != null && search.length() > 0) {
            return find(search);
        }
        return _Lists.newArrayList();
    }

    // -- DEPENDENCIES
    
    @Inject QueryResultsCache queryResultsCache;
    @Inject PasswordEncryptionService passwordEncryptionService;
    @Inject ApplicationRoleRepository applicationRoleRepository;
    @Inject ApplicationUserFactory applicationUserFactory;
    @Inject RepositoryService repository;
    @Inject FactoryService factory;

    

}
