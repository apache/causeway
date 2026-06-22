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
package org.apache.causeway.extensions.commandlog.applib.spi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.ReplayResultMapping.OnConflictPolicy;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMappingRepository;

class CommandReplayMappingListenerPersistentTest {

    @Test
    void records_result_mapping_and_returns_it_from_lookup() {
        FakeRepository repository = new FakeRepository();
        CommandReplayMappingListenerPersistent listener = new CommandReplayMappingListenerPersistent(
                repository, OnConflictPolicy.THROW_EXCEPTION);
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");

        listener.onReplayResult(recordedResult, actualResult, null);

        assertThat(listener.lookup(recordedResult)).contains(actualResult);
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void records_command_interaction_id_when_creating_mapping() {
        FakeRepository repository = new FakeRepository();
        CommandReplayMappingListenerPersistent listener = new CommandReplayMappingListenerPersistent(
                repository, OnConflictPolicy.THROW_EXCEPTION);
        UUID commandInteractionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        CommandLogEntry commandLogEntry = commandLogEntry(commandInteractionId);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");

        listener.onReplayResult(recordedResult, actualResult, commandInteractionId);

        assertThat(repository.findByRecordedBookmark(recordedResult))
                .isPresent()
                .get()
                .extracting(CommandReplayResultMapping::getCommandInteractionId)
                .isEqualTo(commandInteractionId);
    }

    @Test
    void equal_recorded_and_actual_bookmark_is_recorded() {
        FakeRepository repository = new FakeRepository();
        CommandReplayMappingListenerPersistent listener = new CommandReplayMappingListenerPersistent(
                repository, OnConflictPolicy.THROW_EXCEPTION);
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");

        listener.onReplayResult(recordedResult, recordedResult, null);

        assertThat(listener.lookup(recordedResult)).contains(recordedResult);
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void repeated_recorded_bookmark_mapping_with_same_actual_bookmark_keeps_original_command_interaction_id() {
        FakeRepository repository = new FakeRepository();
        CommandReplayMappingListenerPersistent listener = new CommandReplayMappingListenerPersistent(
                repository, OnConflictPolicy.THROW_EXCEPTION);
        UUID firstCommandInteractionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID secondCommandInteractionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");

        listener.onReplayResult(recordedResult, actualResult, firstCommandInteractionId);
        listener.onReplayResult(recordedResult, actualResult, secondCommandInteractionId);

        assertThat(repository.findByRecordedBookmark(recordedResult))
                .isPresent()
                .get()
                .extracting(CommandReplayResultMapping::getCommandInteractionId)
                .isEqualTo(firstCommandInteractionId);
    }

    @Test
    void repeated_recorded_bookmark_mapping_with_same_actual_bookmark_is_idempotent() {
        FakeRepository repository = new FakeRepository();
        CommandReplayMappingListenerPersistent listener = new CommandReplayMappingListenerPersistent(
                repository, OnConflictPolicy.THROW_EXCEPTION);
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");

        listener.onReplayResult(recordedResult, actualResult, null);
        listener.onReplayResult(recordedResult, actualResult, null);

        assertThat(listener.lookup(recordedResult)).contains(actualResult);
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void repeated_recorded_bookmark_mapping_with_different_actual_bookmark_is_rejected_by_default() {
        FakeRepository repository = new FakeRepository();
        CommandReplayMappingListenerPersistent listener = new CommandReplayMappingListenerPersistent(
                repository, OnConflictPolicy.THROW_EXCEPTION);
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark firstActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        Bookmark secondActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "3");

        listener.onReplayResult(recordedResult, firstActualResult, null);

        assertThatThrownBy(() -> listener.onReplayResult(recordedResult, secondActualResult, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("demoInvoice:1")
                .hasMessageContaining("demoInvoice:2")
                .hasMessageContaining("demoInvoice:3");
        assertThat(listener.lookup(recordedResult)).contains(firstActualResult);
    }

    @Test
    void repeated_recorded_bookmark_mapping_with_different_actual_bookmark_is_logged_and_ignored_when_configured() {
        FakeRepository repository = new FakeRepository();
        CommandReplayMappingListenerPersistent listener = new CommandReplayMappingListenerPersistent(
                repository, OnConflictPolicy.LOG_AND_CONTINUE);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark firstActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        Bookmark secondActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "3");

        listener.onReplayResult(recordedResult, firstActualResult, null);
        listener.onReplayResult(recordedResult, secondActualResult, null);

        assertThat(listener.lookup(recordedResult)).contains(firstActualResult);
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void logged_conflict_keeps_original_command_interaction_id() {
        FakeRepository repository = new FakeRepository();
        CommandReplayMappingListenerPersistent listener = new CommandReplayMappingListenerPersistent(
                repository, OnConflictPolicy.LOG_AND_CONTINUE);
        UUID firstCommandInteractionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID secondCommandInteractionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark firstActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        Bookmark secondActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "3");

        listener.onReplayResult(recordedResult, firstActualResult, firstCommandInteractionId);
        listener.onReplayResult(recordedResult, secondActualResult, secondCommandInteractionId);

        assertThat(repository.findByRecordedBookmark(recordedResult))
                .isPresent()
                .get()
                .extracting(CommandReplayResultMapping::getCommandInteractionId)
                .isEqualTo(firstCommandInteractionId);
    }

    @Test
    void repository_finds_changed_mappings() {
        FakeRepository repository = new FakeRepository();
        Bookmark identityResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark changedRecordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        Bookmark changedActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "3");

        CommandReplayResultMapping identityMapping = repository.createAndPersist(identityResult, identityResult);
        CommandReplayResultMapping changedMapping = repository.createAndPersist(changedRecordedResult, changedActualResult);

        assertThat(new ArrayList<Object>(repository.findChanged()))
                .containsExactly(changedMapping)
                .doesNotContain(identityMapping);
    }

    @Test
    void repository_finds_mappings_by_actual_bookmark() {
        FakeRepository repository = new FakeRepository();
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "9");
        Bookmark firstRecordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark secondRecordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        Bookmark otherRecordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "3");
        Bookmark otherActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "4");

        CommandReplayResultMapping firstMapping = repository.createAndPersist(firstRecordedResult, actualResult);
        CommandReplayResultMapping secondMapping = repository.createAndPersist(secondRecordedResult, actualResult);
        repository.createAndPersist(otherRecordedResult, otherActualResult);

        assertThat(new ArrayList<Object>(repository.findByActualBookmark(actualResult)))
                .containsExactly(firstMapping, secondMapping);
    }

    @Test
    void autoconfiguration_creates_persistent_listener_when_configured() {
        try (AnnotationConfigApplicationContext context = contextWithPersistentStorage(
                CausewayConfigurationForTest.class,
                RepositoryConfiguration.class,
                CommandReplayMappingListenerPersistent.BeanFactory.class)) {

            assertThat(context.getBean(CommandReplayMappingListener.class))
                    .isInstanceOf(CommandReplayMappingListenerPersistent.class);
        }
    }

    @Test
    void autoconfiguration_does_not_create_persistent_listener_without_repository() {
        try (AnnotationConfigApplicationContext context = contextWithPersistentStorage(
                CausewayConfigurationForTest.class,
                CommandReplayMappingListenerPersistent.BeanFactory.class)) {

            assertThat(context.getBeansOfType(CommandReplayMappingListener.class)).isEmpty();
        }
    }

    @Test
    void autoconfiguration_backs_off_when_custom_listener_exists() {
        try (AnnotationConfigApplicationContext context = contextWithPersistentStorage(
                CausewayConfigurationForTest.class,
                RepositoryConfiguration.class,
                CustomListenerConfiguration.class,
                CommandReplayMappingListenerPersistent.BeanFactory.class)) {

            assertThat(context.getBeansOfType(CommandReplayMappingListener.class))
                    .hasSize(1)
                    .containsKey("customCommandReplayMappingListener");
            assertThat(context.getBean(CommandReplayMappingListener.class))
                    .isSameAs(CustomListenerConfiguration.CUSTOM_LISTENER);
        }
    }

    private static CommandLogEntry commandLogEntry(final UUID interactionId) {
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        return commandLogEntry;
    }

    private static AnnotationConfigApplicationContext contextWithPersistentStorage(final Class<?>... configClasses) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "causeway.extensions.command-log.replay-result-mapping.storage-strategy", "PERSISTENT")));
        context.register(configClasses);
        context.refresh();
        return context;
    }

    @Configuration(proxyBeanMethods = false)
    static class CausewayConfigurationForTest {

        @Bean
        CausewayConfiguration causewayConfiguration() {
            return new CausewayConfiguration(new StandardEnvironment(), Optional.empty());
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class RepositoryConfiguration {

        static final FakeRepository REPOSITORY = new FakeRepository();

        @Bean
        CommandReplayResultMappingRepository commandReplayResultMappingRepository() {
            return REPOSITORY;
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class CustomListenerConfiguration {

        static final CommandReplayMappingListener CUSTOM_LISTENER = new CommandReplayMappingListener() {};

        @Bean
        CommandReplayMappingListener customCommandReplayMappingListener() {
            return CUSTOM_LISTENER;
        }

    }

    static class FakeRepository implements CommandReplayResultMappingRepository {

        private final Map<Bookmark, CommandReplayResultMapping> mappings = new LinkedHashMap<>();

        @Override
        public Optional<CommandReplayResultMapping> findByRecordedBookmark(final Bookmark recordedBookmark) {
            return Optional.ofNullable(mappings.get(recordedBookmark));
        }

        @Override
        public List<? extends CommandReplayResultMapping> findByActualBookmark(final Bookmark actualBookmark) {
            return mappings.values().stream()
                    .filter(mapping -> mapping.getActualBookmark().equals(actualBookmark))
                    .collect(Collectors.toList());
        }

        @Override
        public List<? extends CommandReplayResultMapping> findChanged() {
            return mappings.values().stream()
                    .filter(mapping -> !mapping.getRecordedBookmark().equals(mapping.getActualBookmark()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<? extends CommandReplayResultMapping> findAll() {
            return new ArrayList<>(mappings.values());
        }

        @Override
        public void removeAll() {
            mappings.clear();
        }

        @Override
        public CommandReplayResultMapping createAndPersist(
                final Bookmark recordedBookmark,
                final Bookmark actualBookmark,
                final UUID commandInteractionId) {
            FakeMapping mapping = new FakeMapping();
            mapping.init(recordedBookmark, actualBookmark, commandInteractionId);
            mappings.put(recordedBookmark, mapping);
            return mapping;
        }
    }

    static class FakeMapping extends CommandReplayResultMapping {

        private Bookmark recordedBookmark;
        private Bookmark actualBookmark;
        private UUID commandInteractionId;

        @Override
        public Bookmark getRecordedBookmark() {
            return recordedBookmark;
        }

        @Override
        public void setRecordedBookmark(final Bookmark recordedBookmark) {
            this.recordedBookmark = recordedBookmark;
        }

        @Override
        public Bookmark getActualBookmark() {
            return actualBookmark;
        }

        @Override
        public void setActualBookmark(final Bookmark actualBookmark) {
            this.actualBookmark = actualBookmark;
        }

        @Override
        public UUID getCommandInteractionId() {
            return commandInteractionId;
        }

        @Override
        public void setCommandInteractionId(final UUID commandInteractionId) {
            this.commandInteractionId = commandInteractionId;
        }
    }
}
