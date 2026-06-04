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
package org.apache.causeway.extensions.commandlog.applib.dom;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.internal.base._Casts;

/**
 * Provides supporting functionality for querying and persisting replay result mapping entities.
 *
 * @since 2.1 {@index}
 */
public abstract class CommandReplayResultMappingRepositoryAbstract<C extends CommandReplayResultMapping>
implements CommandReplayResultMappingRepository {

    @Inject Provider<RepositoryService> repositoryServiceProvider;
    @Inject FactoryService factoryService;

    private final Class<C> entityClass;

    protected CommandReplayResultMappingRepositoryAbstract(final Class<C> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Optional<CommandReplayResultMapping> findByRecordedBookmark(final Bookmark recordedBookmark) {
        return _Casts.uncheckedCast(repositoryService().firstMatch(
                Query.named(entityClass, CommandReplayResultMapping.Nq.FIND_BY_RECORDED_BOOKMARK)
                        .withParameter("recordedBookmark", recordedBookmark)));
    }

    @Override
    public List<? extends CommandReplayResultMapping> findAll() {
        return _Casts.uncheckedCast(repositoryService().allMatches(
                Query.named(entityClass, CommandReplayResultMapping.Nq.FIND)));
    }

    @Override
    public CommandReplayResultMapping createAndPersist(final Bookmark recordedBookmark, final Bookmark actualBookmark) {
        C mapping = factoryService.detachedEntity(entityClass);
        mapping.init(recordedBookmark, actualBookmark);
        repositoryService().persistAndFlush(mapping);
        return mapping;
    }

    private RepositoryService repositoryService() {
        return repositoryServiceProvider.get();
    }

}
