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
package org.apache.isis.extensions.executionoutbox.applib.spiimpl;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.extensions.executionoutbox.applib.IsisModuleExtExecutionOutboxApplib;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */

@Service
@Named(ExecutionSubscriberForExecutionOutbox.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Outbox")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2

public class ExecutionSubscriberForExecutionOutbox implements ExecutionSubscriber {

    static final String LOGICAL_TYPE_NAME = IsisModuleExtExecutionOutboxApplib.NAMESPACE + ".ExecutionSubscriberForExecutionOutbox";

    final ExecutionOutboxEntryRepository<? extends ExecutionOutboxEntry> repository;
    final IsisConfiguration isisConfiguration;

    @Override
    public boolean isEnabled() {
        return isisConfiguration.getExtensions().getExecutionOutbox().getPersist().isEnabled();
    }

    @Override
    public void onExecution(Execution<?, ?> execution) {
        if (!isEnabled()) {
            return;
        }

        repository.createEntryAndPersist(execution);
    }

}
