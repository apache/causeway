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
package org.apache.causeway.extensions.executionlog.applib.dom.mixins;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.extensions.executionlog.applib.CausewayModuleExtExecutionLogApplib;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Contributes a <code>siblingExecutions</code> collection to {@link ExecutionLogEntry}.
 *
 * @since 2.0 {@index}
 */
@Collection(
        domainEvent = ExecutionLogEntry_siblingExecutions.CollectionDomainEvent.class
)
@RequiredArgsConstructor
public class ExecutionLogEntry_siblingExecutions {

    private final ExecutionLogEntry executionLogEntry;

    public static class CollectionDomainEvent
            extends CausewayModuleExtExecutionLogApplib.CollectionDomainEvent<ExecutionLogEntry_siblingExecutions, ExecutionLogEntry> { }


    @MemberSupport public List<? extends ExecutionLogEntry> coll() {
        val entries = new ArrayList<>(executionLogEntryRepository.findByInteractionId(executionLogEntry.getInteractionId()));
        entries.remove(executionLogEntry);
        return entries;
    }

    @Inject ExecutionLogEntryRepository<? extends ExecutionLogEntry> executionLogEntryRepository;

}
