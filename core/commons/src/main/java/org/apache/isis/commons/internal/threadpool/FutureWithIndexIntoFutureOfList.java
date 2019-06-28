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

package org.apache.isis.commons.internal.threadpool;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class FutureWithIndexIntoFutureOfList<T> implements Future<T> {
    final Future<List<T>> commonFuture;
    final int index;
    
    FutureWithIndexIntoFutureOfList(Future<List<T>> commonFuture, int index) {
        this.commonFuture = commonFuture;
        this.index = index;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return commonFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return commonFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return commonFuture.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return commonFuture.get().get(index);
    }

    @Override
    public T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return commonFuture.get(timeout, unit).get(index);
    }
}