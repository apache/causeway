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
package org.apache.isis.applib.services.wrapper.control;

import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * Modifies the way in which an asynchronous action initiated through the
 * {@link org.apache.isis.applib.services.wrapper.WrapperFactory} is actually
 * executed.
 *
 * <p>
 *     Executing in a separate thread means that the target and arguments are
 *     used in a new {@link org.apache.isis.applib.services.iactn.Interaction}
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
    public AsyncControl with(ExceptionHandler exceptionHandler) {
        return super.with(exceptionHandler);
    }



    @Getter @NonNull
    private ExecutorService executorService =
                            ForkJoinPool.commonPool();

    /**
     * Specifies the {@link ExecutorService} to use to obtain the thread
     * to invoke the action.
     *
     * <p>
     * The default executor service is the common pool.
     * </p>
     *
     *
     * @param executorService
     */
    public AsyncControl<R> with(ExecutorService executorService) {
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
    private TimeZone timeZone;
    public AsyncControl<R> withTimeZone(final @NonNull TimeZone timeZone) {
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
    public void setFuture(Future<R> future) {
        this.future = future;
    }

    private String logMessage() {
        StringBuilder buf = new StringBuilder("Failed to execute ");
        if(getMethod() != null) {
            buf.append(" ").append(getMethod().getName()).append(" ");
            if(getBookmark() != null) {
                buf.append(" on '")
                        .append(getBookmark().getLogicalTypeName())
                        .append(":")
                        .append(getBookmark().getIdentifier())
                        .append("'");
            }
        }
        return buf.toString();
    }

    // ...
}
