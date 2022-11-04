/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.executionrepublisher.applib.contributions;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.*;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntryType;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryRepository;
import org.apache.causeway.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryType;
import org.apache.causeway.extensions.executionrepublisher.applib.CausewayModuleExtExecutionRepublisherApplib;

import lombok.RequiredArgsConstructor;

/**
 * This contributes a copyToOutbox action to each {@link ExecutionLogEntry} (from the <i>Execution Log</i> extension)
 * so that it can be republished in the outbox.
 *
 * <p>
 *     This is useful when both the <i>Execution Log</i> and <i>Execution Outbox</i> extensions are in use, and there
 *     was a downstream problem with the processing of an execution <i>from the outbox</i>; the mixin takes a copy of
 *     that execution from the log and copies it to the outbox in order that it can be reprocessed again.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = ExecutionLogEntry_copyToOutbox.ActionDomainEvent.class,
        semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE
)
@ActionLayout(
        position = ActionLayout.Position.PANEL,
        cssClassFa = "share-alt",
        cssClass = "btn-warning"
)
@RequiredArgsConstructor
public class ExecutionLogEntry_copyToOutbox {

    private final ExecutionLogEntry executionLogEntry;


    public static class ActionDomainEvent extends CausewayModuleExtExecutionRepublisherApplib.ActionDomainEvent<ExecutionLogEntry_copyToOutbox> { }

    @MemberSupport public ExecutionLogEntry act() {

        outboxRepository.upsert(
                executionLogEntry.getInteractionId(),
                executionLogEntry.getSequence(),
                map(executionLogEntry.getExecutionType()),
                executionLogEntry.getTimestamp(),
                executionLogEntry.getUsername(),
                executionLogEntry.getTarget(),
                executionLogEntry.getLogicalMemberIdentifier(),
                executionLogEntry.getInteractionDto()
        );

        return executionLogEntry;
    }

    @Programmatic
    static ExecutionOutboxEntryType map(ExecutionLogEntryType executionType) {
        return executionType == ExecutionLogEntryType.ACTION_INVOCATION
                ? ExecutionOutboxEntryType.ACTION_INVOCATION
                : ExecutionOutboxEntryType.PROPERTY_EDIT;
    }

    @Inject ExecutionOutboxEntryRepository<? extends ExecutionOutboxEntry> outboxRepository;

}
