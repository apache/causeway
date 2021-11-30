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

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.Synchronized;

/**
 * package private implementation of _Lazy
 * @since 2.0
 */
final class _Lazy_ThreadSafe<T> implements _Lazy<T> {

    private final Supplier<? extends T> supplier;
    private T value;
    private boolean memoized;
    private boolean getting;

    _Lazy_ThreadSafe(final @NonNull Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    @Override @Synchronized
    public boolean isMemoized() {
        return getting
                ? false // while getting always false
                : memoized;
    }

    @Override @Synchronized
    public void clear() {
        guardAgainstRecursiveCall();
        this.memoized = false;
        this.value = null;
    }

    @Override @Synchronized
    public T get() {
        if(memoized) {
            return value;
        }
        guardAgainstRecursiveCall();
        getting = true; // prevent the supplier from doing a nested call
        value = supplier.get();
        getting = false;
        memoized = true;
        return value;
    }

    @Override @Synchronized
    public Optional<T> getMemoized() {
        guardAgainstRecursiveCall();
        return Optional.ofNullable(value);
    }

    @Override @Synchronized
    public void set(final T value) {
        if(memoized) {
            throw _Exceptions.illegalState("cannot set value '%s' on Lazy that has already memoized a value", ""+value);
        }
        guardAgainstRecursiveCall();
        memoized = true;
        this.value = value;
    }

    // -- HELPER

    private final void guardAgainstRecursiveCall() {
        if(getting) {
            throw _Exceptions.illegalState("recursive call of lazy getter detected");
        }
    }

}
