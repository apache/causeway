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

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.ReplayResultMapping.OnConflictPolicy;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMappingRepository;

import lombok.extern.log4j.Log4j2;

/**
 * Persistent implementation of {@link CommandReplayMappingListener}.
 *
 * @since 2.1 {@index}
 */
@Log4j2
public class CommandReplayMappingListenerPersistent implements CommandReplayMappingListener {

    private final CommandReplayResultMappingRepository commandReplayResultMappingRepository;
    private final OnConflictPolicy onConflictPolicy;

    public CommandReplayMappingListenerPersistent(
            final CommandReplayResultMappingRepository commandReplayResultMappingRepository,
            final OnConflictPolicy onConflictPolicy) {
        this.commandReplayResultMappingRepository = commandReplayResultMappingRepository;
        this.onConflictPolicy = onConflictPolicy;
    }

    @Override
    public Optional<Bookmark> lookup(
            final CommandLogEntry commandLogEntry,
            final Bookmark recordedBookmark) {
        return commandReplayResultMappingRepository.findByRecordedBookmark(recordedBookmark)
                .map(CommandReplayResultMapping::getActualBookmark);
    }

    @Override
    public void onReplayResult(
            final Bookmark recordedResult,
            final Bookmark actualResult,
            final CommandLogEntry commandLogEntry) {
        final Optional<CommandReplayResultMapping> existingMappingIfAny =
                commandReplayResultMappingRepository.findByRecordedBookmark(recordedResult);
        if(existingMappingIfAny.isEmpty()) {
            commandReplayResultMappingRepository.createAndPersist(recordedResult, actualResult);
            return;
        }
        final Bookmark existingActualResult = existingMappingIfAny.get().getActualBookmark();
        if(!existingActualResult.equals(actualResult)) {
            final String message = String.format(
                    "Recorded result bookmark '%s' was already mapped to actual bookmark '%s', cannot map to '%s'",
                    recordedResult,
                    existingActualResult,
                    actualResult);
            if(onConflictPolicy == OnConflictPolicy.LOG_AND_CONTINUE) {
                log.error(message);
                return;
            }
            throw new IllegalStateException(message);
        }
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
                final CommandReplayResultMappingRepository commandReplayResultMappingRepository,
                final CausewayConfiguration causewayConfiguration) {
            return new CommandReplayMappingListenerPersistent(
                    commandReplayResultMappingRepository,
                    causewayConfiguration
                            .getExtensions()
                            .getCommandLog()
                            .getReplayResultMapping()
                            .getOnConflictPolicy());
        }

    }

}
