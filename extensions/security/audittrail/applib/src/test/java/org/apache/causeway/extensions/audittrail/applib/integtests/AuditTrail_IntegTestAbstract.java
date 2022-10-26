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
package org.apache.causeway.extensions.audittrail.applib.integtests;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.applib.mixins.system.DomainChangeRecord;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntry;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntryRepository;
import org.apache.causeway.extensions.audittrail.applib.integtests.model.Counter;
import org.apache.causeway.extensions.audittrail.applib.integtests.model.CounterRepository;
import org.apache.causeway.extensions.audittrail.applib.integtests.model.Counter_bumpUsingMixin;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

public abstract class AuditTrail_IntegTestAbstract extends CausewayIntegrationTestAbstract {

    @BeforeAll
    static void beforeAll() {
        CausewayPresets.forcePrototyping();
    }

    @BeforeEach
    void setUp() {
        counterRepository.removeAll();
        interactionService.nextInteraction();

        auditTrailEntryRepository.removeAll();
        interactionService.nextInteraction();

        assertThat(counterRepository.find()).isEmpty();
        assertThat(auditTrailEntryRepository.findAll()).isEmpty();
    }

    protected abstract Counter newCounter(String name);

    @Test
    void created() {

        // when
        val counter1 = counterRepository.persist(newCounter("counter-1"));
        val target1 = bookmarkService.bookmarkFor(counter1).orElseThrow();
        interactionService.nextInteraction();

        // then
        var entries = auditTrailEntryRepository.findAll();
        val propertyIds = entries.stream().map(AuditTrailEntry::getPropertyId).collect(Collectors.toList());
        assertThat(propertyIds).contains("name", "num", "num2");

        val entriesById = entries.stream().collect(Collectors.toMap(AuditTrailEntry::getPropertyId, x -> x));
        assertThat(entriesById.get("name"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getLogicalMemberIdentifier).isEqualTo("audittrail.test.Counter#name"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPreValue).isEqualTo("[NEW]"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPostValue).isEqualTo("counter-1"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getInteractionId).isNotNull())
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getSequence).isEqualTo(0))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getTarget).isEqualTo(target1))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getTimestamp).isNotNull())
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getType).isEqualTo(DomainChangeRecord.ChangeType.AUDIT_ENTRY))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getUsername).isEqualTo("__system"));
        assertThat(entriesById.get("num"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getLogicalMemberIdentifier).isEqualTo("audittrail.test.Counter#num"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPreValue).isEqualTo("[NEW]"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPostValue).isNull());
        assertThat(entriesById.get("num2"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getLogicalMemberIdentifier).isEqualTo("audittrail.test.Counter#num2"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPreValue).isEqualTo("[NEW]"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPostValue).isNull());
    }

    @Test
    void updated_using_mixin() {

        // given
        var counter1 = counterRepository.persist(newCounter("counter-1"));
        val target1 = bookmarkService.bookmarkFor(counter1).orElseThrow();
        interactionService.nextInteraction();

        auditTrailEntryRepository.removeAll();
        interactionService.nextInteraction();

        assertThat(counterRepository.find()).hasSize(1);
        assertThat(auditTrailEntryRepository.findAll()).isEmpty();

        // when
        counter1 = bookmarkService.lookup(target1, Counter.class).orElseThrow();
        wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
        interactionService.nextInteraction();

        // then
        var entries = auditTrailEntryRepository.findAll();
        var propertyIds = entries.stream().map(AuditTrailEntry::getPropertyId).collect(Collectors.toList());
         assertThat(propertyIds).containsExactly("num");

        var entriesById = entries.stream().collect(Collectors.toMap(AuditTrailEntry::getPropertyId, x -> x));
        assertThat(entriesById.get("num"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getLogicalMemberIdentifier).isEqualTo("audittrail.test.Counter#num"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPreValue).isNull())
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPostValue).isEqualTo("1"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getInteractionId).isNotNull())
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getSequence).isEqualTo(0))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getTarget).isEqualTo(target1))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getTimestamp).isNotNull())
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getType).isEqualTo(DomainChangeRecord.ChangeType.AUDIT_ENTRY))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getUsername).isEqualTo("__system"));

        // given
        auditTrailEntryRepository.removeAll();
        interactionService.nextInteraction();

        // when bump again
        counter1 = bookmarkService.lookup(target1, Counter.class).orElseThrow();
        wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
        interactionService.nextInteraction();

        // then
        entries = auditTrailEntryRepository.findAll();
        propertyIds = entries.stream().map(AuditTrailEntry::getPropertyId).collect(Collectors.toList());
        assertThat(propertyIds).containsExactly("num");

        entriesById = entries.stream().collect(Collectors.toMap(AuditTrailEntry::getPropertyId, x -> x));
        assertThat(entriesById.get("num"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPreValue).isEqualTo("1"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPostValue).isEqualTo("2"));

    }

    @Test
    void deleted() {

        // given
        var counter1 = counterRepository.persist(newCounter("counter-1"));
        counter1.setNum(1L);
        counter1.setNum2(2L);
        var target1 = bookmarkService.bookmarkFor(counter1).orElseThrow();
        interactionService.nextInteraction();

        auditTrailEntryRepository.removeAll();
        interactionService.nextInteraction();

        // when
        counter1 = bookmarkService.lookup(target1, Counter.class).orElseThrow();
        counterRepository.remove(counter1);
        interactionService.nextInteraction();

        // then
        var entries = auditTrailEntryRepository.findAll();
        val propertyIds = entries.stream().map(AuditTrailEntry::getPropertyId).collect(Collectors.toList());
        assertThat(propertyIds).contains("name", "num", "num2");

        val entriesById = entries.stream().collect(Collectors.toMap(AuditTrailEntry::getPropertyId, x -> x));
        assertThat(entriesById.get("name"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getLogicalMemberIdentifier).isEqualTo("audittrail.test.Counter#name"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPreValue).isEqualTo("counter-1"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPostValue).isEqualTo("[DELETED]"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getInteractionId).isNotNull())
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getSequence).isEqualTo(0))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getTarget).isEqualTo(target1))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getTimestamp).isNotNull())
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getType).isEqualTo(DomainChangeRecord.ChangeType.AUDIT_ENTRY))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getUsername).isEqualTo("__system"));
        assertThat(entriesById.get("num"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getLogicalMemberIdentifier).isEqualTo("audittrail.test.Counter#num"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPreValue).isEqualTo("1"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPostValue).isEqualTo("[DELETED]"));
        assertThat(entriesById.get("num2"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getLogicalMemberIdentifier).isEqualTo("audittrail.test.Counter#num2"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPreValue).isEqualTo("2"))
                .satisfies(e -> assertThat(e).extracting(AuditTrailEntry::getPostValue).isEqualTo("[DELETED]"));

    }

    @Inject InteractionService interactionService;
    @Inject CounterRepository<? extends Counter> counterRepository;
    @Inject WrapperFactory wrapperFactory;
    @Inject BookmarkService bookmarkService;
    @Inject AuditTrailEntryRepository<? extends AuditTrailEntry> auditTrailEntryRepository;


}
