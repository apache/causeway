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
package org.apache.causeway.commons.internal.base;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import lombok.SneakyThrows;

class _OneshotTest {

    final _Oneshot oneshot = new _Oneshot();
    final LongAdder counter = new LongAdder();
    final AtomicReference<String> errors = new AtomicReference<>();

    @Test
    void test() {
        oneshot.trigger(this::sayHelloOnce);
        assertEquals(1, counter.intValue());
        assertEquals(null, errors.get());
    }

    @SneakyThrows
    void sayHelloOnce() {
        if(counter.intValue()>0) {
            errors.set("recursion detected");
            fail("recursion detected");
        }

        counter.increment();

        // calling 'sayHelloOnce' from a different thread, while still executing in the main thread,
        // must not lead to a recursion
        var thread = new Thread(()->oneshot.trigger(this::sayHelloOnce));
        thread.start();
        thread.join();
    }

}
