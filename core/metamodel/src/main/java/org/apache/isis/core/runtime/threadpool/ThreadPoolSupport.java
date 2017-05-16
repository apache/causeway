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

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThreadPoolSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolSupport.class);

    public static ThreadGroup group;

    public static ThreadPoolExecutor executor   ;

    static {
        group = new ThreadGroup(ThreadPoolSupport.class.getName());

        final int corePoolSize = Runtime.getRuntime().availableProcessors();
        final int maximumPoolSize = Runtime.getRuntime().availableProcessors();
        final int keepAliveTimeSecs = 5;

        final int queueCapacity = 25;
        final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(queueCapacity);

        executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTimeSecs, TimeUnit.SECONDS,
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
                returnValues.add(join(future));
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
        }
        return null;
    }

    public static List<Future<Object>> invokeAll(final List<Callable<Object>> callables) {
        try {
            return executor.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
