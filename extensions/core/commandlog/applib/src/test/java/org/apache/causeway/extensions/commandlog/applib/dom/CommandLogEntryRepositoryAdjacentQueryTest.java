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

import java.sql.Timestamp;
import java.util.List;

import jakarta.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.apache.causeway.applib.query.NamedQuery;
import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.repository.RepositoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandLogEntryRepositoryAdjacentQueryTest {

    private final RepositoryService repositoryService = mock(RepositoryService.class);
    private final Repository repository = new Repository();

    @BeforeEach
    void setUp() {
        repository.repositoryServiceProvider = providerOf(repositoryService);
        when(repositoryService.allMatches(any(Query.class))).thenReturn(List.of());
    }

    @Test
    void selectsOrderedForegroundQueriesAndOptionalLimit() {
        var timestamp = Timestamp.valueOf("2026-08-05 12:00:00");

        repository.findForegroundSinceTimestamp(timestamp, 7);
        var after = captureLastQuery();
        assertThat(after.getName()).isEqualTo(CommandLogEntry.Nq.FIND_FOREGROUND_BY_TIMESTAMP_AFTER);
        assertThat(after.getParametersByName()).containsEntry("from", timestamp);
        assertThat(after.getRange().getLimit()).isEqualTo(7);

        repository.findForegroundBeforeTimestamp(timestamp, null);
        var before = captureLastQuery();
        assertThat(before.getName()).isEqualTo(CommandLogEntry.Nq.FIND_FOREGROUND_BY_TIMESTAMP_BEFORE);
        assertThat(before.getParametersByName()).containsEntry("to", timestamp);
        assertThat(before.getRange().isUnconstrained()).isTrue();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private NamedQuery<CommandLogEntry> captureLastQuery() {
        var captor = ArgumentCaptor.forClass(Query.class);
        verify(repositoryService, org.mockito.Mockito.atLeastOnce()).allMatches(captor.capture());
        var queries = captor.getAllValues();
        return (NamedQuery<CommandLogEntry>) queries.get(queries.size() - 1);
    }

    private static Provider<RepositoryService> providerOf(final RepositoryService service) {
        return () -> service;
    }

    private static final class Repository
            extends CommandLogEntryRepositoryAbstract<CommandLogEntry> {
        private Repository() { super(CommandLogEntry.class); }
    }
}
