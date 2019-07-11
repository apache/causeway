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
package org.apache.isis.extensions.secman.jdo.dom.tenancy;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;

import com.google.common.collect.Lists;

@DomainService(
        nature = NatureOfService.DOMAIN,
        repositoryFor = ApplicationTenancy.class
)
public class ApplicationTenancyRepository {

    // -- findByNameOrPathMatching

    @Programmatic
    public List<ApplicationTenancy> findByNameOrPathMatchingCached(final String search) {
        return queryResultsCache.execute(new Callable<List<ApplicationTenancy>>() {
            @Override public List<ApplicationTenancy> call() throws Exception {
                return findByNameOrPathMatching(search);
            }
        }, ApplicationTenancyRepository.class, "findByNameOrPathMatchingCached", search);
    }

    @Programmatic
    public List<ApplicationTenancy> findByNameOrPathMatching(final String search) {
        if (search == null) {
            return Lists.newArrayList();
        }
        return repository.allMatches(new QueryDefault<>(ApplicationTenancy.class, "findByNameOrPathMatching", "regex", String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."))));
    }

    

    // -- findByName
    @Programmatic
    public ApplicationTenancy findByNameCached(final String name) {
        return queryResultsCache.execute(new Callable<ApplicationTenancy>() {
            @Override
            public ApplicationTenancy call() throws Exception {
                return findByName(name);
            }
        }, ApplicationTenancyRepository.class, "findByNameCached", name);
    }

    @Programmatic
    public ApplicationTenancy findByName(final String name) {
        return repository.uniqueMatch(new QueryDefault<>(ApplicationTenancy.class, "findByName", "name", name));
    }
    

    // -- findByPath

    @Programmatic
    public ApplicationTenancy findByPathCached(final String path) {
        return queryResultsCache.execute(new Callable<ApplicationTenancy>() {
            @Override
            public ApplicationTenancy call() throws Exception {
                return findByPath(path);
            }
        }, ApplicationTenancyRepository.class, "findByPathCached", path);
    }

    @Programmatic
    public ApplicationTenancy findByPath(final String path) {
        if (path == null) {
            return null;
        }
        return repository.uniqueMatch(new QueryDefault<>(ApplicationTenancy.class, "findByPath", "path", path));
    }
    

    // -- autoComplete

    @Action(semantics = SemanticsOf.SAFE)
    public List<ApplicationTenancy> findMatching(final String search) {
        if (search != null && search.length() > 0) {
            return findByNameOrPathMatching(search);
        }
        return Lists.newArrayList();
    }
    

    // -- newTenancy

    @Programmatic
    public ApplicationTenancy newTenancy(
            final String name,
            final String path,
            final ApplicationTenancy parent) {
        ApplicationTenancy tenancy = findByPath(path);
        if (tenancy == null) {
            tenancy = getApplicationTenancyFactory().newApplicationTenancy();
            tenancy.setName(name);
            tenancy.setPath(path);
            tenancy.setParent(parent);
            repository.persist(tenancy);
        }
        return tenancy;
    }

    

    // -- allTenancies
    @Programmatic
    public List<ApplicationTenancy> allTenancies() {
        return queryResultsCache.execute(new Callable<List<ApplicationTenancy>>() {
            @Override
            public List<ApplicationTenancy> call() throws Exception {
                return allTenanciesNoCache();
            }
        }, ApplicationTenancyRepository.class, "allTenancies");
    }

    @Programmatic
    public List<ApplicationTenancy> allTenanciesNoCache() {
        return repository.allInstances(ApplicationTenancy.class);
    }

    

    // -- injected
    /**
     * Will only be injected to if the programmer has supplied an implementation.  Otherwise
     * this class will install a default implementation in the {@link #getApplicationTenancyFactory() accessor}.
     */
    @Inject
    ApplicationTenancyFactory applicationTenancyFactory;

    private ApplicationTenancyFactory getApplicationTenancyFactory() {
        return applicationTenancyFactory != null
                ? applicationTenancyFactory
                : (applicationTenancyFactory = new ApplicationTenancyFactory.Default(factory));
    }

    @Inject
    RepositoryService repository;

    @Inject
    FactoryService factory;
    
    @Inject
    QueryResultsCache queryResultsCache;

    

}
