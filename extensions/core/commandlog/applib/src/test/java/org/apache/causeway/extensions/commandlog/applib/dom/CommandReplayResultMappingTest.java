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

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;

import static org.assertj.core.api.Assertions.assertThat;

class CommandReplayResultMappingTest {

    @Test
    void initializesAndTitlesMapping() {
        var mapping = new Mapping();
        var interactionId = UUID.randomUUID();

        mapping.init(bookmark("1"), bookmark("2"), interactionId);

        assertThat(mapping.getRecordedBookmark()).isEqualTo(bookmark("1"));
        assertThat(mapping.getActualBookmark()).isEqualTo(bookmark("2"));
        assertThat(mapping.getCommandInteractionId()).isEqualTo(interactionId);
        assertThat(mapping.title()).isEqualTo("demo.Invoice:1 → demo.Invoice:2");
    }

    @Test
    void permitsMissingInteractionId() {
        var mapping = new Mapping();

        mapping.init(bookmark("1"), bookmark("1"), null);

        assertThat(mapping.getCommandInteractionId()).isNull();
        assertThat(mapping.title()).isEqualTo("demo.Invoice:1 → demo.Invoice:1");
    }

    private static Bookmark bookmark(final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", id);
    }

    private static final class Mapping implements CommandReplayResultMapping {
        private Bookmark recordedBookmark;
        private Bookmark actualBookmark;
        private @Nullable UUID commandInteractionId;

        @Override public Bookmark getRecordedBookmark() { return recordedBookmark; }
        @Override public void setRecordedBookmark(final Bookmark value) { recordedBookmark = value; }
        @Override public Bookmark getActualBookmark() { return actualBookmark; }
        @Override public void setActualBookmark(final Bookmark value) { actualBookmark = value; }
        @Override public @Nullable UUID getCommandInteractionId() { return commandInteractionId; }
        @Override public void setCommandInteractionId(final @Nullable UUID value) { commandInteractionId = value; }
    }
}
