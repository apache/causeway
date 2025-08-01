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
package org.apache.causeway.extensions.secman.applib.role.dom;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.springframework.stereotype.Repository;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.permission.dom.mixins.ApplicationPermission_delete;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.util.RegexReplacer;

/**
 *
 * @since 2.0 {@index}
 */
@Repository
@Named(CausewayModuleExtSecmanApplib.NAMESPACE + ".ApplicationRoleRepository")
public abstract class ApplicationRoleRepositoryAbstract<R extends ApplicationRole>
implements ApplicationRoleRepository {

    @Inject private FactoryService factoryService;
    @Inject private RepositoryService repository;
    @Inject private CausewayConfiguration config;
    @Inject private RegexReplacer regexReplacer;
    @Inject private Provider<QueryResultsCache> queryResultsCacheProvider;

    private final Class<R> applicationRoleClass;

    protected ApplicationRoleRepositoryAbstract(final Class<R> applicationRoleClass) {
        this.applicationRoleClass = applicationRoleClass;
    }

    @Override
    public ApplicationRole newApplicationRole() {
        return factoryService.detachedEntity(applicationRoleClass);
    }

    @Override
    public Optional<ApplicationRole> findByNameCached(final String name) {
        return queryResultsCacheProvider.get().execute(()->findByName(name),
                ApplicationRoleRepositoryAbstract.class, "findByNameCached", name);
    }

    @Override
    public ApplicationRole upsert(final String name, final String roleDescription) {
        return findByName(name)
                .orElseGet(() -> newRole(name, roleDescription));
    }

    @Override
    public Optional<ApplicationRole> findByName(final String name) {
        if(name == null) {
            return Optional.empty();
        }
        return _Casts.uncheckedCast(
                repository.uniqueMatch(Query.named(applicationRoleClass, ApplicationRole.Nq.FIND_BY_NAME)
                .withParameter("name", name))
        );
    }

    @Override
    public Collection<ApplicationRole> findNameContaining(final String search) {

        if(search != null && search.length() > 0) {
            var nameRegex = regexReplacer.asRegex(search);
            return repository.allMatches(
                    Query.named(applicationRoleClass, ApplicationRole.Nq.FIND_BY_NAME_CONTAINING)
                    .withParameter("regex", nameRegex))
                    .stream()
                    .collect(_Sets.toUnmodifiableSorted());
        }
        return Collections.emptySortedSet();
    }

    @Override
    public ApplicationRole newRole(
            final String name,
            final String description) {
        ApplicationRole role = findByName(name).orElse(null);
        if (role == null){
            role = newApplicationRole();
            role.setName(name);
            role.setDescription(description);
            repository.persist(role);
        }
        return role;
    }

    @Override
    public Collection<ApplicationRole> allRoles() {
        return repository.allInstances(applicationRoleClass)
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override
    public Collection<ApplicationRole> findMatching(final String search) {
        if (search != null && search.length() > 0 ) {
            return findNameContaining(search);
        }
        return Collections.emptySortedSet();
    }

    @Override
    public void addRoleToUser(
            final ApplicationRole role,
            final ApplicationUser user) {

        user.getRoles().add(role);
        role.getUsers().add(user);

        repository.persistAndFlush(user, role);
    }

    @Override
    public void removeRoleFromUser(
            final ApplicationRole role,
            final ApplicationUser user) {

        user.getRoles().remove(role);
        role.getUsers().remove(user);

        repository.persistAndFlush(user, role);
    }

    @Override
    public boolean isAdminRole(final ApplicationRole genericRole) {
        var adminRoleName = config.extensions().secman().seed().admin().roleName();
        final ApplicationRole adminRole = findByNameCached(adminRoleName).orElse(null);
        return Objects.equals(adminRole, genericRole);
    }

    @Override
    public void deleteRole(final ApplicationRole role) {

        role.getUsers().clear();
        var permissions = role.getPermissions();
        for (var permission : permissions) {
            var deleteMixin = factoryService.mixin(ApplicationPermission_delete.class, permission);
            deleteMixin.act();
        }
        repository.removeAndFlush(role);
    }

    @Override
    public Collection<ApplicationRole> getRoles(
            final ApplicationUser user) {
        return user.getRoles();
    }

}
