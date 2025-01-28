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

import java.util.Optional;

import org.springframework.util.function.ThrowingSupplier;

import org.apache.causeway.commons.internal.exceptions._Exceptions;

import org.jspecify.annotations.NonNull;

/**
 * package private implementation of _Lazy
 * @since 2.0
 */
final class _Lazy_Simple<T> implements _Lazy<T> {

    private final ThrowingSupplier<? extends T> supplier;
    private T value;
    private boolean memoized;
    private boolean getting;

    _Lazy_Simple(final @NonNull ThrowingSupplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public boolean isMemoized() {
        guardAgainstRecursiveCall();
        return memoized;
    }

    @Override
    public void clear() {
        guardAgainstRecursiveCall();
        this.memoized = false;
        this.value = null;
    }

    @Override
    public T get() {
        if(memoized) {
            return value;
        }
        guardAgainstRecursiveCall();
        getting = true; // prevent the supplier from doing a nested call
        try {
            value = supplier.get();
        } finally {
            getting = false;
            memoized = true; // post condition as per contract
        }
        return value;
    }

    @Override
    public Optional<T> getMemoized() {
        guardAgainstRecursiveCall();
        return Optional.ofNullable(value);
    }

    @Override
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
