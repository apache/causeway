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
package org.apache.causeway.extensions.secman.applib.tenancy.dom;

import java.util.Collection;
import java.util.Collections;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.util.RegexReplacer;

import org.jspecify.annotations.NonNull;

/**
 *
 * @since 2.0 {@index}
 */
public abstract class ApplicationTenancyRepositoryAbstract<T extends ApplicationTenancy>
implements ApplicationTenancyRepository {

    @Inject private FactoryService factory;
    @Inject private RepositoryService repository;
    @Inject private RegexReplacer regexReplacer;
    @Inject private Provider<QueryResultsCache> queryResultsCacheProvider;

    private final Class<T> applicationTenancyClass;

    protected ApplicationTenancyRepositoryAbstract(final Class<T> applicationTenancyClass) {
        this.applicationTenancyClass = applicationTenancyClass;
    }

    @Override
    public ApplicationTenancy newApplicationTenancy() {
        return factory.detachedEntity(applicationTenancyClass);
    }

    // -- findByNameOrPathMatching

    @Override
    public Collection<ApplicationTenancy> findByNameOrPathMatchingCached(final String search) {
        return queryResultsCacheProvider.get().execute(
                () -> findByNameOrPathMatching(search),
                ApplicationTenancyRepositoryAbstract.class, "findByNameOrPathMatchingCached",
                search);
    }

    public Collection<ApplicationTenancy> findByNameOrPathMatching(final String search) {
        if (search == null) {
            return Collections.emptySortedSet();
        }
        var regex = regexReplacer.asRegex(search);
        return repository.allMatches(Query.named(this.applicationTenancyClass, ApplicationTenancy.Nq.FIND_BY_NAME_OR_PATH_MATCHING)
                .withParameter("regex", regex))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    // -- findByName

    public ApplicationTenancy findByNameCached(final String name) {
        return queryResultsCacheProvider.get().execute(
                () -> findByName(name),
                ApplicationTenancyRepositoryAbstract.class, "findByNameCached",
                name);
    }

    public ApplicationTenancy findByName(final String name) {
        return repository.uniqueMatch(Query.named(this.applicationTenancyClass, ApplicationTenancy.Nq.FIND_BY_NAME)
                .withParameter("name", name)).orElse(null);
    }

    // -- findByPath

    public ApplicationTenancy findByPathCached(final String path) {
        return queryResultsCacheProvider.get().execute(
                () -> findByPath(path),
                ApplicationTenancyRepositoryAbstract.class, "findByPathCached",
                path);
    }

    @Override
    public ApplicationTenancy findByPath(final String path) {
        if (path == null) {
            return null;
        }
        return repository.uniqueMatch(Query.named(this.applicationTenancyClass, ApplicationTenancy.Nq.FIND_BY_PATH)
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
            final ApplicationTenancy parent) {
        ApplicationTenancy tenancy = findByPath(path);
        if (tenancy == null) {
            tenancy = newApplicationTenancy();
            tenancy.setName(name);
            tenancy.setPath(path);
            tenancy.setParent(parent);
            if(parent != null) {
                parent.getChildren().add(tenancy);
            }
            repository.persistAndFlush(tenancy);
        }
        return tenancy;
    }

    // --

    @Override
    public Collection<ApplicationTenancy> allTenancies() {
        return queryResultsCacheProvider.get().execute(
                () -> allTenanciesNoCache(),
                ApplicationTenancyRepositoryAbstract.class, "allTenancies");
    }

    public Collection<ApplicationTenancy> allTenanciesNoCache() {
        return repository.allInstances(this.applicationTenancyClass)
                .stream()
                .map(this.applicationTenancyClass::cast)
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override
    public Collection<ApplicationTenancy> getRootTenancies() {
        return repository.allInstances(this.applicationTenancyClass)
                .stream()
                .filter(ApplicationTenancy::isRoot)
                .map(this.applicationTenancyClass::cast)
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override
    public Collection<ApplicationTenancy> getChildren(
            final @NonNull ApplicationTenancy tenancy) {
        return tenancy.getChildren()
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override
    public void setTenancyOnUser(
            final @NonNull ApplicationTenancy tenancy,
            final @NonNull ApplicationUser user) {
        // no need to add to users set, since will be done by the ORM.
        user.setAtPath(tenancy.getPath());
    }

    @Override
    public void clearTenancyOnUser(
            final @NonNull ApplicationUser user) {
        // no need to remove from users set, since will be done by the ORM.
        user.setAtPath(null);
    }

    @Override
    public void setParentOnTenancy(
            final @NonNull ApplicationTenancy tenancy,
            final @NonNull ApplicationTenancy parent) {
        tenancy.setParent(parent);
        parent.getChildren().add(tenancy);
    }

    @Override
    public void clearParentOnTenancy(
            final @NonNull ApplicationTenancy tenancy) {
        var parent = tenancy.getParent();
        if(parent != null) {
            parent.getChildren().remove(tenancy);
            tenancy.setParent(null);
        }
    }

}
