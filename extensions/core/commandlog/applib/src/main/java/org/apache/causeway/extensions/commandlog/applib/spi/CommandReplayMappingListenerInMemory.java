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

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.ReplayResultMapping.OnConflictPolicy;

import lombok.extern.slf4j.Slf4j;

/**
 * Default instance-scoped replay mapping listener.
 */
@Slf4j
public final class CommandReplayMappingListenerInMemory implements CommandReplayMappingListener {

    private final ConcurrentMap<Bookmark, Mapping> mappingByRecordedBookmark = new ConcurrentHashMap<>();
    private final OnConflictPolicy onConflictPolicy;

    public CommandReplayMappingListenerInMemory() {
        this(OnConflictPolicy.THROW_EXCEPTION);
    }

    CommandReplayMappingListenerInMemory(final OnConflictPolicy onConflictPolicy) {
        this.onConflictPolicy = onConflictPolicy;
    }

    @Override
    public Optional<Bookmark> lookup(final Bookmark recordedBookmark) {
        return Optional.ofNullable(mappingByRecordedBookmark.get(recordedBookmark))
                .map(Mapping::actualBookmark);
    }

    @Override
    public void onReplayResult(
            final Bookmark recordedResult,
            final Bookmark actualResult,
            final UUID interactionId) {
        var proposed = new Mapping(actualResult, interactionId);
        var existing = mappingByRecordedBookmark.putIfAbsent(recordedResult, proposed);
        if (existing == null || existing.actualBookmark().equals(actualResult)) {
            return;
        }

        var message = "Recorded result bookmark '%s' was already mapped to actual bookmark '%s', cannot map to '%s'"
                .formatted(recordedResult, existing.actualBookmark(), actualResult);
        if (onConflictPolicy == OnConflictPolicy.LOG_AND_CONTINUE) {
            log.error(message);
            return;
        }
        throw new IllegalStateException(message);
    }

    Mapping mappingFor(final Bookmark recordedBookmark) {
        return mappingByRecordedBookmark.get(recordedBookmark);
    }

    record Mapping(Bookmark actualBookmark, UUID commandInteractionId) {
    }

    @Configuration(proxyBeanMethods = false)
    public static class BeanFactory {

        @Bean
        @ConditionalOnMissingBean(CommandReplayMappingListener.class)
        @ConditionalOnProperty(
                prefix = "causeway.extensions.command-log.replay-result-mapping",
                name = "storage-strategy",
                havingValue = "IN_MEMORY",
                matchIfMissing = true)
        CommandReplayMappingListener commandReplayMappingListenerInMemory(
                final CausewayConfiguration causewayConfiguration) {
            return new CommandReplayMappingListenerInMemory(causewayConfiguration
                    .extensions()
                    .commandLog()
                    .replayResultMapping()
                    .onConflictPolicy());
        }
    }
}
