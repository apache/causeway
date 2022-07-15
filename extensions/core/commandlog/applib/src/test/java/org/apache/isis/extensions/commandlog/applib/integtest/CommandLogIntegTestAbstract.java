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
package org.apache.isis.extensions.commandlog.applib.integtest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.isis.applib.mixins.system.DomainChangeRecord;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.isis.extensions.commandlog.applib.dom.ReplayState;
import org.apache.isis.extensions.commandlog.applib.integtest.model.Counter;
import org.apache.isis.extensions.commandlog.applib.integtest.model.CounterRepository;
import org.apache.isis.extensions.commandlog.applib.integtest.model.Counter_bumpUsingMixin;
import org.apache.isis.extensions.commandlog.applib.integtest.model.Counter_bumpUsingMixinWithCommandPublishingDisabled;
import org.apache.isis.schema.cmd.v2.ActionDto;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.val;

public abstract class CommandLogIntegTestAbstract extends IsisIntegrationTestAbstract {


    @BeforeAll
    static void beforeAll() {
        IsisPresets.forcePrototyping();
    }

    Counter counter;

    @BeforeEach
    void beforeEach() {
        counterRepository.removeAll();
        commandLogEntryRepository.removeAll();

        counter = createE1();

        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();
        assertThat(mostRecentCompleted).isEmpty();
    }

    private Counter createE1() {
        List<Counter> before = counterRepository.find();
        assertThat(before).isEmpty();

        val e1 = counterRepository.persist(newCounter());

        List<Counter> after = counterRepository.find();
        assertThat(after).hasSize(1);

        return e1;
    }

    protected abstract Counter newCounter();


    @Test
    void invoke_mixin() {

        // when
        wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter).act();
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();
        assertThat(mostRecentCompleted).isPresent();

        CommandLogEntry commandLogEntry = mostRecentCompleted.get();

        assertThat(commandLogEntry.getInteractionId()).isNotNull();
        assertThat(commandLogEntry.getCompletedAt()).isNotNull();
        assertThat(commandLogEntry.getDuration()).isNotNull();
        assertThat(commandLogEntry.getException()).isEqualTo("");
        assertThat(commandLogEntry.getLogicalMemberIdentifier()).isNotNull();
        assertThat(commandLogEntry.getLogicalMemberIdentifier()).isEqualTo("commandlog.test.Counter#bumpUsingMixin");
        assertThat(commandLogEntry.getUsername()).isEqualTo("__system");
        assertThat(commandLogEntry.getResult()).isNotNull();
        assertThat(commandLogEntry.getResultSummary()).isEqualTo("OK");
        assertThat(commandLogEntry.getReplayState()).isEqualTo(ReplayState.UNDEFINED);
        assertThat(commandLogEntry.getReplayStateFailureReason()).isNull();
        assertThat(commandLogEntry.getTarget()).isNotNull();
        assertThat(commandLogEntry.getTimestamp()).isNotNull();
        assertThat(commandLogEntry.getType()).isEqualTo(DomainChangeRecord.ChangeType.COMMAND);
        assertThat(commandLogEntry.getCommandDto()).isNotNull();
        CommandDto commandDto = commandLogEntry.getCommandDto();
        assertThat(commandDto).isNotNull();
        assertThat(commandDto.getMember()).isInstanceOf(ActionDto.class);
        assertThat(commandDto.getMember().getLogicalMemberIdentifier()).isEqualTo(commandLogEntry.getLogicalMemberIdentifier());
    }

    @Test
    void invoke_direct() {

        // when
        wrapperFactory.wrap(counter).bumpUsingDeclaredAction();
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();
        assertThat(mostRecentCompleted).isPresent();

        CommandLogEntry commandLogEntry = mostRecentCompleted.get();

        assertThat(commandLogEntry.getInteractionId()).isNotNull();
        assertThat(commandLogEntry.getCompletedAt()).isNotNull();
        assertThat(commandLogEntry.getDuration()).isNotNull();
        assertThat(commandLogEntry.getException()).isEqualTo("");
        assertThat(commandLogEntry.getLogicalMemberIdentifier()).isNotNull();
        assertThat(commandLogEntry.getLogicalMemberIdentifier()).isEqualTo("commandlog.test.Counter#bumpUsingDeclaredAction");
        assertThat(commandLogEntry.getUsername()).isEqualTo("__system");
        assertThat(commandLogEntry.getResult()).isNotNull();
        assertThat(commandLogEntry.getResultSummary()).isEqualTo("OK");
        assertThat(commandLogEntry.getReplayState()).isEqualTo(ReplayState.UNDEFINED);
        assertThat(commandLogEntry.getReplayStateFailureReason()).isNull();
        assertThat(commandLogEntry.getTarget()).isNotNull();
        assertThat(commandLogEntry.getTimestamp()).isNotNull();
        assertThat(commandLogEntry.getType()).isEqualTo(DomainChangeRecord.ChangeType.COMMAND);
        assertThat(commandLogEntry.getCommandDto()).isNotNull();
        CommandDto commandDto = commandLogEntry.getCommandDto();
        assertThat(commandDto).isNotNull();
        assertThat(commandDto.getMember()).isInstanceOf(ActionDto.class);
        assertThat(commandDto.getMember().getLogicalMemberIdentifier()).isEqualTo(commandLogEntry.getLogicalMemberIdentifier());
    }

    @Test
    void invoke_mixin_disabled() {

        // when
        wrapperFactory.wrapMixin(Counter_bumpUsingMixinWithCommandPublishingDisabled.class, counter).act();
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();
        assertThat(mostRecentCompleted).isEmpty();
    }

    @Test
    void invoke_direct_disabled() {

        // when
        wrapperFactory.wrap(counter).bumpUsingDeclaredActionWithCommandPublishingDisabled();
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();
        assertThat(mostRecentCompleted).isEmpty();
    }



    @Test
    void edit() {

        // when
        wrapperFactory.wrap(counter).setNum(99L);
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();
        assertThat(mostRecentCompleted).isPresent();

        CommandLogEntry commandLogEntry = mostRecentCompleted.get();

        assertThat(commandLogEntry.getInteractionId()).isNotNull();
        assertThat(commandLogEntry.getCompletedAt()).isNotNull();
        assertThat(commandLogEntry.getDuration()).isNotNull();
        assertThat(commandLogEntry.getException()).isEqualTo("");
        assertThat(commandLogEntry.getLogicalMemberIdentifier()).isNotNull();
        assertThat(commandLogEntry.getLogicalMemberIdentifier()).isEqualTo("commandlog.test.Counter#num");
        assertThat(commandLogEntry.getUsername()).isEqualTo("__system");
        assertThat(commandLogEntry.getResult()).isNull();
        assertThat(commandLogEntry.getResultSummary()).isEqualTo("OK (VOID)");
        assertThat(commandLogEntry.getReplayState()).isEqualTo(ReplayState.UNDEFINED);
        assertThat(commandLogEntry.getReplayStateFailureReason()).isNull();
        assertThat(commandLogEntry.getTarget()).isNotNull();
        assertThat(commandLogEntry.getTimestamp()).isNotNull();
        assertThat(commandLogEntry.getType()).isEqualTo(DomainChangeRecord.ChangeType.COMMAND);
        CommandDto commandDto = commandLogEntry.getCommandDto();
        assertThat(commandDto).isNotNull();
        assertThat(commandDto.getMember()).isInstanceOf(PropertyDto.class);
    }

    @Test
    void edit_disabled() {

        // when
        wrapperFactory.wrap(counter).setNum2(99L);
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();
        assertThat(mostRecentCompleted).isEmpty();
    }


    @Test
    void roundtrip_CLE_bookmarks() {

        // given
        wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter).act();
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();
        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();

        CommandLogEntry commandLogEntry = mostRecentCompleted.get();
        CommandDto commandDto = commandLogEntry.getCommandDto();

        // when
        Optional<Bookmark> cleBookmarkIfAny = bookmarkService.bookmarkFor(commandLogEntry);

        // then
        assertThat(cleBookmarkIfAny).isPresent();
        Bookmark cleBookmark = cleBookmarkIfAny.get();
        String identifier = cleBookmark.getIdentifier();
        assertThat(identifier).startsWith("u_");
        UUID.fromString(identifier.substring(2)); // should not fail, ie check the format is as we expect

        // when we start a new session and lookup from the bookmark
        interactionService.closeInteractionLayers();
        interactionService.openInteraction();

        Optional<Object> cle2IfAny = bookmarkService.lookup(cleBookmarkIfAny.get());
        assertThat(cle2IfAny).isPresent();

        CommandLogEntry cle2 = (CommandLogEntry) cle2IfAny.get();
        CommandDto commandDto2 = cle2.getCommandDto();

        assertThat(commandDto2).isEqualTo(commandDto);


    }

    @Inject CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
    @Inject InteractionService interactionService;
    @Inject CounterRepository counterRepository;
    @Inject WrapperFactory wrapperFactory;
    @Inject BookmarkService bookmarkService;
    @Inject MetaModelService metaModelService;

}
