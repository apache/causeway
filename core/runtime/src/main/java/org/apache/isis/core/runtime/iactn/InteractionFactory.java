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

package org.apache.isis.core.runtime.iactn;

import java.util.concurrent.Callable;

import org.apache.isis.core.security.authentication.Authentication;

import lombok.NonNull;

/**
 * The factory of {@link InteractionSession}(s) and {@link InteractionLayer}(s), 
 * also holding a reference to the current authentication layer stack using
 * a thread-local.
 * <p>
 * The implementation is a singleton service.
 */
public interface InteractionFactory {

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * If present, reuses the current top level {@link InteractionLayer}, otherwise creates a new 
     * anonymous one.
     * @see {@link #openInteraction(Authentication)}
     */
    InteractionLayer openInteraction();
    
    /**
     * Returns a new {@link InteractionLayer} that is a holder of {@link Authentication} on top 
     * of the current thread's authentication layer stack.
     * <p>
     * If available reuses an existing {@link InteractionSession}, otherwise creates a new one.
     * <p> 
     * The {@link InteractionSession} represents a user's span of activities interacting with 
     * the application. The session's stack is later closed using {@link #closeSessionStack()}.
     *
     * @param authentication - the {@link Authentication} to associate with the new top of 
     * the stack (non-null)
     * 
     * @apiNote if the current {@link InteractionLayer} (if any) has an {@link Authentication} that
     * equals that of the given one, as an optimization, no new layer is pushed onto the stack; 
     * instead the current one is returned
     */
    InteractionLayer openInteraction(@NonNull Authentication authentication);

    /**
     * @return whether the calling thread is within the context of an open IsisInteractionSession
     */
    boolean isInInteractionSession();

    /**
     * @return whether the calling thread is within the context of an open IsisTransactionSession
     */
    boolean isInTransaction();

    
    /**
     * Executes a piece of code in a new (possibly nested) IsisInteraction.
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
    default void runAuthenticated(@NonNull Authentication authentication, @NonNull ThrowingRunnable runnable) {
        final Callable<Void> callable = ()->{runnable.run(); return null;};
        callAuthenticated(authentication, callable);
    }

    /**
     * Executes a piece of code in a (possibly reused) {@link InteractionSession}.
     * If there is already an open session stacked on the current thread then that one is used, 
     * otherwise an anonymous session is created.
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
     * closes all open {@link InteractionLayer}(s) as stacked on the current thread
     */
    void closeSessionStack();


}
