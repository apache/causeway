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
package org.apache.causeway.extensions.executionoutbox.applib.app;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.extensions.executionoutbox.applib.CausewayModuleExtExecutionOutboxApplib;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * This service exposes a set of menu actions to search and list {@link ExecutionOutboxEntry execution}s in the outbox,
 * by default under the &quot;Activity&quot; secondary menu.
 *
 * @since 2.0 {@index}
 */
@Named(ExecutionOutboxMenu.LOGICAL_TYPE_NAME)
@DomainService(
    nature = NatureOfService.VIEW
)
@DomainServiceLayout(
    named = "Activity",
    menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class ExecutionOutboxMenu {

    public static final String LOGICAL_TYPE_NAME =
            CausewayModuleExtExecutionOutboxApplib.NAMESPACE + ".ExecutionOutboxMenu";

    public static abstract class ActionDomainEvent
        extends CausewayModuleExtExecutionOutboxApplib.ActionDomainEvent<ExecutionOutboxMenu> { }


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(describedAs = "Returns to oldest 100 entries (next to be processed) in the outbox")
    public List<? extends ExecutionOutboxEntry> findOldest() {
        return executionOutboxEntryRepository.findOldest();
    }

    @Action(semantics = SemanticsOf.SAFE, restrictTo = RestrictTo.PROTOTYPING)
    @ActionLayout(describedAs = "Returns all entries (still to be processed) in the outbox")
    public List<? extends ExecutionOutboxEntry> findAll() {
        return executionOutboxEntryRepository.findAll();
    }

    final ExecutionOutboxEntryRepository<? extends ExecutionOutboxEntry> executionOutboxEntryRepository;
    final ClockService clockService;



    private LocalDate now() {
        return clockService.getClock().nowAsLocalDate(ZoneId.systemDefault());
    }
}

