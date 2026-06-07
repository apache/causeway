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

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

/**
 * Looks up replay command inputs before execution and listens for replay result bookmark observations.
 *
 * @since 2.1 {@index}
 */
public interface CommandReplayMappingListener {

    /**
     * Optionally looks up a recorded command target or reference-valued action parameter bookmark before replay execution.
     *
     * @param commandLogEntry the command log entry being replayed
     * @param recordedBookmark the target or reference parameter bookmark recorded in the imported command DTO
     * @return replacement bookmark, or empty to keep the recorded bookmark
     */
    default Optional<Bookmark> lookup(
            final CommandLogEntry commandLogEntry,
            final Bookmark recordedBookmark) {
        return Optional.empty();
    }

    /**
     * Notifies that replay observed an actual result bookmark for a command whose imported command log entry had a recorded result bookmark.
     *
     * @param recordedResult the result bookmark recorded during import
     * @param actualResult the result bookmark produced by replay execution
     * @param commandLogEntry the replayed command log entry
     */
    default void onReplayResult(
            Bookmark recordedResult,
            Bookmark actualResult,
            CommandLogEntry commandLogEntry) {}

}
