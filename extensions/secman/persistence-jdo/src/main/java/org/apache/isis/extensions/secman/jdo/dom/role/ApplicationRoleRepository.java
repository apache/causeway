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

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;

@DomainService(
        nature = NatureOfService.DOMAIN,
        repositoryFor = ApplicationRole.class
)
public class ApplicationRoleRepository  {
    
    @Inject RepositoryService repository;
    @Inject FactoryService factory;
    @Inject QueryResultsCache queryResultsCache;
    
    /**
     * Will only be injected to if the programmer has supplied an implementation.  Otherwise
     * this class will install a default implementation in {@link #getApplicationRoleFactory()}.
     */
    @Inject ApplicationRoleFactory applicationRoleFactory;

    private ApplicationRoleFactory getApplicationRoleFactory() {
        return applicationRoleFactory != null
                ? applicationRoleFactory
                : (applicationRoleFactory = new ApplicationRoleFactory.Default(factory));
    }


    @Programmatic
    public ApplicationRole findByNameCached(final String name) {
        return queryResultsCache.execute(()->findByName(name),
                ApplicationRoleRepository.class, "findByNameCached", name);
    }

    @Programmatic
    public ApplicationRole findByName(final String name) {
        if(name == null) {
            return null;
        }
        return repository.uniqueMatch(new QueryDefault<>(ApplicationRole.class, "findByName", "name", name));
    }

    @Programmatic
    public List<ApplicationRole> findNameContaining(final String search) {
        if(search != null && search.length() > 0) {
            String nameRegex = String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."));
            return repository.allMatches(new QueryDefault<>(ApplicationRole.class, "findByNameContaining", "nameRegex", nameRegex));
        }
        return Lists.newArrayList();
    }

    @Programmatic
    public ApplicationRole newRole(
            final String name,
            final String description) {
        ApplicationRole role = findByName(name);
        if (role == null){
            role = getApplicationRoleFactory().newApplicationRole();
            role.setName(name);
            role.setDescription(description);
            repository.persist(role);
        }
        return role;
    }

    @Programmatic
    public List<ApplicationRole> allRoles() {
        return repository.allInstances(ApplicationRole.class);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public List<ApplicationRole> findMatching(String search) {
        if (search != null && search.length() > 0 ) {
            return findNameContaining(search);
        }
        return Lists.newArrayList();
    }

}
