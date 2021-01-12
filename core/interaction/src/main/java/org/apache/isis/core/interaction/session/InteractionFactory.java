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

import org.apache.isis.commons.functional.ThrowingRunnable;
import org.apache.isis.core.security.authentication.Authentication;

import lombok.NonNull;

/**
 * The factory of {@link InteractionSession}(s) and {@link AuthenticationLayer}(s), 
 * also holding a reference to the current authentication layer stack using
 * a thread-local.
 * <p>
 * The implementation is a singleton service.
 */
public interface InteractionFactory {

    /**
     * If present, reuses the current top level {@link AuthenticationLayer}, otherwise creates a new 
     * anonymous one.
     * @see {@link #openInteraction(Authentication)}
     */
    AuthenticationLayer openInteraction();
    
    /**
     * Returns a new or reused {@link AuthenticationLayer} that is a holder of {@link Authentication} 
     * on top of the current thread's authentication layer stack.
     * <p>
     * If available reuses an existing {@link InteractionSession}, otherwise creates a new one.
     * <p> 
     * The {@link InteractionSession} represents a user's span of activities interacting with 
     * the application. The session's stack is later closed using {@link #closeSessionStack()}.
     *
     * @param authentication - the {@link Authentication} to associate with the new top of 
     * the stack (non-null)
     * 
     * @apiNote if the current {@link AuthenticationLayer} (if any) has an {@link Authentication} that
     * equals that of the given one, as an optimization, no new layer is pushed onto the stack; 
     * instead the current one is returned
     */
    AuthenticationLayer openInteraction(@NonNull Authentication authentication);

    /**
     * @return whether the calling thread is within the context of an open {@link InteractionSession} 
     */
    boolean isInInteractionSession();
    
    /**
     * Executes a block of code with a new or reused {@link InteractionSession} using a new or 
     * reused {@link AuthenticationLayer}.
     * <p>
     * If there is currently no {@link InteractionSession} a new one is created.
     * <p>
     * If there is currently an {@link AuthenticationLayer} that has an equal {@link Authentication}
     * to the given one, it is reused, otherwise a new one is created.
     * 
     * @param authentication - the user details to run under (non-null)
     * @param callable - the piece of code to run (non-null)
     * 
     */
    <R> R callAuthenticated(@NonNull Authentication authentication, @NonNull Callable<R> callable);
    
    /**
     * Variant of {@link #callAuthenticated(Authentication, Callable)} that takes a runnable.
     * @param authentication - the user details to run under (non-null)
     * @param runnable (non-null)
     */
    void runAuthenticated(@NonNull Authentication authentication, @NonNull ThrowingRunnable runnable);

    /**
     * Executes a block of code with a new or reused {@link InteractionSession} using a new or 
     * reused {@link AuthenticationLayer}.
     * <p>
     * If there is currently no {@link InteractionSession} a new one is created and a new 
     * anonymous {@link AuthenticationLayer} is returned. Otherwise both, session and layer are reused.
     * 
     * @param <R>
     * @param callable (non-null)
     */
    <R> R callAnonymous(@NonNull Callable<R> callable);
    
    /**
     * Variant of {@link #callAnonymous(Callable)} that takes a runnable.
     * @param runnable (non-null)
     */
    void runAnonymous(@NonNull ThrowingRunnable runnable);

    /**
     * closes all open {@link AuthenticationLayer}(s) as stacked on the current thread
     */
    void closeSessionStack();


}
