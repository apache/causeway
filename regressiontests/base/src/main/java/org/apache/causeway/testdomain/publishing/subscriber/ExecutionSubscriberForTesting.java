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
package org.apache.causeway.testdomain.publishing.subscriber;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.causeway.applib.util.schema.MemberExecutionDtoUtils;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.schema.ixn.v2.MemberExecutionDto;
import org.apache.causeway.testdomain.util.kv.KVStoreForTesting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Priority(PriorityPrecedence.LATE)
@Qualifier("Testing")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Slf4j
public class ExecutionSubscriberForTesting
implements ExecutionSubscriber {

    private final KVStoreForTesting kvStore;

    @PostConstruct
    public void init() {
        log.info("about to initialize");
    }

    @Override
    public void onExecution(final Execution<?, ?> execution) {

        @SuppressWarnings("unchecked")
        var publishedEntries =
        (List<Execution<?, ?>>) kvStore.get(this, "publishedExecutions").orElseGet(ArrayList::new);

        publishedEntries.add(execution);

        kvStore.put(this, "publishedExecutions", publishedEntries);
        if(log.isDebugEnabled()) {
            final MemberExecutionDto dto = execution.getDto();
            log.debug("publish execution {}", MemberExecutionDtoUtils.dtoMapper(dto.getClass()).toString(dto));
        }
    }

    // -- UTILITIES

    @SuppressWarnings("unchecked")
    public static Can<Execution<?, ?>> getPublishedExecutions(final KVStoreForTesting kvStore) {
        return Can.ofCollection(
                (List<Execution<?, ?>>) kvStore.get(ExecutionSubscriberForTesting.class, "publishedExecutions")
                .orElse(null));
    }

    public static void clearPublishedEntries(final KVStoreForTesting kvStore) {
        kvStore.clear(ExecutionSubscriberForTesting.class);
    }

}
