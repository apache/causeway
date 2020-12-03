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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 *
 * @param <R> - return value.
 */
// tag::refguide[]
@Log4j2
public class AsyncControl<R> extends ControlAbstract<AsyncControl<R>> {

    public static AsyncControl<Void> returningVoid() {                        // <.>
        return new AsyncControl<>(Void.class);
    }
    public static <X> AsyncControl<X> returning(final Class<X> cls) {     // <.>
        return new AsyncControl<X>(cls);
    }

    @Getter
    private final Class<R> returnType;                                  // <.>

    private AsyncControl(final Class<R> returnType) {
        this.returnType = returnType;
        with(exception -> {                                             // <.>
            log.error(logMessage(), exception);
            return null;
        });
    }

    @Getter @NonNull
    private ExecutorService executorService =                           // <.>
                            ForkJoinPool.commonPool();
    public AsyncControl<R> with(ExecutorService executorService) {
        // end::refguide[]
        this.executorService = executorService;
        return this;
        // tag::refguide[]
        // ...
    }

    // end::refguide[]
    /**
     * Defaults to the system clock, if not overridden
     */
    // tag::refguide[]
    @Getter
    private VirtualClock clock;                                         // <.>
    public AsyncControl<R> withClock(final VirtualClock clock) {
        // end::refguide[]
        this.clock = clock;
        return this;
        // tag::refguide[]
        // ...
    }

    // end::refguide[]
    /**
     * Defaults to user initiating the action, if not overridden
     */
    // tag::refguide[]
    @Getter
    private UserMemento user;                                           // <.>
    public AsyncControl<R> withUser(final UserMemento user) {
        // end::refguide[]
        this.user = user;
        return this;
        // tag::refguide[]
        // ...
    }

    // end::refguide[]
    /**
     * Set by framework.
     *
     * Contains the result of the invocation.  However, if an entity is returned, then the object is automatically
     * detached because the persistence session within which it was obtained will have been closed already.
     */
    // tag::refguide[]
    @Getter @Setter
    private Future<R> future;                                           // <.>

    // end::refguide[]
    private String logMessage() {
        StringBuilder buf = new StringBuilder("Failed to execute ");
        if(getMethod() != null) {
            buf.append(" ").append(getMethod().getName()).append(" ");
            if(getBookmark() != null) {
                buf.append(" on '")
                        .append(getBookmark().getObjectType())
                        .append(":")
                        .append(getBookmark().getIdentifier())
                        .append("'");
            }
        }
        return buf.toString();
    }

    // tag::refguide[]
    // ...
}
// end::refguide[]
