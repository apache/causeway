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
package org.apache.causeway.viewer.graphql.viewer.integration;

import java.util.concurrent.CompletableFuture;

import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategyParameters;
import graphql.execution.FieldValueInfo;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.viewer.graphql.applib.auth.UserMementoProvider;

@Service
public class AsyncExecutionStrategyResolvingWithinInteraction extends AsyncExecutionStrategy {

    private final InteractionService interactionService;

    private final UserMementoProvider userMementoProvider;

    public AsyncExecutionStrategyResolvingWithinInteraction(
            final InteractionService interactionService,
            final UserMementoProvider userMementoProvider) {

        this.interactionService = interactionService;
        this.userMementoProvider = userMementoProvider;
    }

    @Override
    protected CompletableFuture<FieldValueInfo> resolveFieldWithInfo(
            final ExecutionContext executionContext,
            final ExecutionStrategyParameters parameters) {

        var userMemento = userMementoProvider.userMemento(executionContext, parameters);

        if (userMemento != null) {
            return interactionService.call(
                    InteractionContext.builder().user(userMemento).build(),
                    () -> super.resolveFieldWithInfo(executionContext, parameters)
            );
        } else {
            return interactionService.callAnonymous(
                    () -> super.resolveFieldWithInfo(executionContext, parameters)
            );
        }
    }
}
