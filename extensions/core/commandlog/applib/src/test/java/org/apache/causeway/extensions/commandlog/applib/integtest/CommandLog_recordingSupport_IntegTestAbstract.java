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

import javax.inject.Inject;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.eventbus.EventBusService;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RecordingSupport;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMappingRepository;
import org.apache.causeway.extensions.commandlog.applib.integtest.model.Counter;
import org.apache.causeway.extensions.commandlog.applib.integtest.model.CounterRepository;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;


public abstract class CommandLog_recordingSupport_IntegTestAbstract extends CausewayIntegrationTestAbstract {


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
    void recording_support_logs_property_edit_without_command_publishing_annotation() {


        // when
        wrapperFactory.wrap(counter1).setName("updated-counter-1");
        interactionService.nextInteraction();

        // then
        List<? extends CommandLogEntry> entries = commandLogEntryRepository.findAll();
        assertThat(entries).hasSize(1);
        CommandLogEntry commandLogEntry = entries.get(0);
        assertThat(commandLogEntry.getLogicalMemberIdentifier()).isEqualTo("commandlog.test.Counter#name");
        assertThat(commandLogEntry.getCommandDto()).isNotNull();
        assertThat(commandLogEntry.getCommandDto().getMember()).isInstanceOf(PropertyDto.class);
    }

    @Test
    void recording_support_logs_property_edit_with_command_publishing_disabled() {

        // when
        wrapperFactory.wrap(counter1).setNum2(99L);
        interactionService.nextInteraction();

        // then
        List<? extends CommandLogEntry> entries = commandLogEntryRepository.findAll();
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getLogicalMemberIdentifier()).isEqualTo("commandlog.test.Counter#num2");
        assertThat(entries.get(0).getCommandDto()).isNotNull();
        assertThat(entries.get(0).getCommandDto().getMember()).isInstanceOf(PropertyDto.class);
    }

    @Test
    void recording_support_does_not_duplicate_explicitly_command_published_property_edit() {

        // when
        wrapperFactory.wrap(counter1).setNum(99L);
        interactionService.nextInteraction();

        // then
        List<? extends CommandLogEntry> entries = commandLogEntryRepository.findAll();
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getLogicalMemberIdentifier()).isEqualTo("commandlog.test.Counter#num");
    }


    @Inject CommandLogEntryRepository commandLogEntryRepository;
    @Inject CommandReplayResultMappingRepository commandReplayResultMappingRepository;
    @Inject SudoService sudoService;
    @Inject ClockService clockService;
    @Inject InteractionService interactionService;
    @Inject InteractionLayerTracker interactionLayerTracker;
    @Inject CounterRepository counterRepository;
    @Inject WrapperFactory wrapperFactory;
    @Inject BookmarkService bookmarkService;
    @Inject CausewayConfiguration causewayConfiguration;
    @Inject CausewayBeanTypeRegistry causewayBeanTypeRegistry;
    @Inject EventBusService eventBusService;

}
