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
package org.apache.causeway.core.runtimeservices.wrapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.causeway.applib.services.wrapper.WrapperFactory.AsyncProxy;

//TODO this is just a proof of concept; chaining makes non sense once future is no longer a proxy
record AsyncProxyInternal<T>(CompletableFuture<T> future, AsyncExecutorService executor) implements AsyncProxy<T> {
    @Override public AsyncProxy<Void> thenAcceptAsync(Consumer<? super T> action) {
        return map(in->in.thenAcceptAsync(action, executor));
    }

    @Override public <U> AsyncProxy<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        return map(in->in.thenApplyAsync(fn, executor));
    }

    @Override public AsyncProxy<T> orTimeout(long timeout, TimeUnit unit) {
        return map(in->in.orTimeout(timeout, unit));
    }

    @Override public T join() {
        return future.join();
    }

    // -- HELPER

    private <U> AsyncProxy<U> map(Function<CompletableFuture<T>, CompletableFuture<U>> fn) {
        return new AsyncProxyInternal<>(fn.apply(future), executor);
    }
}