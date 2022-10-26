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
package org.apache.causeway.regressiontests.cmdexecauditsess.generic.integtest;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.applib.mixins.system.DomainChangeRecord;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.facets.object.publish.entitychange.EntityChangePublishingFacet;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntry;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryRepository;
import org.apache.causeway.regressiontests.cmdexecauditsess.generic.integtest.model.Counter;
import org.apache.causeway.regressiontests.cmdexecauditsess.generic.integtest.model.CounterRepository;
import org.apache.causeway.regressiontests.cmdexecauditsess.generic.integtest.model.Counter_bumpUsingMixin;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

public abstract class CmdExecAuditSessLog_IntegTestAbstract extends CausewayIntegrationTestAbstract {

    @BeforeAll
    static void beforeAll() {
        CausewayPresets.forcePrototyping();
    }

    Bookmark target1;

    @BeforeEach
    void beforeEach() {
        interactionService.nextInteraction();

        counterRepository.removeAll();

        assertThat(counterRepository.find()).isEmpty();

        val counter1 = counterRepository.persist(newCounter("counter-1"));
        target1 = bookmarkService.bookmarkFor(counter1).orElseThrow();

        assertThat(counterRepository.find()).hasSize(1);

        interactionService.nextInteraction();
        commandLogEntryRepository.removeAll();
        executionLogEntryRepository.removeAll();
        executionOutboxEntryRepository.removeAll();
        auditTrailEntryRepository.removeAll();

        interactionService.nextInteraction();

        assertThat(commandLogEntryRepository.findAll()).isEmpty();
        assertThat(executionLogEntryRepository.findAll()).isEmpty();
        assertThat(executionOutboxEntryRepository.findAll()).isEmpty();
        assertThat(auditTrailEntryRepository.findAll()).isEmpty();
    }

    protected abstract Counter newCounter(String name);

    @Inject SpecificationLoader specificationLoader;


    @Test
    void check_facets() {
        assertEntityPublishingDisabledFor(auditTrailEntryRepository.getEntityClass());
        assertEntityPublishingDisabledFor(commandLogEntryRepository.getEntityClass());
        assertEntityPublishingDisabledFor(executionLogEntryRepository.getEntityClass());
        assertEntityPublishingDisabledFor(executionOutboxEntryRepository.getEntityClass());

    }

    private void assertEntityPublishingDisabledFor(Class<?> entityClass) {
        val objectSpecification = specificationLoader.loadSpecification(entityClass);
        EntityChangePublishingFacet facet = objectSpecification.getFacet(EntityChangePublishingFacet.class);
        Assertions.assertThat(facet)
                        .satisfies(f -> assertThat(f).isNotNull())
                        .satisfies(f -> assertThat(f.isEnabled()).isFalse())
        ;
    }

    @Test
    void invoke_mixin() {

        // given
        val counter1 = bookmarkService.lookup(target1, Counter.class).orElseThrow();
        val interaction = interactionService.currentInteraction().orElseThrow();

        // when
        wrapperFactory.wrapMixinT(Counter_bumpUsingMixin.class, counter1).act();

        // then
        // ... execution log already persisted
        var executionLogEntries = executionLogEntryRepository.findMostRecent();
        assertThat(executionLogEntries).hasSize(1);

        val executionLogEntry = executionLogEntries.get(0);

        assertThat(executionLogEntry)
                .satisfies(e -> assertThat(e.getInteractionId()).isEqualTo(interaction.getInteractionId()))
                .satisfies(e -> assertThat(e.getCompletedAt()).isNotNull())
                .satisfies(e -> assertThat(e.getDuration()).isNotNull())
                .satisfies(e -> assertThat(e.getLogicalMemberIdentifier()).isNotNull())
                .satisfies(e -> assertThat(e.getLogicalMemberIdentifier()).isEqualTo("cmdexecauditsess.test.Counter#bumpUsingMixin"))
                .satisfies(e -> assertThat(e.getUsername()).isEqualTo("__system"))
                .satisfies(e -> assertThat(e.getTarget()).isEqualTo(target1))
                .satisfies(e -> assertThat(e.getTimestamp()).isNotNull())
                .satisfies(e -> assertThat(e.getType()).isEqualTo(DomainChangeRecord.ChangeType.EXECUTION))
                .satisfies(e -> assertThat(e.getInteractionDto())
                        .satisfies(dto -> assertThat(dto).isNotNull())
                        .satisfies(dto -> assertThat(dto.getExecution()).isInstanceOf(ActionInvocationDto.class))
                        .satisfies(dto -> assertThat(dto.getExecution().getLogicalMemberIdentifier()).isEqualTo(executionLogEntry.getLogicalMemberIdentifier()))
                )
        ;

        // then
        // ... and execution outbox already persisted
        var executionOutboxEntries = executionOutboxEntryRepository.findAll();
        assertThat(executionOutboxEntries).hasSize(1);

        val executionOutboxEntry = executionOutboxEntries.get(0);

        assertThat(executionOutboxEntry)
                .satisfies(e -> assertThat(e.getInteractionId()).isEqualTo(interaction.getInteractionId()))
                .satisfies(e -> assertThat(e.getCompletedAt()).isNotNull())
                .satisfies(e -> assertThat(e.getDuration()).isNotNull())
                .satisfies(e -> assertThat(e.getLogicalMemberIdentifier()).isNotNull())
                .satisfies(e -> assertThat(e.getLogicalMemberIdentifier()).isEqualTo("cmdexecauditsess.test.Counter#bumpUsingMixin"))
                .satisfies(e -> assertThat(e.getUsername()).isEqualTo("__system"))
                .satisfies(e -> assertThat(e.getTarget()).isEqualTo(target1))
                .satisfies(e -> assertThat(e.getTimestamp()).isNotNull())
                .satisfies(e -> assertThat(e.getType()).isEqualTo(DomainChangeRecord.ChangeType.EXECUTION))
                .satisfies(e -> assertThat(e.getInteractionDto())
                        .satisfies(dto -> assertThat(dto).isNotNull())
                        .satisfies(dto -> assertThat(dto.getExecution()).isInstanceOf(ActionInvocationDto.class))
                        .satisfies(dto -> assertThat(dto.getExecution().getLogicalMemberIdentifier()).isEqualTo(executionLogEntry.getLogicalMemberIdentifier()))
                )
        ;

        // ... but command not yet persisted
        var commandLogEntries = commandLogEntryRepository.findAll();
        assertThat(commandLogEntries).isEmpty();

        // ... and audit entries not yet generated
        var auditTrailEntries = auditTrailEntryRepository.findAll();
        assertThat(auditTrailEntries).isEmpty();

        // when
        interactionService.nextInteraction();   // flushes the command and audit trail entries

        // then
        // ... command entry created
        commandLogEntries = commandLogEntryRepository.findAll();
        assertThat(commandLogEntries).hasSize(1);

        val commandLogEntry = commandLogEntries.get(0);

        assertThat(commandLogEntry)
                .satisfies(e -> assertThat(e.getInteractionId()).isEqualTo(interaction.getInteractionId()))
                .satisfies(e -> assertThat(e.getCompletedAt()).isNotNull())
                .satisfies(e -> assertThat(e.getDuration()).isNotNull())
                .satisfies(e -> assertThat(e.getException()).isEqualTo(""))
                .satisfies(e -> assertThat(e.getLogicalMemberIdentifier()).isNotNull())
                .satisfies(e -> assertThat(e.getLogicalMemberIdentifier()).isEqualTo("cmdexecauditsess.test.Counter#bumpUsingMixin"))
                .satisfies(e -> assertThat(e.getUsername()).isEqualTo("__system"))
                .satisfies(e -> assertThat(e.getResult()).isNotNull())
                .satisfies(e -> assertThat(e.getResultSummary()).isEqualTo("OK"))
                .satisfies(e -> assertThat(e.getReplayState()).isEqualTo(ReplayState.UNDEFINED))
                .satisfies(e -> assertThat(e.getReplayStateFailureReason()).isNull())
                .satisfies(e -> assertThat(e.getTarget()).isEqualTo(target1))
                .satisfies(e -> assertThat(e.getTimestamp()).isNotNull())
                .satisfies(e -> assertThat(e.getType()).isEqualTo(DomainChangeRecord.ChangeType.COMMAND))
                .satisfies(e -> assertThat(e.getCommandDto())
                        .satisfies(dto -> assertThat(dto).isNotNull())
                        .satisfies(dto -> assertThat(dto.getMember()).isInstanceOf(ActionDto.class))
                        .satisfies(dto -> assertThat(dto.getMember().getLogicalMemberIdentifier()).isEqualTo(commandLogEntry.getLogicalMemberIdentifier()))
                );

        if(!isJpa()) {
            // and then
            // ... audit trail entry created
            auditTrailEntries = auditTrailEntryRepository.findAll();
            assertThat(auditTrailEntries).hasSize(1);

            var propertyIds = auditTrailEntries.stream().map(AuditTrailEntry::getPropertyId).collect(Collectors.toList());
            assertThat(propertyIds).containsExactly("num");

            var entriesById = auditTrailEntries.stream().collect(Collectors.toMap(AuditTrailEntry::getPropertyId, x -> x));
            assertThat(entriesById.get("num"))
                    .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getLogicalMemberIdentifier).isEqualTo("cmdexecauditsess.test.Counter#num"))
                    .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPreValue).isNull())
                    .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPostValue).isEqualTo("1"))
                    .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getInteractionId).isEqualTo(interaction.getInteractionId()))
                    .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getSequence).isEqualTo(0))
                    .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getTarget).isEqualTo(target1))
                    .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getTimestamp).isNotNull())
                    .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getType).isEqualTo(DomainChangeRecord.ChangeType.AUDIT_ENTRY))
                    .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getUsername).isEqualTo("__system"));
        }
    }

    @Test
    void invoke_direct() {

        // given
        val counter1 = bookmarkService.lookup(target1, Counter.class).orElseThrow();

        // when
        wrapperFactory.wrap(counter1).bumpUsingDeclaredAction();

        // then
        // ... execution already persisted
        var executionLogEntries = executionLogEntryRepository.findMostRecent();
        assertThat(executionLogEntries).hasSize(1);

        // ... but command not yet persisted
        var commandLogEntries = commandLogEntryRepository.findAll();
        assertThat(commandLogEntries).isEmpty();

        // ... and audit entries not yet generated
        var auditTrailEntries = auditTrailEntryRepository.findAll();
        assertThat(auditTrailEntries).isEmpty();

        // when
        interactionService.nextInteraction();   // flushes the command and audit trail entries

        // then
        // ... command entry created
        commandLogEntries = commandLogEntryRepository.findAll();
        assertThat(commandLogEntries).hasSize(1);

        if(!isJpa()) {
            // and then
            // ... audit trail entry created
            auditTrailEntries = auditTrailEntryRepository.findAll();
            assertThat(auditTrailEntries).hasSize(1);
        }

    }


    @Test
    void edit() {

        // given
        val counter1 = bookmarkService.lookup(target1, Counter.class).orElseThrow();

        // when
        wrapperFactory.wrap(counter1).setNum(99L);

        // then
        // ... execution already persisted
        var executionLogEntries = executionLogEntryRepository.findMostRecent();
        assertThat(executionLogEntries).hasSize(1);

        // ... but command not yet persisted
        var commandLogEntries = commandLogEntryRepository.findAll();
        assertThat(commandLogEntries).isEmpty();

        // ... and audit entries not yet generated
        var auditTrailEntries = auditTrailEntryRepository.findAll();
        assertThat(auditTrailEntries).isEmpty();

        // when
        interactionService.nextInteraction();   // flushes the command and audit trail entries

        // then
        // ... command entry created
        commandLogEntries = commandLogEntryRepository.findAll();
        assertThat(commandLogEntries).hasSize(1);

        if(!isJpa()) {
            // and then
            // ... audit trail entry created
            auditTrailEntries = auditTrailEntryRepository.findAll();
            assertThat(auditTrailEntries).hasSize(1);
        }

    }

    boolean isJpa() {
        return causewayBeanTypeRegistry.determineCurrentPersistenceStack().isJpa();
    }


    @Inject AuditTrailEntryRepository<? extends AuditTrailEntry> auditTrailEntryRepository;
    @Inject ExecutionOutboxEntryRepository<? extends ExecutionOutboxEntry> executionOutboxEntryRepository;
    @Inject ExecutionLogEntryRepository<? extends ExecutionLogEntry> executionLogEntryRepository;
    @Inject CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
    @Inject SudoService sudoService;
    @Inject ClockService clockService;
    @Inject InteractionService interactionService;
    @Inject CounterRepository<? extends Counter> counterRepository;
    @Inject WrapperFactory wrapperFactory;
    @Inject BookmarkService bookmarkService;

    @Inject CausewayBeanTypeRegistry causewayBeanTypeRegistry;

}
