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
package org.apache.causeway.applib.services.publishing.log;

import jakarta.annotation.Priority;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.causeway.applib.util.schema.InteractionDtoUtils;
import org.apache.causeway.schema.ixn.v2.InteractionDto;

import lombok.extern.slf4j.Slf4j;

/**
 * Simple implementation of {@link ExecutionSubscriber} that just logs out the {@link Execution}'s
 * DTO to a debug log.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(ExecutionLogger.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.LATE)
@Qualifier("Logging")
@Slf4j
public class ExecutionLogger implements ExecutionSubscriber {

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".ExecutionLogger";

    @Override
    public boolean isEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void onExecution(final Execution<?, ?> execution) {

        final InteractionDto interactionDto =
                InteractionDtoUtils.newInteractionDto(execution, InteractionDtoUtils.Strategy.DEEP);

        log.debug(InteractionDtoUtils.dtoMapper().toString(interactionDto));
    }

}

