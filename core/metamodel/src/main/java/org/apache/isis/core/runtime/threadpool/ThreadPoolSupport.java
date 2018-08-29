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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThreadPoolSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolSupport.class);

    private final static int KEEP_ALIVE_TIME_SECS = 5;
    private final static int QUEUE_CAPACITY = 5000;

    private static final ThreadGroup group;
    private static final BlockingQueue<Runnable> workQueue;
    private static final ThreadPoolExecutor executor   ;

    static {
        group = new ThreadGroup(ThreadPoolSupport.class.getName());
        workQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

        final int corePoolSize = Runtime.getRuntime().availableProcessors();
        final int maximumPoolSize = Runtime.getRuntime().availableProcessors();

        executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                KEEP_ALIVE_TIME_SECS,
                TimeUnit.SECONDS,
                workQueue,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(final Runnable r) {
                        return new Thread(group, r);
                    }
                });
    }

    public static List<Object> join(final List<Future<Object>> futures) {
        if (futures == null) {
            return null;
        }

        final long t0 = System.currentTimeMillis();
        try{
            final List<Object> returnValues = Lists.newArrayList();
            for (Future<Object> future : futures) {
                Object result;
                try {
                    result = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    // ignore
                    result = null;
                }
                returnValues.add(result);
            }
            return returnValues;
        } finally {
            final long t1 = System.currentTimeMillis();
            if(LOG.isInfoEnabled()) {
                LOG.info("join'ing {} tasks: waited {} milliseconds ", futures.size(), (t1-t0));
            }
        }
    }

    public static List<Object> joinGatherFailures(final List<Future<Object>> futures) {
        if (futures == null) {
            return null;
        }

        final long t0 = System.currentTimeMillis();
        try{
            final List<Object> returnValues = Lists.newArrayList();
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
            final long t1 = System.currentTimeMillis();
            LOG.info("join'ing {} tasks: waited {} milliseconds ", futures.size(), (t1-t0));
        }
    }

    public static Object join(final Future<Object> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            // ignore
            return null;
        }
    }

    public static List<Future<Object>> invokeAll(final List<Callable<Object>> callables) {
        final long queuedAt = System.currentTimeMillis();
        try {
            ImmutableList<Callable<Object>> timedCallables =
                    FluentIterable.from(callables).transform(
                        new Function<Callable<Object>, Callable<Object>>() {
                            @Override
                            public Callable<Object> apply(final Callable<Object> callable) {
                                return new Callable<Object>() {
                                    @Override
                                    public Object call() throws Exception {

                                        final long startedAt = System.currentTimeMillis();
                                        if(LOG.isDebugEnabled()) {
                                            LOG.debug("START: workQueue.size: {}, waited for: {}ms, {}",
                                                    workQueue.size(),
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
                                    }
                                };
                            }
                        }).toList();
            return executor.invokeAll(timedCallables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<Future<Object>> invokeAll(final Callable<Object>... callables) {
        return invokeAll(Arrays.asList(callables));
    }
    public static List<Future<Object>> invokeSerial(final Callable<Object>... callables) {
        List<Future<Object>> futures = Lists.newArrayList();
        for (Callable<Object> callable : callables) {
            List<Future<Object>> x = invokeAll(callable);
            join(x);
            futures.addAll(x);
        }
        return futures;
    }

}
