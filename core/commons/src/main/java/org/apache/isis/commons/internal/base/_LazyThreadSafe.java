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

import static org.apache.isis.commons.internal.base._With.requires;

import java.util.function.Supplier;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Thread-safe Supplier with memoization.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0.0
 */
public final class _LazyThreadSafe<T> implements Supplier<T> {

    private final Supplier<? extends T> supplier;
    private T value;
    private boolean memoized;

    /**
     * Thread-safe variant to {@link _Lazy#of(Supplier)}.
     * Concurrent calls to this lazy's get() method will never result in concurrent calls to the 
     * specified {@code supplier}. 
     * @param supplier
     * @return an (thread-safe) instance of _Lacy that initializes with the specified {@code supplier}
     */
    public static <T> _LazyThreadSafe<T> of(Supplier<? extends T> supplier) {
        return new _LazyThreadSafe<T>(supplier);
    }

    private _LazyThreadSafe(Supplier<? extends T> supplier) {
        this.supplier = requires(supplier, "supplier");
    }

    /**
     * @return whether this lazy got initialized and holds a memoized value
     */
    public boolean isMemoized() {
        synchronized (this) {
            return memoized;    
        }
    }

    /**
     * Clears the lazy's memoized value. Resets this lazy to its initial state.<br>
     * isMemoized() = false;
     *
     */
    public void clear() {
        synchronized (this) {
            this.memoized = false;
            this.value = null;
        }
    }

    /**
     * Evaluates this lazy value and memoizes it, when called the first time
     * after initialization or clear().
     */
    @Override
    public T get() {
        synchronized (this) {
            if(memoized) {
                return value;
            }
            memoized=true;
            return value = supplier.get();    
        }
    }

}
