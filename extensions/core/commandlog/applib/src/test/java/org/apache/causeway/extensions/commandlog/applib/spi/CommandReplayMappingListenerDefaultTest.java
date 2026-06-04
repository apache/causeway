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

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.ReplayResultMapping.OnConflictPolicy;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

class CommandReplayMappingListenerDefaultTest {

    @Test
    void records_result_mapping_and_returns_it_from_remap() {
        CommandReplayMappingListenerDefault listener = new CommandReplayMappingListenerDefault();
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");

        listener.onReplayResultMapped(recordedResult, actualResult, commandLogEntry);

        assertThat(listener.remap(commandLogEntry, recordedResult)).contains(actualResult);
    }

    @Test
    void equal_recorded_and_actual_bookmark_is_not_recorded() {
        CommandReplayMappingListenerDefault listener = new CommandReplayMappingListenerDefault();
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");

        listener.onReplayResultMapped(recordedResult, recordedResult, commandLogEntry);

        assertThat(listener.remap(commandLogEntry, recordedResult)).isEmpty();
    }

    @Test
    void repeated_recorded_bookmark_mapping_with_same_actual_bookmark_is_idempotent() {
        CommandReplayMappingListenerDefault listener = new CommandReplayMappingListenerDefault();
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark actualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");

        listener.onReplayResultMapped(recordedResult, actualResult, commandLogEntry);
        listener.onReplayResultMapped(recordedResult, actualResult, commandLogEntry);

        assertThat(listener.remap(commandLogEntry, recordedResult)).contains(actualResult);
    }

    @Test
    void repeated_recorded_bookmark_mapping_with_different_actual_bookmark_is_rejected_by_default() {
        CommandReplayMappingListenerDefault listener = new CommandReplayMappingListenerDefault();
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark firstActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        Bookmark secondActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "3");

        listener.onReplayResultMapped(recordedResult, firstActualResult, commandLogEntry);

        assertThatThrownBy(() -> listener.onReplayResultMapped(recordedResult, secondActualResult, commandLogEntry))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("demoInvoice:1")
                .hasMessageContaining("demoInvoice:2")
                .hasMessageContaining("demoInvoice:3");
        assertThat(listener.remap(commandLogEntry, recordedResult)).contains(firstActualResult);
    }

    @Test
    void repeated_recorded_bookmark_mapping_with_different_actual_bookmark_is_logged_and_ignored_when_configured() {
        CommandReplayMappingListenerDefault listener = new CommandReplayMappingListenerDefault(OnConflictPolicy.LOG_AND_CONTINUE);
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
        Bookmark firstActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
        Bookmark secondActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "3");

        listener.onReplayResultMapped(recordedResult, firstActualResult, commandLogEntry);
        listener.onReplayResultMapped(recordedResult, secondActualResult, commandLogEntry);

        assertThat(listener.remap(commandLogEntry, recordedResult)).contains(firstActualResult);
    }

    @Test
    void unmapped_bookmark_returns_no_replacement() {
        CommandReplayMappingListenerDefault listener = new CommandReplayMappingListenerDefault();
        CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
        Bookmark recordedBookmark = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");

        assertThat(listener.remap(commandLogEntry, recordedBookmark)).isEmpty();
    }

    @Test
    void autoconfiguration_creates_default_listener_when_missing() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                CausewayConfigurationForTest.class,
                CommandReplayMappingListenerDefault.BeanFactory.class)) {

            assertThat(context.getBean(CommandReplayMappingListener.class))
                    .isInstanceOf(CommandReplayMappingListenerDefault.class);
        }
    }

    @Test
    void autoconfiguration_backs_off_when_custom_listener_exists() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                CausewayConfigurationForTest.class,
                CustomListenerConfiguration.class,
                CommandReplayMappingListenerDefault.BeanFactory.class)) {

            assertThat(context.getBeansOfType(CommandReplayMappingListener.class))
                    .hasSize(1)
                    .containsKey("customCommandReplayMappingListener");
            assertThat(context.getBean(CommandReplayMappingListener.class))
                    .isSameAs(CustomListenerConfiguration.CUSTOM_LISTENER);
        }
    }

    @Test
    void autoconfiguration_uses_configured_conflict_policy() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                LogAndContinueCausewayConfigurationForTest.class,
                CommandReplayMappingListenerDefault.BeanFactory.class)) {
            CommandReplayMappingListener listener = context.getBean(CommandReplayMappingListener.class);
            CommandLogEntry commandLogEntry = mock(CommandLogEntry.class);
            Bookmark recordedResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "1");
            Bookmark firstActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "2");
            Bookmark secondActualResult = Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", "3");

            listener.onReplayResultMapped(recordedResult, firstActualResult, commandLogEntry);
            listener.onReplayResultMapped(recordedResult, secondActualResult, commandLogEntry);

            assertThat(listener.remap(commandLogEntry, recordedResult)).contains(firstActualResult);
        }
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
