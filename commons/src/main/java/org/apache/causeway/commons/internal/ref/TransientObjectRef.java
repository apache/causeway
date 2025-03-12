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
package org.apache.causeway.commons.internal.ref;

import java.io.Serializable;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

import lombok.Getter;

/**
 * non thread-safe
 */
public final class TransientObjectRef<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private transient @Nullable T object;
    private transient boolean updating;

    public TransientObjectRef(@Nullable final T object) {
        this.object = object;
    }

    public T computeIfAbsent(final Supplier<T> supplier) {
        return object!=null
            ? object
            : update(__->supplier.get());
    }

    /**
     * the updater itself must not directly modify this TransientObjectRef instance
     */
    public T update(final UnaryOperator<T> updater) {
        if(updating) {
            throw new UnsupportedOperationException("nested call to update detected");
        }
        this.updating = true;
        try {
            this.object = updater.apply(object);
        } finally {
            this.updating = false;
        }
        return object;
    }

    @Override
    public String toString() {
        return "TransientObjectRef[%s]".formatted(object!=null ? "PRESENT" : "EMPTY");
    }

}
