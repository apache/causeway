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

package org.apache.isis.commons.internal.base;

import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * "consumes" the values, conceiving no information to JIT whether the value is actually used afterwards. 
 * This can save from the dead-code elimination of the computations resulting in the given values.
 * </p>
 * <p>
 * This is a poor man's version of JMH's Blackhole (org.openjdk.jmh.infra.Blackhole).
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Blackhole {

    /**
     * Consume object. This call provides a side effect preventing JIT to eliminate dependent computations.
     *
     * @param obj object to consume.
     */
    public static void consume(Object obj) {

        int tlrMask = internal.tlrMask; // volatile read
        int tlr = (internal.tlr = (internal.tlr * 1664525 + 1013904223));

        if ((tlr & tlrMask) == 0) {
            // SHOULD ALMOST NEVER HAPPEN
            internal.obj1 = new WeakReference<>(obj);
            internal.tlrMask = (tlrMask << 1) + 1;
        }

    }

    // -- HELPER

    public final static class _Blackhole_Internal {

        public int tlr;
        public volatile int tlrMask;
        public volatile Object obj1;

        public _Blackhole_Internal() {
            Random r = new Random(System.nanoTime());
            tlr = r.nextInt();
            tlrMask = 1;
            obj1 = new Object();
        }

    }

    private final static _Blackhole_Internal internal = new _Blackhole_Internal();

}
