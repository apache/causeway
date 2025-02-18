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

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.clock.VirtualClock;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.commons.internal.assertions._Assert;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * Modifies the way in which an asynchronous action initiated through the
 * {@link org.apache.causeway.applib.services.wrapper.WrapperFactory} is actually
 * executed.
 *
 * <p>
 *     Executing in a separate thread means that the target and arguments are
 *     used in a new {@link org.apache.causeway.applib.services.iactn.Interaction}
 *     (and transaction).  If any of these are entities, they are retrieved
 *     from the database afresh; it isn't possible to pass domain entity
 *     references from the foreground calling thread to the background threads.
 * </p>
 *
 * @param <R> - return value.
 *
 * @since 2.0 {@index}
 */
@Log4j2
public class AsyncControl<R> extends ControlAbstract<AsyncControl<R>> {

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
     *
     * @param cls
     * @param <X>
     */
    public static <X> AsyncControl<X> returning(final Class<X> cls) {
        return new AsyncControl<X>(cls);
    }

    @Getter
    private final Class<R> returnType;

    private AsyncControl(final Class<R> returnType) {
        this.returnType = returnType;
        with(exception -> {
            log.error(logMessage(), exception);
            return null;
        });
    }

    /**
     * Skip checking business rules (hide/disable/validate) before
     * executing the underlying property or action
     */
    @Override
    public AsyncControl<R> withSkipRules() {
        return super.withSkipRules();
    }

    /**
     * How to handle exceptions if they occur, using the provided
     * {@link ExceptionHandler}.
     *
     * <p>
     *     The default behaviour is to rethrow the exception.
     * </p>
     */
    @Override
    public AsyncControl<R> with(final ExceptionHandler exceptionHandler) {
        return super.with(exceptionHandler);
    }

    @Getter @Nullable
    private ExecutorService executorService = null;

    /**
     * Specifies the {@link ExecutorService} to use to obtain the thread
     * to invoke the action.
     * <p>
     * The default is {@code null}, indicating, that its the {@link WrapperFactory}'s
     * responsibility to provide a suitable {@link ExecutorService}.
     *
     * @param executorService - null-able
     */
    public AsyncControl<R> with(final ExecutorService executorService) {
        this.executorService = executorService;
        return this;
        // ...
    }

    /**
     * Defaults to the system clock, if not overridden
     */
    @Getter
    private VirtualClock clock;
    public AsyncControl<R> withClock(final @NonNull VirtualClock clock) {
        this.clock = clock;
        return this;
        // ...
    }

    /**
     * Defaults to the system locale, if not overridden
     */
    @Getter
    private Locale locale;
    public AsyncControl<R> withLocale(final @NonNull Locale locale) {
        this.locale = locale;
        return this;
        // ...
    }

    /**
     * Defaults to the system time zone, if not overridden
     */
    @Getter
    private ZoneId timeZone;
    public AsyncControl<R> withTimeZone(final @NonNull ZoneId timeZone) {
        this.timeZone = timeZone;
        return this;
        // ...
    }

    @Getter
    private UserMemento user;
    /**
     * Specifies the user for the session used to execute the command
     * asynchronously, in the background.
     *
     * <p>
     * If not specified, then the user of the current foreground session is used.
     * </p>
     */
    public AsyncControl<R> withUser(final @NonNull UserMemento user) {
        this.user = user;
        return this;
        // ...
    }

    /**
     * Contains the result of the invocation.
     *
     * <p>
     * If an entity is returned, then the object is automatically detached
     * because the persistence session within which it was obtained will have
     * been closed already.
     * </p>
     */
    @Getter
    private Future<R> future;

    /**
     * For framework use only.
     */
    public void setFuture(final Future<R> future) {
        this.future = future;
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
    @SneakyThrows
    public R waitForResult(final long timeout, final TimeUnit unit) {
        _Assert.assertNotNull(future,
                ()->"detected call to waitForResult(..) before future was set");
        return future.get(timeout, unit);
    }

    private String logMessage() {
        StringBuilder buf = new StringBuilder("Failed to execute ");
        if(getMethod() != null) {
            buf.append(" ").append(getMethod().getName()).append(" ");
            if(getBookmark() != null) {
                buf.append(" on '")
                        .append(getBookmark().logicalTypeName())
                        .append(":")
                        .append(getBookmark().identifier())
                        .append("'");
            }
        }
        return buf.toString();
    }

}
