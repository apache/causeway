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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.util.function.ThrowingConsumer;
import org.springframework.util.function.ThrowingFunction;

import org.apache.causeway.applib.services.wrapper.WrapperFactory.AsyncProxy;

import lombok.SneakyThrows;

//TODO this is just a proof of concept; chaining makes non sense once future no longer holds a proxy
record AsyncProxyInternal<T>(
        Future<T> future,
        AsyncExecutor executor) implements AsyncProxy<T> {

    @Override public AsyncProxy<Void> thenAcceptAsync(ThrowingConsumer<? super T> action) {
        return thenApplyAsync(adapt(action));
    }

    @Override public <U> AsyncProxy<U> thenApplyAsync(ThrowingFunction<? super T, ? extends U> fn) {
        return map(()->fn.apply(future.get()));
    }

    @Override public AsyncProxy<T> orTimeout(long timeout, TimeUnit unit) {
        return map(()->future.get(timeout, unit));
    }

    @SneakyThrows
    @Override public T join() {
        return future.get();
    }

    // -- HELPER

    /// converts ThrowingConsumer<T> to ThrowingFunction<T, Void>
    private ThrowingFunction<? super T, Void> adapt(ThrowingConsumer<? super T> action) {
        return t->{action.accept(t); return (Void)null; };
    }

    private <U> AsyncProxy<U> map(Callable<U> callable) {
        return new AsyncProxyInternal<>(executor.submit(callable), executor);
    }

}