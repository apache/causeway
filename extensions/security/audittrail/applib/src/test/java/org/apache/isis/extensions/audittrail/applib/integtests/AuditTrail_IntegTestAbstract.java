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
import org.junit.jupiter.api.Disabled;
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

import lombok.val;

public abstract class AuditTrail_IntegTestAbstract extends IsisIntegrationTestAbstract {

    @BeforeAll
    static void beforeAll() {
        IsisPresets.forcePrototyping();
    }

    Bookmark target1, target2;


    @BeforeEach
    void setUp() {
        counterRepository.removeAll();
        auditTrailEntryRepository.removeAll();

        assertThat(counterRepository.find()).isEmpty();

        val counter1 = counterRepository.persist(newCounter("counter-1"));
        val counter2 = counterRepository.persist(newCounter("counter-2"));
        target1 = bookmarkService.bookmarkFor(counter1).orElseThrow();
        target2 = bookmarkService.bookmarkFor(counter2).orElseThrow();

        assertThat(counterRepository.find()).hasSize(2);

        List<? extends AuditTrailEntry> mostRecentCompleted = auditTrailEntryRepository.findAll();
        assertThat(mostRecentCompleted).isEmpty();
    }

    protected abstract Counter newCounter(String name);

    // @Disabled   // currently failing for JDO (and JPA not yet implemented anyway)
    @Test
    void mixin() {

        // when
        val counter1 = bookmarkService.lookup(target1).orElseThrow();
        wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
        interactionService.closeInteractionLayers();    // to flush

        interactionService.openInteraction();

        // then
        val entries = auditTrailEntryRepository.findAll();
        assertThat(entries).hasSize(3); // Counter has three properties

        val recentByTarget = auditTrailEntryRepository.findRecentByTarget(target1);
        assertThat(recentByTarget).hasSize(3);
    }


    @Inject InteractionService interactionService;
    @Inject CounterRepository counterRepository;
    @Inject WrapperFactory wrapperFactory;
    @Inject BookmarkService bookmarkService;
    @Inject AuditTrailEntryRepository<? extends AuditTrailEntry> auditTrailEntryRepository;


}
