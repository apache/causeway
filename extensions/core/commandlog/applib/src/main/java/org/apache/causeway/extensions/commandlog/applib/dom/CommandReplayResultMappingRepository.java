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
package org.apache.causeway.extensions.commandlog.applib.dom;

import java.util.List;
import java.util.Optional;

import org.apache.causeway.applib.services.bookmark.Bookmark;

/**
 * Provides supporting functionality for querying and persisting replay result mappings.
 *
 * @since 2.1 {@index}
 */
public interface CommandReplayResultMappingRepository {

    Optional<CommandReplayResultMapping> findByRecordedBookmark(final Bookmark recordedBookmark);

    List<? extends CommandReplayResultMapping> findByActualBookmark(final Bookmark actualBookmark);

    List<? extends CommandReplayResultMapping> findChanged();

    List<? extends CommandReplayResultMapping> findAll();

    CommandReplayResultMapping createAndPersist(final Bookmark recordedBookmark, final Bookmark actualBookmark);

}
