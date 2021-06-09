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

package org.apache.isis.core.interaction.session;

import java.util.concurrent.Callable;

import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.ThrowingRunnable;
import org.apache.isis.core.security.authentication.Authentication;
import org.apache.isis.core.security.authentication.manager.AnonymousInteractionFactory;

import lombok.NonNull;

/**
 * The factory of {@link Authentication}(s) and {@link InteractionLayer}(s),
 * holding a reference to the current
 * {@link InteractionLayer authentication layer} stack using
 * a thread-local.
 *
 * <p>
 * @apiNote This is a framework internal class and so does not constitute a formal API.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface InteractionFactory
extends AnonymousInteractionFactory {

    /**
     * If present, reuses the current top level {@link InteractionLayer}, otherwise creates a new
     * anonymous one.
     *
     * @see #openInteraction(InteractionContext)
     */
    InteractionLayer openInteraction();

    /**
     * Returns a new or reused {@link InteractionLayer} that is a holder of {@link Authentication}
     * on top of the current thread's authentication layer stack.
     * <p>
     * If available reuses an existing {@link Authentication}, otherwise creates a new one.
     * <p>
     * The {@link InteractionLayer} represents a user's span of activities interacting with
     * the application. The session's stack is later closed using {@link #closeInteractionLayers()}.
     *
     * @param interactionContext
     *
     * @apiNote if the current {@link InteractionLayer} (if any) has an {@link Authentication} that
     * equals that of the given one, as an optimization, no new layer is pushed onto the stack;
     * instead the current one is returned
     */
    InteractionLayer openInteraction(
            @NonNull InteractionContext interactionContext);

    /**
     * @return whether the calling thread is within the context of an open {@link InteractionLayer}
     */
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
     * As per {@link #call(InteractionContext, Callable)}, using the {@link InteractionContext}
     * {@link Authentication#getInteractionContext() obtained} from the provided {@link Authentication}.
     *
     * @param authentication - the user details to run under (non-null)
     * @param callable - the piece of code to run (non-null)
     */
    <R> R callAuthenticated(@NonNull Authentication authentication, @NonNull Callable<R> callable);

    /**
     * Variant of {@link #call(InteractionContext, Callable)} that takes a runnable.
     *
     * @param interactionContext - the user details to run under (non-null)
     * @param runnable (non-null)
     */
    void run(@NonNull InteractionContext interactionContext, @NonNull ThrowingRunnable runnable);

    /**
     * As per {@link #callAuthenticated(Authentication, Callable)}, but for a runnable.
     *
     * @param authentication - the user details to run under (non-null)
     * @param runnable (non-null)
     */
    void runAuthenticated(@NonNull Authentication authentication, @NonNull ThrowingRunnable runnable);

    /**
     * As per {@link #call(InteractionContext, Callable)}, but using an {@link InteractionContext}
     * {@link Authentication#getInteractionContext() obtained} from an anonymous {@link Authentication}.
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
    @Override
    void runAnonymous(@NonNull ThrowingRunnable runnable);

    /**
     * closes all open {@link InteractionLayer}(s) as stacked on the current thread
     */
    void closeInteractionLayers();


}
