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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.user.RoleMemento;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;

import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategyParameters;
import graphql.execution.FieldValueInfo;
import lombok.RequiredArgsConstructor;

@Service
public class AsyncExecutionStrategyResolvingWithinInteraction extends AsyncExecutionStrategy {

    private final InteractionService interactionService;

    private final UserMemento userMemento;

    public AsyncExecutionStrategyResolvingWithinInteraction(
            final InteractionService interactionService,
            final CausewayConfiguration causewayConfiguration) {
        this.interactionService = interactionService;

        String fallbackUsername = causewayConfiguration.getViewer().getGqlv().getAuthentication().getFallback().getUsername();
        List<String> fallbackRoles = causewayConfiguration.getViewer().getGqlv().getAuthentication().getFallback().getRoles();
        userMemento = fallbackUsername != null
                        ? UserMemento.builder()
                            .name(fallbackUsername)
                            .roles(Can.ofStream(
                                    fallbackRoles.stream()
                                                .map(roleName -> RoleMemento.builder()
                                                                    .name(roleName)
                                                                    .build()
                                                )
                                    )
                            ).build()
                        : null;
    }


    @Override
    protected CompletableFuture<FieldValueInfo> resolveFieldWithInfo(
            final ExecutionContext executionContext,
            final ExecutionStrategyParameters parameters) {

        // TODO: propagate identity from executionContext
        // interactionService.openInteraction(InteractionContext.builder().user(UserMemento.builder().build()).build());

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
