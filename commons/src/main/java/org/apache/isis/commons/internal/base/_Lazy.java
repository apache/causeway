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

import java.util.function.Supplier;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * (non-thread-safe) Supplier with memoization.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public interface _Lazy<T> extends Supplier<T> {

    // -- INTERFACE

    /**
     * @return whether this lazy got initialized and holds a memoized value
     */
    public boolean isMemoized();

    /**
     * Clears the lazy's memoized value. Resets this lazy to its initial state.<br>
     * isMemoized() = false;
     *
     */
    public void clear();

    /**
     * Evaluates this lazy value and memoizes it, when called the first time
     * after initialization or clear().
     * <p>
     * Postcondition when memoization throws an exception: isMemoized()->true and get()->null  
     */
    @Override
    public T get();
    
    /**
     * Sets the memoized value, if not already memoized. 
     * @param value
     * @throws IllegalStateException if already memoized
     */
    public void set(T value);
    

    // -- FACTORIES

    /**
     * Concurrent calls to this lazy's get() method might result in concurrent calls to the 
     * specified {@code supplier}. 
     * @param supplier
     * @return an (non-thread-safe) instance of _Lacy that initializes with the specified {@code supplier}
     */
    public static <T> _Lazy<T> of(Supplier<? extends T> supplier) {
        return new _Lazy_Simple<T>(supplier);
    }

    /**
     * Thread-safe variant to {@link _Lazy#of(Supplier)}.
     * Concurrent calls to this lazy's get() method will never result in concurrent calls to the 
     * specified {@code supplier}. 
     * @param supplier
     * @return an (thread-safe) instance of _Lacy that initializes with the specified {@code supplier}
     */
    public static <T> _Lazy<T> threadSafe(Supplier<? extends T> supplier) {
        return new _Lazy_ThreadSafe<T>(supplier);
    }

}
