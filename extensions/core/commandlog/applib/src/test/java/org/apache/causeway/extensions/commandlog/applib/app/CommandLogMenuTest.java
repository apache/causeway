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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMappingRepository;

class CommandLogMenuTest {

    @Test
    void find_replay_result_mappings_returns_all_mappings() {
        FakeRepository repository = new FakeRepository();
        CommandReplayResultMapping identityMapping = repository.createAndPersist(bookmark("1"), bookmark("1"));
        CommandReplayResultMapping changedMapping = repository.createAndPersist(bookmark("2"), bookmark("3"));

        CommandLogMenu menu = menu(Optional.of(repository));

        assertThat(new ArrayList<Object>(menu.new findReplayResultMappings().act()))
                .containsExactly(identityMapping, changedMapping);
    }

    @Test
    void find_changed_replay_result_mappings_returns_only_changed_mappings() {
        FakeRepository repository = new FakeRepository();
        CommandReplayResultMapping identityMapping = repository.createAndPersist(bookmark("1"), bookmark("1"));
        CommandReplayResultMapping changedMapping = repository.createAndPersist(bookmark("2"), bookmark("3"));

        CommandLogMenu menu = menu(Optional.of(repository));

        assertThat(new ArrayList<Object>(menu.new findChangedReplayResultMappings().act()))
                .containsExactly(changedMapping)
                .doesNotContain(identityMapping);
    }

    @Test
    void find_replay_result_mapping_by_recorded_bookmark_returns_matching_mapping() {
        FakeRepository repository = new FakeRepository();
        CommandReplayResultMapping mapping = repository.createAndPersist(bookmark("1"), bookmark("2"));

        CommandLogMenu menu = menu(Optional.of(repository));

        assertThat(new ArrayList<Object>(menu.new findReplayResultMappingByRecordedBookmark().act(bookmark("1"))))
                .containsExactly(mapping);
    }

    @Test
    void find_replay_result_mapping_by_recorded_bookmark_returns_empty_list_when_missing() {
        FakeRepository repository = new FakeRepository();

        CommandLogMenu menu = menu(Optional.of(repository));

        assertThat(menu.new findReplayResultMappingByRecordedBookmark().act(bookmark("missing")))
                .isEmpty();
    }

    @Test
    void find_replay_result_mappings_by_actual_bookmark_returns_matching_mappings() {
        FakeRepository repository = new FakeRepository();
        CommandReplayResultMapping firstMapping = repository.createAndPersist(bookmark("1"), bookmark("9"));
        CommandReplayResultMapping secondMapping = repository.createAndPersist(bookmark("2"), bookmark("9"));
        repository.createAndPersist(bookmark("3"), bookmark("4"));

        CommandLogMenu menu = menu(Optional.of(repository));

        assertThat(new ArrayList<Object>(menu.new findReplayResultMappingsByActualBookmark().act(bookmark("9"))))
                .containsExactly(firstMapping, secondMapping);
    }

    @Test
    void delete_replay_result_mappings_removes_all_mappings() {
        FakeRepository repository = new FakeRepository();
        repository.createAndPersist(bookmark("1"), bookmark("2"));
        repository.createAndPersist(bookmark("3"), bookmark("4"));

        MessageService messageService = mock(MessageService.class);
        CommandLogMenu menu = menu(Optional.of(repository), messageService);

        menu.new deleteReplayResultMappings().act();

        assertThat(repository.findAll()).isEmpty();
        assertThat(repository.removeAllInvoked).isTrue();
        verify(messageService).informUser("Deleted 2 command replay result mappings");
    }

    @Test
    void replay_result_mapping_actions_are_hidden_when_repository_is_unavailable() {
        CommandLogMenu menu = menu(Optional.empty());

        assertThat(menu.new findReplayResultMappings().hideAct()).isTrue();
        assertThat(menu.new findChangedReplayResultMappings().hideAct()).isTrue();
        assertThat(menu.new findReplayResultMappingByRecordedBookmark().hideAct()).isTrue();
        assertThat(menu.new findReplayResultMappingsByActualBookmark().hideAct()).isTrue();
        assertThat(menu.new deleteReplayResultMappings().hideAct()).isTrue();
    }

    @Test
    void delete_replay_result_mappings_is_idempotent_are_you_sure() {
        assertThat(actionOn(CommandLogMenu.deleteReplayResultMappings.class).semantics())
                .isEqualTo(SemanticsOf.IDEMPOTENT_ARE_YOU_SURE);
    }

    @Test
    void replay_workflow_actions_are_ordered_by_layout_sequence() {
        assertThat(sequenceOf(CommandLogMenu.commandManager.class))
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

    private static Action actionOn(final Class<?> actionClass) {
        return actionClass.getAnnotation(Action.class);
    }

    private static int sequenceOf(final Class<?> actionClass) {
        return Integer.parseInt(actionClass.getAnnotation(ActionLayout.class).sequence());
    }

    private static CommandLogMenu menu(final Optional<CommandReplayResultMappingRepository> repository) {
        return menu(repository, mock(MessageService.class));
    }

    private static CommandLogMenu menu(
            final Optional<CommandReplayResultMappingRepository> repository,
            final MessageService messageService) {
        return new CommandLogMenu(
                mock(CommandLogEntryRepository.class),
                repository,
                mock(ClockService.class),
                null,
                messageService);
    }

    private static Bookmark bookmark(final String identifier) {
        return Bookmark.forLogicalTypeNameAndIdentifier("demoInvoice", identifier);
    }

    static class FakeRepository implements CommandReplayResultMappingRepository {

        private final Map<Bookmark, CommandReplayResultMapping> mappings = new LinkedHashMap<>();
        private boolean removeAllInvoked;

        @Override
        public Optional<CommandReplayResultMapping> findByRecordedBookmark(final Bookmark recordedBookmark) {
            return Optional.ofNullable(mappings.get(recordedBookmark));
        }

        @Override
        public List<? extends CommandReplayResultMapping> findByActualBookmark(final Bookmark actualBookmark) {
            return mappings.values().stream()
                    .filter(mapping -> mapping.getActualBookmark().equals(actualBookmark))
                    .collect(Collectors.toList());
        }

        @Override
        public List<? extends CommandReplayResultMapping> findChanged() {
            return mappings.values().stream()
                    .filter(mapping -> !mapping.getRecordedBookmark().equals(mapping.getActualBookmark()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<? extends CommandReplayResultMapping> findAll() {
            return new ArrayList<>(mappings.values());
        }

        @Override
        public void removeAll() {
            removeAllInvoked = true;
            mappings.clear();
        }

        @Override
        public CommandReplayResultMapping createAndPersist(
                final Bookmark recordedBookmark,
                final Bookmark actualBookmark,
                final UUID commandInteractionId) {
            FakeMapping mapping = new FakeMapping();
            mapping.init(recordedBookmark, actualBookmark, commandInteractionId);
            mappings.put(recordedBookmark, mapping);
            return mapping;
        }
    }

    static class FakeMapping extends CommandReplayResultMapping {

        private Bookmark recordedBookmark;
        private Bookmark actualBookmark;
        private UUID commandInteractionId;

        @Override
        public Bookmark getRecordedBookmark() {
            return recordedBookmark;
        }

        @Override
        public void setRecordedBookmark(final Bookmark recordedBookmark) {
            this.recordedBookmark = recordedBookmark;
        }

        @Override
        public Bookmark getActualBookmark() {
            return actualBookmark;
        }

        @Override
        public void setActualBookmark(final Bookmark actualBookmark) {
            this.actualBookmark = actualBookmark;
        }

        @Override
        public UUID getCommandInteractionId() {
            return commandInteractionId;
        }

        @Override
        public void setCommandInteractionId(final UUID commandInteractionId) {
            this.commandInteractionId = commandInteractionId;
        }
    }
}
