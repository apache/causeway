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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

/**
 * Default in-memory implementation of {@link CommandReplayMappingListener}.
 *
 * @since 2.1 {@index}
 */
public class CommandReplayMappingListenerDefault implements CommandReplayMappingListener {

    private final Map<Bookmark, Bookmark> actualBookmarkByRecordedBookmark = new HashMap<>();

    @Override
    public Optional<Bookmark> remap(
            final CommandLogEntry commandLogEntry,
            final Bookmark recordedBookmark) {
        return Optional.ofNullable(actualBookmarkByRecordedBookmark.get(recordedBookmark));
    }

    @Override
    public void onReplayResultMapped(
            final Bookmark recordedResult,
            final Bookmark actualResult,
            final CommandLogEntry commandLogEntry) {
        if(!recordedResult.equals(actualResult)) {
            actualBookmarkByRecordedBookmark.put(recordedResult, actualResult);
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class BeanFactory {

        @Bean
        @ConditionalOnMissingBean(CommandReplayMappingListener.class)
        CommandReplayMappingListener commandReplayMappingListenerDefault() {
            return new CommandReplayMappingListenerDefault();
        }

    }

}
