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

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import lombok.NonNull;

/**
 * Controls the way that a (synchronous) wrapper works.
 *
 * @since 2.0 revised for 3.4 {@index}
 */
public record SyncControl(
        /**
         * Skip checking business rules (hide/disable/validate) before
         * executing the underlying property or action
         */
        boolean isSkipRules,
        boolean isSkipExecute,
        /**
         * Get notified on action invocation or property change.
         */
        Can<CommandListener> commandListeners,
        /**
         * How to handle exceptions if they occur, using the provided
         * {@link ExceptionHandler}.
         *
         * <p>The default behaviour is to rethrow the exception.
         */
        ExceptionHandler exceptionHandler) {

    @FunctionalInterface
    public interface CommandListener {
        public void onCommand(
                InteractionContext interactionContext,
                CommandDto commandDto,
                UUID parentInteractionId);
    }

    public static SyncControl defaults() {
        return new SyncControl(false, false, null, null);
    }

    public SyncControl(
            boolean isSkipRules,
            boolean isSkipExecute,
            @Nullable Can<CommandListener> commandListeners,
            @Nullable ExceptionHandler exceptionHandler) {
        this.isSkipRules = isSkipRules;
        this.isSkipExecute = isSkipExecute;
        this.commandListeners = commandListeners!=null
                ? commandListeners
                : Can.empty();
        this.exceptionHandler = exceptionHandler!=null
                ? exceptionHandler
                : exception -> { throw exception; };
    }

    /**
     * Skip checking business rules (hide/disable/validate) before
     * executing the underlying property or action
     */
    public SyncControl withSkipRules() {
        return new SyncControl(true, isSkipExecute, commandListeners, exceptionHandler);
    }
    public SyncControl withCheckRules() {
        return new SyncControl(false, isSkipExecute, commandListeners, exceptionHandler);
    }

    /**
     * Explicitly set the action to be executed.
     */
    public SyncControl withExecute() {
        return new SyncControl(isSkipRules, false, commandListeners, exceptionHandler);
    }
    /**
     * Explicitly set the action to <i>not</i> be executed, in other words a 'dry run'.
     */
    public SyncControl withNoExecute() {
        return new SyncControl(isSkipRules, true, commandListeners, exceptionHandler);
    }

    public SyncControl listen(@NonNull CommandListener commandListener) {
        return new SyncControl(isSkipRules, isSkipExecute, commandListeners.add(commandListener), exceptionHandler);
    }

    /**
     * How to handle exceptions if they occur, using the provided {@link ExceptionHandler}.
     *
     * <p>The default behaviour is to rethrow the exception.
     */
    public SyncControl withExceptionHandler(final @NonNull ExceptionHandler exceptionHandler) {
        return new SyncControl(isSkipRules, isSkipExecute, commandListeners, exceptionHandler);
    }

    /**
     * @return whether this and other share the same execution mode, ignoring exceptionHandling
     */
    public boolean isEquivalent(SyncControl other) {
        return this.isSkipExecute == other.isSkipExecute
                && this.isSkipRules == other.isSkipRules;
    }

}
