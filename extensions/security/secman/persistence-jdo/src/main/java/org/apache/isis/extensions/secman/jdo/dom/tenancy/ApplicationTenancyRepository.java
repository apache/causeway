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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Repository;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser;

import lombok.NonNull;
import lombok.val;

@Repository
@Named("isisExtSecman.applicationTenancyRepository")
public class ApplicationTenancyRepository 
implements org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancyRepository<ApplicationTenancy> {

    @Inject private FactoryService factory;
    @Inject private RepositoryService repository;
    
    @Inject private javax.inject.Provider<QueryResultsCache> queryResultsCacheProvider;
    
    @Override
    public ApplicationTenancy newApplicationTenancy() {
        return factory.detachedEntity(new ApplicationTenancy());
    }
    
    // -- findByNameOrPathMatching

    @Override
    public Collection<ApplicationTenancy> findByNameOrPathMatchingCached(final String search) {
        return queryResultsCacheProvider.get().execute(new Callable<Collection<ApplicationTenancy>>() {
            @Override public Collection<ApplicationTenancy> call() throws Exception {
                return findByNameOrPathMatching(search);
            }
        }, ApplicationTenancyRepository.class, "findByNameOrPathMatchingCached", search);
    }

    public Collection<ApplicationTenancy> findByNameOrPathMatching(final String search) {
        if (search == null) {
            return Collections.emptySortedSet();
        }
        return repository.allMatches(Query.named(ApplicationTenancy.class, "findByNameOrPathMatching")
                .withParameter("regex", String.format("(?i).*%s.*", search.replace("*", ".*").replace("?", "."))))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    // -- findByName

    public ApplicationTenancy findByNameCached(final String name) {
        return queryResultsCacheProvider.get().execute(new Callable<ApplicationTenancy>() {
            @Override
            public ApplicationTenancy call() throws Exception {
                return findByName(name);
            }
        }, ApplicationTenancyRepository.class, "findByNameCached", name);
    }

    public ApplicationTenancy findByName(final String name) {
        return repository.uniqueMatch(Query.named(ApplicationTenancy.class, "findByName")
                .withParameter("name", name)).orElse(null);
    }


    // -- findByPath

    public ApplicationTenancy findByPathCached(final String path) {
        return queryResultsCacheProvider.get().execute(new Callable<ApplicationTenancy>() {
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
        return repository.uniqueMatch(Query.named(ApplicationTenancy.class, "findByPath")
                .withParameter("path", path))
                .orElse(null);
    }


    // -- autoComplete
    @Override
    public Collection<ApplicationTenancy> findMatching(final String search) {
        if (search != null && search.length() > 0) {
            return findByNameOrPathMatching(search);
        }
        return Collections.emptySortedSet();
    }

    // -- newTenancy

    @Override
    public ApplicationTenancy newTenancy(
            final String name,
            final String path,
            final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy parent) {
        ApplicationTenancy tenancy = findByPath(path);
        if (tenancy == null) {
            tenancy = newApplicationTenancy();
            tenancy.setName(name);
            tenancy.setPath(path);
            tenancy.setParent((ApplicationTenancy) parent);
            repository.persist(tenancy);
        }
        return tenancy;
    }

    // -- 

    @Override
    public Collection<ApplicationTenancy> allTenancies() {
        return queryResultsCacheProvider.get().execute(new Callable<Collection<ApplicationTenancy>>() {
            @Override
            public Collection<ApplicationTenancy> call() throws Exception {
                return allTenanciesNoCache();
            }
        }, ApplicationTenancyRepository.class, "allTenancies");
    }

    public Collection<ApplicationTenancy> allTenanciesNoCache() {
        return repository.allInstances(ApplicationTenancy.class)
                .stream()
                .map(ApplicationTenancy.class::cast)
                .collect(_Sets.toUnmodifiableSorted());
    }
    
    @Override
    public void setTenancyOnUser(
            @NonNull final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy genericTenancy, 
            @NonNull final org.apache.isis.extensions.secman.api.user.ApplicationUser genericUser) {
        val tenancy = _Casts.<ApplicationTenancy>uncheckedCast(genericTenancy);
        val user = _Casts.<ApplicationUser>uncheckedCast(genericUser);
        // no need to add to users set, since will be done by JDO/DN.
        user.setAtPath(tenancy.getPath());
    }
    
    @Override
    public void clearTenancyOnUser(
            @NonNull final org.apache.isis.extensions.secman.api.user.ApplicationUser genericUser) {
        val user = _Casts.<ApplicationUser>uncheckedCast(genericUser);
        // no need to remove from users set, since will be done by JDO/DN.
        user.setAtPath(null);
    }

    @Override
    public void setParentOnTenancy(
            @NonNull final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy genericTenancy,
            @NonNull final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy genericParent) {
        val tenancy = _Casts.<ApplicationTenancy>uncheckedCast(genericTenancy);
        val parent = _Casts.<ApplicationTenancy>uncheckedCast(genericParent);
        // no need to add to children set, since will be done by JDO/DN.
        tenancy.setParent(parent);
    }

    @Override
    public void clearParentOnTenancy(
            @NonNull final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy genericTenancy) {
        val tenancy = _Casts.<ApplicationTenancy>uncheckedCast(genericTenancy);
        // no need to remove from children set, since will be done by JDO/DN.
        tenancy.setParent(null);
    }

    @Override
    public Collection<ApplicationTenancy> getChildren(
            @NonNull final org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy genericTenancy) {
        val tenancy = _Casts.<ApplicationTenancy>uncheckedCast(genericTenancy);
        return tenancy.getChildren()
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

}
