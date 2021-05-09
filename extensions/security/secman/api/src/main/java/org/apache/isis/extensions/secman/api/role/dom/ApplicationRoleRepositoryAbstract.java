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
package org.apache.isis.extensions.secman.api.role.dom;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Repository;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.permission.dom.mixins.ApplicationPermission_delete;

import lombok.val;

@Repository
@Named("isis.ext.secman.ApplicationRoleRepository")
public abstract class ApplicationRoleRepositoryAbstract<R extends org.apache.isis.extensions.secman.api.role.dom.ApplicationRole>
implements org.apache.isis.extensions.secman.api.role.dom.ApplicationRoleRepository<org.apache.isis.extensions.secman.api.role.dom.ApplicationRole> {

    @Inject private FactoryService factoryService;
    @Inject private RepositoryService repository;
    @Inject private SecmanConfiguration configBean;

    @Inject private javax.inject.Provider<QueryResultsCache> queryResultsCacheProvider;

    private final Class<R> applicationRoleClass;

    protected ApplicationRoleRepositoryAbstract(Class<R> applicationRoleClass) {
        this.applicationRoleClass = applicationRoleClass;
    }

    @Override
    public org.apache.isis.extensions.secman.api.role.dom.ApplicationRole newApplicationRole() {
        return factoryService.detachedEntity(applicationRoleClass);
    }

    @Override
    public Optional<org.apache.isis.extensions.secman.api.role.dom.ApplicationRole> findByNameCached(final String name) {
        return queryResultsCacheProvider.get().execute(()->findByName(name),
                ApplicationRoleRepositoryAbstract.class, "findByNameCached", name);
    }

    @Override
    public org.apache.isis.extensions.secman.api.role.dom.ApplicationRole upsert(final String name, final String roleDescription) {
        return findByName(name)
                .orElseGet(() -> newRole(name, roleDescription));
    }

    @Override
    public Optional<org.apache.isis.extensions.secman.api.role.dom.ApplicationRole> findByName(final String name) {
        if(name == null) {
            return Optional.empty();
        }
        return _Casts.uncheckedCast(
                repository.uniqueMatch(Query.named(applicationRoleClass, org.apache.isis.extensions.secman.api.role.dom.ApplicationRole.NAMED_QUERY_FIND_BY_NAME)
                .withParameter("name", name))
        );
    }

    @Override
    public Collection<org.apache.isis.extensions.secman.api.role.dom.ApplicationRole> findNameContaining(final String search) {

        if(search != null && search.length() > 0) {
            String nameRegex = String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."));
            return repository.allMatches(
                    Query.named(applicationRoleClass, org.apache.isis.extensions.secman.api.role.dom.ApplicationRole.NAMED_QUERY_FIND_BY_NAME_CONTAINING)
                    .withParameter("nameRegex", nameRegex))
                    .stream()
                    .collect(_Sets.toUnmodifiableSorted());
        }
        return Collections.emptySortedSet();
    }

    @Override
    public org.apache.isis.extensions.secman.api.role.dom.ApplicationRole newRole(
            final String name,
            final String description) {
        org.apache.isis.extensions.secman.api.role.dom.ApplicationRole role = findByName(name).orElse(null);
        if (role == null){
            role = newApplicationRole();
            role.setName(name);
            role.setDescription(description);
            repository.persist(role);
        }
        return role;
    }

    @Override
    public Collection<org.apache.isis.extensions.secman.api.role.dom.ApplicationRole> allRoles() {
        return repository.allInstances(applicationRoleClass)
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override
    public Collection<org.apache.isis.extensions.secman.api.role.dom.ApplicationRole> findMatching(String search) {
        if (search != null && search.length() > 0 ) {
            return findNameContaining(search);
        }
        return Collections.emptySortedSet();
    }

    @Override
    public void addRoleToUser(
            org.apache.isis.extensions.secman.api.role.dom.ApplicationRole role,
            org.apache.isis.extensions.secman.api.user.dom.ApplicationUser user) {

        user.getRoles().add(role);
        role.getUsers().add(user);

        repository.persistAndFlush(user);
    }

    @Override
    public void removeRoleFromUser(
            org.apache.isis.extensions.secman.api.role.dom.ApplicationRole role,
            org.apache.isis.extensions.secman.api.user.dom.ApplicationUser user) {

        user.getRoles().remove(role);
        role.getUsers().remove(user);

        repository.persistAndFlush(user);
    }

    @Override
    public boolean isAdminRole(org.apache.isis.extensions.secman.api.role.dom.ApplicationRole genericRole) {
        final org.apache.isis.extensions.secman.api.role.dom.ApplicationRole adminRole = findByNameCached(configBean.getAdminRoleName()).orElse(null);
        return Objects.equals(adminRole, genericRole);
    }

    @Override
    public void deleteRole(org.apache.isis.extensions.secman.api.role.dom.ApplicationRole role) {

        role.getUsers().clear();
        val permissions = role.getPermissions();
        for (val permission : permissions) {
            val deleteMixin = factoryService.mixin(ApplicationPermission_delete.class, permission);
            deleteMixin.act();
        }
        repository.removeAndFlush(role);
    }

    @Override
    public Collection<org.apache.isis.extensions.secman.api.role.dom.ApplicationRole> getRoles(
            org.apache.isis.extensions.secman.api.user.dom.ApplicationUser user) {
        return user.getRoles();
    }

}
