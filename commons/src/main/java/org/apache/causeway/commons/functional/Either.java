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
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The {@link Either} type represents a value of one of two possible types (a disjoint union),
 * referred to by {@code left} or {@code right}.
 * <p>
 * Factory methods {@link Either#left(Object)} and {@link Either#right(Object)}
 * correspond to the two possible values.
 *
 * @since 2.0 {@index}
 */
public interface Either<L, R>  {

    // -- FACTORIES

    public static <L, R> Either<L, R> left(final @NonNull L left) {
        return new Left<>(left);
    }

    public static <L, R> Either<L, R> right(final @NonNull R right) {
        return new Right<>(right);
    }

    // -- ACCESSORS

    Optional<L> left();
    Optional<R> right();

    L leftIfAny();
    R rightIfAny();

    // -- PREDICATES

    boolean isLeft();
    boolean isRight();

    // -- MAPPING

    <T> Either<T, R> mapLeft(final @NonNull Function<L, T> leftMapper);
    <T> Either<L, T> mapRight(final @NonNull Function<R, T> rightMapper);

    <X, Y> Either<X, Y> map(
            final @NonNull Function<L, X> leftMapper,
            final @NonNull Function<R, Y> rightMapper);

    // -- FOLDING

    <T> T fold(@NonNull BiFunction<L, R, T> biMapper);

    <T> T fold(
            @NonNull Function<L, T> leftMapper,
            @NonNull Function<R, T> rightMapper);

    // -- TERMINALS

    void accept(
            @NonNull Consumer<L> leftConsumer,
            @NonNull Consumer<R> rightConsumer);

    // -- LEFT

    @lombok.Value
    @RequiredArgsConstructor(access=AccessLevel.PROTECTED)
    static final class Left<L, R> implements Either<L, R>, Serializable {
        private static final long serialVersionUID = 1L;

        private final L left;

        @Override public Optional<L> left() { return Optional.ofNullable(left); }
        @Override public Optional<R> right() { return Optional.empty(); }

        @Override public L leftIfAny() { return left; }
        @Override public R rightIfAny() { return null; }

        @Override public final boolean isLeft() { return true; }
        @Override public final boolean isRight() { return false; }

        @Override public final <T> Either<T, R> mapLeft(final @NonNull Function<L, T> leftMapper){
            return Either.left(leftMapper.apply(left)); }
        @Override public final <T> Either<L, T> mapRight(final @NonNull Function<R, T> rightMapper){
            return Either.left(left); }

        @Override
        public final <X, Y> Either<X, Y> map(
                final @NonNull Function<L, X> leftMapper,
                final @NonNull Function<R, Y> rightMapper){
            return Either.left(leftMapper.apply(left));
        }

        @Override
        public final <T> T fold(final @NonNull BiFunction<L, R, T> biMapper){
            return biMapper.apply(left, null);
        }

        @Override
        public final <T> T fold(
                final @NonNull Function<L, T> leftMapper,
                final @NonNull Function<R, T> rightMapper){
            return leftMapper.apply(left);
        }

        @Override
        public final void accept(
                final @NonNull Consumer<L> leftConsumer,
                final @NonNull Consumer<R> rightConsumer) {
            leftConsumer.accept(left);
        }

    }

    // -- RIGHT

    @lombok.Value
    @RequiredArgsConstructor(access=AccessLevel.PROTECTED)
    static final class Right<L, R> implements Either<L, R>, Serializable {
        private static final long serialVersionUID = 1L;

        private final R right;

        @Override public Optional<L> left() { return Optional.empty(); }
        @Override public Optional<R> right() { return Optional.ofNullable(right); }

        @Override public L leftIfAny() { return null; }
        @Override public R rightIfAny() { return right; }

        @Override public final boolean isLeft() { return false; }
        @Override public final boolean isRight() { return true; }

        @Override public final <T> Either<T, R> mapLeft(final @NonNull Function<L, T> leftMapper){
            return Either.right(right); }
        @Override public final <T> Either<L, T> mapRight(final @NonNull Function<R, T> rightMapper){
            return Either.right(rightMapper.apply(right)); }

        @Override
        public final <X, Y> Either<X, Y> map(
                final @NonNull Function<L, X> leftMapper,
                final @NonNull Function<R, Y> rightMapper){
            return Either.right(rightMapper.apply(right));
        }

        @Override
        public final <T> T fold(final @NonNull BiFunction<L, R, T> biMapper){
            return biMapper.apply(null, right);
        }

        @Override
        public final <T> T fold(
                final @NonNull Function<L, T> leftMapper,
                final @NonNull Function<R, T> rightMapper){
            return rightMapper.apply(right);
        }

        @Override
        public final void accept(
                final @NonNull Consumer<L> leftConsumer,
                final @NonNull Consumer<R> rightConsumer) {
            rightConsumer.accept(right);
        }

    }

    // -- TYPE COMPOSITION

    @FunctionalInterface
    public static interface HasEither<L, R> extends Either<L, R> {

        Either<L, R> getEither();

        @Override default Optional<L> left() { return getEither().left(); }
        @Override default Optional<R> right() { return getEither().right(); }

        @Override default L leftIfAny() { return getEither().leftIfAny(); }
        @Override default R rightIfAny() { return getEither().rightIfAny(); }

        @Override default boolean isLeft() { return getEither().isLeft(); }
        @Override default boolean isRight() { return getEither().isRight(); }

        @Override default <T> Either<T, R> mapLeft(final @NonNull Function<L, T> leftMapper){
            return getEither().mapLeft(leftMapper); }
        @Override default <T> Either<L, T> mapRight(final @NonNull Function<R, T> rightMapper){
            return getEither().mapRight(rightMapper); }

        @Override default <X, Y> Either<X, Y> map(
                final @NonNull Function<L, X> leftMapper,
                final @NonNull Function<R, Y> rightMapper){
            return getEither().map(leftMapper, rightMapper);
        }

        @Override default <T> T fold(final @NonNull BiFunction<L, R, T> biMapper){
            return getEither().fold(biMapper);
        }

        @Override default <T> T fold(
                final @NonNull Function<L, T> leftMapper,
                final @NonNull Function<R, T> rightMapper){
            return getEither().fold(leftMapper, rightMapper);
        }

        @Override default void accept(
                final @NonNull Consumer<L> leftConsumer,
                final @NonNull Consumer<R> rightConsumer) {
            getEither().accept(leftConsumer, rightConsumer);
        }

    }

}
