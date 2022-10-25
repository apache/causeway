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
package org.apache.causeway.extensions.commandlog.applib.integtest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
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
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.applib.integtest.model.Counter;
import org.apache.causeway.extensions.commandlog.applib.integtest.model.CounterRepository;
import org.apache.causeway.extensions.commandlog.applib.integtest.model.Counter_bumpUsingMixin;
import org.apache.causeway.extensions.commandlog.applib.integtest.model.Counter_bumpUsingMixinWithCommandPublishingDisabled;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

public abstract class CommandLog_IntegTestAbstract extends CausewayIntegrationTestAbstract {


    @BeforeAll
    static void beforeAll() {
        CausewayPresets.forcePrototyping();
    }

    Counter counter1;
    Counter counter2;

    @BeforeEach
    void beforeEach() {
        interactionService.nextInteraction();

        counterRepository.removeAll();
        commandLogEntryRepository.removeAll();

        assertThat(counterRepository.find()).isEmpty();

        counter1 = counterRepository.persist(newCounter("counter-1"));
        counter2 = counterRepository.persist(newCounter("counter-2"));

        assertThat(counterRepository.find()).hasSize(2);

        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();
        assertThat(mostRecentCompleted).isEmpty();
    }

    protected abstract Counter newCounter(String name);


    @Test
    void invoke_mixin() {

        // when
        wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
        interactionService.nextInteraction();

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
        wrapperFactory.wrap(counter1).bumpUsingDeclaredAction();
        interactionService.nextInteraction();

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
        wrapperFactory.wrapMixin(Counter_bumpUsingMixinWithCommandPublishingDisabled.class, counter1).act();
        interactionService.nextInteraction();

        // then
        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();
        assertThat(mostRecentCompleted).isEmpty();
    }

    @Test
    void invoke_direct_disabled() {

        // when
        wrapperFactory.wrap(counter1).bumpUsingDeclaredActionWithCommandPublishingDisabled();
        interactionService.nextInteraction();

        // then
        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();
        assertThat(mostRecentCompleted).isEmpty();
    }



    @Test
    void edit() {

        // when
        wrapperFactory.wrap(counter1).setNum(99L);
        interactionService.nextInteraction();

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
        assertThat(commandDto.getMember().getLogicalMemberIdentifier()).isEqualTo(commandLogEntry.getLogicalMemberIdentifier());
    }

    @Test
    void edit_disabled() {

        // when
        wrapperFactory.wrap(counter1).setNum2(99L);
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();
        assertThat(mostRecentCompleted).isEmpty();
    }


    @Test
    void roundtrip_CLE_bookmarks() {

        // given
        wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
        interactionService.nextInteraction();

        Optional<? extends CommandLogEntry> mostRecentCompleted = commandLogEntryRepository.findMostRecentCompleted();

        CommandLogEntry commandLogEntry = mostRecentCompleted.get();
        CommandDto commandDto = commandLogEntry.getCommandDto();

        // when
        Optional<Bookmark> cleBookmarkIfAny = bookmarkService.bookmarkFor(commandLogEntry);

        // then
        assertThat(cleBookmarkIfAny).isPresent();
        Bookmark cleBookmark = cleBookmarkIfAny.get();
        String identifier = cleBookmark.getIdentifier();
        if (causewayBeanTypeRegistry.determineCurrentPersistenceStack().isJdo()) {
            assertThat(identifier).startsWith("u_");
            UUID.fromString(identifier.substring("u_".length())); // should not fail, ie check the format is as we expect
        } else {
            UUID.fromString(identifier); // should not fail, ie check the format is as we expect
        }

        // when we start a new session and lookup from the bookmark
        interactionService.nextInteraction();

        Optional<Object> cle2IfAny = bookmarkService.lookup(cleBookmarkIfAny.get());
        assertThat(cle2IfAny).isPresent();

        CommandLogEntry cle2 = (CommandLogEntry) cle2IfAny.get();
        CommandDto commandDto2 = cle2.getCommandDto();

        assertThat(commandDto2).isEqualTo(commandDto);


    }

    @Test
    void test_all_the_repository_methods() {

        // given
        sudoService.run(InteractionContext.switchUser(UserMemento.builder().name("user-1").build()), () -> {
            wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
        });
        interactionService.nextInteraction();

        // when
        Optional<? extends CommandLogEntry> commandTarget1User1IfAny = commandLogEntryRepository.findMostRecentCompleted();

        // then
        Assertions.assertThat(commandTarget1User1IfAny).isPresent();
        var commandTarget1User1 = commandTarget1User1IfAny.get();
        val commandTarget1User1Id = commandTarget1User1.getInteractionId();

        // given (different user, same target, same day)
        counter1 = counterRepository.findByName("counter-1");
        sudoService.run(
                InteractionContext.switchUser(
                        UserMemento.builder().name("user-2").build()),
                () -> wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act()
        );
        interactionService.nextInteraction();

        // when
        Optional<? extends CommandLogEntry> commandTarget1User2IfAny = commandLogEntryRepository.findMostRecentCompleted();

        // then
        Assertions.assertThat(commandTarget1User2IfAny).isPresent();
        var commandTarget1User2 = commandTarget1User2IfAny.get();
        val commandTarget1User2Id = commandTarget1User2.getInteractionId();


        // given (same user, same target, yesterday)
        counter1 = counterRepository.findByName("counter-1");
        final UUID[] commandTarget1User1YesterdayIdHolder = new UUID[1];
        sudoService.run(
                InteractionContext.switchUser(
                        UserMemento.builder().name("user-1").build()),
                () -> {
                    val yesterday = clockService.getClock().nowAsLocalDateTime().minusDays(1);
                    sudoService.run(
                            InteractionContext.switchClock(VirtualClock.nowAt(yesterday)),
                            () -> {
                                wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
                                commandTarget1User1YesterdayIdHolder[0] = interactionLayerTracker.currentInteraction().get().getInteractionId();
                                interactionService.closeInteractionLayers();    // to flush within changed time...
                            }
                    );
                });
        interactionService.openInteraction();

        // when, then
        final UUID commandTarget1User1YesterdayId = commandTarget1User1YesterdayIdHolder[0];

        // given (same user, different target, same day)
        counter2 = counterRepository.findByName("counter-2");
        sudoService.run(InteractionContext.switchUser(UserMemento.builder().name("user-1").build()), () -> {
            wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter2).act();
        });
        interactionService.nextInteraction();

        // when
        Optional<? extends CommandLogEntry> commandTarget2User1IfAny = commandLogEntryRepository.findMostRecentCompleted();

        // then
        Assertions.assertThat(commandTarget2User1IfAny).isPresent();
        var commandTarget2User1 = commandTarget2User1IfAny.get();
        val commandTarget2User1Id = commandTarget2User1.getInteractionId();

        // when
        Optional<? extends CommandLogEntry> commandTarget1User1ById = commandLogEntryRepository.findByInteractionId(commandTarget1User1Id);
        Optional<? extends CommandLogEntry> commandTarget1User2ById = commandLogEntryRepository.findByInteractionId(commandTarget1User2Id);
        Optional<? extends CommandLogEntry> commandTarget1User1YesterdayById = commandLogEntryRepository.findByInteractionId(commandTarget1User1YesterdayId);
        Optional<? extends CommandLogEntry> commandTarget2User1ById = commandLogEntryRepository.findByInteractionId(commandTarget2User1Id);

        // then
        Assertions.assertThat(commandTarget1User1ById).isPresent();
        Assertions.assertThat(commandTarget1User2ById).isPresent();
        Assertions.assertThat(commandTarget1User1YesterdayById).isPresent();
        Assertions.assertThat(commandTarget2User1ById).isPresent();
        Assertions.assertThat(commandTarget2User1ById.get()).isSameAs(commandTarget2User1);

        // given
        commandTarget1User1 = commandTarget1User1ById.get();
        commandTarget1User2 = commandTarget1User2ById.get();
        val commandTarget1User1Yesterday = commandTarget1User1YesterdayById.get();
        commandTarget2User1 = commandTarget2User1ById.get();

        val target1 = commandTarget1User1.getTarget();
        val username1 = commandTarget1User1.getUsername();
        val from = commandTarget1User1.getStartedAt().toLocalDateTime().toLocalDate();
        val to = from.plusDays(1);

        // when
        List<? extends CommandLogEntry> notYetReplayed = commandLogEntryRepository.findNotYetReplayed();

        // then
        Assertions.assertThat(notYetReplayed).isEmpty();

        if (causewayBeanTypeRegistry.determineCurrentPersistenceStack().isJdo()) {

            // fails in JPA; possibly need to get the agent working for dirty tracking.

            // given
            commandTarget1User1.setReplayState(ReplayState.PENDING);

            // when
            List<? extends CommandLogEntry> notYetReplayed2 = commandLogEntryRepository.findNotYetReplayed();

            // then
            Assertions.assertThat(notYetReplayed2).hasSize(1);
            Assertions.assertThat(notYetReplayed2.get(0).getInteractionId()).isEqualTo(commandTarget1User1.getInteractionId());
        }

        // when
        List<? extends CommandLogEntry> byFromAndTo = commandLogEntryRepository.findByFromAndTo(from, to);

        // then
        Assertions.assertThat(byFromAndTo).hasSize(3);
        Assertions.assertThat(byFromAndTo.get(0).getInteractionId()).isEqualTo(commandTarget2User1.getInteractionId()); // the more recent


        // when
        List<? extends CommandLogEntry> byTarget1AndFromAndTo = commandLogEntryRepository.findByTargetAndFromAndTo(target1, from, to);

        // then
        Assertions.assertThat(byTarget1AndFromAndTo).hasSize(2);
        Assertions.assertThat(byTarget1AndFromAndTo.get(0).getInteractionId()).isEqualTo(commandTarget1User2.getInteractionId()); // the more recent

        // when
        List<? extends CommandLogEntry> recentByTargetOfCommand1 = commandLogEntryRepository.findRecentByTarget(target1);

        // then
        Assertions.assertThat(recentByTargetOfCommand1).hasSize(3);
        Assertions.assertThat(recentByTargetOfCommand1.get(0).getInteractionId()).isEqualTo(commandTarget1User2.getInteractionId()); // the more recent

        // when
        List<? extends CommandLogEntry> recentByUsername = commandLogEntryRepository.findRecentByUsername(username1);

        // then
        Assertions.assertThat(recentByUsername).hasSize(3);
        Assertions.assertThat(recentByUsername.get(0).getInteractionId()).isEqualTo(commandTarget2User1.getInteractionId()); // the more recent

        // when
        List<? extends CommandLogEntry> byParent = commandLogEntryRepository.findByParent(commandTarget1User1);

        // then // TODO: would need nested executions for this to show up.
        Assertions.assertThat(byParent).isEmpty();

        // when
        List<? extends CommandLogEntry> completed = commandLogEntryRepository.findCompleted();

        // then
        Assertions.assertThat(completed).hasSize(4);
        Assertions.assertThat(completed.get(0).getInteractionId()).isEqualTo(commandTarget2User1.getInteractionId()); // the more recent

        // when
        List<? extends CommandLogEntry> current = commandLogEntryRepository.findCurrent();

        // then // TODO: would need more sophistication in fixtures to test
        Assertions.assertThat(current).isEmpty();

        // when
        List<? extends CommandLogEntry> since = commandLogEntryRepository.findSince(commandTarget1User1.getInteractionId(), 3);

        // then
        Assertions.assertThat(since).hasSize(2);
        Assertions.assertThat(since.get(0).getInteractionId()).isEqualTo(commandTarget1User2.getInteractionId()); // oldest first

        // when
        List<? extends CommandLogEntry> sinceWithBatchSize1 = commandLogEntryRepository.findSince(commandTarget1User1.getInteractionId(), 1);

        // then
        Assertions.assertThat(sinceWithBatchSize1).hasSize(1);
        Assertions.assertThat(sinceWithBatchSize1.get(0).getInteractionId()).isEqualTo(commandTarget1User2.getInteractionId()); // oldest fist

        // when
        Optional<? extends CommandLogEntry> mostRecentReplayedIfAny = commandLogEntryRepository.findMostRecentReplayed();

        // then
        Assertions.assertThat(mostRecentReplayedIfAny).isEmpty();

        if (causewayBeanTypeRegistry.determineCurrentPersistenceStack().isJdo()) {

            // fails in JPA; possibly need to get the agent working for dirty tracking.

            // given
            commandTarget1User1.setReplayState(ReplayState.OK);

            // when
            Optional<? extends CommandLogEntry> mostRecentReplayedIfAny2 = commandLogEntryRepository.findMostRecentReplayed();

            // then
            Assertions.assertThat(mostRecentReplayedIfAny2).isPresent();
            Assertions.assertThat(mostRecentReplayedIfAny2.get().getInteractionId()).isEqualTo(commandTarget1User1Id);
        }

    }

    @Inject CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
    @Inject SudoService sudoService;
    @Inject ClockService clockService;
    @Inject InteractionService interactionService;
    @Inject InteractionLayerTracker interactionLayerTracker;
    @Inject CounterRepository counterRepository;
    @Inject WrapperFactory wrapperFactory;
    @Inject BookmarkService bookmarkService;
    @Inject CausewayBeanTypeRegistry causewayBeanTypeRegistry;

}
