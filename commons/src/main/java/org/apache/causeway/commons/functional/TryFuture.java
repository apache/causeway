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
package org.apache.causeway.commons.functional;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;

/**
 * Wraps a {@link Future} and catches any exceptions within a {@link Try},
 * such that no exceptions escape the future's {@code get(..)} methods.
 *
 * @param <T> The result type returned by this TryFuture's {@code tryGet(..)} methods
 * @see Future
 * @see Try
 * @since 3.4
 */
public record TryFuture<T>(@NonNull Future<T> future) {

    /**
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if the task or executor is null
     */
    public TryFuture(@NonNull Callable<T> task, @NonNull ExecutorService executor) {
        this(executor.submit(task));
    }

    public Try<T> tryGet() {
        return Try.call(future::get);
    }

    public Try<T> tryGet(long timeout, TimeUnit unit) {
        return Try.call(()->future.get(timeout, unit));
    }

}

