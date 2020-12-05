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

/**
 * The factory of {@link InteractionSession}s, also holding a reference to the
 * current session using a thread-local.
 *
 * <p>
 *     The class can in considered as analogous to (and is in many ways a wrapper for) a JDO
 *     <code>PersistenceManagerFactory</code>.
 * </p>
 *
 * <p>
 *     The implementation is a singleton service.
 * </p>
 */
public interface InteractionFactory {

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * Creates a new {@link InteractionSession}, which represents a user's span of
     * activities interacting with the application.
     *
     * <p>
     *     If there is already an {@link InteractionSession} available (as held
     *     in a thread-local stack), then the interactions are stacked.
     *     These are closed using {@link #closeSessionStack()}.
     * </p>
     *
     * @param authenticationSession
     */
    InteractionLayer openInteraction();
    
    /**
     * Creates a new {@link InteractionSession}, which represents a user's span of
     * activities interacting with the application.
     *
     * <p>
     *     If there is already an {@link InteractionSession} available (as held
     *     in a thread-local stack), then the interactions are stacked.
     *     These are closed using {@link #closeSessionStack()}.
     * </p>
     *
     * @param authenticationSession
     */
    InteractionLayer openInteraction(Authentication authenticationSession);

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
     * @param authenticationSession - the user to run under
     * @param callable - the piece of code to run
     * 
     */
    <R> R callAuthenticated(Authentication authenticationSession, Callable<R> callable);
    
    /**
     * Variant of {@link #callAuthenticated(Authentication, Callable)} that takes a runnable.
     * @param authenticationSession
     * @param runnable
     */
    default void runAuthenticated(Authentication authenticationSession, ThrowingRunnable runnable) {
        final Callable<Void> callable = ()->{runnable.run(); return null;};
        callAuthenticated(authenticationSession, callable);
    }

    /**
     * Executes a piece of code in a (possibly reused) IsisInteraction.
     * If there is already an open session stacked on the current thread then that one is used, 
     * otherwise an anonymous session is created.
     * @param <R>
     * @param callable
     */
    <R> R callAnonymous(Callable<R> callable);
    
    /**
     * Variant of {@link #callAnonymous(Callable)} that takes a runnable.
     * @param runnable
     */
    void runAnonymous(ThrowingRunnable runnable);

    /**
     * closes all open InteractionClosures as stacked on the current thread
     */
    void closeSessionStack();


}
