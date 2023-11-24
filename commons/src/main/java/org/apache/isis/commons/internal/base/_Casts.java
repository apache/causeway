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
import java.util.function.BiFunction;

import org.springframework.lang.Nullable;

import lombok.NonNull;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Casting Utilities
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Casts {

    private _Casts(){}

    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(final @Nullable Object obj) {
        return (T) obj;
    }

    /**
     * Casts an object to the class or interface represented by given {@code cls} Class object,
     * then wraps the result in an {@link Optional}. The {@link Optional} is empty if the cast
     * fails or provided {@code value} is {@code null}.
     * @param <T>
     * @param value
     * @param cls
     * @return non-null
     */
    public static <T> Optional<T> castTo(final @NonNull Class<T> cls, final @Nullable Object value) {
        if(value==null) {
            return Optional.empty();
        }
        if(cls.isAssignableFrom(value.getClass())) {
            return Optional.of(cls.cast(value));
        }
        return Optional.empty();
    }

    /**
     * Casts an object to the class or interface represented by given {@code cls} Class object.
     * Returns {@code null}, if the cast fails or provided {@code value} is {@code null}.
     * @param <T>
     * @param value
     * @param cls
     * @return casted value, or null
     */
    public static @Nullable <T> T castToOrElseNull(final @Nullable Object value, final @NonNull Class<T> cls) {
        if(value==null) {
            return null;
        }
        if(cls.isAssignableFrom(value.getClass())) {
            return cls.cast(value);
        }
        return null;
    }

    /**
     * Dependent on whether left or right can be cast to {@code cls}, the appropriate functional
     * interface is chosen to produce the result.
     * @param left
     * @param right
     * @param cls
     * @param onBothCast
     * @param onLeftCast
     * @param onRightCast
     * @param onNonCast
     */
    public static <T, R, U, V> R castThenApply(
            final @Nullable U left,
            final @Nullable V right,
            final @NonNull Class<T> cls,
            final BiFunction<T, T, R> onBothCast,
            final BiFunction<T, V, R> onLeftCast,
            final BiFunction<U, T, R> onRightCast,
            final BiFunction<U, V, R> onNonCast) {

        T left_casted=null, right_casted=null;
        boolean left_not_casted=false, right_not_casted=false;

        if(left==null) {
            left_casted = null;
        } else if(cls.isAssignableFrom(left.getClass())) {
            left_casted = cls.cast(left);
        } else {
            left_not_casted = true;
        }

        if(right==null) {
            right_casted = null;
        } else if(cls.isAssignableFrom(right.getClass())) {
            right_casted = cls.cast(right);
        } else {
            right_not_casted = true;
        }

        if(left_not_casted && right_not_casted) {
            return onNonCast.apply(left, right);
        }

        if(!left_not_casted && !right_not_casted) {
            return onBothCast.apply(left_casted, right_casted);
        }

        if(left_not_casted) {
            return onRightCast.apply(left, right_casted);
        }

        return onLeftCast.apply(left_casted, right);

    }


}
