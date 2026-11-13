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

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Import;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMappingRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CommandReplayResultMapping_deleteTest {

    @Test
    void actionRequiresConfirmationAndRemovesOnlyItsMapping() {
        var mapping = mock(CommandReplayResultMapping.class);
        var repository = mock(CommandReplayResultMappingRepository.class);
        var mixin = new CommandReplayResultMapping_delete(mapping);
        mixin.repository = repository;

        mixin.act();

        verify(repository).remove(mapping);
        var action = CommandReplayResultMapping_delete.class.getAnnotation(Action.class);
        assertThat(action.semantics()).isEqualTo(SemanticsOf.IDEMPOTENT_ARE_YOU_SURE);
        assertThat(action.commandPublishing()).isEqualTo(Publishing.DISABLED);
        assertThat(action.executionPublishing()).isEqualTo(Publishing.DISABLED);
    }

    @Test
    void moduleAndLayoutExposeDeleteAndAuditFields() throws Exception {
        var imports = CausewayModuleExtCommandLogApplib.class.getAnnotation(Import.class);
        assertThat(Arrays.asList(imports.value())).contains(
                CommandReplayResultMapping_delete.class,
                CommandReplayResultMapping.TableColumnOrderDefault.class);

        try (var stream = getClass().getResourceAsStream(
                "/org/apache/causeway/extensions/commandlog/applib/dom/CommandReplayResultMapping.layout.fallback.xml")) {
            assertThat(stream).isNotNull();
            var layout = new String(stream.readAllBytes());
            assertThat(layout).contains("<cpt:action id=\"delete\" position=\"PANEL\"/>");
            assertThat(layout).contains("<cpt:property id=\"recordedBookmark\"/>");
            assertThat(layout).contains("<cpt:property id=\"actualBookmark\"/>");
            assertThat(layout).contains("<cpt:property id=\"commandInteractionId\"/>");
        }
    }
}
