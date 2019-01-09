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

package org.apache.isis.core.runtime.threadpool;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.base._With.requires;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * ThreadPoolSupport is application-scoped, meaning ThreadPoolSupport is closed on
 * application's end of life-cycle.
 * <br/><br/>
 * Implementation Note: ThreadPoolSupport::close is triggered by _Context.clear()
 * when application shuts down.
 *
 */
public final class ThreadPoolSupport implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolSupport.class);

    public static ThreadPoolExecutionMode HIGHEST_CONCURRENCY_EXECUTION_MODE_ALLOWED = ThreadPoolExecutionMode.PARALLEL;
    
    private final static int KEEP_ALIVE_TIME_SECS = 5;
    private final static int QUEUE_CAPACITY = Integer.MAX_VALUE;

    private final ThreadGroup group;
    private final ThreadPoolExecutor concurrentExecutor;

    /**
     * @return the application-scoped singleton ThreadPoolSupport instance
     */
    public static ThreadPoolSupport getInstance() {
        return _Context.computeIfAbsent(ThreadPoolSupport.class, __-> new ThreadPoolSupport());
    }
    
    ThreadPoolSupport() {

        group = new ThreadGroup(ThreadPoolSupport.class.getName());
        
        final ThreadPoolSizeAdvisor advisor = ThreadPoolSizeAdvisor.get();

        final ThreadFactory threadFactory = (Runnable r) -> new Thread(group, r);

        final Supplier<BlockingQueue<Runnable>> workQueueFactory =
                ()->new LinkedBlockingQueue<>(QUEUE_CAPACITY);

        concurrentExecutor = new ThreadPoolExecutor(
                advisor.corePoolSize(),
                advisor.maximumPoolSize(),
                KEEP_ALIVE_TIME_SECS,
                TimeUnit.SECONDS,
                workQueueFactory.get(),
                threadFactory);
    }
    
    /*
     * Implementation Note: triggered by _Context.clear() when application shuts down.
     */
    @Override
    public void close() throws Exception {
        concurrentExecutor.shutdown();
    }
    
    /**
     * @return this thread-pool's underlying concurrent executor
     */
    public Executor getExecutor() {
        return concurrentExecutor;
    }
    
    /**
     * Non-blocking call. 
     * <p>
     * If the computation requires an open IsisSession use {@code IsisContext.compute(Supplier)} instead,
     * which utilizes a ForkJoinPool instead.
     * 
     * @param computation - async task 
     * @return new CompletableFuture utilizing this thread-pool's underlying concurrent executor
     */
    public <T> CompletableFuture<T> newCompletableFuture(Supplier<T> computation) {
        requires(computation, "computation");
        return CompletableFuture.supplyAsync(computation, getExecutor());
    }

    /**
     * Executes specified {@code callables} on the default executor.  
     * See {@link ThreadPoolExecutor#invokeAll(java.util.Collection)}
     * @param proposedExecutionMode - if 'higher concurrency than allowed' is replaced by 'highest-allowed' 
     * @param callables - nullable
     * @return non-null
     */
    public List<Future<Object>> invokeAll(
            final ThreadPoolExecutionMode proposedExecutionMode,
            @Nullable final List<Callable<Object>> callables) {

        if(isEmpty(callables)) {
            return emptyList();
        }

        requires(proposedExecutionMode, "proposedExecutionMode");

        final ThreadPoolExecutionMode executionMode = 
                ThreadPoolExecutionMode.honorHighestConcurrencyAllowed(proposedExecutionMode);

        switch (executionMode) {
        case PARALLEL:
            return invokeAll(concurrentExecutor, callables);

        case SEQUENTIAL:
        {
            final Future<List<Object>> commonFuture = 
                    uncheckedCast(invokeAll(concurrentExecutor, 
                            singletonList(toSingleTask(callables))).get(0));

            return IntStream.range(0, callables.size())
                    .mapToObj(index->new FutureWithIndexIntoFutureOfList<Object>(commonFuture, index))
                    .collect(toList());
        }

        case SEQUENTIAL_WITHIN_CALLING_THREAD:
        {
            return callables.stream()
            .map(FutureTask::new)
            .peek(FutureTask::run) // immediately run task on submission
            .collect(toList());
        }

        default:
            throw _Exceptions.unmatchedCase(ThreadPoolSupport.HIGHEST_CONCURRENCY_EXECUTION_MODE_ALLOWED);
        }

    }
    
    /**
     * Executes specified {@code callables} on the default executor.  
     * See {@link ThreadPoolExecutor#invokeAll(java.util.Collection)}
     * @param callables nullable
     * @return non-null
     */
    public List<Future<Object>> invokeAll(@Nullable final List<Callable<Object>> callables) {
        return invokeAll(HIGHEST_CONCURRENCY_EXECUTION_MODE_ALLOWED, callables);
    }

//TODO [ahuber] I'm proposing removal, in order to not bloat this class and since wrapping vargs 
// on the callers side with Arrays.asList(...) is trivial
//    /**
//     * Executes specified {@code callables} on the default executor.
//     * See {@link ThreadPoolExecutor#invokeAll }
//     * @param executionMode
//     * @param callables nullable
//     * @return non-null
//     */
//    @SafeVarargs
//    public final List<Future<Object>> invokeAll(
//            final ThreadPoolExecutionMode executionMode, 
//            final Callable<Object>... callables) {
//        
//        requires(executionMode, "executionMode");
//        requires(callables, "callables");
//        return invokeAll(executionMode, Arrays.asList(callables));
//    }

//    /**
//     * Wraps specified {@code callables} into a single task, which is then executed on the default executor.    
//     * The single task itself executes specified {@code callables} in sequence, one by one.
//     * @param callables nullable
//     * @return non-null
//     */
//    public List<Future<Object>> invokeAllSequential(@Nullable final List<Callable<Object>> callables) {
//        if(_NullSafe.isEmpty(callables)) {
//            return emptyList();
//        }
//        final Future<List<Object>> commonFuture = 
//                uncheckedCast(invokeAll(singletonList(toSingleTask(callables))).get(0));
//
//        return IntStream.range(0, callables.size())
//        .mapToObj(index->new FutureWithIndexIntoFutureOfList<Object>(commonFuture, index))
//        .collect(toList());
//    }
//
//    /**
//     * Wraps specified {@code callables} into a single task, which is then executed on the default executor.    
//     * The single task itself executes specified {@code callables} in sequence, one by one.
//     * @param callables nullable
//     * @return non-null
//     */
//    @SafeVarargs
//    public final List<Future<Object>> invokeAllSequential(final Callable<Object>... callables) {
//        return invokeAllSequential(Arrays.asList(callables));
//    }


    /**
     * Waits if necessary for the computation to complete. Suppresses checked exceptions.
     * @param futures
     * @return list of computation results.
     */
    public List<Object> join(@Nullable final List<Future<Object>> futures) {
        if (futures == null) {
            return null;
        }
        
        final long t0 = System.nanoTime();
        try{
            final List<Object> returnValues = _Lists.newArrayList();
            for (Future<Object> future : futures) {
                returnValues.add(join(future));
            }
            return returnValues;
        } finally {
            final long t1 = System.nanoTime();
            LOG.info("join'ing {} tasks: waited {} milliseconds ", futures.size(), 0.000_001 * (t1-t0));
        }
    }

    /**
     * Waits if necessary for the computation to complete. Re-throws any checked exception as RuntimeException.
     * @param futures
     * @return list of computation results.
     */
    public List<Object> joinGatherFailures(final List<Future<Object>> futures) {
        if (futures == null) {
            return null;
        }

        final long t0 = System.nanoTime();
        try{
            final List<Object> returnValues = _Lists.newArrayList();
            for (Future<Object> future : futures) {
                final Object result;
                try {
                    result = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                returnValues.add(result);
            }
            return returnValues;
        } finally {
            final long t1 = System.nanoTime();
            LOG.info("join'ing {} tasks: waited {} milliseconds ", futures.size(), 0.000_001 * (t1-t0));
        }
    }


    /**
     * Waits if necessary for the computation to complete. (Suppresses checked exceptions.)
     * @param future
     * @return the computation result
     */
    public Object join(final Future<Object> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            // ignore
            return null;
        }
    }
    
    @Override
    public String toString() {
        return concurrentExecutor.toString();
    }

    // -- HELPERS
    
    private List<Future<Object>> invokeAll(ThreadPoolExecutor executor, @Nullable final List<Callable<Object>> callables) {
        if(isEmpty(callables)) {
            return Collections.emptyList();
        }
        try {
            return executor.invokeAll(timed(executor, callables));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Callable<Object>> timed(
            final ThreadPoolExecutor executor,
            final List<Callable<Object>> callables) {
        final long queuedAt = System.currentTimeMillis();
        return callables.stream()
                .map(callable -> timed(callable, executor.getQueue().size(), queuedAt))
                .collect(Collectors.toList());
    }

    private static Callable<Object> timed(
            final Callable<Object> callable,
            final int queueSize,
            final long queuedAt) {

        return () -> {
            final long startedAt = System.currentTimeMillis();
            if(LOG.isDebugEnabled()) {
                LOG.debug("START: workQueue.size: {}, waited for: {}ms, {}",
                        queueSize,
                        startedAt - queuedAt,
                        callable.toString());
            }
            try {
                return callable.call();
            } finally {
                final long completedAt = System.currentTimeMillis();
                if(LOG.isDebugEnabled()) {
                    LOG.debug("END: completed in: {}ms, {}",
                            completedAt - startedAt,
                            callable.toString());
                }
            }
        };
    }

    private Callable<Object> toSingleTask(final List<Callable<Object>> callables) {
        return () -> {
            final List<Object> resultList = _Lists.newArrayList();
            for(Callable<Object> callable : callables) {
                resultList.add(callable.call()); // any exceptions thrown are propagated
            }
            return resultList;
        };
    }
    
    private static boolean isEmpty(Collection<?> x) { return x==null || x.size() == 0; }

}
