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
package org.apache.isis.core.commons.internal.base;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import static org.apache.isis.core.commons.internal.base._With.requires;

/**
 * <h1>- internal use only -</h1>
 * <p>
 *  Provides a generic (left-fold) reduction class.
 *  </p>
 *  <p>
 *  Most intuitive example of a reduction is finding the
 *  minimum value from a list of values. See {@link ReductionTest} for examples.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 *
 */
public final class _Reduction<T> implements Consumer<T> {

    private final BinaryOperator<T> accumulator;
    private T result;
    private boolean initialized = false;


    /**
     * Inspired by {@link Stream#reduce(Object, BinaryOperator)}
     * @param identity
     * @param accumulator
     * @return
     */
    public static <T> _Reduction<T> of(@Nullable T identity, BinaryOperator<T> accumulator){
        return new _Reduction<T>(identity, accumulator, true);
    }

    /**
     * Inspired by {@link Stream#reduce(BinaryOperator)}
     * @param accumulator
     * @return
     */
    public static <T> _Reduction<T> of(BinaryOperator<T> accumulator){
        return new _Reduction<T>(null, accumulator, false);
    }

    private _Reduction(@Nullable T identity, BinaryOperator<T> accumulator, boolean initialized) {
        requires(accumulator, "accumulator");
        this.initialized = initialized;
        this.result = identity;
        this.accumulator = accumulator;
    }

    @Override
    public void accept(@Nullable T next) {
        if(!initialized) {
            result = next;
            initialized = true;
            return;
        }
        result = accumulator.apply(result, next);
    }

    /**
     * Returns the reduction result if ever initialized, Optional.empty() otherwise.
     * @return non-null
     */
    public Optional<T> getResult() {
        if(!initialized) {
            return Optional.empty();
        }
        return Optional.ofNullable(result);
    }


}
