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
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.collections._Lists;

@DomainService(
        nature = NatureOfService.DOMAIN,
        repositoryFor = ApplicationTenancy.class
        )
public class ApplicationTenancyRepository 
implements org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancyRepository {

    // -- findByNameOrPathMatching

    @Override
    public List<ApplicationTenancy> findByNameOrPathMatchingCached(final String search) {
        return queryResultsCache.execute(new Callable<List<ApplicationTenancy>>() {
            @Override public List<ApplicationTenancy> call() throws Exception {
                return findByNameOrPathMatching(search);
            }
        }, ApplicationTenancyRepository.class, "findByNameOrPathMatchingCached", search);
    }

    public List<ApplicationTenancy> findByNameOrPathMatching(final String search) {
        if (search == null) {
            return _Lists.newArrayList();
        }
        return repository.allMatches(new QueryDefault<>(ApplicationTenancy.class, "findByNameOrPathMatching", "regex", String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."))));
    }

    // -- findByName

    public ApplicationTenancy findByNameCached(final String name) {
        return queryResultsCache.execute(new Callable<ApplicationTenancy>() {
            @Override
            public ApplicationTenancy call() throws Exception {
                return findByName(name);
            }
        }, ApplicationTenancyRepository.class, "findByNameCached", name);
    }

    public ApplicationTenancy findByName(final String name) {
        return repository.uniqueMatch(new QueryDefault<>(ApplicationTenancy.class, "findByName", "name", name));
    }


    // -- findByPath

    public ApplicationTenancy findByPathCached(final String path) {
        return queryResultsCache.execute(new Callable<ApplicationTenancy>() {
            @Override
            public ApplicationTenancy call() throws Exception {
                return findByPath(path);
            }
        }, ApplicationTenancyRepository.class, "findByPathCached", path);
    }

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
        return _Lists.newArrayList();
    }


    // -- newTenancy

    @Override
    public ApplicationTenancy newTenancy(
            final String name,
            final String path,
            final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy parent) {
        ApplicationTenancy tenancy = findByPath(path);
        if (tenancy == null) {
            tenancy = applicationTenancyFactory.newApplicationTenancy();
            tenancy.setName(name);
            tenancy.setPath(path);
            tenancy.setParent((ApplicationTenancy) parent);
            repository.persist(tenancy);
        }
        return tenancy;
    }

    // -- allTenancies

    @Override
    public List<ApplicationTenancy> allTenancies() {
        return queryResultsCache.execute(new Callable<List<ApplicationTenancy>>() {
            @Override
            public List<ApplicationTenancy> call() throws Exception {
                return allTenanciesNoCache();
            }
        }, ApplicationTenancyRepository.class, "allTenancies");
    }

    public List<ApplicationTenancy> allTenanciesNoCache() {
        return repository.allInstances(ApplicationTenancy.class);
    }

    // -- DEPENDENCIES

    @Inject ApplicationTenancyFactory applicationTenancyFactory;
    @Inject RepositoryService repository;
    @Inject FactoryService factory;
    @Inject QueryResultsCache queryResultsCache;

}
