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

import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.clock.VirtualClock;
import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Modifies the way in which an asynchronous action initiated through the
 * {@link org.apache.causeway.applib.services.wrapper.WrapperFactory} is actually
 * executed.
 *
 * <p> Executing in a separate thread means that the target and arguments are
 * used in a new {@link org.apache.causeway.applib.services.iactn.Interaction}
 * (and transaction).  If any of these are entities, they are retrieved
 * from the database afresh; it isn't possible to pass domain entity
 * references from the foreground calling thread to the background threads.
 *
 * @param <R> - return value.
 *
 * @since 2.0 {@index}
 */
@Slf4j
public record AsyncControl (
        SyncControl syncControl,
        @Nullable ExecutorService executorService,
        /**
         * Defaults to the system clock, if not overridden
         */
        @Nullable VirtualClock clock,
        /**
         * Defaults to the system locale, if not overridden
         */
        @Nullable Locale locale,
        /**
         * Defaults to the system time zone, if not overridden
         */
        @Nullable ZoneId timeZone,
        /**
         * Specifies the user for the session used to execute the command
         * asynchronously, in the background.
         *
         * <p>If not specified, then the user of the current foreground session is used.
         */
        @Nullable UserMemento user
        ) {

    public static AsyncControl defaults() {
        return new AsyncControl(SyncControl.defaults(),
                /*executorService*/null,
                /*clock*/null, /*locale*/null, /*timeZone*/null, /*user*/null);
    }

    /**
     * Explicitly set the action to be executed.
     */
    public AsyncControl withExecute() {
        return new AsyncControl(syncControl.withExecute(), executorService, clock, locale, timeZone, user);
    }
    /**
     * Explicitly set the action to <i>not</i >be executed, in other words a 'dry run'.
     */
    public AsyncControl withNoExecute() {
        return new AsyncControl(syncControl.withNoExecute(), executorService, clock, locale, timeZone, user);
    }

    /**
     * Skip checking business rules (hide/disable/validate) before
     * executing the underlying property or action
     */
    public AsyncControl withSkipRules() {
        return new AsyncControl(syncControl.withSkipRules(), executorService, clock, locale, timeZone, user);
    }
    public AsyncControl withCheckRules() {
        return new AsyncControl(syncControl.withCheckRules(), executorService, clock, locale, timeZone, user);
    }

    /**
     * How to handle exceptions if they occur, using the provided {@link ExceptionHandler}.
     *
     * <p>The default behaviour is to rethrow the exception.
     *
     * <p>Changes are made in place, returning the same instance.
     */
    public AsyncControl setExceptionHandler(final @NonNull ExceptionHandler exceptionHandler) {
        syncControl.setExceptionHandler(exceptionHandler);
        return this;
    }

    /**
     * Specifies the {@link ExecutorService} to use to obtain the thread
     * to invoke the action.
     *
     * <p>The default is {@code null}, indicating, that its the {@link WrapperFactory}'s
     * responsibility to provide a suitable {@link ExecutorService}.
     *
     * @param executorService - null-able
     */
    public AsyncControl with(final ExecutorService executorService) {
        return new AsyncControl(syncControl, executorService, clock, locale, timeZone, user);
    }

    public AsyncControl listen(final SyncControl.@NonNull CommandListener commandListener) {
        return new AsyncControl(syncControl.listen(commandListener), executorService, clock, locale, timeZone, user);
    }

    /**
     * Defaults to the system clock, if not overridden
     */
    public AsyncControl withClock(final @NonNull VirtualClock clock) {
        return new AsyncControl(syncControl, executorService, clock, locale, timeZone, user);
    }

    /**
     * Defaults to the system locale, if not overridden
     */
    public AsyncControl withLocale(final @NonNull Locale locale) {
        return new AsyncControl(syncControl, executorService, clock, locale, timeZone, user);
    }

    /**
     * Defaults to the system time zone, if not overridden
     */
    public AsyncControl withTimeZone(final @NonNull ZoneId timeZone) {
        return new AsyncControl(syncControl, executorService, clock, locale, timeZone, user);
    }

    /**
     * Specifies the user for the session used to execute the command
     * asynchronously, in the background.
     *
     * <p>If not specified, then the user of the current foreground session is used.
     */
    public AsyncControl withUser(final @NonNull UserMemento user) {
        return new AsyncControl(syncControl, executorService, clock, locale, timeZone, user);
    }

    public InteractionContext override(
            final InteractionContext interactionContext) {
        return InteractionContext.builder()
            .clock(Optional.ofNullable(clock()).orElseGet(interactionContext::getClock))
            .locale(Optional.ofNullable(locale()).map(UserLocale::valueOf).orElse(null)) // if not set in asyncControl use defaults (set override to null)
            .timeZone(Optional.ofNullable(timeZone()).orElseGet(interactionContext::getTimeZone))
            .user(Optional.ofNullable(user()).orElseGet(interactionContext::getUser))
            .build();
    }

}
