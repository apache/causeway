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

import org.springframework.util.StringUtils;

import org.apache.causeway.applib.annotation.Where;
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
        ExceptionHandler exceptionHandler,
        /**
         * Simulated viewerId, honoring feature filtering.
         */
        String viewerId,
        Where where) {

    //TODO can this be further simplified, or is there already an API we can reuse?

    /**
     * @since 2.0 revised for 3.4 {@index}
     */
    @FunctionalInterface
    public interface CommandListener {
        public void onCommand(
                /**
                 * The unique {@link Command#getInteractionId() interactionId} of the parent {@link Command}, which is to say the
                 * {@link Command} that was active in the original interaction where
                 * {@link org.apache.causeway.applib.services.wrapper.WrapperFactory#asyncWrap(Object, AsyncControl)} (or its brethren)
                 * was called.
                 *
                 * <p>This can be useful for custom systems to link parent and child commands together.
                 */
                UUID parentInteractionId,
                InteractionContext interactionContext,
                /**
                 * Details of the actual child command (action or property edit) to be performed.
                 *
                 * <p>Ultimately this can be handed onto the {@link org.apache.causeway.applib.services.command.CommandExecutorService}.
                 */
                CommandDto commandDto);
    }

    public static SyncControl defaults() {
        return new SyncControl(false, false, null, null, null, null);
    }

    public SyncControl {
        commandListeners = commandListeners!=null
            ? commandListeners
            : Can.empty();
        exceptionHandler = exceptionHandler!=null
            ? exceptionHandler
            : exception -> { throw exception; };
        viewerId = StringUtils.hasText(viewerId)
            ? viewerId
            : "NoViewer";
    	where = where!=null
            ? where
            : Where.ANYWHERE;
    }

    /**
     * Skip checking business rules (hide/disable/validate) before
     * executing the underlying property or action
     */
    public SyncControl withSkipRules() {
        return new SyncControl(true, isSkipExecute, commandListeners, exceptionHandler, viewerId, where);
    }
    public SyncControl withCheckRules() {
        return new SyncControl(false, isSkipExecute, commandListeners, exceptionHandler, viewerId, where);
    }

    /**
     * Explicitly set the action to be executed.
     */
    public SyncControl withExecute() {
        return new SyncControl(isSkipRules, false, commandListeners, exceptionHandler, viewerId, where);
    }
    /**
     * Explicitly set the action to <i>not</i> be executed, in other words a 'dry run'.
     */
    public SyncControl withNoExecute() {
        return new SyncControl(isSkipRules, true, commandListeners, exceptionHandler, viewerId, where);
    }

    public SyncControl listen(@NonNull final CommandListener commandListener) {
        return new SyncControl(isSkipRules, isSkipExecute, commandListeners.add(commandListener), exceptionHandler, viewerId, where);
    }

    /**
     * How to handle exceptions if they occur, using the provided {@link ExceptionHandler}.
     *
     * <p>The default behaviour is to rethrow the exception.
     */
    public SyncControl withExceptionHandler(final @NonNull ExceptionHandler exceptionHandler) {
        return new SyncControl(isSkipRules, isSkipExecute, commandListeners, exceptionHandler, viewerId, where);
    }

    public SyncControl withViewerId(final String viewerId) {
        return new SyncControl(isSkipRules, isSkipExecute, commandListeners, exceptionHandler, viewerId, where);
    }

    public SyncControl withWhere(final Where where) {
        return new SyncControl(isSkipRules, isSkipExecute, commandListeners, exceptionHandler, viewerId, where);
    }

    /**
     * @return whether this and other share the same execution mode, ignoring exceptionHandling
     */
    public boolean isEquivalent(final SyncControl other) {
        return this.isSkipExecute == other.isSkipExecute
                && this.isSkipRules == other.isSkipRules;
    }

}
