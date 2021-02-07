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
package org.apache.isis.commons.functional;

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
 * The {@link Result} type represents a value of one of two possible types (a disjoint union). 
 * The data constructors {@link Result#success(Object)} and {@link Result#failure(Throwable)}
 * represent the two possible values.
 * 
 * @since 2.0 {@index}
 */
@RequiredArgsConstructor(access=AccessLevel.PRIVATE, staticName="of")
@ToString @EqualsAndHashCode
public final class Result<L> {

    private final L value;
    private final Throwable throwable;
    private final boolean isSuccess;
    
    // -- FACTORIES

    public static <L> Result<L> of(final @NonNull Callable<L> callable) {
        try {
            return success(callable.call());
        } catch (Throwable e) {
            return failure(e);
        }
    }
    
    public static Result<Void> ofVoid(final @NonNull ThrowingRunnable runnable) {
        return of(ThrowingRunnable.toCallable(runnable));
    }
    
    public static <L> Result<L> success(final @Nullable L value) {
        return of(value, null, true);
    }
    
    public static <L> Result<L> failure(final @NonNull Throwable throwable) {
        return of(null, throwable, false);
    }
    
    // -- FACTORY SHORTCUTS
    
    public static <L> Result<L> failure(final @NonNull String message) {
        return failure(new RuntimeException(message));
    }
    
    public static <L> Result<L> failure(final @NonNull String message, final @NonNull Throwable cause) {
        return failure(new RuntimeException(message, cause));
    }
    
    // -- PREDICATES
    
    public boolean isSuccess() {
        return isSuccess;
    }
    
    public boolean isFailure() {
        return !isSuccess();
    }
    
    // -- ACCESSORS
    
    public Optional<L> getValue() {
        return Optional.ofNullable(value); 
    }

    public Optional<Throwable> getFailure() {
        return Optional.ofNullable(throwable); 
    }
    
    // -- PEEKING
    
    public Result<L> ifSuccess(final @NonNull Consumer<L> valueConsumer){
        if(isSuccess()) {
            valueConsumer.accept(value);
        }
        return this;
    }
    
    public Result<L> ifSuccessAndValuePresent(final @NonNull Consumer<L> valueConsumer){
        getValue().ifPresent(valueConsumer::accept);
        return this;
    }
    
    public Result<L> ifFailure(final @NonNull Consumer<Throwable> exceptionConsumer){
        if(isFailure()) {
            exceptionConsumer.accept(throwable);
        }
        return this;
    }
    
    // -- MAP NULL TO FAILURE
    
    public <E extends Throwable> Result<L> mapSuccessWithEmptyValueToFailure(
            final @NonNull Supplier<E> onNullValue){
        return isSuccess()
                && value==null
                ? Result.failure(onNullValue.get())
                : this;
    }
    
    public <E extends Throwable> Result<L> mapSuccessWithEmptyValueToNoSuchElement(){
        return mapSuccessWithEmptyValueToFailure(NoSuchElementException::new);
    }
    
    // -- MAPPING

    public <T> Result<T> mapSuccess(final @NonNull Function<L, T> successMapper){
        return isSuccess()
                ? Result.of(()->successMapper.apply(value))
                : Result.failure(throwable);
    }

    public Result<L> mapFailure(final @NonNull UnaryOperator<Throwable> failureMapper){
        if (isSuccess()) {
            return this;
        }
        try {
            return Result.failure(failureMapper.apply(throwable));
        } catch (Throwable e) {
            return failure(e);
        }
    }
    
    // -- FOLDING
    
    public <T> T fold(
            final @NonNull Function<L, T> successMapper,
            final @NonNull Function<Throwable, T> failureMapper){
        return isSuccess()
                ? successMapper.apply(value)
                : failureMapper.apply(throwable);
    }
    
    // -- EXTRACTION
    
    @SneakyThrows
    public L presentElseFail() {
        if (isSuccess()) {
            if(value==null) {
                throw new NoSuchElementException();
            }
            return value;
        }
        throw throwable;
    }
    
    @SneakyThrows
    public Optional<L> optionalElseFail() {
        if (isSuccess()) {
            return getValue();
        }
        throw throwable;
    }
    
    @SneakyThrows
    public L presentElseThrow(final @NonNull UnaryOperator<Throwable> toThrowable) {
        if (isSuccess()) {
            if(value==null) {
                throw toThrowable.apply(new NoSuchElementException());
            }
            return value;
        }
        throw toThrowable.apply(throwable);
    }
    
    @SneakyThrows
    public Optional<L> optionalElseThrow(final @NonNull UnaryOperator<Throwable> toThrowable) {
        if (isSuccess()) {
            return getValue();
        }
        throw toThrowable.apply(throwable);
    }
    
    public L presentElse(final @NonNull L defaultValue) {
        if (isSuccess()) {
            if(value!=null) {
                return value;
            }
        }
        return defaultValue;
    }
    
    public L presentElseGet(final @NonNull Supplier<L> defaultValueSupplier) {
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

    
}
