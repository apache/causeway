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

/**
 * <h1>- internal use only -</h1>
 * <p>
 * One-shot utility, thread-safe and serializable
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * @since 2.0
 */
public final class _Oneshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Object $lock = new Object[0]; // serializable lock

    private volatile int triggerCount = 0;

    /**
     * Returns whether the trigger was accepted.
     */
    public boolean trigger() {
        synchronized ($lock) {
            if(triggerCount==0) {
                ++ triggerCount;
                return true;
            }
            return false;
        }
    }

    /**
     * Returns whether the {@link Runnable} was actually executed
     * (indifferent to whether completing with or without success).
     * If the {@link Runnable} throws an {@link Exception}, this one-shot will be exhausted regardless.
     */
    public boolean trigger(final Runnable runnable) {
        synchronized ($lock) {
            if(triggerCount==0) {
                ++ triggerCount;
                runnable.run();
            }
            return false;
        }
    }


    /**
     * resets to initial condition, that is it allows one more trigger
     */
    public void reset() {
        synchronized ($lock) {
            triggerCount = 0;
        }
    }

}
