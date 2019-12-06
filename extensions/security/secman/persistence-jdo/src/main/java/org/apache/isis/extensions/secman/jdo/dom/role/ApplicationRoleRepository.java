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

import lombok.extern.log4j.Log4j2;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.collections._Lists;
import org.springframework.stereotype.Repository;

@Repository
@Named("isisExtSecman.applicationRoleRepository")
@Log4j2
public class ApplicationRoleRepository 
implements org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository {

    @Inject RepositoryService repository;
    @Inject FactoryService factory;
    @Inject QueryResultsCache queryResultsCache;
    @Inject ApplicationRoleFactory applicationRoleFactory;

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
    public List<ApplicationRole> findNameContaining(final String search) {
        if(search != null && search.length() > 0) {
            String nameRegex = String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."));
            return repository.allMatches(new QueryDefault<>(ApplicationRole.class, "findByNameContaining", "nameRegex", nameRegex));
        }
        return _Lists.newArrayList();
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
    public List<ApplicationRole> allRoles() {
        return repository.allInstances(ApplicationRole.class);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public List<ApplicationRole> findMatching(String search) {
        if (search != null && search.length() > 0 ) {
            return findNameContaining(search);
        }
        return _Lists.newArrayList();
    }

}
