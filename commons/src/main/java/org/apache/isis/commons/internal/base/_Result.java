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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * A specialization of the {@link _Either} monad.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0
 */
@RequiredArgsConstructor(access=AccessLevel.PRIVATE, staticName="of")
@ToString @EqualsAndHashCode
public final class _Result<L> {

    private final L value;
    private final Throwable exception;
    private final boolean isSuccess;
    
    // -- FACTORIES

    public static <L> _Result<L> of(@NonNull Callable<L> callable) {
        try {
            return success(callable.call());
        } catch (Throwable e) {
            return failure(e);
        }
    }
    
    public static <L> _Result<L> ofNullable(@NonNull Callable<L> callable) {
        try {
            return successNullable(callable.call());
        } catch (Throwable e) {
            return failure(e);
        }
    }
    
    public static <L> _Result<L> success(@NonNull L value) {
        return of(value, null, true);
    }

    public static <L> _Result<L> successNullable(@Nullable L value) {
        return of(value, null, true);
    }
    
    public static <L> _Result<L> failure(@NonNull Throwable exception) {
        return of(null, exception, false);
    }
    
    // -- PREDICATES
    
    public boolean isSuccess() {
        return isSuccess;
    }
    
    public boolean isFailure() {
        return !isSuccess();
    }
    
    // -- ACCESSORS
    
    public Optional<L> value() {
        return Optional.ofNullable(value); 
    }

    public Optional<Throwable> exception() {
        return Optional.ofNullable(exception); 
    }

//    public L valueIfAny() {
//        return value; 
//    }
//
//    public Throwable exceptionIfAny() {
//        return exception; 
//    }
    
    // -- PEEKING
    
    public _Result<L> onSuccess(final @NonNull Consumer<L> valueConsumer){
        if(isSuccess()) {
            valueConsumer.accept(value);
        }
        return this;
    }
    
    public _Result<L> onFailure(final @NonNull Consumer<Throwable> exceptionConsumer){
        if(isFailure()) {
            exceptionConsumer.accept(exception);
        }
        return this;
    }
    
    // -- MAPPING

    public <T> _Result<T> mapValue(final @NonNull Function<L, T> valueMapper){
        return isSuccess()
                ? _Result.of(()->valueMapper.apply(value))
                : _Result.failure(exception);
    }

    public _Result<L> mapException(final @NonNull UnaryOperator<Throwable> exceptionMapper){
        if (isSuccess()) {
            return this;
        }
        try {
            return _Result.failure(exceptionMapper.apply(exception));
        } catch (Throwable e) {
            return failure(e);
        }
    }
    
    // -- FOLDING
    
    public <T> T fold(
            final @NonNull Function<L, T> valueMapper,
            final @NonNull Function<Throwable, T> exceptionMapper){
        return isSuccess()
                ? valueMapper.apply(value)
                : exceptionMapper.apply(exception);
    }
    
    // -- REDUCTION
    
    @SneakyThrows
    public L getOrThrow() {
        if (isSuccess()) {
            if(value==null) {
                throw new NoSuchElementException();
            }
            return value;
        }
        throw exception;
    }
    
    @SneakyThrows
    public @Nullable L getNullableOrThrow() {
        if (isSuccess()) {
            return value;
        }
        throw exception;
    }
    
    public L getOrDefault(final @NonNull L defaultValue) {
        if (isSuccess()) {
            if(value!=null) {
                return value;
            }
        }
        return defaultValue;
    }
    
    public @Nullable L getNullableOrDefault(final @Nullable L defaultValue) {
        if (isSuccess()) {
            return value;
        }
        return defaultValue;
    }
    
    public L getOrElse(final @NonNull Supplier<L> defaultValueSupplier) {
        if (isSuccess()) {
            if(value!=null) {
                return value;
            }
        }
        val defaultValue = defaultValueSupplier.get();
        if(defaultValue!=null) {
            return defaultValue;
        }
        throw new NoSuchElementException();
    }
    
    public @Nullable L getNullableOrElse(final @NonNull Supplier<L> defaultValueSupplier) {
        if (isSuccess()) {
            return value;
        }
        return defaultValueSupplier.get();
    }
    
}
