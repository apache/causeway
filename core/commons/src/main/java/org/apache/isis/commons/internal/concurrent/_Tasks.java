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
package org.apache.isis.commons.internal.concurrent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;

import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;
import static org.apache.isis.commons.internal.base._With.requires;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Framework internal concurrency support.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0
 */
@Log4j2
public final class _Tasks {

    public static _Tasks create() {
        return new _Tasks();
    }

    public void addRunnable(Runnable runnable) {
        requires(runnable, "runnable");
        addRunnable(runnable, null);
    }

    public void addRunnable(Runnable runnable, @Nullable Supplier<String> name) {
        requires(runnable, "runnable");
        callables.add(new NamedCallable<Object>(name) {

            @Override
            public Void call() throws Exception {
                runnable.run();
                return null;
            }

        });
    }

    public void addRunnable(String name, Runnable runnable) {
        requires(runnable, "runnable");
        addRunnable(runnable, ()->name);
    }

    public List<Callable<Object>> getCallables() {
        return Collections.unmodifiableList(callables);
    }

    public void invokeAndWait(boolean concurrent) {

        val t0 = System.nanoTime();
        val tasksExecuted = new LongAdder();

        try {

            if(concurrent) {

                //				val forkJoinPool = new ForkJoinPool();
                //				val anyErrorRef = new AtomicReference<RuntimeException>();
                //				
                //				forkJoinPool.submit(()->{
                val anyError = callables.parallelStream()
                        .map(_Tasks::call)
                        .peek(__->tasksExecuted.increment())
                        .filter(_Either::isRight)
                        .findAny()
                        .map(_Either::rightIfAny)
                        .orElse(null);

                //					anyErrorRef.set(anyError);
                //				});
                //				
                //				forkJoinPool.shutdown();
                //				
                //				try {
                //					System.err.println("wait for ForkJoinPool " + forkJoinPool);
                //					forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                //					System.err.println("done waiting for ForkJoinPool " + forkJoinPool);
                //				} catch (InterruptedException e) {
                //					throw _Exceptions.unrecoverable("exception while waiting on the ForkJoinPool to terminate", e);
                //				}
                //				
                //				val anyError = anyErrorRef.get();
                if(anyError!=null) {
                    throw anyError;
                }

            } else {

                for(Callable<?> callable : getCallables()) {
                    val eitherResultOrError = call(callable);
                    tasksExecuted.increment();

                    if(eitherResultOrError.isRight()) {
                        throw eitherResultOrError.rightIfAny();	
                    }
                }

            }

        } finally {

            if(log.isDebugEnabled()) {
                val t1 = System.nanoTime();
                log.printf(Level.DEBUG, 
                        "running %d/%d tasks %s, took %.3f milliseconds ",
                        tasksExecuted.longValue(),
                        callables.size(),
                        concurrent ? "concurrent" : "sequential",
                                0.000_001 * (t1-t0));	
            }

            callables.clear();
        }

    }

    // -- IMPLEMENTATION DETAILS

    private static <T> _Either<T, RuntimeException> call(Callable<T> callable) {

        try {
            val result = callable.call();
            return _Either.leftNullable(result);
        } catch (Throwable cause) {

            val name = callable instanceof NamedCallable
                    ? callable.toString()
                            : "unnamend";

                    val msg = String.format("failure while executing callable '%s'", name);
                    log.error(msg, cause);
                    return _Either.right(_Exceptions.unrecoverable(msg, cause));
        }

    }

    private final List<Callable<Object>> callables = _Lists.newArrayList();

    @RequiredArgsConstructor
    private abstract static class NamedCallable<T> implements Callable<T> {

        private final Supplier<String> name;

        @Override
        public String toString() {
            return mapIfPresentElse(name, Supplier::get, super.toString());
        }

    }


}
