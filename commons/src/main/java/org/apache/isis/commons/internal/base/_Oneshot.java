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

import java.io.Serializable;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * One-shot idiom helper utility, thread-safe and serializable
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0
 *
 */
public final class _Oneshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Object $lock = new Object[0]; // serializable lock

    private int shotCount = 0;

    /**
     * @return whether the shot actually happened (was allowed)
     */
    public boolean shoot() {
        synchronized ($lock) {
            if(shotCount==0) {
                ++ shotCount;
                return true;
            }
            return false;
        }
    }

    /**
     * resets to initial condition, that is it allows one more shot
     */
    public void reset() {
        synchronized ($lock) {
            shotCount = 0;
        }
    }

}
