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
package org.apache.causeway.commons.internal.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import lombok.Builder;

/**
 * <h1>- internal use only -</h1>
 *
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
@Builder
public record _ConcurrentContext(
        ExecutorService executorService,
        boolean enableExecutionLogging
        ) {

    public static _ConcurrentContextBuilder singleThreaded() {
        return _ConcurrentContext.builder()
                .executorService(Executors.newSingleThreadExecutor())
                .enableExecutionLogging(true);
    }

    public static _ConcurrentContextBuilder forkJoin() {
        return _ConcurrentContext.builder()
                .executorService(ForkJoinPool.commonPool())
                .enableExecutionLogging(true);
    }

    public static _ConcurrentContextBuilder sequential() {
        return _ConcurrentContext.builder()
                .executorService(null)
                .enableExecutionLogging(true);
    }

    public boolean shouldRunSequential() {
        return executorService == null;
    }

}
