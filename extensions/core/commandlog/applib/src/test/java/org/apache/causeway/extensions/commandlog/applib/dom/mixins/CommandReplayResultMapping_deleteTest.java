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
package org.apache.causeway.extensions.commandlog.applib.dom.mixins;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping;

class CommandReplayResultMapping_deleteTest {

    @Test
    void action_semantics_require_are_you_sure_and_disable_publishing() {
        Action action = CommandReplayResultMapping_delete.class.getAnnotation(Action.class);
        ActionLayout actionLayout = CommandReplayResultMapping_delete.class.getAnnotation(ActionLayout.class);

        assertThat(action.semantics()).isEqualTo(SemanticsOf.IDEMPOTENT_ARE_YOU_SURE);
        assertThat(action.commandPublishing()).isEqualTo(Publishing.DISABLED);
        assertThat(action.executionPublishing()).isEqualTo(Publishing.DISABLED);
        assertThat(action.domainEvent()).isEqualTo(CommandReplayResultMapping_delete.DomainEvent.class);
        assertThat(actionLayout.describedAs()).contains("cannot be undone");
    }

    @Test
    void act_removes_mixed_in_mapping() {
        FakeMapping mapping = new FakeMapping();
        RepositoryService repositoryService = mock(RepositoryService.class);
        CommandReplayResultMapping_delete mixin = new CommandReplayResultMapping_delete(mapping);
        mixin.repositoryService = repositoryService;

        mixin.act();

        verify(repositoryService).removeAndFlush(mapping);
    }

    @Test
    void command_log_applib_module_imports_delete_mixin() {
        Import importAnnotation = CausewayModuleExtCommandLogApplib.class.getAnnotation(Import.class);

        assertThat(Arrays.<Class<?>>asList(importAnnotation.value()))
                .contains(CommandReplayResultMapping_delete.class);
    }

    @Test
    void layout_places_delete_action() throws Exception {
        String layout = new String(CommandReplayResultMapping_deleteTest.class
                .getResourceAsStream("/org/apache/causeway/extensions/commandlog/applib/dom/CommandReplayResultMapping.layout.fallback.xml")
                .readAllBytes());

        assertThat(layout).contains("<cpt:action id=\"delete\" position=\"PANEL\"/>");
        assertThat(layout).contains("<cpt:property id=\"recordedBookmark\"/>");
        assertThat(layout).contains("<cpt:property id=\"actualBookmark\"/>");
        assertThat(layout).contains("<cpt:property id=\"commandInteractionId\"/>");
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
