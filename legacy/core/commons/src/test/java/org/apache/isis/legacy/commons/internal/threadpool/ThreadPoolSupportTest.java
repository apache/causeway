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

package org.apache.isis.legacy.commons.internal.threadpool;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.isis.legacy.commons.internal.threadpool.ThreadPoolExecutionMode;
import org.apache.isis.legacy.commons.internal.threadpool.ThreadPoolSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.commons.internal.collections._Lists;

@Deprecated
class ThreadPoolSupportTest {

    private ThreadPoolSupport pool;
    private Buffer buffer;

    @BeforeEach
    void setUp() throws Exception {
        pool = new ThreadPoolSupport();
        buffer = new Buffer();
    }

    @AfterEach
    void tearDown() throws Exception {
        pool.close();
    }

    @Test
    void shouldPreserveSequence_whenNoThread() {
        buffer.append("A");
        buffer.append("B");
        buffer.append("C");
        Assertions.assertEquals("ABC", buffer.toString());
    }

    @Test
    void shouldPreserveSequence_whenSequentialExecution() {
        runSequentialExecutionWithMode(ThreadPoolExecutionMode.SEQUENTIAL);
    }

    @Test
    void shouldPreserveSequence_whenSequentialExecutionWithinCallingThread() {
        runSequentialExecutionWithMode(ThreadPoolExecutionMode.SEQUENTIAL_WITHIN_CALLING_THREAD);
    }

    // -- HELPER

    void runSequentialExecutionWithMode(ThreadPoolExecutionMode executionMode) {

        final List<Future<Object>> futures = _Lists.newArrayList();

        final List<Callable<Object>> tasks = _Lists.of(
                ()->{buffer.append("A"); return "A";},
                ()->{buffer.append("B"); return "B";},
                ()->{buffer.append("C"); return "C";},
                ()->{buffer.append("D"); return "D";},
                ()->{buffer.append("E"); return "E";},
                ()->{buffer.append("F"); return "F";},
                ()->{buffer.append("G"); return "G";},
                ()->{buffer.append("H"); return "H";},
                ()->{buffer.append("I"); return "I";},
                ()->{buffer.append("J"); return "J";}
                );

        for(int i=0; i<256; ++i) {
            final List<Future<Object>> taskFutures = pool.invokeAll(executionMode, tasks);
            pool.joinGatherFailures(taskFutures);
            futures.addAll(taskFutures);
        }

        final String resultString = futures.stream()
                .map(future->{try {
                    return future.get();
                } catch (InterruptedException | ExecutionException e) {
                    return ""+e;
                }})
                .map(result->""+result)
                .collect(Collectors.joining());

        final String buffer_allowedSequencesRemoved = buffer.toString().replace("ABCDEFGHIJ", "");
        final String result_allowedSequencesRemoved = resultString.replace("ABCDEFGHIJ", "");


        Assertions.assertEquals("", buffer_allowedSequencesRemoved);
        Assertions.assertEquals("", result_allowedSequencesRemoved);

    }

    /** thread-safe StringBuffer */
    private static class Buffer {

        final StringBuffer sb = new StringBuffer();

        public Buffer append(final CharSequence c) {
            synchronized (sb) {
                sb.append(c);    
            }
            return this;
        }

        @Override
        public String toString() {
            synchronized (sb) {
                return sb.toString();    
            }
        }

    }

}
