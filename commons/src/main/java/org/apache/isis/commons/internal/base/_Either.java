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
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * <h1>- internal use only -</h1>
 * <p>
 *      A holder of either a left or right reference, only one of both can be present.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
@RequiredArgsConstructor(access=AccessLevel.PRIVATE, staticName="of")
@ToString @EqualsAndHashCode
public final class _Either<L, R> {

    private final L left;
    private final R right;
    private final boolean isLeft;

    // -- FACTORIES

    public static <L, R> _Either<L, R> left(L left) {
        requires(left, "left");
        return of(left, null, true);
    }

    public static <L, R> _Either<L, R> right(R right) {
        requires(right, "right");
        return of(null, right, false);
    }

    public static <L, R> _Either<L, R> leftNullable(@Nullable L left) {
        return of(left, null, true);
    }

    public static <L, R> _Either<L, R> rightNullable(@Nullable R right) {
        return of(null, right, false);
    }

    // -- ACCESSORS

    public Optional<L> left() {
        return Optional.ofNullable(left);
    }

    public Optional<R> right() {
        return Optional.ofNullable(right);
    }

    public L leftIfAny() {
        return left;
    }

    public R rightIfAny() {
        return right;
    }

    // -- PREDICATES

    public boolean isLeft() {
        return isLeft;
    }

    public boolean isRight() {
        return !isLeft;
    }

    public boolean isPresentLeft() {
        return left!=null;
    }

    public boolean isPresentRight() {
        return right!=null;
    }

    // -- MAPPING

    public <T> _Either<T, R> mapLeft(final @NonNull Function<L, T> leftMapper){
        return isLeft()
                ? _Either.left(leftMapper.apply(left))
                : _Either.right(right);
    }

    public <T> _Either<L, T> mapRight(final @NonNull Function<R, T> rightMapper){
        return isLeft()
                ? _Either.left(left)
                : _Either.right(rightMapper.apply(right));
    }

    public <X, Y> _Either<X, Y> bimap(
            final @NonNull Function<L, X> leftMapper,
            final @NonNull Function<R, Y> rightMapper){
        return isLeft()
                ? left(leftMapper.apply(left))
                : right(rightMapper.apply(right));
    }

    public <X, Y> _Either<X, Y> bimapNullable(
            final @NonNull Function<L, X> leftMapper,
            final @NonNull Function<R, Y> rightMapper){
        return isLeft()
                ? leftNullable(leftMapper.apply(left))
                : rightNullable(rightMapper.apply(right));
    }

    public _Either<L, R> mapIfLeft(Function<L, _Either<L, R>> leftRemapper){
        return isLeft()
                ? leftRemapper.apply(left)
                : this;
    }

    public _Either<L, R> mapIfRight(Function<R, _Either<L, R>> rightRemapper){
        return isLeft()
                ? this
                : rightRemapper.apply(right);
    }


    // -- FOLDING

    public <T> T fold(BiFunction<L, R, T> biMapper){
        return biMapper.apply(left, right);
    }

    public <T> T fold(
            final @NonNull Function<L, T> leftMapper,
            final @NonNull Function<R, T> rightMapper){
        return isLeft()
                ? leftMapper.apply(left)
                : rightMapper.apply(right);
    }

    // -- TERMINALS

    public void accept(Consumer<L> leftConsumer, Consumer<R> rightConsumer) {
        if(isLeft()) {
            leftConsumer.accept(left);
        } else {
            rightConsumer.accept(right);
        }
    }


}
