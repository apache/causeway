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
package org.apache.isis.extensions.audittrail.applib.integtests;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.extensions.audittrail.applib.dom.AuditTrailEntry;
import org.apache.isis.extensions.audittrail.applib.dom.AuditTrailEntryRepository;
import org.apache.isis.extensions.audittrail.applib.integtests.model.Counter;
import org.apache.isis.extensions.audittrail.applib.integtests.model.CounterRepository;
import org.apache.isis.extensions.audittrail.applib.integtests.model.Counter_bumpUsingMixin;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

public abstract class AuditTrail_IntegTestAbstract extends IsisIntegrationTestAbstract {

    @BeforeAll
    static void beforeAll() {
        IsisPresets.forcePrototyping();
    }

    Counter counter1;
    Counter counter2;

    @BeforeEach
    void setUp() {
        counterRepository.removeAll();
        auditTrailEntryRepository.removeAll();

        assertThat(counterRepository.find()).isEmpty();

        counter1 = counterRepository.persist(newCounter("counter-1"));
        counter2 = counterRepository.persist(newCounter("counter-2"));

        assertThat(counterRepository.find()).hasSize(2);

        List<? extends AuditTrailEntry> mostRecentCompleted = auditTrailEntryRepository.findAll();
        assertThat(mostRecentCompleted).isEmpty();
    }

    protected abstract Counter newCounter(String name);

    @Test
    void mixin() {

        // when
        wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        List<? extends AuditTrailEntry> entries = auditTrailEntryRepository.findAll();
        assertThat(entries).hasSize(4); // Counter has four properties

        Bookmark target = bookmarkService.bookmarkFor(counter1).orElseThrow();

        auditTrailEntryRepository.findRecentByTarget()
    }


    @Inject InteractionService interactionService;
    @Inject CounterRepository counterRepository;
    @Inject WrapperFactory wrapperFactory;
    @Inject BookmarkService bookmarkService;
    @Inject AuditTrailEntryRepository<? extends AuditTrailEntry> auditTrailEntryRepository;


}
