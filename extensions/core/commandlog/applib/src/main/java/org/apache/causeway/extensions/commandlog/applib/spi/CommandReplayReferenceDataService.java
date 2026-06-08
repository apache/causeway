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

import java.util.List;
import java.util.Objects;

import org.apache.causeway.applib.services.bookmark.Bookmark;

/**
 * Classifies command replay/export participants that are stable reference data.
 *
 * <p>Implementations must only return {@code true} for bookmarked entities whose logical type and identifier are
 * expected to be stable, well-known, and present in every replay environment.</p>
 *
 * <p>Applications may register multiple implementations. A bookmark is considered reference data when any registered
 * implementation accepts it.</p>
 */
@FunctionalInterface
public interface CommandReplayReferenceDataService {

    boolean isReferenceData(Bookmark bookmark);

    static boolean isReferenceData(
            final List<? extends CommandReplayReferenceDataService> services,
            final Bookmark bookmark) {
        return services != null
                && bookmark != null
                && services.stream()
                .filter(Objects::nonNull)
                .anyMatch(service -> service.isReferenceData(bookmark));
    }
}
