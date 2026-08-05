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

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.ReplayResultMapping.OnConflictPolicy;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMappingRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Built-in replay mapping listener backed by commandlog persistence.
 *
 * @since 4.0 {@index}
 */
@Slf4j
public final class CommandReplayMappingListenerPersistent implements CommandReplayMappingListener {

    private final CommandReplayResultMappingRepository repository;
    private final OnConflictPolicy onConflictPolicy;

    public CommandReplayMappingListenerPersistent(
            final CommandReplayResultMappingRepository repository,
            final OnConflictPolicy onConflictPolicy) {
        this.repository = repository;
        this.onConflictPolicy = onConflictPolicy;
    }

    @Override
    public Optional<Bookmark> lookup(final Bookmark recordedBookmark) {
        return repository.findByRecordedBookmark(recordedBookmark)
                .map(CommandReplayResultMapping::getActualBookmark);
    }

    @Override
    public void onReplayResult(
            final Bookmark recordedResult,
            final Bookmark actualResult,
            final UUID interactionId) {
        var existing = repository.findByRecordedBookmark(recordedResult);
        if (existing.isEmpty()) {
            repository.createAndPersist(recordedResult, actualResult, interactionId);
            return;
        }

        var existingActualResult = existing.orElseThrow().getActualBookmark();
        if (existingActualResult.equals(actualResult)) {
            return;
        }

        var message = "Recorded result bookmark '%s' was already mapped to actual bookmark '%s', cannot map to '%s'"
                .formatted(recordedResult, existingActualResult, actualResult);
        if (onConflictPolicy == OnConflictPolicy.LOG_AND_CONTINUE) {
            log.error(message);
            return;
        }
        throw new IllegalStateException(message);
    }

    @Configuration(proxyBeanMethods = false)
    public static class BeanFactory {

        @Bean
        @ConditionalOnBean(CommandReplayResultMappingRepository.class)
        @ConditionalOnMissingBean(CommandReplayMappingListener.class)
        @ConditionalOnProperty(
                prefix = "causeway.extensions.command-log.replay-result-mapping",
                name = "storage-strategy",
                havingValue = "PERSISTENT")
        CommandReplayMappingListener commandReplayMappingListenerPersistent(
                final CommandReplayResultMappingRepository repository,
                final CausewayConfiguration causewayConfiguration) {
            return new CommandReplayMappingListenerPersistent(
                    repository,
                    causewayConfiguration.extensions()
                            .commandLog()
                            .replayResultMapping()
                            .onConflictPolicy());
        }
    }
}
