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
package org.apache.isis.commons.btree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Either;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Represents a binary tree data structure of function elements.
 *
 * @since 2.0 {@index}
 */
@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
@ToString @EqualsAndHashCode
public class FunCompound<T, R> {

    private final _Either<Function<T, R>, FunCompound<T, R>> left;
    private final _Either<Function<T, R>, FunCompound<T, R>> right;
    private final int elementCount;

    // -- FACTORIES

    public static <T, R> FunCompound<T, R> of(final @NonNull Function<T, R> left) {
        return new FunCompound<>(_Either.left(left), _Either.right(nil()), 1);
    }

    public static <T, R> FunCompound<T, R> of(final @NonNull Function<T, R> left, final @NonNull Function<T, R> right) {
        return new FunCompound<>(_Either.left(left), _Either.left(right), 2);
    }

    public static <T, R> FunCompound<T, R> of(
            final @NonNull Function<T, R> left,
            final @NonNull FunCompound<T, R> right) {
        return new FunCompound<>(
                _Either.left(left),
                _Either.right(right),
                1 + right.elementCount);
    }

    public static <T, R> FunCompound<T, R> of(
            final @NonNull FunCompound<T, R> left,
            final @NonNull Function<T, R> right) {
        return new FunCompound<>(
                _Either.right(left),
                _Either.left(right),
                left.elementCount + 1);
    }

    public static <T, R> FunCompound<T, R> of(
            final @NonNull FunCompound<T, R> left,
            final @NonNull FunCompound<T, R> right) {
        return new FunCompound<>(
                _Either.right(left),
                _Either.right(right),
                left.elementCount + right.elementCount);
    }

    // -- SIZE / ELEMENT COUNT

    /**
     * @return Number of contained elements of type <T>.
     */
    public int size() {
        return elementCount;
    }

    // -- TRAVERSAL

    public Stream<Function<T, R>> streamDepthFirstPostorder() {
        return elementCount!=0 // intercept NIL
                ? Stream.concat(
                        left.fold(Stream::of, FunCompound::streamDepthFirstPostorder),
                        right.fold(Stream::of, FunCompound::streamDepthFirstPostorder))
                : Stream.empty();
    }

    // -- EXTRACTION

    public List<Function<T, R>> flatten() {
        return streamDepthFirstPostorder()
                .collect(Collectors.toCollection(()->new ArrayList<>(elementCount)));
    }

    // -- APPLY VALUE

    public Compound<R> apply(final T value) {
        return new AppliedStruct<>(this, value);
    }

    final static class AppliedStruct<A, B> extends Compound<B> {

        final FunCompound<A, B> origin;
        final A value;

        protected AppliedStruct(
                final FunCompound<A, B> origin,
                final A value) {
            super(null, null, origin.elementCount);
            this.origin = origin;
            this.value = value;
        }

        @Override
        public Stream<B> streamDepthFirstPostorder() {
            return origin.streamDepthFirstPostorder()
                    .map(fun->fun.apply(value));
        }

    }

    // -- MAPPING

    public <X> FunCompound<T, X> map(final Function<R, X> mapper) {
        return new MappedFunStruct<>(this, mapper);
    }

    final static class MappedFunStruct<A, B, C> extends FunCompound<A, C> {

        final FunCompound<A, B> origin;
        final Function<B, C> mapper;

        protected MappedFunStruct(
                final FunCompound<A, B> origin,
                final Function<B, C> mapper) {
            super(null, null, origin.elementCount);
            this.origin = origin;
            this.mapper = mapper;
        }

        @Override
        public Stream<Function<A, C>> streamDepthFirstPostorder() {
            return origin.streamDepthFirstPostorder()
                    .map(fun->fun.andThen(mapper));
        }

    }

    // -- COMPOSITION

    public <X> FunCompound<T, X> compose(final FunCompound<R, X> other) {
        return new ComposedFunStruct<>(this, other);
    }

    final static class ComposedFunStruct<A, B, C> extends FunCompound<A, C> {

        final FunCompound<A, B> origin;
        final FunCompound<B, C> other;

        protected ComposedFunStruct(
                final FunCompound<A, B> origin,
                final FunCompound<B, C> other) {
            super(null, null, origin.elementCount);
            this.origin = origin;
            this.other = other;
        }

        @Override
        public Stream<Function<A, C>> streamDepthFirstPostorder() {
            return origin.streamDepthFirstPostorder()
                    .flatMap(fun->
                        other.streamDepthFirstPostorder()
                        .map(otherFun->fun.andThen(otherFun))
                    );
        }

    }

    // -- NIL / THE EMPTY STRUCT

    private final static FunCompound<?, ?> NIL = new FunCompound<>(null, null, 0);
    public static <T, R> FunCompound<T, R> nil() {
        return _Casts.uncheckedCast(NIL);
    }

}
