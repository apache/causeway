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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.clock.VirtualClock;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.commons.internal.assertions._Assert;

import lombok.SneakyThrows;
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
public record AsyncControl<R>(
        Class<R> returnType,
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
        @Nullable UserMemento user,
        /**
         * Contains the result of the invocation.
         *
         * <p> If an entity is returned, then the object is automatically detached
         * because the persistence session within which it was obtained will have
         * been closed already.
         */
        AtomicReference<Future<R>> futureRef) {

    /**
     * Factory method to instantiate a control instance for a void action
     * or a property edit (where there is no need or intention to provide a
     * return value through the `Future`).
     */
    public static AsyncControl<Void> returningVoid() {
        return new AsyncControl<>(Void.class);
    }

    /**
     * Factory method to instantiate for a control instance for an action
     * returning a value of `<R>` (where this value will be returned through
     * the `Future`).
     */
    public static <X> AsyncControl<X> returning(final Class<X> cls) {
        return new AsyncControl<X>(cls);
    }

    // non canonical constructor
    private AsyncControl(final Class<R> returnType) {
        this(returnType,
            SyncControl.control(),
            /*executorService*/null, /*clock*/null, /*locale*/null, /*timeZone*/null, /*user*/null,
            new AtomicReference<>());
    }

    /**
     * Explicitly set the action to be executed.
     */
    public AsyncControl<R> withExecute() {
        return new AsyncControl<>(returnType, syncControl.withExecute(), executorService, clock, locale, timeZone, user, futureRef);
    }

    /**
     * Explicitly set the action to <i>not</i >be executed, in other words a
     * &quot;dry run&quot;.
     */
    public AsyncControl<R> withNoExecute() {
        return new AsyncControl<>(returnType, syncControl.withExecute(), executorService, clock, locale, timeZone, user, futureRef);
    }

    /**
     * Skip checking business rules (hide/disable/validate) before
     * executing the underlying property or action
     */
    public AsyncControl<R> withSkipRules() {
        return new AsyncControl<>(returnType, syncControl.withSkipRules(), executorService, clock, locale, timeZone, user, futureRef);
    }

    public AsyncControl<R> withCheckRules() {
        return new AsyncControl<>(returnType, syncControl.withCheckRules(), executorService, clock, locale, timeZone, user, futureRef);
    }

    /**
     * How to handle exceptions if they occur, using the provided {@link ExceptionHandler}.
     *
     * <p>The default behaviour is to rethrow the exception.
     *
     * <p>Changes are made in place, returning the same instance.
     */
    public AsyncControl<R> setExceptionHandler(final @NonNull ExceptionHandler exceptionHandler) {
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
    public AsyncControl<R> with(final ExecutorService executorService) {
        return new AsyncControl<>(returnType, syncControl, executorService, clock, locale, timeZone, user, futureRef);
    }

    /**
     * Defaults to the system clock, if not overridden
     */
    public AsyncControl<R> withClock(final @NonNull VirtualClock clock) {
        return new AsyncControl<>(returnType, syncControl, executorService, clock, locale, timeZone, user, futureRef);
    }

    /**
     * Defaults to the system locale, if not overridden
     */
    public AsyncControl<R> withLocale(final @NonNull Locale locale) {
        return new AsyncControl<>(returnType, syncControl, executorService, clock, locale, timeZone, user, futureRef);
    }

    /**
     * Defaults to the system time zone, if not overridden
     */
    public AsyncControl<R> withTimeZone(final @NonNull ZoneId timeZone) {
        return new AsyncControl<>(returnType, syncControl, executorService, clock, locale, timeZone, user, futureRef);
    }

    /**
     * Specifies the user for the session used to execute the command
     * asynchronously, in the background.
     *
     * <p>If not specified, then the user of the current foreground session is used.
     */
    public AsyncControl<R> withUser(final @NonNull UserMemento user) {
        return new AsyncControl<>(returnType, syncControl, executorService, clock, locale, timeZone, user, futureRef);
    }

    public Future<R> future() {
        return futureRef.get();
    }

    /**
     * Waits on the callers thread, for a maximum amount of time,
     * for the result of the invocation to become available.
     * @param timeout the maximum time to wait
     * @param unit the time unit of the {@code timeout} argument
     * @return the invocation result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws TimeoutException if the wait timed out
     */
    @SuppressWarnings("javadoc")
    @SneakyThrows
    public R waitForResult(final long timeout, final TimeUnit unit) {
        _Assert.assertNotNull(future(),
                ()->"detected call to waitForResult(..) before future was set");
        return future().get(timeout, unit);
    }

    // -- DEPRECATIONS

    @Deprecated public Class<R> getReturnType() { return returnType(); }
    @Deprecated public ExecutorService getExecutorService() { return executorService(); }

    /**
     * Defaults to the system clock, if not overridden
     */
    @Deprecated public VirtualClock getClock() { return clock(); }
    /**
     * Defaults to the system locale, if not overridden
     */
    @Deprecated public Locale getLocale() { return locale(); }
    /**
     * Defaults to the system time zone, if not overridden
     */
    @Deprecated public ZoneId getTimeZone() { return timeZone(); }
    /**
     * Specifies the user for the session used to execute the command
     * asynchronously, in the background.
     *
     * <p>If not specified, then the user of the current foreground session is used.
     */
    @Deprecated public UserMemento getUser() { return user(); }

    @Deprecated public Future<R> getFuture() { return future(); }

}
