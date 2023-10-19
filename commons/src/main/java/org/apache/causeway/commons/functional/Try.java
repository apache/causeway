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
import java.util.function.Supplier;

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

    /** success case with no value */
    public static <T> Try<T> empty() {
        return success(null);
    }

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
     * If this is a {@link Success}, peeks into the {@code value} wrapped in an {@link Optional}.
     * <p>
     * If given valueConsumer throws an exception, a failed {@link Try} is returned.
     */
    Try<T> ifSuccess(final @NonNull ThrowingConsumer<Optional<T>> valueConsumer);
    /**
     * If this is a {@link Success} peeks into the (null-able) {@code value}.
     * <p>
     * If given valueConsumer throws an exception, a failed {@link Try} is returned.
     * @apiNote If preceded with a call to {@link #mapEmptyToFailure()},
     *      the success value - as passed over to the valueConsumer - is guaranteed non-null.
     */
    Try<T> ifSuccessAsNullable(final @NonNull ThrowingConsumer<T> valueConsumer);
    /**
     * If this is a {@link Failure}, peeks into the {@code failure}.
     * <p>
     * If given exceptionConsumer throws an exception, a failed {@link Try} is returned.
     */
    Try<T> ifFailure(final @NonNull ThrowingConsumer<Throwable> exceptionConsumer);

    // -- FAIL EARLY

    /** Throws the contained failure if any. */
    Try<T> ifFailureFail();
    /** Throws {@link NoSuchElementException} if {@code value} is {@code null}. */
    Try<T> ifAbsentFail();

    // -- MAPPING

    /**
     * If this is a {@link Success}, maps this {@link Try} to another,
     * by calling the successMapper with the {@code value} wrapped by an {@link Optional}.
     * Otherwise if this is a {@link Failure}, acts as identity operator,
     * though implementations may return a new instance.
     * <p>
     * If given successMapper throws an exception, a failed {@link Try} is returned.
     */
    <R> Try<R> mapSuccess(@NonNull ThrowingFunction<Optional<T>, R> successMapper);
    /**
     * If this is a {@link Success}, maps this {@link Try} to another,
     * by calling the successMapper with the {@code value} (which may be null).
     * Otherwise if this is a {@link Failure}, acts as identity operator,
     * though implementations may return a new instance.
     * <p>
     * If given successMapper throws an exception, a failed {@link Try} is returned.
     * @apiNote If preceded with a call to {@link #mapEmptyToFailure()},
     *      the success value - as passed over to the successMapper - is guaranteed non-null.
     */
    <R> Try<R> mapSuccessAsNullable(@NonNull ThrowingFunction<T, R> successMapper);
    /**
     * If this {@link Try} holds a non-null {@code value} (and hence is also a {@link Success}),
     * maps this {@link Try} to another,
     * by calling the successMapper with the {@code value} (which is non-null).
     * Otherwise acts as identity operator, that is,
     * either stay an empty {@link Success} or stay a {@link Failure},
     * though implementations may return a new instance.
     * <p>
     * If given successMapper throws an exception, a failed {@link Try} is returned.
     */
    <R> Try<R> mapSuccessWhenPresent(@NonNull ThrowingFunction<T, R> successMapper);
    /**
     * If this is a {@link Failure}, maps this {@link Try} to another.
     * Otherwise if this is a {@link Success} acts as identity operator,
     * though implementations may return a new instance.
     * <p>
     * If given failureMapper throws an exception, a failed {@link Try} is returned
     * (hiding the original failure).
     */
    Try<T> mapFailure(@NonNull ThrowingFunction<Throwable, Throwable> failureMapper);
    /**
     * If this is a {@link Failure}, recovers to a {@link Success}.
     * Otherwise if this is a {@link Success} acts as identity operator,
     * though implementations may return a new instance.
     * <p>
     * If given recoveryMapper throws an exception, a failed {@link Try} is returned.
     */
    Try<T> mapFailureToSuccess(@NonNull ThrowingFunction<Throwable, T> recoveryMapper);
    /**
     * Maps this {@link Try} to {@link Failure} if this is a {@link Success} with an empty {@code value}.
     * Otherwise acts as identity operator,
     * though implementations may return a new instance.
     */
    Try<T> mapEmptyToFailure();
    /**
     * Maps this {@link Try} to {@link Either}
     * using according mapping function {@code successMapper} or {@code failureMapper}.
     * <p>
     * Any exceptions thrown by given failureMapper or successMapper are propagated without catching.
     * @apiNote It is a common functional programming convention, to map the success value <i>right</i>.
     */
    <L, R> Either<L, R> mapToEither(
            final @NonNull ThrowingFunction<Throwable, L> failureMapper,
            final @NonNull ThrowingFunction<Optional<T>, R> successMapper);

    // -- FLAT MAPPING / FUNCTION COMPOSITION

    /**
     * Variant of {@link #mapSuccess(ThrowingFunction)},
     * utilizing a different successMapper, one that returns a {@link Try}.
     * @see #mapSuccess(ThrowingFunction)
     */
    <R> Try<R> flatMapSuccess(@NonNull ThrowingFunction<Optional<T>, Try<R>> successMapper);

    /**
     * Variant of {@link #mapSuccessAsNullable(ThrowingFunction)},
     * utilizing a different successMapper, one that returns a {@link Try}.
     * @see #mapSuccessAsNullable(ThrowingFunction)
     */
    <R> Try<R> flatMapSuccessAsNullable(@NonNull ThrowingFunction<T, Try<R>> successMapper);

    /**
     * Variant of {@link #mapSuccessWhenPresent(ThrowingFunction)},
     * utilizing a different successMapper, one that returns a {@link Try}.
     * @see #mapSuccessWhenPresent(ThrowingFunction)
     */
    <R> Try<R> flatMapSuccessWhenPresent(@NonNull ThrowingFunction<T, Try<R>> successMapper);

    // -- ACCEPT

    /**
     * Either consumes the success or the failure.
     * <p>
     * However, if any of given failureConsumer or successConsumer throws an exception, a failed {@link Try} is returned.
     * @apiNote Order of arguments conforms to {@link #mapToEither(ThrowingFunction, ThrowingFunction)}
     */
    Try<T> accept(
            final @NonNull ThrowingConsumer<Throwable> failureConsumer,
            final @NonNull ThrowingConsumer<Optional<T>> successConsumer);

    // -- FOLDING

    /**
     * Maps the contained {@code value} or {@code failure} to a new value of type {@code R}
     * using according mapping function {@code successMapper} or {@code failureMapper}.
     * <p>
     * Any exceptions thrown by given failureMapper or successMapper are propagated without catching.
     * @apiNote Order of arguments conforms to {@link #mapToEither(ThrowingFunction, ThrowingFunction)}
     */
    <R> R fold(
            final @NonNull ThrowingFunction<Throwable, R> failureMapper,
            final @NonNull ThrowingFunction<Optional<T>, R> successMapper);

    // -- CONCATENATION

    /**
     * If this is a {@link Success}, maps it to a new {@link Try} based on given {@link Callable}.
     * Otherwise if its a {@link Failure}, acts as identity operator.
     */
    <R> Try<R> thenCall(final @NonNull Callable<R> callable);
    /**
     * If this is a {@link Success}, maps it to a new {@link Try} based on given {@link ThrowingRunnable}.
     * Otherwise if this is a {@link Failure}, acts as identity operator.
     */
    Try<Void> thenRun(final @NonNull ThrowingRunnable runnable);
    /**
     * If this is a {@link Success}, maps it to a new {@link Try} based on given {@link Supplier}.
     * Otherwise if this is a {@link Failure}, acts as identity operator.
     */
    <R> Try<R> then(final @NonNull Callable<? extends Try<R>> next);

    /**
     * If this is a {@link Failure}, maps it to a new {@link Try} based on given {@link Callable}.
     * Otherwise if this is a {@link Success}, acts as identity operator.
     */
    Try<T> orCall(final @NonNull Callable<T> fallback);

    // -- SHORTCUTS

    /**
     * If this is a {@link Failure} throws the contained failure,
     * otherwise if this is a {@link Success}, returns the success value as null-able.
     */
    @Nullable
    default T valueAsNullableElseFail() {
        ifFailureFail();
        return getValue().orElse(null);
    }

    /**
     * If this is a {@link Failure} throws the contained failure,
     * otherwise if this is a {@link Success},
     * either returns the success value if it is NOT <code>null</code>
     * or throws a {@link NoSuchElementException}.
     */
    default T valueAsNonNullElseFail() {
        ifFailureFail();
        return getValue().orElseThrow();
    }

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
        public Try<T> ifSuccess(final @NonNull ThrowingConsumer<Optional<T>> valueConsumer) {
            try {
                valueConsumer.accept(getValue());
                return this;
            } catch (Throwable e) {
                return Try.failure(e);
            }
        }
        @Override
        public Try<T> ifSuccessAsNullable(final @NonNull ThrowingConsumer<T> valueConsumer) {
            try {
                valueConsumer.accept(getValue().orElse(null));
                return this;
            } catch (Throwable e) {
                return Try.failure(e);
            }
        }

        @Override
        public Success<T> ifFailure(final @NonNull ThrowingConsumer<Throwable> exceptionConsumer) {
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
        public <R> Try<R> mapSuccess(final @NonNull ThrowingFunction<Optional<T>, R> successMapper) {
            return Try.call(()->successMapper.apply(getValue()));
        }
        @Override
        public <R> Try<R> mapSuccessAsNullable(final @NonNull ThrowingFunction<T, R> successMapper) {
            return Try.call(()->successMapper.apply(getValue().orElse(null)));
        }
        @Override
        public <R> Try<R> mapSuccessWhenPresent(final @NonNull ThrowingFunction<T, R> successMapper) {
            return getValue()
                    .map(value->Try.call(()->successMapper.apply(value)))
                    .orElseGet(Try::empty);
        }
        @Override
        public <R> Try<R> flatMapSuccess(final @NonNull ThrowingFunction<Optional<T>, Try<R>> successMapper) {
            try {
                return successMapper.apply(getValue());
            } catch (Throwable ex) {
                return Try.failure(ex);
            }
        }
        @Override
        public <R> Try<R> flatMapSuccessAsNullable(final @NonNull ThrowingFunction<T, Try<R>> successMapper) {
            try {
                return successMapper.apply(getValue().orElse(null));
            } catch (Throwable ex) {
                return Try.failure(ex);
            }
        }
        @Override
        public <R> Try<R> flatMapSuccessWhenPresent(final @NonNull ThrowingFunction<T, Try<R>> successMapper) {
            var value = getValue().orElse(null);
            if(value==null) return Try.empty();
            try {
                return successMapper.apply(value);
            } catch (Throwable ex) {
                return Try.failure(ex);
            }
        }

        @Override
        public Success<T> mapFailure(final @NonNull ThrowingFunction<Throwable, Throwable> failureMapper){
            return this;
        }
        @Override
        public Try<T> mapFailureToSuccess(final @NonNull ThrowingFunction<Throwable, T> recoveryMapper) {
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
        public <R> Try<R> then(final @NonNull Callable<? extends Try<R>> next) {
            try {
                return next.call();
            } catch (Throwable e) {
                return Try.failure(e);
            }
        }

        @Override
        public Try<T> orCall(@NonNull final Callable<T> fallback) {
            return this;
        }

        @Override
        public Try<T> accept(
                final @NonNull ThrowingConsumer<Throwable> failureConsumer,
                final @NonNull ThrowingConsumer<Optional<T>> successConsumer) {
            try {
                successConsumer.accept(getValue());
                return this;
            } catch (Throwable e) {
                return Try.failure(e);
            }
        }

        @Override
        public <R> R fold(
                final @NonNull ThrowingFunction<Throwable, R> failureMapper,
                final @NonNull ThrowingFunction<Optional<T>, R> successMapper) {
            return successMapper.apply(getValue());
        }

        @Override
        public <L, R> Either<L, R> mapToEither(
                final @NonNull ThrowingFunction<Throwable, L> failureMapper,
                final @NonNull ThrowingFunction<Optional<T>, R> successMapper) {
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
        public Failure<T> ifSuccess(final @NonNull ThrowingConsumer<Optional<T>> valueConsumer) {
            return this;
        }
        @Override
        public Failure<T> ifSuccessAsNullable(final @NonNull ThrowingConsumer<T> valueConsumer) {
            return this;
        }

        @Override
        public Failure<T> ifFailure(final @NonNull ThrowingConsumer<Throwable> exceptionConsumer) {
            try {
                exceptionConsumer.accept(throwable);
                return this;
            } catch (Throwable e) {
                return Try.failure(e);
            }
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
        public <R> Failure<R> mapSuccess(final @NonNull ThrowingFunction<Optional<T>, R> successMapper) {
            return new Failure<>(throwable); // railway pattern:  once failed, stays failed
        }
        @Override
        public <R> Failure<R> mapSuccessAsNullable(final @NonNull ThrowingFunction<T, R> successMapper) {
            return new Failure<>(throwable); // railway pattern:  once failed, stays failed
        }
        @Override
        public <R> Failure<R> mapSuccessWhenPresent(final @NonNull ThrowingFunction<T, R> successMapper) {
            return new Failure<>(throwable); // railway pattern:  once failed, stays failed
        }
        @Override
        public <R> Failure<R> flatMapSuccess(final @NonNull ThrowingFunction<Optional<T>, Try<R>> successMapper) {
            return new Failure<>(throwable); // railway pattern:  once failed, stays failed
        }
        @Override
        public <R> Failure<R> flatMapSuccessAsNullable(final @NonNull ThrowingFunction<T, Try<R>> successMapper) {
            return new Failure<>(throwable); // railway pattern:  once failed, stays failed
        }
        @Override
        public <R> Failure<R> flatMapSuccessWhenPresent(final @NonNull ThrowingFunction<T, Try<R>> successMapper) {
            return new Failure<>(throwable); // railway pattern:  once failed, stays failed
        }

        @Override
        public Failure<T> mapFailure(final @NonNull ThrowingFunction<Throwable, Throwable> failureMapper){
            try {
                return new Failure<>(failureMapper.apply(throwable));
            } catch (Throwable e) {
                return failure(e);
            }
        }
        @Override
        public Try<T> mapFailureToSuccess(final @NonNull ThrowingFunction<Throwable, T> recoveryMapper) {
            return Try.call(()->recoveryMapper.apply(throwable));
        }

        @Override
        public Try<T> mapEmptyToFailure() {
            return this; // identity operation
        }

        @Override
        public <R> Failure<R> thenCall(final @NonNull Callable<R> callable) {
            return new Failure<>(throwable); // railway pattern:  once failed, stays failed
        }

        @Override
        public Try<Void> thenRun(final @NonNull ThrowingRunnable runnable) {
            return new Failure<>(throwable); // railway pattern:  once failed, stays failed
        }

        @Override
        public <R> Try<R> then(final @NonNull Callable<? extends Try<R>> next) {
            return new Failure<>(throwable); // railway pattern:  once failed, stays failed
        }

        @Override
        public Try<T> orCall(@NonNull final Callable<T> fallback) {
            return Try.call(fallback);
        }

        @Override
        public Try<T> accept(
                final @NonNull ThrowingConsumer<Throwable> failureConsumer,
                final @NonNull ThrowingConsumer<Optional<T>> successConsumer) {
            try {
                failureConsumer.accept(throwable);
                return this;
            } catch (Throwable e) {
                return Try.failure(e);
            }
        }

        @Override
        public <R> R fold(
                final @NonNull ThrowingFunction<Throwable, R> failureMapper,
                final @NonNull ThrowingFunction<Optional<T>, R> successMapper) {
            return failureMapper.apply(throwable);
        }

        @Override
        public <L, R> Either<L, R> mapToEither(
                final @NonNull ThrowingFunction<Throwable, L> failureMapper,
                final @NonNull ThrowingFunction<Optional<T>, R> successMapper) {
            return Either.left(failureMapper.apply(throwable));
        }

    }

}
