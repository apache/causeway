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
package org.apache.causeway.commons.handler;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 * Implements the <em>Chain of Responsibility</em> design pattern.
 * <p>
 * <em>Chain of Responsibility</em> allows passing requests along the chain of handlers,
 * until one of them handles the request.
 *
 * @since 2.0
 *
 * @param <X> request type
 * @param <R> response type
 */
public record ChainOfResponsibility<X, R>(
        String name,
        Can<? extends Handler<X, R>> handlers) {

    /**
     * A chain of responsibility is made up of handlers, that are asked in sequence,
     * whether they handle a request.
     *
     * @since 2.0
     *
     * @param <X> request type
     * @param <R> response type
     */
    public static interface Handler<X, R> {
        boolean isHandling(X request);
        R handle(X request);
    }

    public ChainOfResponsibility(@Nullable final String name, @Nullable final Can<? extends Handler<X, R>> handlers) {
        this.name = _Strings.nonEmpty(name).orElse("unnamed chain");
        this.handlers = handlers!=null
                ? handlers
                : Can.empty();
    }

    // could be widened to SequencedCollection once available
    public ChainOfResponsibility(final String name, @Nullable final List<? extends Handler<X, R>> handlers) {
        this(name, Can.ofCollection(handlers));
    }

    public ChainOfResponsibility(final String name, @Nullable final Handler<X, R>[] handlers) {
        this(name, Can.ofArray(handlers));
    }

    /**
     * The {@code request} is passed along the chain of handlers, until one of them handles the request.
     * <p>
     * Strict variant of {@link #handle(Object)}.
     * @return response of the first handler that handled the request (returning {@code null} is NOT allowed),
     * or throws {@link NoSuchElementException}, if no handler handled the request
     * @throws NoSuchElementException
     * @see #handle(Object)
     */
    public R handleElseFail(final X request) {
        for(var handler : handlers) {
            if(!handler.isHandling(request)) continue;
            return Objects.requireNonNull(handler.handle(request), ()->
                "a handler returend null for request %s".formatted(request));
        }
        throw _Exceptions.noSuchElement("no handler found for request %s", request);
    }

    /**
     * The {@code request} is passed along the chain of handlers, until one of them handles the request.
     * <p>
     * Permissive variant of {@link #handleElseFail(Object)}.
     * @return response of the first handler that handled the request (returning {@code null} is allowed)
     * wrapped in an Optional,
     * or an empty Optional, if no handler handled the request
     * @see #handleElseFail(Object)
     */
    public Optional<R> handle(final X request) {
        for(var handler : handlers) {
            if(!handler.isHandling(request)) continue;
            return Optional.ofNullable(handler.handle(request));
        }
        return Optional.empty();
    }

}
