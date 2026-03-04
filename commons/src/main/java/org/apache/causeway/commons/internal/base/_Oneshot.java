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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h1>- internal use only -</h1>
 *
 * <p>One-shot utility, thread-safe and serializable
 *
 * <p><b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0
 */
public final class _Oneshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final AtomicInteger counter = new AtomicInteger(0); // is serializable

    /**
     * Returns whether the trigger was accepted.
     */
    public boolean trigger() {
        return counter.compareAndSet(0, 1);
    }

    /**
     * Returns whether the {@link Runnable} was actually executed
     * (indifferent to whether completing with or without success).
     * If the {@link Runnable} throws an {@link Exception}, this one-shot will be exhausted regardless.
     */
    public boolean trigger(final Runnable runnable) {
        // attempt to change 0 -> 1 atomically; only the thread that succeeds runs the runnable
        if (counter.compareAndSet(0, 1)) {
            runnable.run();
            return true;
        } else
            return false;
    }

    /**
     * resets to initial condition, that is, it allows one more trigger
     */
    public void reset() {
        counter.set(0);
    }

}
