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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.causeway.applib.query.NamedQuery;
import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.inject.Provider;

class CommandReplayResultMappingRepositoryAbstractTest {

    private final RepositoryService repositoryService = mock(RepositoryService.class);
    private final FactoryService factoryService = mock(FactoryService.class);
    private final Repository repository = new Repository();

    @BeforeEach
    void setUp() {
        repository.repositoryServiceProvider = providerOf(repositoryService);
        repository.factoryService = factoryService;
    }

    @SuppressWarnings("unchecked")
	@Test
    void selectsNamedQueriesAndParameters() {
        when(repositoryService.firstMatch(any(Query.class))).thenReturn(Optional.empty());
        when(repositoryService.allMatches(any(Query.class))).thenReturn(List.of());
        var recorded = bookmark("1");
        var actual = bookmark("2");

        repository.findByRecordedBookmark(recorded);
        var firstQuery = captureFirstMatchQuery();
        assertThat(firstQuery.getName()).isEqualTo(CommandReplayResultMapping.Nq.FIND_BY_RECORDED_BOOKMARK);
        assertThat(firstQuery.getParametersByName()).containsEntry("recordedBookmark", recorded);

        repository.findByActualBookmark(actual);
        var actualQuery = captureLastAllMatchesQuery();
        assertThat(actualQuery.getName()).isEqualTo(CommandReplayResultMapping.Nq.FIND_BY_ACTUAL_BOOKMARK);
        assertThat(actualQuery.getParametersByName()).containsEntry("actualBookmark", actual);

        repository.findChanged();
        assertThat(captureLastAllMatchesQuery().getName()).isEqualTo(CommandReplayResultMapping.Nq.FIND_CHANGED);

        repository.findAll();
        assertThat(captureLastAllMatchesQuery().getName()).isEqualTo(CommandReplayResultMapping.Nq.FIND);
    }

    @Test
    void createsInitializesAndFlushesMapping() {
        var mapping = new Mapping();
        var interactionId = UUID.randomUUID();
        when(factoryService.detachedEntity(Mapping.class)).thenReturn(mapping);

        var result = repository.createAndPersist(bookmark("1"), bookmark("2"), interactionId);

        assertThat(result).isSameAs(mapping);
        assertThat(mapping.getRecordedBookmark()).isEqualTo(bookmark("1"));
        assertThat(mapping.getActualBookmark()).isEqualTo(bookmark("2"));
        assertThat(mapping.getCommandInteractionId()).isEqualTo(interactionId);
        verify(repositoryService).persistAndFlush(mapping);
    }

    @Test
    void removesOneOrAllMappings() {
        var mapping = new Mapping();

        repository.remove(mapping);
        repository.removeAll();

        verify(repositoryService).removeAndFlush(mapping);
        verify(repositoryService).removeAll(Mapping.class);
    }

    @SuppressWarnings({"unchecked"})
    private NamedQuery<Mapping> captureFirstMatchQuery() {
        var captor = ArgumentCaptor.forClass(Query.class);
        verify(repositoryService).firstMatch(captor.capture());
        return (NamedQuery<Mapping>) captor.getValue();
    }

    @SuppressWarnings({"unchecked"})
    private NamedQuery<Mapping> captureLastAllMatchesQuery() {
        var captor = ArgumentCaptor.forClass(Query.class);
        verify(repositoryService, org.mockito.Mockito.atLeastOnce()).allMatches(captor.capture());
        var values = captor.getAllValues();
        return (NamedQuery<Mapping>) values.get(values.size() - 1);
    }

    private static Provider<RepositoryService> providerOf(final RepositoryService service) {
        return () -> service;
    }

    private static Bookmark bookmark(final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", id);
    }

    private static final class Repository
            extends CommandReplayResultMappingRepositoryAbstract<Mapping> {
        private Repository() { super(Mapping.class); }
    }

    private static final class Mapping implements CommandReplayResultMapping {
        private Bookmark recordedBookmark;
        private Bookmark actualBookmark;
        private @Nullable UUID commandInteractionId;

        @Override public Bookmark getRecordedBookmark() { return recordedBookmark; }
        @Override public void setRecordedBookmark(final Bookmark value) { recordedBookmark = value; }
        @Override public Bookmark getActualBookmark() { return actualBookmark; }
        @Override public void setActualBookmark(final Bookmark value) { actualBookmark = value; }
        @Override public @Nullable UUID getCommandInteractionId() { return commandInteractionId; }
        @Override public void setCommandInteractionId(final @Nullable UUID value) { commandInteractionId = value; }
    }
}
