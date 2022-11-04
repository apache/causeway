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
package org.apache.causeway.extensions.executionlog.applib.spiimpl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.causeway.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@Service
@RequiredArgsConstructor
public class ExecutionSubscriberForExecutionLog implements ExecutionSubscriber {

    final @Inject ExecutionLogEntryRepository repository;

    @Override
    public void onExecution(Execution<?, ?> execution) {
        repository.createEntryAndPersist(execution);
    }

}
