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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

class CommandReplayMappingListenerInMemoryTest {

    @Test
    void records_result_mapping_and_returns_it_from_lookup() {
        CommandReplayMappingListenerInMemory listener = new CommandReplayMappingListenerInMemory();
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");

        listener.onReplayResult(recordedResult, actualResult, commandLogEntry);

        assertThat(listener.lookup(commandLogEntry, recordedResult)).contains(actualResult);
    }

    @Test
    void records_command_interaction_id_when_creating_mapping() {
        CommandReplayMappingListenerInMemory listener = new CommandReplayMappingListenerInMemory();
        UUID commandInteractionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        CommandLogEntry commandLogEntry = commandLogEntry(commandInteractionId);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");

        listener.onReplayResult(recordedResult, actualResult, commandLogEntry);

        assertThat(listener.getMapping(recordedResult).getCommandInteractionId()).isEqualTo(commandInteractionId);
    }

    @Test
    void repeated_recorded_bookmark_mapping_with_same_actual_bookmark_keeps_original_command_interaction_id() {
        CommandReplayMappingListenerInMemory listener = new CommandReplayMappingListenerInMemory();
        UUID firstCommandInteractionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID secondCommandInteractionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");

        listener.onReplayResult(recordedResult, actualResult, commandLogEntry(firstCommandInteractionId));
        listener.onReplayResult(recordedResult, actualResult, commandLogEntry(secondCommandInteractionId));

        assertThat(listener.getMapping(recordedResult).getCommandInteractionId()).isEqualTo(firstCommandInteractionId);
    }

    @Test
    void logged_conflict_keeps_original_command_interaction_id() {
        CommandReplayMappingListenerInMemory listener = new CommandReplayMappingListenerInMemory(OnConflictPolicy.LOG_AND_CONTINUE);
        UUID firstCommandInteractionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID secondCommandInteractionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark firstActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        Bookmark secondActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "3");

        listener.onReplayResult(recordedResult, firstActualResult, commandLogEntry(firstCommandInteractionId));
        listener.onReplayResult(recordedResult, secondActualResult, commandLogEntry(secondCommandInteractionId));

        assertThat(listener.getMapping(recordedResult).getCommandInteractionId()).isEqualTo(firstCommandInteractionId);
    }

    @Test
    void equal_recorded_and_actual_bookmark_is_recorded() {
        CommandReplayMappingListenerInMemory listener = new CommandReplayMappingListenerInMemory();
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");

        listener.onReplayResult(recordedResult, recordedResult, commandLogEntry);

        assertThat(listener.lookup(commandLogEntry, recordedResult)).contains(recordedResult);
    }

    @Test
    void repeated_recorded_bookmark_mapping_with_same_actual_bookmark_is_idempotent() {
        CommandReplayMappingListenerInMemory listener = new CommandReplayMappingListenerInMemory();
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");

        listener.onReplayResult(recordedResult, actualResult, commandLogEntry);
        listener.onReplayResult(recordedResult, actualResult, commandLogEntry);

        assertThat(listener.lookup(commandLogEntry, recordedResult)).contains(actualResult);
    }

    @Test
    void repeated_recorded_bookmark_mapping_with_different_actual_bookmark_is_rejected_by_default() {
        CommandReplayMappingListenerInMemory listener = new CommandReplayMappingListenerInMemory();
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark firstActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        Bookmark secondActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "3");

        listener.onReplayResult(recordedResult, firstActualResult, commandLogEntry);

        assertThatThrownBy(() -> listener.onReplayResult(recordedResult, secondActualResult, commandLogEntry))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("demoInvoice:1")
                .hasMessageContaining("demoInvoice:2")
                .hasMessageContaining("demoInvoice:3");
        assertThat(listener.lookup(commandLogEntry, recordedResult)).contains(firstActualResult);
    }

    @Test
    void repeated_recorded_bookmark_mapping_with_different_actual_bookmark_is_logged_and_ignored_when_configured() {
        CommandReplayMappingListenerInMemory listener = new CommandReplayMappingListenerInMemory(OnConflictPolicy.LOG_AND_CONTINUE);
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark firstActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        Bookmark secondActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "3");

        listener.onReplayResult(recordedResult, firstActualResult, commandLogEntry);
        listener.onReplayResult(recordedResult, secondActualResult, commandLogEntry);

        assertThat(listener.lookup(commandLogEntry, recordedResult)).contains(firstActualResult);
    }

    @Test
    void unmapped_bookmark_returns_no_replacement() {
        CommandReplayMappingListenerInMemory listener = new CommandReplayMappingListenerInMemory();
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedBookmark = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");

        assertThat(listener.lookup(commandLogEntry, recordedBookmark)).isEmpty();
    }

    @Test
    void autoconfiguration_creates_default_listener_when_missing() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                CausewayConfigurationForTest.class,
                CommandReplayMappingListenerInMemory.BeanFactory.class)) {

            assertThat(context.getBean(CommandReplayMappingListener.class))
                    .isInstanceOf(CommandReplayMappingListenerInMemory.class);
        }
    }

    @Test
    void autoconfiguration_backs_off_when_custom_listener_exists() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                CausewayConfigurationForTest.class,
                CustomListenerConfiguration.class,
                CommandReplayMappingListenerInMemory.BeanFactory.class)) {

            assertThat(context.getBeansOfType(CommandReplayMappingListener.class))
                    .hasSize(1)
                    .containsKey("customCommandReplayMappingListener");
            assertThat(context.getBean(CommandReplayMappingListener.class))
                    .isSameAs(CustomListenerConfiguration.CUSTOM_LISTENER);
        }
    }

    @Test
    void autoconfiguration_does_not_create_in_memory_listener_when_persistent_storage_is_configured() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                    "causeway.extensions.command-log.replay-result-mapping.storage-strategy", "PERSISTENT")));
            context.register(CausewayConfigurationForTest.class, CommandReplayMappingListenerInMemory.BeanFactory.class);
            context.refresh();

            assertThat(context.getBeansOfType(CommandReplayMappingListener.class)).isEmpty();
        }
    }

    @Test
    void autoconfiguration_uses_configured_conflict_policy() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                LogAndContinueCausewayConfigurationForTest.class,
                CommandReplayMappingListenerInMemory.BeanFactory.class)) {
            CommandReplayMappingListener listener = context.getBean(CommandReplayMappingListener.class);
            CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
            Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
            Bookmark firstActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
            Bookmark secondActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "3");

            listener.onReplayResult(recordedResult, firstActualResult, commandLogEntry);
            listener.onReplayResult(recordedResult, secondActualResult, commandLogEntry);

            assertThat(listener.lookup(commandLogEntry, recordedResult)).contains(firstActualResult);
        }
    }

    private static CommandLogEntry commandLogEntry(final UUID interactionId) {
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        when(commandLogEntry.getInteractionId()).thenReturn(interactionId);
        return commandLogEntry;
    }

    @Configuration(proxyBeanMethods = false)
    static class CausewayConfigurationForTest {

        @Bean
        CausewayConfiguration causewayConfiguration() {
            return new CausewayConfiguration(new StandardEnvironment(), Optional.empty());
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class LogAndContinueCausewayConfigurationForTest {

        @Bean
        CausewayConfiguration causewayConfiguration() {
            CausewayConfiguration causewayConfiguration = new CausewayConfiguration(new StandardEnvironment(), Optional.empty());
            causewayConfiguration.getExtensions().getCommandLog().getReplayResultMapping()
                    .setOnConflictPolicy(OnConflictPolicy.LOG_AND_CONTINUE);
            return causewayConfiguration;
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

}
