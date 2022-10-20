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
package org.apache.causeway.applib.services.iactnlayer;

import java.util.concurrent.Callable;

import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.functional.Try;

import lombok.NonNull;

/**
 * A low-level service to programmatically &quot;connect&quot; (or create a
 * session, or interact with; choose your term) the the framework's runtime.
 *
 * <p>
 *     This service is used internally by the framework itself, for example
 *     when a viewer receives a request a new {@link InteractionLayer} is created
 *     for the duration of the users's interaction.  It is also used by integration
 *     tests, to be able to connect to the database.
 * </p>
 *
 * <p>
 *     You could think of this as analogous to an <code>HttpRequest</code>, or
 *     a JPA <code>EntityManager</code> or JDO <code>PersistenceManager</code>.
 * </p>
 *
 * <p>
 *     There are two main APIs exposed.  One is to
 *     {@link #openInteraction(InteractionContext) open} a new {@link InteractionLayer},
 *     to be {@link #closeInteractionLayers() closed later}.  The other is to
 *     execute a {@link Callable} or {@link ThrowingRunnable runnable} within the
 *     duration of an {@link InteractionLayer}, wrapping up automatically.
 *     This is what is used by {@link org.apache.causeway.applib.services.sudo.SudoService}, for example.
 * </p>
 *
 * @see org.apache.causeway.applib.services.sudo.SudoService
 * @see InteractionLayerTracker
 * @since 2.x {@index}
 */
public interface InteractionService extends InteractionLayerTracker {

    /**
     * If present, reuses the current top level {@link InteractionLayer}, otherwise creates a new
     * anonymous one.
     *
     * @see #openInteraction(InteractionContext)
     */
    InteractionLayer openInteraction();

    /**
     * Returns a new or reused {@link InteractionLayer} that is a holder of the {@link InteractionContext}
     * on top of the current thread's interaction layer stack.
     *
     * <p>
     * If available reuses an existing {@link InteractionContext}, otherwise creates a new one.
     * </p>
     *
     * <p>
     * The {@link InteractionLayer} represents a user's span of activities interacting with
     * the application.  These can be stacked (usually temporarily), for example for a sudo
     * session or to mock the clock.  The stack is later closed using {@link #closeInteractionLayers()}.
     * </p>
     *
     * @param interactionContext
     *
     * @apiNote if the current {@link InteractionLayer} (if any) has an {@link InteractionContext} that
     * equals that of the given one, as an optimization, no new layer is pushed onto the stack;
     * instead the current one is returned
     */
    InteractionLayer openInteraction(
            @NonNull InteractionContext interactionContext);

    /**
     * closes all open {@link InteractionLayer}(s) as stacked on the current thread
     */
    void closeInteractionLayers();

    /**
     * @return whether the calling thread is within the context of an open {@link InteractionLayer}
     */
    @Override
    boolean isInInteraction();

    /**
     * Executes a block of code with a new or reused {@link InteractionContext} using a new or
     * reused {@link InteractionLayer}.
     *
     * <p>
     * If there is currently no {@link InteractionLayer} a new one is created.
     * </p>
     *
     * <p>
     * If there is currently an {@link InteractionLayer} that has an equal {@link InteractionContext}
     * to the given one, it is reused, otherwise a new one is created.
     * </p>
     *
     * @param interactionContext - the context to run under (non-null)
     * @param callable - the piece of code to run (non-null)
     */
    <R> R call(@NonNull InteractionContext interactionContext, @NonNull Callable<R> callable);

    /**
     * Variant of {@link #call(InteractionContext, Callable)} that takes a runnable.
     *
     * @param interactionContext - the user details to run under (non-null)
     * @param runnable (non-null)
     */
    void run(@NonNull InteractionContext interactionContext, @NonNull ThrowingRunnable runnable);

    /**
     * As per {@link #call(InteractionContext, Callable)}, but using an
     * anonymous {@link InteractionContext}.
     *
     * @param <R>
     * @param callable (non-null)
     */
    <R> R callAnonymous(@NonNull Callable<R> callable);

    /**
     * As per {@link #callAnonymous(Callable)}, but for a runnable.
     *
     * @param runnable (non-null)
     */
    void runAnonymous(@NonNull ThrowingRunnable runnable);

    // -- TRY SUPPORT

    /**
     * Variant of {@link #call(InteractionContext, Callable)} that wraps the return value
     * with a {@link Try}, also catching any exception, that might have occurred.
     */
    default <R> Try<R> callAndCatch(
            final @NonNull InteractionContext interactionContext,
            final @NonNull Callable<R> callable) {
        return Try.call(()->call(interactionContext, callable));
    }

    /**
     * Variant of {@link #run(InteractionContext, ThrowingRunnable)} that returns
     * a {@link Try} of {@code Result<Void>},
     * also catching any exception, that might have occurred.
     */
    default Try<Void> runAndCatch(
            final @NonNull InteractionContext interactionContext,
            final @NonNull ThrowingRunnable runnable){
        return callAndCatch(interactionContext, ThrowingRunnable.toCallable(runnable));
    }

    /**
     * Variant of {@link #callAnonymous(Callable)} that wraps the return value
     * with a {@link Try}, also catching any exception, that might have occurred.
     */
    default <R> Try<R> callAnonymousAndCatch(
            final @NonNull Callable<R> callable) {
        return Try.call(()->callAnonymous(callable));
    }

    /**
     * Variant of {@link #runAnonymous(ThrowingRunnable)} that returns
     * a {@link Try} of {@code Try<Void>},
     * also catching any exception, that might have occurred.
     */
    default Try<Void> runAnonymousAndCatch(
            final @NonNull ThrowingRunnable runnable) {
        return callAnonymousAndCatch(ThrowingRunnable.toCallable(runnable));
    }

    /**
     * Primarily for testing, closes the current interaction and opens a new one.
     *
     * <p>
     *     In tests, this is a good way to simulate multiple interactions within a scenario.  If you use the popular
     *     given/when/then structure, consider using at the end of each "given" or "when" block.
     * </p>
     *
     * @see #closeInteractionLayers()
     * @see #openInteraction()
     * @see #nextInteraction(InteractionContext)
     */
    default InteractionLayer nextInteraction() {
        closeInteractionLayers();
        return openInteraction();
    }

    /**
     * Primarily for testing, closes the current interaction and opens a new one with the specified
     * {@link InteractionContext}.
     *
     * @see #closeInteractionLayers()
     * @see #openInteraction(InteractionContext)
     * @see #nextInteraction()
     */
    default InteractionLayer nextInteraction(final InteractionContext interactionContext) {
        closeInteractionLayers();
        return openInteraction(interactionContext);
    }

}
