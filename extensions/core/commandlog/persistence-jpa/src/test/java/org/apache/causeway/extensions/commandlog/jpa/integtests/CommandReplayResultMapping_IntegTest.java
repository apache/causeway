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
package org.apache.causeway.extensions.commandlog.jpa.integtests;

import java.util.ArrayList;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.persistence.Table;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMappingRepository;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListener;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListenerPersistent;
import org.apache.causeway.extensions.commandlog.jpa.dom.CommandReplayResultMapping;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = AppManifest.class,
        properties = "causeway.extensions.command-log.replay-result-mapping.storage-strategy=PERSISTENT")
@ActiveProfiles("test")
class CommandReplayResultMapping_IntegTest extends CausewayIntegrationTestAbstract {

    @Inject CommandReplayResultMappingRepository repository;
    @Inject CommandReplayMappingListener mappingListener;
    @Inject InteractionService interactionService;

    @BeforeEach
    void setUp() {
        interactionService.nextInteraction();
        repository.removeAll();
    }

    @Test
    void jpaRepositoryPersistsAndQueriesIdentityChangedAndInteractionId() {
        var interactionId = UUID.randomUUID();
        var identity = repository.createAndPersist(bookmark("1"), bookmark("1"), null);
        var firstChanged = repository.createAndPersist(bookmark("2"), bookmark("9"), interactionId);
        var secondChanged = repository.createAndPersist(bookmark("3"), bookmark("9"), null);

        assertThat(new ArrayList<Object>(repository.findAll()))
                .containsExactly(identity, firstChanged, secondChanged);
        assertThat(new ArrayList<Object>(repository.findChanged()))
                .containsExactly(firstChanged, secondChanged);
        assertThat(repository.findByRecordedBookmark(bookmark("2"))).contains(firstChanged);
        assertThat(new ArrayList<Object>(repository.findByActualBookmark(bookmark("9"))))
                .containsExactly(firstChanged, secondChanged);
        assertThat(repository.findByRecordedBookmark(bookmark("2")).orElseThrow().getCommandInteractionId())
                .isEqualTo(interactionId);
    }

    @Test
    void persistentListenerIsSelectedAndSurvivesRepositoryReads() {
        assertThat(mappingListener).isInstanceOf(CommandReplayMappingListenerPersistent.class);
        var interactionId = UUID.randomUUID();

        mappingListener.onReplayResult(bookmark("4"), bookmark("5"), interactionId);

        assertThat(repository.findByRecordedBookmark(bookmark("4"))).get()
                .extracting(
                        mapping -> mapping.getActualBookmark(),
                        mapping -> mapping.getCommandInteractionId())
                .containsExactly(bookmark("5"), interactionId);
        assertThat(mappingListener.lookup(bookmark("4"))).contains(bookmark("5"));
    }

    @Test
    void recordedBookmarkHasDatabaseUniquenessMetadata() {
        var table = CommandReplayResultMapping.class.getAnnotation(Table.class);

        assertThat(table.uniqueConstraints()).singleElement().satisfies(constraint ->
                assertThat(constraint.columnNames()).containsExactly("recordedBookmark"));
        assertThat(table.indexes()).extracting(index -> index.columnList())
                .containsExactly("recordedBookmark", "actualBookmark");
    }

    @Test
    void repositoryCanRemoveOneOrAllMappings() {
        var first = repository.createAndPersist(bookmark("6"), bookmark("7"), null);
        repository.createAndPersist(bookmark("8"), bookmark("9"), null);

        repository.remove(first);
        assertThat(repository.findAll()).hasSize(1);

        repository.removeAll();
        assertThat(repository.findAll()).isEmpty();
    }

    private static Bookmark bookmark(final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", id);
    }
}
