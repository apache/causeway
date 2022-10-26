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
package org.apache.causeway.extensions.executionoutbox.applib.integtest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.applib.mixins.system.DomainChangeRecord;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryRepository;
import org.apache.causeway.extensions.executionoutbox.applib.integtest.model.Counter;
import org.apache.causeway.extensions.executionoutbox.applib.integtest.model.CounterRepository;
import org.apache.causeway.extensions.executionoutbox.applib.integtest.model.Counter_bumpUsingMixin;
import org.apache.causeway.extensions.executionoutbox.applib.integtest.model.Counter_bumpUsingMixinWithExecutionPublishingDisabled;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;
import org.apache.causeway.schema.ixn.v2.InteractionDto;
import org.apache.causeway.schema.ixn.v2.PropertyEditDto;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

public abstract class ExecutionOutbox_IntegTestAbstract extends CausewayIntegrationTestAbstract {


    @BeforeAll
    static void beforeAll() {
        CausewayPresets.forcePrototyping();
    }

    Counter counter1;
    Counter counter2;

    @BeforeEach
    void beforeEach() {
        counterRepository.removeAll();
        executionOutboxEntryRepository.removeAll();

        assertThat(counterRepository.find()).isEmpty();

        counter1 = counterRepository.persist(newCounter("counter-1"));
        counter2 = counterRepository.persist(newCounter("counter-2"));

        assertThat(counterRepository.find()).hasSize(2);

        List<? extends ExecutionOutboxEntry> all = executionOutboxEntryRepository.findOldest();
        assertThat(all).isEmpty();
    }

    protected abstract Counter newCounter(String name);


    @Test
    void invoke_mixin() {
        counter1 = counterRepository.findByName("counter-1");
        // when
        wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        List<? extends ExecutionOutboxEntry> all = executionOutboxEntryRepository.findOldest();
        assertThat(all).hasSize(1);

        ExecutionOutboxEntry executionOutboxEntry = all.get(0);

        assertThat(executionOutboxEntry.getInteractionId()).isNotNull();
        assertThat(executionOutboxEntry.getCompletedAt()).isNotNull();
        assertThat(executionOutboxEntry.getDuration()).isNotNull();
        assertThat(executionOutboxEntry.getLogicalMemberIdentifier()).isNotNull();
        assertThat(executionOutboxEntry.getLogicalMemberIdentifier()).isEqualTo("executionoutbox.test.Counter#bumpUsingMixin");
        assertThat(executionOutboxEntry.getUsername()).isEqualTo("__system");
        assertThat(executionOutboxEntry.getTarget()).isNotNull();
        assertThat(executionOutboxEntry.getTimestamp()).isNotNull();
        assertThat(executionOutboxEntry.getType()).isEqualTo(DomainChangeRecord.ChangeType.EXECUTION);
        assertThat(executionOutboxEntry.getInteractionDto()).isNotNull();
        InteractionDto interactionDto = executionOutboxEntry.getInteractionDto();
        assertThat(interactionDto).isNotNull();
        assertThat(interactionDto.getExecution()).isInstanceOf(ActionInvocationDto.class);
        assertThat(interactionDto.getExecution().getLogicalMemberIdentifier()).isEqualTo(executionOutboxEntry.getLogicalMemberIdentifier());
    }

    @Test
    void invoke_direct() {

        // when
        wrapperFactory.wrap(counter1).bumpUsingDeclaredAction();
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        List<? extends ExecutionOutboxEntry> all = executionOutboxEntryRepository.findOldest();
        assertThat(all).hasSize(1);

        ExecutionOutboxEntry executionLogEntry = all.get(0);

        assertThat(executionLogEntry.getInteractionId()).isNotNull();
        assertThat(executionLogEntry.getCompletedAt()).isNotNull();
        assertThat(executionLogEntry.getDuration()).isNotNull();
        assertThat(executionLogEntry.getLogicalMemberIdentifier()).isNotNull();
        assertThat(executionLogEntry.getLogicalMemberIdentifier()).isEqualTo("executionoutbox.test.Counter#bumpUsingDeclaredAction");
        assertThat(executionLogEntry.getUsername()).isEqualTo("__system");
        assertThat(executionLogEntry.getTarget()).isNotNull();
        assertThat(executionLogEntry.getTimestamp()).isNotNull();
        assertThat(executionLogEntry.getType()).isEqualTo(DomainChangeRecord.ChangeType.EXECUTION);
        assertThat(executionLogEntry.getInteractionDto()).isNotNull();
        InteractionDto interactionDto = executionLogEntry.getInteractionDto();
        assertThat(interactionDto).isNotNull();
        assertThat(interactionDto.getExecution()).isInstanceOf(ActionInvocationDto.class);
        assertThat(interactionDto.getExecution().getLogicalMemberIdentifier()).isEqualTo(executionLogEntry.getLogicalMemberIdentifier());
    }

    @Test
    void invoke_mixin_disabled() {

        // when
        wrapperFactory.wrapMixin(Counter_bumpUsingMixinWithExecutionPublishingDisabled.class, counter1).act();
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        List<? extends ExecutionOutboxEntry> all = executionOutboxEntryRepository.findOldest();
        assertThat(all).isEmpty();
    }

    @Test
    void invoke_direct_disabled() {

        // when
        wrapperFactory.wrap(counter1).bumpUsingDeclaredActionWithExecutionPublishingDisabled();
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        List<? extends ExecutionOutboxEntry> all = executionOutboxEntryRepository.findOldest();
        assertThat(all).isEmpty();
    }



    @Test
    void edit() {

        // when
        wrapperFactory.wrap(counter1).setNum(99L);
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        List<? extends ExecutionOutboxEntry> all = executionOutboxEntryRepository.findOldest();
        assertThat(all).hasSize(1);

        ExecutionOutboxEntry executionLogEntry = all.get(0);

        assertThat(executionLogEntry.getInteractionId()).isNotNull();
        assertThat(executionLogEntry.getCompletedAt()).isNotNull();
        assertThat(executionLogEntry.getDuration()).isNotNull();
        assertThat(executionLogEntry.getLogicalMemberIdentifier()).isNotNull();
        assertThat(executionLogEntry.getLogicalMemberIdentifier()).isEqualTo("executionoutbox.test.Counter#num");
        assertThat(executionLogEntry.getUsername()).isEqualTo("__system");
        assertThat(executionLogEntry.getTarget()).isNotNull();
        assertThat(executionLogEntry.getTimestamp()).isNotNull();
        assertThat(executionLogEntry.getType()).isEqualTo(DomainChangeRecord.ChangeType.EXECUTION);
        InteractionDto interactionDto = executionLogEntry.getInteractionDto();
        assertThat(interactionDto).isNotNull();
        assertThat(interactionDto.getExecution()).isInstanceOf(PropertyEditDto.class);
        assertThat(interactionDto.getExecution().getLogicalMemberIdentifier()).isEqualTo(executionLogEntry.getLogicalMemberIdentifier());
    }

    @Test
    void edit_disabled() {

        // when
        wrapperFactory.wrap(counter1).setNum2(99L);
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        List<? extends ExecutionOutboxEntry> all = executionOutboxEntryRepository.findOldest();
        assertThat(all).isEmpty();
    }


    @Test
    void roundtrip_EOE_bookmarks() {

        // given
        wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();
        List<? extends ExecutionOutboxEntry> all = executionOutboxEntryRepository.findOldest();

        ExecutionOutboxEntry executionLogEntry = all.get(0);
        InteractionDto interactionDto = executionLogEntry.getInteractionDto();

        // when
        Optional<Bookmark> eleBookmarkIfAny = bookmarkService.bookmarkFor(executionLogEntry);

        // then
        assertThat(eleBookmarkIfAny).isPresent();
        Bookmark eleBookmark = eleBookmarkIfAny.get();
        String identifier = eleBookmark.getIdentifier();
        UUID.fromString(identifier.substring(0, identifier.indexOf("_"))); // should not fail, ie check the format is as we expect
        Integer.parseInt(identifier.substring(identifier.indexOf("_")+1)); // should not fail, ie check the format is as we expect

        // when we start a new session and lookup from the bookmark
        interactionService.closeInteractionLayers();
        interactionService.openInteraction();

        Optional<Object> cle2IfAny = bookmarkService.lookup(eleBookmarkIfAny.get());
        assertThat(cle2IfAny).isPresent();

        ExecutionOutboxEntry ele2 = (ExecutionOutboxEntry) cle2IfAny.get();
        InteractionDto interactionDto2 = ele2.getInteractionDto();

        assertThat(interactionDto2).isEqualTo(interactionDto);
    }

    @Test
    void test_all_the_repository_methods() {

        // given
        sudoService.run(InteractionContext.switchUser(UserMemento.builder().name("user-1").build()), () -> {
            wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
        });
        interactionService.closeInteractionLayers();    // to flush
        interactionService.openInteraction();

        // when
        List<? extends ExecutionOutboxEntry> executionTarget1User1IfAny = executionOutboxEntryRepository.findOldest();

        // then
        assertThat(executionTarget1User1IfAny).hasSize(1);
        var executionTarget1User1 = executionTarget1User1IfAny.get(0);
        val executionTarget1User1Id = executionTarget1User1.getInteractionId();

        // when
        Optional<? extends ExecutionOutboxEntry> executionTarget1User1ById = executionOutboxEntryRepository.findByInteractionIdAndSequence(executionTarget1User1Id, 0);

        // then
        assertThat(executionTarget1User1ById).isPresent();
        assertThat(executionTarget1User1ById.get()).isSameAs(executionTarget1User1);

    }

    @Inject ExecutionOutboxEntryRepository<? extends ExecutionOutboxEntry> executionOutboxEntryRepository;
    @Inject SudoService sudoService;
    @Inject ClockService clockService;
    @Inject InteractionService interactionService;
    @Inject CounterRepository counterRepository;
    @Inject WrapperFactory wrapperFactory;
    @Inject BookmarkService bookmarkService;

}
