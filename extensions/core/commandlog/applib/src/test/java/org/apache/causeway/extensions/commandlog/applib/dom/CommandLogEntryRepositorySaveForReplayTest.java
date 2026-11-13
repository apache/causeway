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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandLogEntryRepositorySaveForReplayTest {

    private final RepositoryService repositoryService = mock(RepositoryService.class);
    private final FactoryService factoryService = mock(FactoryService.class);
    private final Repository repository = new Repository();

    @BeforeEach
    void setUp() {
        repository.repositoryServiceProvider = () -> repositoryService;
        repository.factoryService = factoryService;
    }

    @SuppressWarnings("unchecked")
	@Test
    void reImportOfExistingInteractionIdReturnsExistingEntryWithoutCreatingDuplicate() {
        var interactionId = UUID.randomUUID();
        var existing = mock(CommandLogEntry.class);
        when(repositoryService.uniqueMatch(any(Query.class))).thenReturn(Optional.of(existing));

        var returned = repository.saveForReplay(command(interactionId.toString()));

        assertThat(returned).isSameAs(existing);
        verify(factoryService, never()).detachedEntity(any());
        verify(repositoryService, never()).persistAndFlush(any());
    }

    @SuppressWarnings("unchecked")
	@Test
    void firstImportCreatesAndPersistsNewEntry() {
        var created = mock(CommandLogEntry.class);
        when(repositoryService.uniqueMatch(any(Query.class))).thenReturn(Optional.empty());
        when(factoryService.detachedEntity(CommandLogEntry.class)).thenReturn(created);

        var returned = repository.saveForReplay(command(UUID.randomUUID().toString()));

        assertThat(returned).isSameAs(created);
        verify(repositoryService).persistAndFlush(created);
    }

    @SuppressWarnings("unchecked")
	@Test
    void malformedInteractionIdSkipsTheLookupAndCreatesNewEntry() {
        var created = mock(CommandLogEntry.class);
        when(factoryService.detachedEntity(CommandLogEntry.class)).thenReturn(created);

        var returned = repository.saveForReplay(command("not-a-uuid"));

        assertThat(returned).isSameAs(created);
        verify(repositoryService, never()).uniqueMatch(any(Query.class));
        verify(repositoryService).persistAndFlush(created);
    }

    private static CommandDto command(final String interactionId) {
        var command = new CommandDto();
        command.setInteractionId(interactionId);
        return command;
    }

    private static final class Repository
            extends CommandLogEntryRepositoryAbstract<CommandLogEntry> {
        private Repository() { super(CommandLogEntry.class); }
    }
}
