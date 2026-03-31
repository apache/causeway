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
package org.apache.causeway.core.mmtestsupport;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.core.metamodel.interactions.layer.InteractionCarrier;
import org.apache.causeway.core.metamodel.interactions.layer.InteractionLayer;
import org.apache.causeway.core.metamodel.interactions.layer.InteractionLayerStack;
import org.apache.causeway.core.metamodel.interactions.layer.InteractionLayerTracker;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import io.micrometer.observation.Observation;

/**
 * A pass-through implementation, free of side-effects,
 * in support of simple JUnit tests.
 */
public class InteractionService_forTesting
implements InteractionService, InteractionLayerTracker {

    final InteractionLayerStack layerStack = new InteractionLayerStack();
    
    @Override
    public Interaction openInteraction() {
        return openInteractionLayer().interaction();
    }

    @Override
    public Interaction openInteraction(final @NonNull InteractionContext interactionContext) {
    	return openInteractionLayer(interactionContext).interaction();
    }

    private InteractionLayer openInteractionLayer() {
        final UserMemento userMemento = UserMemento.system();
        return openInteractionLayer(InteractionContext.ofUserWithSystemDefaults(userMemento));
    }

    private InteractionLayer openInteractionLayer(final @NonNull InteractionContext interactionContext) {
        final Interaction interaction = new Interaction_forTesting();
        var interactionCarrier = new InteractionCarrier() {
			@Override public Interaction interaction() {
				return interaction;
			}
		};
        
        return layerStack.push(interactionCarrier, interactionContext, Observation.NOOP);
    }

    @Override
    public void closeInteractionLayers() {
        layerStack.clear();
    }

    @Override
    public boolean isInInteraction() {
        return !layerStack.isEmpty();
    }

    @Override public Optional<UUID> getInteractionId() {
        return currentInteractionLayer()
                .map(InteractionLayer::interaction)
                .map(Interaction::getInteractionId);
    }

    @Override public Optional<InteractionLayer> currentInteractionLayer() {
        return layerStack.isEmpty()
                ? Optional.empty()
                : Optional.of(layerStack.peek());
    }

    @Override public int getInteractionLayerCount() {
        return layerStack.size();
    }

    @Override @SneakyThrows
    public <R> R call(final @NonNull InteractionContext interactionContext, final @NonNull Callable<R> callable) {
        try {
            openInteraction(interactionContext);
            return callable.call();
        } finally {
            layerStack.pop();
        }
    }

    @Override @SneakyThrows
    public void run(final @NonNull InteractionContext interactionContext, final @NonNull ThrowingRunnable runnable) {
        try {
            openInteraction(interactionContext);
            runnable.run();
        } finally {
            layerStack.pop();
        }
    }

    @Override @SneakyThrows
    public void runAnonymous(final @NonNull ThrowingRunnable runnable) {
        try {
            openInteraction();
            runnable.run();
        } finally {
            layerStack.pop();
        }
    }

    @Override @SneakyThrows
    public <R> R callAnonymous(final @NonNull Callable<R> callable) {
        try {
            openInteraction();
            return callable.call();
        } finally {
            layerStack.pop();
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
    }

}
