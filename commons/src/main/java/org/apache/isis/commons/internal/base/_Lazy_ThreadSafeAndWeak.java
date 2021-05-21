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
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.NonNull;

/**
 * package private mixin for _Lazy
 * @since 2.0
 */
final class _Lazy_ThreadSafeAndWeak<T> implements _Lazy<T> {

    private final Supplier<? extends T> supplier;
    private WeakReference<T> weakValueReference = null;

    _Lazy_ThreadSafeAndWeak(final @NonNull Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public boolean isMemoized() {
        throw _Exceptions.unsupportedOperation("undecidable for weak references");
    }

    @Override
    public void clear() {
        synchronized (this) {
            if(weakValueReference!=null) {
                weakValueReference.clear();
                weakValueReference = null;
            }
        }
    }

    @Override
    public T get() {
        synchronized (this) {
            if(weakValueReference!=null) {
                final T value = weakValueReference.get();
                if(value!=null) {
                    return value;
                }
            }
            final T newValue = supplier.get();
            weakValueReference = new WeakReference<T>(newValue);
            return newValue;
        }
    }

    @Override
    public Optional<T> getMemoized() {
        throw _Exceptions.unsupportedOperation("undecidable for weak references");
    }

    @Override
    public void set(T newValue) {
        synchronized (this) {
            weakValueReference = new WeakReference<T>(newValue);
        }
    }

}
