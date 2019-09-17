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

/**
 *  ThreadPollSupport's executions mode where the enum's ordinal corresponds to the level of concurrency.
 */
@Deprecated
enum ThreadPoolExecutionMode {

    /**
     *  Wraps submitted tasks into a single task, which is then executed within the context 
     *  of the calling thread. Basically to allow for switching off background execution to 
     *  run submitted tasks in sequence (legacy behavior) for debugging purposes.
     *  (expected lowest concurrency)
     */
    SEQUENTIAL_WITHIN_CALLING_THREAD,

    /**
     * Wraps submitted tasks into a single task, which is then executed on the default executor.
     * (expected medium concurrency)
     */
    SEQUENTIAL,

    /**
     * Executes submitted tasks on the default executor. 
     * (expected highest concurrency)
     */
    PARALLEL,

    ;

    /**
     * {@link ThreadPoolSupport.HIGHEST_CONCURRENCY_EXECUTION_MODE_ALLOWED} acts as an upper bound for the 
     * concurrency level supported.
     * @param proposedExecutionMode
     * @return proposedExecutionMode or the highest possible concurrent execution mode allowed, if 
     * the proposedExecutionMode exceeds the highest allowed 
     */
    public static ThreadPoolExecutionMode honorHighestConcurrencyAllowed(
            ThreadPoolExecutionMode proposedExecutionMode) {

        final int upper = ThreadPoolSupport.HIGHEST_CONCURRENCY_EXECUTION_MODE_ALLOWED.ordinal();
        final int proposed = proposedExecutionMode.ordinal();
        final int bounded = proposed>upper ? upper : proposed; 

        return ThreadPoolExecutionMode.values()[bounded];
    }

}
