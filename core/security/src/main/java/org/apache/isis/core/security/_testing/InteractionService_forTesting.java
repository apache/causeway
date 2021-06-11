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
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionLayer;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.iactnlayer.ThrowingRunnable;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * A pass-through implementation, free of side-effects,
 * in support of simple JUnit tests.
 */
public class InteractionService_forTesting
implements InteractionService {

    private InteractionLayer interactionLayer = null;

    @Override
    public InteractionLayer openInteraction() {
        final UserMemento userMemento = UserMemento.system();
        final UUID uuid = UUID.randomUUID();
        final Interaction interaction = new Interaction() {
            @Override public <T> T putAttribute(Class<? super T> type, T value) { return null; }
            @Override public <T> T computeAttributeIfAbsent(Class<? super T> type, Function<Class<?>, ? extends T> mappingFunction) { return null; }
            @Override public <T> T getAttribute(Class<T> type) { return null; }
            @Override public void removeAttribute(Class<?> type) { }
            @Override public UUID getInteractionId() { return uuid; }
            @Override public Command getCommand() { return null; }
            @Override public Execution<?, ?> getCurrentExecution() { return null; }
            @Override public Execution<?, ?> getPriorExecution() { return null; }
        };
        interactionLayer = new InteractionLayer(interaction, InteractionContext.ofUserWithSystemDefaults(userMemento));
        return null;
    }

    @Override
    public InteractionLayer openInteraction(@NonNull InteractionContext interactionContext) {
        return openInteraction();
    }

    @Override
    public void closeInteractionLayers() {
        interactionLayer = null;
    }

    @Override
    public boolean isInInteraction() {
        return interactionLayer != null;
    }

    @Override public Optional<UUID> getInteractionId() {
        return currentInteractionLayer()
                .map(InteractionLayer::getInteraction)
                .map(Interaction::getInteractionId);
    }

    @Override public Optional<InteractionLayer> currentInteractionLayer() {
        return Optional.ofNullable(this.interactionLayer);
    }

    @Override public int getInteractionLayerCount() {
        return isInInteraction() ? 1 : 0;
    }

    @Override @SneakyThrows
    public <R> R call(@NonNull InteractionContext interactionContext, @NonNull Callable<R> callable) {
        return callable.call();
    }

    @Override @SneakyThrows
    public void run(@NonNull InteractionContext interactionContext, @NonNull ThrowingRunnable runnable) {
        runnable.run();
    }


    @Override @SneakyThrows
    public void runAnonymous(@NonNull ThrowingRunnable runnable) {
        runnable.run();
    }

    @Override @SneakyThrows
    public <R> R callAnonymous(@NonNull Callable<R> callable) {
        return callable.call();
    }

}
