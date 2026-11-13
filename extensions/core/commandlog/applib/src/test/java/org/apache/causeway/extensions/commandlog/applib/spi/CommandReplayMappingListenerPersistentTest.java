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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.ReplayResultMapping.OnConflictPolicy;
import org.apache.causeway.core.config.CausewayModuleCoreConfig;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMappingRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandReplayMappingListenerPersistentTest {

    @Test
    void recordsIdentityAndChangedMappingsAndLooksThemUp() {
        var repository = new InMemoryRepository();
        var listener = new CommandReplayMappingListenerPersistent(repository, OnConflictPolicy.THROW_EXCEPTION);
        var interactionId = UUID.randomUUID();

        listener.onReplayResult(bookmark("1"), bookmark("1"), interactionId);
        listener.onReplayResult(bookmark("2"), bookmark("3"), null);

        assertThat(listener.lookup(bookmark("1"))).contains(bookmark("1"));
        assertThat(listener.lookup(bookmark("2"))).contains(bookmark("3"));
        assertThat(listener.lookup(bookmark("missing"))).isEmpty();
        assertThat(repository.findByRecordedBookmark(bookmark("1")).orElseThrow().getCommandInteractionId())
                .isEqualTo(interactionId);
    }

    @Test
    void repeatedMappingRetainsFirstInteractionId() {
        var repository = new InMemoryRepository();
        var listener = new CommandReplayMappingListenerPersistent(repository, OnConflictPolicy.THROW_EXCEPTION);
        var firstInteractionId = UUID.randomUUID();
        listener.onReplayResult(bookmark("1"), bookmark("2"), firstInteractionId);

        listener.onReplayResult(bookmark("1"), bookmark("2"), UUID.randomUUID());

        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findByRecordedBookmark(bookmark("1")).orElseThrow().getCommandInteractionId())
                .isEqualTo(firstInteractionId);
    }

    @Test
    void conflictingMappingThrowsOrLogsWithoutReplacingFirst() {
        var strictRepository = new InMemoryRepository();
        var strict = new CommandReplayMappingListenerPersistent(strictRepository, OnConflictPolicy.THROW_EXCEPTION);
        strict.onReplayResult(bookmark("1"), bookmark("2"), null);

        assertThatThrownBy(() -> strict.onReplayResult(bookmark("1"), bookmark("3"), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("demo.Invoice:1")
                .hasMessageContaining("demo.Invoice:2")
                .hasMessageContaining("demo.Invoice:3");
        assertThat(strict.lookup(bookmark("1"))).contains(bookmark("2"));

        var lenientRepository = new InMemoryRepository();
        var lenient = new CommandReplayMappingListenerPersistent(lenientRepository, OnConflictPolicy.LOG_AND_CONTINUE);
        lenient.onReplayResult(bookmark("1"), bookmark("2"), null);
        lenient.onReplayResult(bookmark("1"), bookmark("3"), null);
        assertThat(lenient.lookup(bookmark("1"))).contains(bookmark("2"));
    }

    @Test
    void autoconfigurationSelectsPersistentAndBacksOffAsRequired() {
        contextRunner()
                .withUserConfiguration(
                        RepositoryConfiguration.class,
                        CommandReplayMappingListenerPersistent.BeanFactory.class,
                        CommandReplayMappingListenerInMemory.BeanFactory.class)
                .withPropertyValues("causeway.extensions.command-log.replay-result-mapping.storage-strategy=PERSISTENT")
                .run(context -> {
                    assertThat(context).hasSingleBean(CommandReplayMappingListener.class);
                    assertThat(context.getBean(CommandReplayMappingListener.class))
                            .isInstanceOf(CommandReplayMappingListenerPersistent.class);
                });

        contextRunner()
                .withUserConfiguration(CommandReplayMappingListenerPersistent.BeanFactory.class)
                .withPropertyValues("causeway.extensions.command-log.replay-result-mapping.storage-strategy=PERSISTENT")
                .run(context -> assertThat(context).doesNotHaveBean(CommandReplayMappingListener.class));

        contextRunner()
                .withUserConfiguration(
                        RepositoryConfiguration.class,
                        CustomListenerConfiguration.class,
                        CommandReplayMappingListenerPersistent.BeanFactory.class)
                .withPropertyValues("causeway.extensions.command-log.replay-result-mapping.storage-strategy=PERSISTENT")
                .run(context -> assertThat(context.getBean(CommandReplayMappingListener.class))
                        .isSameAs(CustomListenerConfiguration.CUSTOM_LISTENER));

        contextRunner()
                .withUserConfiguration(
                        RepositoryConfiguration.class,
                        CommandReplayMappingListenerPersistent.BeanFactory.class)
                .withPropertyValues("causeway.extensions.command-log.replay-result-mapping.storage-strategy=IN_MEMORY")
                .run(context -> assertThat(context).doesNotHaveBean(CommandReplayMappingListener.class));
    }

    private static ApplicationContextRunner contextRunner() {
        return new ApplicationContextRunner().withUserConfiguration(CausewayModuleCoreConfig.class);
    }

    private static Bookmark bookmark(final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", id);
    }

    @Configuration(proxyBeanMethods = false)
    static class RepositoryConfiguration {
        @Bean CommandReplayResultMappingRepository commandReplayResultMappingRepository() {
            return new InMemoryRepository();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomListenerConfiguration {
        static final CommandReplayMappingListener CUSTOM_LISTENER = new CommandReplayMappingListener() { };
        @Bean CommandReplayMappingListener customCommandReplayMappingListener() { return CUSTOM_LISTENER; }
    }

    private static final class InMemoryRepository implements CommandReplayResultMappingRepository {
        private final Map<Bookmark, Mapping> mappings = new LinkedHashMap<>();

        @Override public Optional<CommandReplayResultMapping> findByRecordedBookmark(final Bookmark bookmark) {
            return Optional.ofNullable(mappings.get(bookmark));
        }
        @Override public List<? extends CommandReplayResultMapping> findByActualBookmark(final Bookmark bookmark) {
            return mappings.values().stream().filter(mapping -> mapping.getActualBookmark().equals(bookmark)).toList();
        }
        @Override public List<? extends CommandReplayResultMapping> findChanged() {
            return mappings.values().stream()
                    .filter(mapping -> !mapping.getRecordedBookmark().equals(mapping.getActualBookmark())).toList();
        }
        @Override public List<? extends CommandReplayResultMapping> findAll() { return List.copyOf(mappings.values()); }
        @Override public CommandReplayResultMapping createAndPersist(
                final Bookmark recorded, final Bookmark actual, final @Nullable UUID interactionId) {
            var mapping = new Mapping();
            mapping.init(recorded, actual, interactionId);
            mappings.put(recorded, mapping);
            return mapping;
        }
        @Override public void remove(final CommandReplayResultMapping mapping) {
            mappings.remove(mapping.getRecordedBookmark());
        }
        @Override public void removeAll() { mappings.clear(); }
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
