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
package org.apache.causeway.commons.functional;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * The {@link Try} type represents a value of one of two possible types (a disjoint union)
 * of {@link Success} or {@link Failure}.
 * <p>
 * Factory methods {@link Try#success(Object)} and {@link Try#failure(Throwable)}
 * correspond to the two possible values.
 * <p>
 * Follows the <em>Railway Pattern</em>, that is, once failed, stays failed.
 * @see Railway
 *
 * @since 2.0 {@index}
 */
public interface Try<T> {

    // -- FACTORIES

    public static <T> Try<T> call(final @NonNull Callable<T> callable) {
        try {
            return success(callable.call());
        } catch (Throwable e) {
            return failure(e);
        }
    }

    public static Try<Void> run(final @NonNull ThrowingRunnable runnable) {
        try {
            runnable.run();
            return success(null);
        } catch (Throwable e) {
            return failure(e);
        }
    }

    public static <T> Success<T> success(final @Nullable T value) {
        return new Success<>(value);
    }

    public static <T> Failure<T> failure(final @NonNull Throwable throwable) {
        return new Failure<T>(throwable);
    }

    // -- PREDICATES

    boolean isSuccess();
    boolean isFailure();

    // -- ACCESSORS

    /**
     * Optionally returns the contained {@code value} based on presence,
     * that is, if this is a {@link Success} and the value is not {@code null}.
     */
    Optional<T> getValue();
    /**
     * Optionally returns the contained {@code failure} based on presence,
     * that is, if this is a {@link Failure}.
     */
    Optional<Throwable> getFailure();

    // -- PEEKING

    /**
     * Peeks into the {@code value} if this is a {@link Success}.
     */
    Try<T> ifSuccess(final @NonNull Consumer<Optional<T>> valueConsumer);
    /**
     * Peeks into the {@code failure} if this is a {@link Failure}.
     */
    Try<T> ifFailure(final @NonNull Consumer<Throwable> exceptionConsumer);

    // -- FAIL EARLY

    /** Throws the contained failure if any. */
    Try<T> ifFailureFail();
    /** Throws {@link NoSuchElementException} if {@code value} is {@code null}. */
    Try<T> ifAbsentFail();

    // -- MAPPING

    /**
     * Maps this {@link Try} to another if this is a {@link Success}.
     * Otherwise if this is a {@link Failure} acts as identity operator.
     */
    <R> Try<R> mapSuccess(final @NonNull Function<T, R> successMapper);
    /**
     * Maps this {@link Try} to another if its a {@link Failure}.
     * Otherwise if this is a {@link Success} acts as identity operator.
     */
    Try<T> mapFailure(final @NonNull UnaryOperator<Throwable> failureMapper);
    /**
     * Maps this {@link Try} to {@link Failure} if this is a {@link Success} with an empty {@code value}.
     * Otherwise acts as identity operator.
     */
    Try<T> mapEmptyToFailure();
    /**
     * Maps this {@link Try} to {@link Either}
     * using according mapping function {@code successMapper} or {@code failureMapper}.
     * @apiNote It is a common functional programming convention, to map the success value <i>right</i>.
     */
    <L, R> Either<L, R> map(
            final @NonNull Function<Throwable, L> failureMapper,
            final @NonNull Function<Optional<T>, R> successMapper);

    // -- TERMINATE

    /**
     * Either consumes the success or the failure.
     * @apiNote Order of arguments conforms to {@link #map(Function, Function)}
     */
    void accept(
            final @NonNull Consumer<Throwable> failureConsumer,
            final @NonNull Consumer<Optional<T>> successConsumer);

    // -- FOLDING

    /**
     * Maps the contained {@code value} or {@code failure} to a new value of type {@code R}
     * using according mapping function {@code successMapper} or {@code failureMapper}.
     * @apiNote Order of arguments conforms to {@link #map(Function, Function)}
     */
    <R> R fold(
            final @NonNull Function<Throwable, R> failureMapper,
            final @NonNull Function<Optional<T>, R> successMapper);

    // -- CONCATENATION

    /**
     * If this is a {@link Success}, maps it to a new {@link Try} based on given {@link Callable}.
     * Otherwise if its a {@link Failure} acts as identity operator.
     */
    <R> Try<R> thenCall(final @NonNull Callable<R> callable);
    /**
     * If this is a {@link Success}, maps it to new {@link Try} based on given {@link ThrowingRunnable}.
     * Otherwise if its a {@link Failure} acts as identity operator.
     */
    Try<Void> thenRun(final @NonNull ThrowingRunnable runnable);

    // -- SUCCESS

    @lombok.Value
    @RequiredArgsConstructor
    final class Success<T> implements Try<T>, Serializable {
        private static final long serialVersionUID = 1L;

        private final @Nullable T value;

        @Override public boolean isSuccess() { return true; }
        @Override public boolean isFailure() { return false; }

        @Override public Optional<T> getValue() { return Optional.ofNullable(value); }
        @Override public Optional<Throwable> getFailure() { return Optional.empty(); }

        @Override
        public Success<T> ifSuccess(final @NonNull Consumer<Optional<T>> valueConsumer) {
            valueConsumer.accept(getValue());
            return this;
        }

        @Override
        public Success<T> ifFailure(final @NonNull Consumer<Throwable> exceptionConsumer) {
            return this;
        }

        @Override
        public Success<T> ifFailureFail() {
            return this;
        }

        @Override
        public Success<T> ifAbsentFail() {
            if(value==null) throw _Exceptions.noSuchElement();
            return this;
        }

        @Override
        public <R> Try<R> mapSuccess(final @NonNull Function<T, R> successMapper){
            return Try.call(()->successMapper.apply(value));
        }

        @Override
        public Success<T> mapFailure(final @NonNull UnaryOperator<Throwable> failureMapper){
            return this;
        }

        @Override
        public Try<T> mapEmptyToFailure() {
            return value!=null
                    ? this
                    : Try.failure(_Exceptions.noSuchElement());
        }

        @Override
        public <R> Try<R> thenCall(final @NonNull Callable<R> callable) {
            return Try.call(callable);
        }

        @Override
        public Try<Void> thenRun(final @NonNull ThrowingRunnable runnable) {
            return Try.run(runnable);
        }

        @Override
        public void accept(
                final @NonNull Consumer<Throwable> failureConsumer,
                final @NonNull Consumer<Optional<T>> successConsumer) {
            successConsumer.accept(getValue());
        }

        @Override
        public <R> R fold(
                final @NonNull Function<Throwable, R> failureMapper,
                final @NonNull Function<Optional<T>, R> successMapper) {
            return successMapper.apply(getValue());
        }

        @Override
        public <L, R> Either<L, R> map(
                final @NonNull Function<Throwable, L> failureMapper,
                final @NonNull Function<Optional<T>, R> successMapper) {
            return Either.right(successMapper.apply(getValue()));
        }

    }

    // -- FAILURE

    @lombok.Value
    @RequiredArgsConstructor
    final class Failure<T> implements Try<T>, Serializable {
        private static final long serialVersionUID = 1L;

        private final @NonNull Throwable throwable;

        @Override public boolean isSuccess() { return false; }
        @Override public boolean isFailure() { return true; }

        @Override public Optional<T> getValue() { return Optional.empty(); }
        @Override public Optional<Throwable> getFailure() { return Optional.of(throwable); }

        @Override
        public Failure<T> ifSuccess(final @NonNull Consumer<Optional<T>> valueConsumer) {
            return this;
        }

        @Override
        public Failure<T> ifFailure(final @NonNull Consumer<Throwable> exceptionConsumer) {
            exceptionConsumer.accept(throwable);
            return this;
        }

        @Override @SneakyThrows
        public Failure<T> ifFailureFail() {
            throw throwable;
        }

        @Override @SneakyThrows
        public Failure<T> ifAbsentFail() {
            throw _Exceptions.noSuchElement();
        }

        @Override
        public <R> Failure<R> mapSuccess(final @NonNull Function<T, R> successMapper){
            return new Failure<>(throwable);
        }

        @Override
        public Failure<T> mapFailure(final @NonNull UnaryOperator<Throwable> failureMapper){
            try {
                return new Failure<>(failureMapper.apply(throwable));
            } catch (Throwable e) {
                return failure(e);
            }
        }

        @Override
        public Try<T> mapEmptyToFailure() {
            return this;
        }

        @Override
        public <R> Failure<R> thenCall(final @NonNull Callable<R> callable) {
            return new Failure<>(throwable);
        }

        @Override
        public Try<Void> thenRun(final @NonNull ThrowingRunnable runnable) {
            return new Failure<>(throwable);
        }

        @Override
        public void accept(
                final @NonNull Consumer<Throwable> failureConsumer,
                final @NonNull Consumer<Optional<T>> successConsumer) {
            failureConsumer.accept(throwable);
        }

        @Override
        public <R> R fold(
                final @NonNull Function<Throwable, R> failureMapper,
                final @NonNull Function<Optional<T>, R> successMapper) {
            return failureMapper.apply(throwable);
        }

        @Override
        public <L, R> Either<L, R> map(
                final @NonNull Function<Throwable, L> failureMapper,
                final @NonNull Function<Optional<T>, R> successMapper) {
            return Either.left(failureMapper.apply(throwable));
        }

    }

}
