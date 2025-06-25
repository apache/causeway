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
package org.apache.causeway.applib.services.wrapper.control;

import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;

import lombok.NonNull;

/**
 * Controls the way that a (synchronous) wrapper works.
 *
 * @since 2.0 revised for 3.4 {@index}
 */
public record SyncControl(
        /**
         * How to handle exceptions if they occur, using the provided
         * {@link ExceptionHandler}.
         *
         * <p>The default behaviour is to rethrow the exception.
         */
        AtomicReference<ExceptionHandler> exceptionHandlerRef,
        boolean isSkipExecute,
        /**
         * Skip checking business rules (hide/disable/validate) before
         * executing the underlying property or action
         */
        boolean isSkipRules) {

    public static SyncControl control() {
        return new SyncControl(null, false, false);
    }

    public SyncControl(
            @Nullable AtomicReference<ExceptionHandler> exceptionHandlerRef,
            boolean isSkipExecute,
            boolean isSkipRules) {
        this.exceptionHandlerRef = exceptionHandlerRef!=null
                ? exceptionHandlerRef
                : new AtomicReference<>();
        this.isSkipExecute = isSkipExecute;
        this.isSkipRules = isSkipRules;
        if(this.exceptionHandlerRef.get()==null) {
            this.exceptionHandlerRef.set(exception -> { throw exception; });
        }
    }

    /**
     * Explicitly set the action to be executed.
     */
    public SyncControl withExecute() {
        return new SyncControl(exceptionHandlerRef, false, isSkipRules);
    }

    /**
     * Explicitly set the action to <i>not</i >be executed, in other words a
     * &quot;dry run&quot;.
     */
    public SyncControl withNoExecute() {
        return new SyncControl(exceptionHandlerRef, true, isSkipRules);
    }

    /**
     * Skip checking business rules (hide/disable/validate) before
     * executing the underlying property or action
     */
    public SyncControl withSkipRules() {
        return new SyncControl(exceptionHandlerRef, isSkipExecute, true);
    }

    public SyncControl withCheckRules() {
        return new SyncControl(exceptionHandlerRef, isSkipExecute, false);
    }

    /**
     * How to handle exceptions if they occur, using the provided {@link ExceptionHandler}.
     *
     * <p>The default behaviour is to rethrow the exception.
     *
     * <p>Changes are made in place, returning the same instance.
     */
    public SyncControl setExceptionHandler(final @NonNull ExceptionHandler exceptionHandler) {
        exceptionHandlerRef.set(exceptionHandler);
        return this;
    }

    public ExceptionHandler exceptionHandler() {
        return exceptionHandlerRef.get();
    }

    /**
     * @return whether this and other share the same execution mode, ignoring exceptionHandling
     */
    public boolean isEquivalent(SyncControl other) {
        return this.isSkipExecute == other.isSkipExecute
                && this.isSkipRules == other.isSkipRules;
    }

}
