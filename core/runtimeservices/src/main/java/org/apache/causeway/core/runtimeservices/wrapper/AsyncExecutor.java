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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 * Implements {@link ExecutorService} providing an interaction and optional transaction scope for each invocation.
 */
record AsyncExecutor(
        InteractionService interactionService,
        TransactionService transactionService,
        InteractionContext interactionContext,
        /**
         * If empty then executes non-transactionally, similar to {@link Propagation#NEVER},
         * but does NOT throw any exceptions if a transaction exists.
         */
        Optional<Propagation> propagation,
        AsyncExecutionFinisher finisher,
        ExecutorService delegate) implements ExecutorService {

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        return delegate.submit(()->call(task));
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        return delegate.submit(()->run(task::run), result);
    }

    @Override
    public Future<?> submit(final Runnable task) {
        return delegate.submit(()->run(task::run));
    }

    @Override
    public void execute(final Runnable command) {
        delegate.execute(()->run(command::run));
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
        throw _Exceptions.unsupportedOperation(); //return delegate.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException {
        throw _Exceptions.unsupportedOperation(); //return delegate.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw _Exceptions.unsupportedOperation(); //return delegate.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw _Exceptions.unsupportedOperation(); //return delegate.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    // -- HELPER

    private void run(ThrowingRunnable runnable) {
        if(propagation.isEmpty())
            interactionService.run(interactionContext, ()->finish(runnable));
        else
            interactionService.run(interactionContext, ()->transactionService
                .runTransactional(propagation().get(), ()->finish(runnable))
                .ifFailureFail());
    }

    private <T> T call(Callable<T> callable) {
        return propagation.isEmpty()
            ? interactionService.call(interactionContext, ()->finish(callable))
            : interactionService.call(interactionContext, ()->transactionService
                .callTransactional(propagation().get(), ()->finish(callable))
                .valueAsNullableElseFail());
    }

    private <T> T finish(Callable<T> callable) throws Exception {
        return finisher.finish(callable.call());
    }

    private void finish(ThrowingRunnable runnable) throws Exception {
        runnable.run();
        finisher.finish(null);
    }

}