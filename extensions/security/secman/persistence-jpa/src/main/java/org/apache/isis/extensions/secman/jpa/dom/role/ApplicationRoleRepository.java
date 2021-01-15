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
package org.apache.isis.extensions.secman.jpa.dom.role;

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
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUser;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_delete;

import lombok.val;

@Repository
@Named("isisExtSecman.applicationRoleRepository")
public class ApplicationRoleRepository 
implements org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository<ApplicationRole> {

    @Inject private FactoryService factoryService;
    @Inject private RepositoryService repository;
    @Inject private SecurityModuleConfig configBean;
    
    @Inject private javax.inject.Provider<QueryResultsCache> queryResultsCacheProvider;

    
    @Override
    public ApplicationRole newApplicationRole() {
        return factoryService.detachedEntity(new ApplicationRole());
    }
    
    @Override
    public Optional<ApplicationRole> findByNameCached(final String name) {
        return queryResultsCacheProvider.get().execute(()->findByName(name),
                ApplicationRoleRepository.class, "findByNameCached", name);
    }

    @Override
    public Optional<ApplicationRole> findByName(final String name) {
        if(name == null) {
            return Optional.empty();
        }
        return repository.uniqueMatch(Query.named(ApplicationRole.class, "findByName")
                .withParameter("name", name));
    }

    @Override
    public Collection<ApplicationRole> findNameContaining(final String search) {
        
        if(search != null && search.length() > 0) {
            String nameRegex = String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."));
            return repository.allMatches(
                    Query.named(ApplicationRole.class, "findByNameContaining")
                    .withParameter("nameRegex", nameRegex))
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
        return repository.allInstances(ApplicationRole.class)
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override
    public Collection<ApplicationRole> findMatching(String search) {
        if (search != null && search.length() > 0 ) {
            return findNameContaining(search);
        }
        return Collections.emptySortedSet();
    }

    @Override
    public void addRoleToUser(
            org.apache.isis.extensions.secman.api.role.ApplicationRole genericRole, 
            org.apache.isis.extensions.secman.api.user.ApplicationUser genericUser) {
        
        val role = _Casts.<ApplicationRole>uncheckedCast(genericRole);
        val user = _Casts.<ApplicationUser>uncheckedCast(genericUser);
        // no need to add to users set, since will be done by JDO/DN.
        user.getRoles().add(role);
    }
    
    @Override
    public void removeRoleFromUser(
            org.apache.isis.extensions.secman.api.role.ApplicationRole genericRole,
            org.apache.isis.extensions.secman.api.user.ApplicationUser genericUser) {
        
        val role = _Casts.<ApplicationRole>uncheckedCast(genericRole);
        val user = _Casts.<ApplicationUser>uncheckedCast(genericUser);
        // no need to remove from users set, since will be done by JDO/DN.
        user.getRoles().remove(role);
    }

    @Override
    public boolean isAdminRole(org.apache.isis.extensions.secman.api.role.ApplicationRole genericRole) {
        final ApplicationRole adminRole = findByNameCached(configBean.getAdminRoleName()).orElse(null);
        return Objects.equals(adminRole, genericRole);
    }

    @Override
    public void deleteRole(org.apache.isis.extensions.secman.api.role.ApplicationRole genericRole) {
        
        val role = _Casts.<ApplicationRole>uncheckedCast(genericRole);
        
        role.getUsers().clear();
        val permissions = role.getPermissions();
        for (val permission : permissions) {
            val deleteMixin = factoryService.mixin(ApplicationPermission_delete.class, permission);
            deleteMixin.act();
        }
        repository.removeAndFlush(role);
    }

    @Override
    public Collection<ApplicationRole> getRoles(
            org.apache.isis.extensions.secman.api.user.ApplicationUser genericUser) {
        val user = _Casts.<ApplicationUser>uncheckedCast(genericUser);
        return user.getRoles();
    }

}
