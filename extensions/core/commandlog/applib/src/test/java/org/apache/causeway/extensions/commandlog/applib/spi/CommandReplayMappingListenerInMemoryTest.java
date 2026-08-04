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

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.ReplayResultMapping.OnConflictPolicy;
import org.apache.causeway.core.config.CausewayModuleCoreConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandReplayMappingListenerInMemoryTest {

    @Test
    void recordsAndLooksUpChangedMapping() {
        var listener = new CommandReplayMappingListenerInMemory();
        var recorded = bookmark("1");
        var actual = bookmark("2");

        listener.onReplayResult(recorded, actual, null);

        assertThat(listener.lookup(recorded)).contains(actual);
    }

    @Test
    void recordsIdentityMapping() {
        var listener = new CommandReplayMappingListenerInMemory();
        var bookmark = bookmark("1");

        listener.onReplayResult(bookmark, bookmark, null);

        assertThat(listener.lookup(bookmark)).contains(bookmark);
    }

    @Test
    void repeatedMappingIsIdempotentAndRetainsFirstInteractionId() {
        var listener = new CommandReplayMappingListenerInMemory();
        var recorded = bookmark("1");
        var actual = bookmark("2");
        var firstInteractionId = UUID.randomUUID();

        listener.onReplayResult(recorded, actual, firstInteractionId);
        listener.onReplayResult(recorded, actual, UUID.randomUUID());

        assertThat(listener.lookup(recorded)).contains(actual);
        assertThat(listener.mappingFor(recorded).commandInteractionId()).isEqualTo(firstInteractionId);
    }

    @Test
    void conflictingMappingThrowsAndRetainsFirstMappingByDefault() {
        var listener = new CommandReplayMappingListenerInMemory();
        var recorded = bookmark("1");
        var firstActual = bookmark("2");
        var firstInteractionId = UUID.randomUUID();
        listener.onReplayResult(recorded, firstActual, firstInteractionId);

        assertThatThrownBy(() -> listener.onReplayResult(recorded, bookmark("3"), UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("demo.Invoice:1")
                .hasMessageContaining("demo.Invoice:2")
                .hasMessageContaining("demo.Invoice:3");
        assertThat(listener.lookup(recorded)).contains(firstActual);
        assertThat(listener.mappingFor(recorded).commandInteractionId()).isEqualTo(firstInteractionId);
    }

    @Test
    void conflictingMappingCanBeLoggedAndIgnored() {
        var listener = new CommandReplayMappingListenerInMemory(OnConflictPolicy.LOG_AND_CONTINUE);
        var recorded = bookmark("1");
        var firstActual = bookmark("2");
        listener.onReplayResult(recorded, firstActual, UUID.randomUUID());

        listener.onReplayResult(recorded, bookmark("3"), UUID.randomUUID());

        assertThat(listener.lookup(recorded)).contains(firstActual);
    }

    @Test
    void mappingStateIsInstanceScoped() {
        var first = new CommandReplayMappingListenerInMemory();
        first.onReplayResult(bookmark("1"), bookmark("2"), null);

        assertThat(new CommandReplayMappingListenerInMemory().lookup(bookmark("1"))).isEmpty();
    }

    @Test
    void autoconfigurationProvidesInMemoryDefault() {
        contextRunner()
                .withUserConfiguration(CommandReplayMappingListenerInMemory.BeanFactory.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(CommandReplayMappingListener.class);
                    assertThat(context.getBean(CommandReplayMappingListener.class))
                            .isInstanceOf(CommandReplayMappingListenerInMemory.class);
                });
    }

    @Test
    void autoconfigurationBacksOffForCustomListener() {
        contextRunner()
                .withUserConfiguration(
                        CustomListenerConfiguration.class,
                        CommandReplayMappingListenerInMemory.BeanFactory.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(CommandReplayMappingListener.class);
                    assertThat(context.getBean(CommandReplayMappingListener.class))
                            .isSameAs(CustomListenerConfiguration.CUSTOM_LISTENER);
                });
    }

    @Test
    void persistentStrategySuppressesInMemoryDefault() {
        contextRunner()
                .withUserConfiguration(CommandReplayMappingListenerInMemory.BeanFactory.class)
                .withPropertyValues(
                        "causeway.extensions.command-log.replay-result-mapping.storage-strategy=PERSISTENT")
                .run(context -> assertThat(context).doesNotHaveBean(CommandReplayMappingListener.class));
    }

    @Test
    void autoconfigurationUsesConfiguredConflictPolicy() {
        contextRunner()
                .withUserConfiguration(CommandReplayMappingListenerInMemory.BeanFactory.class)
                .withPropertyValues(
                        "causeway.extensions.command-log.replay-result-mapping.on-conflict-policy=LOG_AND_CONTINUE")
                .run(context -> {
                    var listener = context.getBean(CommandReplayMappingListener.class);
                    var recorded = bookmark("1");
                    listener.onReplayResult(recorded, bookmark("2"), null);
                    listener.onReplayResult(recorded, bookmark("3"), null);
                    assertThat(listener.lookup(recorded)).contains(bookmark("2"));
                });
    }

    private static ApplicationContextRunner contextRunner() {
        return new ApplicationContextRunner()
                .withUserConfiguration(CausewayModuleCoreConfig.class);
    }

    private static Bookmark bookmark(final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", id);
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomListenerConfiguration {
        static final CommandReplayMappingListener CUSTOM_LISTENER = new CommandReplayMappingListener() {
        };

        @Bean
        CommandReplayMappingListener customCommandReplayMappingListener() {
            return CUSTOM_LISTENER;
        }
    }
}
