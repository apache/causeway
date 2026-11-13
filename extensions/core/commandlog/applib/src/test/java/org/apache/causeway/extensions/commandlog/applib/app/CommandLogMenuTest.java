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
package org.apache.causeway.extensions.commandlog.applib.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.clock.VirtualClock;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMappingRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandManager;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandLogMenuTest {

    @Test
    void finderActionsReturnExpectedMappings() {
        var repository = new FakeRepository();
        var identity = repository.createAndPersist(bookmark("1"), bookmark("1"), null);
        var changed = repository.createAndPersist(bookmark("2"), bookmark("9"), null);
        var alsoNine = repository.createAndPersist(bookmark("3"), bookmark("9"), null);
        var menu = menu(Optional.of(repository), mock(MessageService.class));

        assertThat(new ArrayList<Object>(menu.new findReplayResultMappings().act()))
                .containsExactly(identity, changed, alsoNine);
        assertThat(new ArrayList<Object>(menu.new findChangedReplayResultMappings().act()))
                .containsExactly(changed, alsoNine);
        assertThat(new ArrayList<Object>(menu.new findReplayResultMappingByRecordedBookmark().act(bookmark("2"))))
                .containsExactly(changed);
        assertThat(menu.new findReplayResultMappingByRecordedBookmark().act(bookmark("missing"))).isEmpty();
        assertThat(new ArrayList<Object>(menu.new findReplayResultMappingsByActualBookmark().act(bookmark("9"))))
                .containsExactly(changed, alsoNine);
    }

    @Test
    void mappingActionsHideWithoutRepository() {
        var menu = menu(Optional.empty(), mock(MessageService.class));

        assertThat(menu.new findReplayResultMappings().hideAct()).isTrue();
        assertThat(menu.new findChangedReplayResultMappings().hideAct()).isTrue();
        assertThat(menu.new findReplayResultMappingByRecordedBookmark().hideAct()).isTrue();
        assertThat(menu.new findReplayResultMappingsByActualBookmark().hideAct()).isTrue();
        assertThat(menu.new deleteReplayResultMappings().hideAct()).isTrue();
    }

    @Test
    void bulkDeleteIsIdempotentAndReportsCount() {
        var repository = new FakeRepository();
        repository.createAndPersist(bookmark("1"), bookmark("2"), null);
        repository.createAndPersist(bookmark("3"), bookmark("4"), null);
        var messages = mock(MessageService.class);
        var menu = menu(Optional.of(repository), messages);

        menu.new deleteReplayResultMappings().act();

        assertThat(repository.findAll()).isEmpty();
        verify(messages).informUser("Deleted 2 command replay result mappings");

        menu.new deleteReplayResultMappings().act();
        verify(messages).informUser("Deleted 0 command replay result mappings");
        assertThat(CommandLogMenu.deleteReplayResultMappings.class.getAnnotation(Action.class).semantics())
                .isEqualTo(SemanticsOf.IDEMPOTENT_ARE_YOU_SURE);
    }

    @Test
    void mappingActionsFollowReplayWorkflowOrder() {
        assertThat(sequenceOf(CommandLogMenu.commandManager.class))
                .isLessThan(sequenceOf(CommandLogMenu.exportManager.class));
        assertThat(sequenceOf(CommandLogMenu.exportManager.class))
                .isLessThan(sequenceOf(CommandLogMenu.replayManager.class));
        assertThat(sequenceOf(CommandLogMenu.replayManager.class))
                .isLessThan(sequenceOf(CommandLogMenu.findReplayResultMappings.class));
        assertThat(sequenceOf(CommandLogMenu.findReplayResultMappings.class))
                .isLessThan(sequenceOf(CommandLogMenu.findChangedReplayResultMappings.class));
        assertThat(sequenceOf(CommandLogMenu.findChangedReplayResultMappings.class))
                .isLessThan(sequenceOf(CommandLogMenu.findReplayResultMappingByRecordedBookmark.class));
        assertThat(sequenceOf(CommandLogMenu.findReplayResultMappingByRecordedBookmark.class))
                .isLessThan(sequenceOf(CommandLogMenu.findReplayResultMappingsByActualBookmark.class));
        assertThat(sequenceOf(CommandLogMenu.findReplayResultMappingsByActualBookmark.class))
                .isLessThan(sequenceOf(CommandLogMenu.deleteReplayResultMappings.class));
    }

    @Test
    void unifiedManagerIsPrimaryAndUsesCurrentHourDefaults() {
        var now = Instant.parse("2026-08-06T10:37:42Z");
        var clockService = mock(ClockService.class);
        var clock = mock(VirtualClock.class);
        when(clock.nowAsJavaSqlTimestamp()).thenReturn(Timestamp.from(now));
        when(clockService.getClock()).thenReturn(clock);
        var context = new ReplayContext(null, null, null, mock(CommandLogEntryRepository.class),
                null, clockService, null, null, null);
        var menu = new CommandLogMenu(
                mock(CommandLogEntryRepository.class), Optional.empty(), clockService, context,
                mock(MessageService.class));
        var action = menu.new commandManager();

        assertThat(action.defaultBaseline()).isEqualTo(Timestamp.from(Instant.parse("2026-08-06T10:00:00Z")));
        var manager = action.act(action.defaultBaseline());
        assertThat(manager.getBaseline()).isEqualTo(action.defaultBaseline());
        // the standard menu opens the manager at the maximum page limit
        assertThat(manager.getLimit()).isEqualTo(CommandManager.MAX_LIMIT);
        assertThat(menu.new exportManager().hideAct()).isTrue();
        assertThat(menu.new replayManager().hideAct()).isTrue();
    }

    private static int sequenceOf(final Class<?> actionClass) {
        return Integer.parseInt(actionClass.getAnnotation(ActionLayout.class).sequence());
    }

    private static CommandLogMenu menu(
            final Optional<CommandReplayResultMappingRepository> repository,
            final MessageService messages) {
        return new CommandLogMenu(
                mock(CommandLogEntryRepository.class),
                repository,
                mock(ClockService.class),
                null,
                messages);
    }

    private static Bookmark bookmark(final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", id);
    }

    static final class FakeRepository implements CommandReplayResultMappingRepository {
        private final Map<Bookmark, Mapping> mappings = new LinkedHashMap<>();

        @Override public Optional<CommandReplayResultMapping> findByRecordedBookmark(final Bookmark bookmark) {
            return Optional.ofNullable(mappings.get(bookmark));
        }
        @Override public List<? extends CommandReplayResultMapping> findByActualBookmark(final Bookmark bookmark) {
            return mappings.values().stream().filter(mapping -> mapping.getActualBookmark().equals(bookmark)).toList();
        }
        @Override public List<? extends CommandReplayResultMapping> findChanged() {
            return mappings.values().stream()
                    .filter(mapping -> !mapping.getRecordedBookmark().equals(mapping.getActualBookmark())).toList();
        }
        @Override public List<? extends CommandReplayResultMapping> findAll() { return List.copyOf(mappings.values()); }
        @Override public CommandReplayResultMapping createAndPersist(
                final Bookmark recorded, final Bookmark actual, final @Nullable UUID interactionId) {
            var mapping = new Mapping();
            mapping.init(recorded, actual, interactionId);
            mappings.put(recorded, mapping);
            return mapping;
        }
        @Override public void remove(final CommandReplayResultMapping mapping) {
            mappings.remove(mapping.getRecordedBookmark());
        }
        @Override public void removeAll() { mappings.clear(); }
    }

    static final class Mapping implements CommandReplayResultMapping {
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
