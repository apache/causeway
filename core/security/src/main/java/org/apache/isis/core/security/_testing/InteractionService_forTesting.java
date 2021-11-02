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
package org.apache.isis.core.security._testing;

import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionLayer;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.commons.functional.ThrowingRunnable;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * A pass-through implementation, free of side-effects,
 * in support of simple JUnit tests.
 */
public class InteractionService_forTesting
implements InteractionService {

    private Stack<InteractionLayer> interactionLayers = new Stack<>();

    @Override
    public InteractionLayer openInteraction() {
        final UserMemento userMemento = UserMemento.system();
        return openInteraction(InteractionContext.ofUserWithSystemDefaults(userMemento));
    }

    @Override
    public InteractionLayer openInteraction(@NonNull final InteractionContext interactionContext) {
        final Interaction interaction = new Interaction_forTesting();
        return interactionLayers.push(
                new InteractionLayer(interaction, interactionContext));
    }

    @Override
    public void closeInteractionLayers() {
        interactionLayers.clear();
    }

    @Override
    public boolean isInInteraction() {
        return interactionLayers.size()>0;
    }

    @Override public Optional<UUID> getInteractionId() {
        return currentInteractionLayer()
                .map(InteractionLayer::getInteraction)
                .map(Interaction::getInteractionId);
    }

    @Override public Optional<InteractionLayer> currentInteractionLayer() {
        return interactionLayers.isEmpty()
                ? Optional.empty()
                : Optional.of(interactionLayers.peek());
    }

    @Override public int getInteractionLayerCount() {
        return interactionLayers.size();
    }

    @Override @SneakyThrows
    public <R> R call(@NonNull final InteractionContext interactionContext, @NonNull final Callable<R> callable) {
        try {
            openInteraction(interactionContext);
            return callable.call();
        } finally {
            interactionLayers.pop();
        }
    }

    @Override @SneakyThrows
    public void run(@NonNull final InteractionContext interactionContext, @NonNull final ThrowingRunnable runnable) {
        try {
            openInteraction(interactionContext);
            runnable.run();
        } finally {
            interactionLayers.pop();
        }
    }


    @Override @SneakyThrows
    public void runAnonymous(@NonNull final ThrowingRunnable runnable) {
        try {
            openInteraction();
            runnable.run();
        } finally {
            interactionLayers.pop();
        }
    }

    @Override @SneakyThrows
    public <R> R callAnonymous(@NonNull final Callable<R> callable) {
        try {
            openInteraction();
            return callable.call();
        } finally {
            interactionLayers.pop();
        }
    }

    @RequiredArgsConstructor
    static class Interaction_forTesting implements Interaction {
        private final UUID uuid = UUID.randomUUID();
        @Override public <T> T putAttribute(final Class<? super T> type, final T value) { return null; }
        @Override public <T> T computeAttributeIfAbsent(final Class<? super T> type, final Function<Class<?>, ? extends T> mappingFunction) { return null; }
        @Override public <T> T getAttribute(final Class<T> type) { return null; }
        @Override public void removeAttribute(final Class<?> type) { }
        @Override public UUID getInteractionId() { return uuid; }
        @Override public Command getCommand() { return null; }
        @Override public Execution<?, ?> getCurrentExecution() { return null; }
        @Override public Execution<?, ?> getPriorExecution() { return null; }
    };

}
