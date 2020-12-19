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

import java.util.concurrent.atomic.LongAdder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;

class ConcurrentTaskListTest {

    @Test
    void tasksShouldAllBeExecuted() {
        
        val counter = new LongAdder();

        _ConcurrentTaskList tasks = _ConcurrentTaskList.named("Test")
                .addRunnable("1", counter::increment)
                .addRunnable("2", counter::increment)
                .addRunnable("3", counter::increment)
                .addRunnable("4", counter::increment)
                .addRunnable("5", counter::increment)
                .addRunnable("6", counter::increment)
                .addRunnable("7", counter::increment)
                .addRunnable("8", counter::increment);
        
        tasks.submit(_ConcurrentContext.forkJoin());
        tasks.await();

        assertEquals(8L, counter.longValue());
        
    }
    
}
