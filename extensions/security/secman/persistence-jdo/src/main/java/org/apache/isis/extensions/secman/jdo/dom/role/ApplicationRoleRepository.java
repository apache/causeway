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
package org.apache.isis.extensions.secman.jdo.dom.role;

import java.util.Collection;
import java.util.Collections;

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
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_delete;

import lombok.val;

@Repository
@Named("isisExtSecman.applicationRoleRepository")
public class ApplicationRoleRepository 
implements org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository {

    @Inject private FactoryService factoryService;
    @Inject private RepositoryService repository;
    @Inject private QueryResultsCache queryResultsCache;
    @Inject private ApplicationRoleFactory applicationRoleFactory;
    @Inject private SecurityModuleConfig configBean;

    @Override
    public ApplicationRole findByNameCached(final String name) {
        return queryResultsCache.execute(()->findByName(name),
                ApplicationRoleRepository.class, "findByNameCached", name);
    }

    @Override
    public ApplicationRole findByName(final String name) {
        if(name == null) {
            return null;
        }
        return repository.uniqueMatch(new QueryDefault<>(ApplicationRole.class, "findByName", "name", name)).orElse(null);
    }

    @Override
    public Collection<org.apache.isis.extensions.secman.api.role.ApplicationRole> findNameContaining(final String search) {
        
        if(search != null && search.length() > 0) {
            String nameRegex = String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."));
            return repository.allMatches(
                    new QueryDefault<>(ApplicationRole.class, 
                            "findByNameContaining", "nameRegex", nameRegex))
                    .stream()
                    .map(org.apache.isis.extensions.secman.api.role.ApplicationRole.class::cast)
                    .collect(_Sets.toUnmodifiableSorted());
        }
        return Collections.emptySortedSet();
    }

    @Override
    public ApplicationRole newRole(
            final String name,
            final String description) {
        ApplicationRole role = findByName(name);
        if (role == null){
            role = applicationRoleFactory.newApplicationRole();
            role.setName(name);
            role.setDescription(description);
            repository.persist(role);
        }
        return role;
    }

    @Override
    public Collection<org.apache.isis.extensions.secman.api.role.ApplicationRole> allRoles() {
        return repository.allInstances(ApplicationRole.class)
                .stream()
                .map(org.apache.isis.extensions.secman.api.role.ApplicationRole.class::cast)
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Collection<org.apache.isis.extensions.secman.api.role.ApplicationRole> findMatching(String search) {
        if (search != null && search.length() > 0 ) {
            return findNameContaining(search);
        }
        return Collections.emptySortedSet();
    }
    
    @Override
    public Collection<org.apache.isis.extensions.secman.api.user.ApplicationUser> getUsers(
            org.apache.isis.extensions.secman.api.role.ApplicationRole genericRole) {
        
        val role = _Casts.<ApplicationRole>uncheckedCast(genericRole);
        return role.getUsers()
                .stream()
                .map(org.apache.isis.extensions.secman.api.user.ApplicationUser.class::cast)
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override
    public void addRoleToUser(
            org.apache.isis.extensions.secman.api.role.ApplicationRole genericRole, 
            org.apache.isis.extensions.secman.api.user.ApplicationUser genericUser) {
        
        val role = _Casts.<ApplicationRole>uncheckedCast(genericRole);
        val user = _Casts.<ApplicationUser>uncheckedCast(genericUser);
        // no need to add to users set, since will be done by JDO/DN.
        user.addToRoles(role);
    }
    
    @Override
    public void removeRoleFromUser(
            org.apache.isis.extensions.secman.api.role.ApplicationRole genericRole,
            org.apache.isis.extensions.secman.api.user.ApplicationUser genericUser) {
        
        val role = _Casts.<ApplicationRole>uncheckedCast(genericRole);
        val user = _Casts.<ApplicationUser>uncheckedCast(genericUser);
        // no need to remove from users set, since will be done by JDO/DN.
        user.removeRole(role);
    }

    @Override
    public boolean isAdminRole(org.apache.isis.extensions.secman.api.role.ApplicationRole genericRole) {
        final ApplicationRole adminRole = findByNameCached(configBean.getAdminRoleName());
        return adminRole.equals(genericRole);
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

}
