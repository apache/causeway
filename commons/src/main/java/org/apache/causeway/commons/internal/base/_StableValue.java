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

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * <h1>- internal use only -</h1>
 * <p><b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * <p>A thread-safe, lazily initialized holder that can be set at most once.
 *
 * <p>This class mimics the behavior of the StableValue API introduced in JDK 25 preview.
 * It ensures that a value is set only once, using a supplier, and subsequent calls return the same value.
 * Useful for caching expensive computations or initializing resources lazily and safely.
 *
 * <p>_StableValue is serializable, but such that we don't marshall the contained value, if any.
 *
 * <p>Equality: 2 _StableValue(s) are equal iff their contained values are equal.
 *
 * @param <T> the type of the value to be held
 * @since 4.0, back-ported to v2
 */
@AllArgsConstructor
public final class _StableValue<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Getter @Accessors(fluent = true)
	private final AtomicReference<T> ref;

    public _StableValue() {
        this(new AtomicReference<>());
    }

    public _StableValue(final T t) {
        this(new AtomicReference<>(t));
    }

    /**
     * Returns the current value if already set, or sets it using the provided supplier
     * and returns the result. This ensures the supplier is only executed once, even
     * under concurrent access.
     *
     * @param supplier the supplier to initialize the value
     * @return the stored value
     */
    public T orElseSet(final Supplier<T> supplier) {
        T value = ref.get();
        if (value == null) {
            T newValue = supplier.get();
            if (ref.compareAndSet(null, newValue)) {
                return newValue;
            } else {
                return ref.get(); // another thread set the value
            }
        }
        return value;
    }

    /**
     * Checks if the value has been set.
     *
     * @return true if the value is set, false otherwise
     */
    public boolean isSet() {
        return ref.get() != null;
    }

    /**
     * Returns the current value. If the value has not been set,
     * an {@link IllegalStateException} is thrown.
     *
     * @return the current value
     * @throws IllegalStateException if the value has not been set
     */
    public T get() {
        T value = ref.get();
        if (value == null) {
            throw new IllegalStateException("Value not set");
        }
        return value;
    }

    // -- CONTRACT

    @Override
    public final boolean equals(final Object other) {
        return other instanceof _StableValue
            ? Objects.equals(this.ref.get(), ((_StableValue<?>)other).ref.get())
            : false;
    }

    // -- SERIALIZATION PROXY

    private final static class StableValueProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        StableValueProxy() {}
        private Object readResolve() {
            return new _StableValue<>();
        }
    }

    private Object writeReplace() {
        return new StableValueProxy();
    }

    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

}