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
package org.apache.causeway.extensions.executionlog.applib.contributions;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.mixins.system.HasInteractionId;
import org.apache.causeway.extensions.executionlog.applib.CausewayModuleExtExecutionLogApplib;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * Contributes a <code>executionLogEntries</code> collection to any object implementing {@link HasInteractionId},
 * in other words the executions that occurred relating to an
 * {@link org.apache.causeway.applib.services.iactn.Interaction}.
 *
 * <p>
 *     For example, the <code>CommandLogEntry</code> (in the <i>command log</i> extension) or the
 *     <code>AuditTrailEntry</code> entity (in the <i>audit trail</i> extension) both implement
 *     {@link HasInteractionId}.
 * </p>
 *
 * @since 2.x {@index}
 */
@Collection(
        domainEvent = HasInteractionId_executionLogEntries.CollectionDomainEvent.class
)
@RequiredArgsConstructor
public class HasInteractionId_executionLogEntries {

    private final HasInteractionId hasInteractionId;

    public static class CollectionDomainEvent
            extends CausewayModuleExtExecutionLogApplib.CollectionDomainEvent<HasInteractionId_executionLogEntries, ExecutionLogEntry> { }

    @MemberSupport public List<? extends ExecutionLogEntry> coll() {
        return executionLogEntryRepository.findByInteractionId(hasInteractionId.getInteractionId());
    }

    @MemberSupport public boolean hideColl() {
        // for the ELE itself, we provide the 'siblingExecutions' mixin.
        return hasInteractionId instanceof ExecutionLogEntry;
    }

    @Inject ExecutionLogEntryRepository<? extends ExecutionLogEntry> executionLogEntryRepository;

}
