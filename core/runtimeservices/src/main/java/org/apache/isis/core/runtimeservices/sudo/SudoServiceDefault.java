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

package org.apache.isis.core.runtimeservices.sudo;

import java.util.concurrent.Callable;
import java.util.function.UnaryOperator;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.iactnlayer.InteractionTracker;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.sudo.SudoServiceListener;
import org.apache.isis.commons.collections.Can;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import jakarta.annotation.PostConstruct;

@Service
@Named("isis.applib.SudoServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SudoServiceDefault implements SudoService {

    private final InteractionService interactionService;
    private final InteractionTracker interactionTracker;

    // -- LISTENERS

    private Can<SudoServiceListener> sudoListeners = Can.empty();

    @PostConstruct @Inject
    public void init(final ServiceRegistry serviceRegistry) {
        this.sudoListeners = serviceRegistry.select(SudoServiceListener.class);
    }

    // -- IMPLEMENTATION

    @Override
    public <T> T call(
            final @NonNull UnaryOperator<InteractionContext> sudoMapper,
            final @NonNull Callable<T> callable) {

        val currentInteractionLayer = interactionTracker.currentInteractionLayerElseFail();
        val currentInteractionContext = currentInteractionLayer.getInteractionContext();
        val sudoInteractionContext = sudoMapper.apply(currentInteractionContext);

        try {
            beforeCall(currentInteractionContext, sudoInteractionContext);

            return interactionService
                    .call(sudoInteractionContext, callable);
        } finally {
            afterCall(sudoInteractionContext, currentInteractionContext);
        }
    }

    // -- HELPER

    private void beforeCall(
            final @NonNull InteractionContext before,
            final @NonNull InteractionContext after) {
        for (val sudoListener : sudoListeners) {
            sudoListener.beforeCall(before, after);
        }
    }

    private void afterCall(
            final @NonNull InteractionContext before,
            final @NonNull InteractionContext after) {
        for (val sudoListener : sudoListeners) {
            sudoListener.afterCall(before, after);
        }
    }





}
