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

package org.apache.causeway.core.runtimeservices.session;

import java.util.UUID;

import jakarta.annotation.Priority;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link InteractionIdGenerator}, which simply returns a
 * {@link UUID#randomUUID() random UUID}.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".InteractionIdGenerator")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Slf4j
public class InteractionIdGeneratorDefault implements InteractionIdGenerator {

    @Override
    public UUID interactionId() {
        return UUID.randomUUID();
    }

}
