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
package org.apache.causeway.extensions.executionlog.applib.integtest;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.applib.clock.VirtualClock;
import org.apache.causeway.applib.mixins.system.DomainChangeRecord;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;
import org.apache.causeway.extensions.executionlog.applib.integtest.model.Counter;
import org.apache.causeway.extensions.executionlog.applib.integtest.model.CounterRepository;
import org.apache.causeway.extensions.executionlog.applib.integtest.model.Counter_bumpUsingMixin;
import org.apache.causeway.extensions.executionlog.applib.integtest.model.Counter_bumpUsingMixinWithExecutionPublishingDisabled;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;
import org.apache.causeway.schema.ixn.v2.InteractionDto;
import org.apache.causeway.schema.ixn.v2.PropertyEditDto;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

public abstract class ExecutionLog_IntegTestAbstract extends CausewayIntegrationTestAbstract {

    @BeforeAll
    static void beforeAll() {
        CausewayPresets.forcePrototyping();
    }

    Counter counter1;
    Counter counter2;

    @BeforeEach
    void beforeEach() {

        counterRepository.removeAll();
        executionLogEntryRepository.removeAll();

        assertThat(counterRepository.find()).isEmpty();

        counter1 = counterRepository.persist(newCounter("counter-1"));
        counter2 = counterRepository.persist(newCounter("counter-2"));

        assertThat(counterRepository.find()).hasSize(2);

        List<? extends ExecutionLogEntry> all = executionLogEntryRepository.findMostRecent();
        assertThat(all).isEmpty();
    }

    protected abstract Counter newCounter(String name);


    @Test
    void invoke_mixin() {
        counter1 = counterRepository.findByName("counter-1");

        // when
        wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();

        // then
        List<? extends ExecutionLogEntry> all = executionLogEntryRepository.findMostRecent();
        assertThat(all).hasSize(1);

        ExecutionLogEntry executionLogEntry = all.get(0);

        assertThat(executionLogEntry.getInteractionId()).isNotNull();
        assertThat(executionLogEntry.getCompletedAt()).isNotNull();
        assertThat(executionLogEntry.getDuration()).isNotNull();
        assertThat(executionLogEntry.getLogicalMemberIdentifier()).isNotNull();
        assertThat(executionLogEntry.getLogicalMemberIdentifier()).isEqualTo("executionlog.test.Counter#bumpUsingMixin");
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
    void invoke_direct() {

        // when
        wrapperFactory.wrap(counter1).bumpUsingDeclaredAction();

        // then
        List<? extends ExecutionLogEntry> all = executionLogEntryRepository.findMostRecent();
        assertThat(all).hasSize(1);

        ExecutionLogEntry executionLogEntry = all.get(0);

        assertThat(executionLogEntry.getInteractionId()).isNotNull();
        assertThat(executionLogEntry.getCompletedAt()).isNotNull();
        assertThat(executionLogEntry.getDuration()).isNotNull();
        assertThat(executionLogEntry.getLogicalMemberIdentifier()).isNotNull();
        assertThat(executionLogEntry.getLogicalMemberIdentifier()).isEqualTo("executionlog.test.Counter#bumpUsingDeclaredAction");
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

        // then
        List<? extends ExecutionLogEntry> all = executionLogEntryRepository.findMostRecent();
        assertThat(all).isEmpty();
    }

    @Test
    void invoke_direct_disabled() {

        // when
        wrapperFactory.wrap(counter1).bumpUsingDeclaredActionWithExecutionPublishingDisabled();

        // then
        List<? extends ExecutionLogEntry> all = executionLogEntryRepository.findMostRecent();
        assertThat(all).isEmpty();
    }

    @Test
    void edit() {

        // when
        wrapperFactory.wrap(counter1).setNum(99L);

        // then
        List<? extends ExecutionLogEntry> all = executionLogEntryRepository.findMostRecent();
        assertThat(all).hasSize(1);

        ExecutionLogEntry executionLogEntry = all.get(0);

        assertThat(executionLogEntry.getInteractionId()).isNotNull();
        assertThat(executionLogEntry.getCompletedAt()).isNotNull();
        assertThat(executionLogEntry.getDuration()).isNotNull();
        assertThat(executionLogEntry.getLogicalMemberIdentifier()).isNotNull();
        assertThat(executionLogEntry.getLogicalMemberIdentifier()).isEqualTo("executionlog.test.Counter#num");
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

        // then
        List<? extends ExecutionLogEntry> all = executionLogEntryRepository.findMostRecent();
        assertThat(all).isEmpty();
    }


    @Test
    void roundtrip_ELE_bookmarks() {

        // given
        wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();

        List<? extends ExecutionLogEntry> all = executionLogEntryRepository.findMostRecent();

        ExecutionLogEntry executionLogEntry = all.get(0);
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
        interactionService.nextInteraction();

        Optional<Object> cle2IfAny = bookmarkService.lookup(eleBookmarkIfAny.get());
        assertThat(cle2IfAny).isPresent();

        ExecutionLogEntry ele2 = (ExecutionLogEntry) cle2IfAny.get();
        InteractionDto interactionDto2 = ele2.getInteractionDto();

        assertThat(interactionDto2).isEqualTo(interactionDto);

    }

    @Test
    void test_all_the_repository_methods() {

        // given
        sudoService.run(InteractionContext.switchUser(UserMemento.builder().name("user-1").build()), () -> {
            wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
        });

        // when
        List<? extends ExecutionLogEntry> executionTarget1User1IfAny = executionLogEntryRepository.findMostRecent(1);

        // then
        assertThat(executionTarget1User1IfAny).hasSize(1);
        var executionTarget1User1 = executionTarget1User1IfAny.get(0);
        val executionTarget1User1Id = executionTarget1User1.getInteractionId();

        // given (different user, same target, same day)
        counter1 = counterRepository.findByName("counter-1");
        sudoService.run(
                InteractionContext.switchUser(
                        UserMemento.builder().name("user-2").build()),
                () -> wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act()
        );
        interactionService.nextInteraction();

        // when
        List<? extends ExecutionLogEntry> executionTarget1User2IfAny = executionLogEntryRepository.findMostRecent(1);

        // then
        assertThat(executionTarget1User2IfAny).hasSize(1);
        var executionTarget1User2 = executionTarget1User2IfAny.get(0);
        val executionTarget1User2Id = executionTarget1User2.getInteractionId();


        // given (same user, same target, yesterday)
        counter1 = counterRepository.findByName("counter-1");
        final UUID[] executionTarget1User1YesterdayIdHolder = new UUID[1];
        sudoService.run(
                InteractionContext.switchUser(
                        UserMemento.builder().name("user-1").build()),
                () -> {
                    val yesterday = clockService.getClock().nowAsLocalDateTime().minusDays(1);
                    sudoService.run(
                            InteractionContext.switchClock(VirtualClock.nowAt(yesterday)),
                            () -> {
                                wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
                                executionTarget1User1YesterdayIdHolder[0] = interactionLayerTracker.currentInteraction().get().getInteractionId();
                                interactionService.closeInteractionLayers();    // to flush within changed time...
                            }
                    );
                });
        interactionService.openInteraction();

        // when, then
        final UUID executionTarget1User1YesterdayId = executionTarget1User1YesterdayIdHolder[0];

        // given (same user, different target, same day)
        counter2 = counterRepository.findByName("counter-2");
        sudoService.run(InteractionContext.switchUser(UserMemento.builder().name("user-1").build()), () -> {
            wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter2).act();
        });
        interactionService.nextInteraction();

        // when
        List<? extends ExecutionLogEntry> executionTarget2User1IfAny = executionLogEntryRepository.findMostRecent(1);

        // then
        assertThat(executionTarget2User1IfAny).hasSize(1);
        var executionTarget2User1 = executionTarget2User1IfAny.get(0);
        val executionTarget2User1Id = executionTarget2User1.getInteractionId();

        // when
        Optional<? extends ExecutionLogEntry> executionTarget1User1ById = executionLogEntryRepository.findByInteractionIdAndSequence(executionTarget1User1Id, 0);
        Optional<? extends ExecutionLogEntry> executionTarget1User2ById = executionLogEntryRepository.findByInteractionIdAndSequence(executionTarget1User2Id, 0);
        Optional<? extends ExecutionLogEntry> executionTarget1User1YesterdayById = executionLogEntryRepository.findByInteractionIdAndSequence(executionTarget1User1YesterdayId, 0);
        Optional<? extends ExecutionLogEntry> executionTarget2User1ById = executionLogEntryRepository.findByInteractionIdAndSequence(executionTarget2User1Id, 0);

        // then
        assertThat(executionTarget1User1ById).isPresent();
        assertThat(executionTarget1User2ById).isPresent();
        assertThat(executionTarget1User1YesterdayById).isPresent();
        assertThat(executionTarget2User1ById).isPresent();
        assertThat(executionTarget2User1ById.get()).isSameAs(executionTarget2User1);

        // given
        counter1 = counterRepository.findByName("counter-1");
        executionTarget1User1 = executionTarget1User1ById.get();
        executionTarget1User2 = executionTarget1User2ById.get();
        val executionTarget1User1Yesterday = executionTarget1User1YesterdayById.get();
        executionTarget2User1 = executionTarget2User1ById.get();

        val target1 = executionTarget1User1.getTarget();
        val username1 = executionTarget1User1.getUsername();
        Timestamp from1 = executionTarget1User1.getStartedAt();
        Timestamp to1 = Timestamp.valueOf(from1.toLocalDateTime().plusDays(1));
        val bookmark1 = bookmarkService.bookmarkForElseFail(counter1);


        // when
        List<? extends ExecutionLogEntry> recentByTarget = executionLogEntryRepository.findRecentByTarget(bookmark1);

        // then
        assertThat(recentByTarget).hasSize(3);


        // when
        List<? extends ExecutionLogEntry> byTargetAndTimestampBefore = executionLogEntryRepository.findByTargetAndTimestampBefore(bookmark1, from1);

        // then
        assertThat(byTargetAndTimestampBefore).hasSize(2); // yesterday, plus cmd1

        // when
        List<? extends ExecutionLogEntry> byTargetAndTimestampAfter = executionLogEntryRepository.findByTargetAndTimestampAfter(bookmark1, from1);

        // then
        assertThat(byTargetAndTimestampAfter).hasSize(2); // cmd1, 2nd

        // when
        List<? extends ExecutionLogEntry> byTargetAndTimestampBetween = executionLogEntryRepository.findByTargetAndTimestampBetween(bookmark1, from1, to1);

        // then
        assertThat(byTargetAndTimestampBetween).hasSize(2); // 1st and 2nd for this target

        // when
        List<? extends ExecutionLogEntry> byTimestampBefore = executionLogEntryRepository.findByTimestampBefore(from1);

        // then
        assertThat(byTimestampBefore).hasSize(2); // cmd1 plus yesterday

        // when
        List<? extends ExecutionLogEntry> byTimestampAfter = executionLogEntryRepository.findByTimestampAfter(from1);

        // then
        assertThat(byTimestampAfter).hasSize(3); // cmd1, 2nd, and for other target

        // when
        List<? extends ExecutionLogEntry> byTimestampBetween = executionLogEntryRepository.findByTimestampBetween(from1, to1);

        // then
        assertThat(byTimestampBetween).hasSize(3); // 1st and 2nd for this target, and other target

        // when
        List<? extends ExecutionLogEntry> byUsername = executionLogEntryRepository.findRecentByUsername(username1);

        // then
        assertThat(byUsername).hasSize(3);

    }

    @Inject ExecutionLogEntryRepository<? extends ExecutionLogEntry> executionLogEntryRepository;
    @Inject SudoService sudoService;
    @Inject ClockService clockService;
    @Inject InteractionService interactionService;
    @Inject InteractionLayerTracker interactionLayerTracker;
    @Inject CounterRepository counterRepository;
    @Inject WrapperFactory wrapperFactory;
    @Inject BookmarkService bookmarkService;

}
