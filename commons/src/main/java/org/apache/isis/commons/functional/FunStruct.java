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
public class FunStruct<T, R> {

    private final _Either<Function<T, R>, FunStruct<T, R>> left;
    private final _Either<Function<T, R>, FunStruct<T, R>> right;
    private final int elementCount;

    // -- FACTORIES

    public static <T, R> FunStruct<T, R> of(final @NonNull Function<T, R> left) {
        return new FunStruct<>(_Either.left(left), _Either.right(nil()), 1);
    }

    public static <T, R> FunStruct<T, R> of(final @NonNull Function<T, R> left, final @NonNull Function<T, R> right) {
        return new FunStruct<>(_Either.left(left), _Either.left(right), 2);
    }

    public static <T, R> FunStruct<T, R> of(
            final @NonNull Function<T, R> left,
            final @NonNull FunStruct<T, R> right) {
        return new FunStruct<>(
                _Either.left(left),
                _Either.right(right),
                1 + right.elementCount);
    }

    public static <T, R> FunStruct<T, R> of(
            final @NonNull FunStruct<T, R> left,
            final @NonNull Function<T, R> right) {
        return new FunStruct<>(
                _Either.right(left),
                _Either.left(right),
                left.elementCount + 1);
    }

    public static <T, R> FunStruct<T, R> of(
            final @NonNull FunStruct<T, R> left,
            final @NonNull FunStruct<T, R> right) {
        return new FunStruct<>(
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
                        left.fold(Stream::of, FunStruct::streamDepthFirstPostorder),
                        right.fold(Stream::of, FunStruct::streamDepthFirstPostorder))
                : Stream.empty();
    }

    // -- EXTRACTION

    public List<Function<T, R>> flatten() {
        return streamDepthFirstPostorder()
                .collect(Collectors.toCollection(()->new ArrayList<>(elementCount)));
    }

    public <X> List<Function<T, X>> mapThenFlatten(final Function<R, X> mapper) {
        return streamDepthFirstPostorder()
                .map(fun->fun.andThen(mapper))
                .collect(Collectors.toCollection(()->new ArrayList<>(elementCount)));
    }

    public List<R> applyThenFlatten(final T value) {
        return streamDepthFirstPostorder()
                .map(fun->fun.apply(value))
                .collect(Collectors.toCollection(()->new ArrayList<>(elementCount)));
    }

    // -- MAPPING

    public <X> FunStruct<T, X> map(final Function<R, X> mapper) {
        return new MappedFunStruct<>(this, mapper);
    }

    final static class MappedFunStruct<A, B, C> extends FunStruct<A, C> {

        final FunStruct<A, B> origin;
        final Function<B, C> mapper;

        protected MappedFunStruct(
                final FunStruct<A, B> origin,
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

    // -- COMPOSE

    public <X> FunStruct<T, X> compose(final FunStruct<R, X> other) {
        return new ComposedFunStruct<>(this, other);
    }

    final static class ComposedFunStruct<A, B, C> extends FunStruct<A, C> {

        final FunStruct<A, B> origin;
        final FunStruct<B, C> other;

        protected ComposedFunStruct(
                final FunStruct<A, B> origin,
                final FunStruct<B, C> other) {
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


    // -- NIL / THE EMPTY COMPOSITION

    private final static FunStruct<?, ?> NIL = new FunStruct<>(null, null, 0);
    public static <T, R> FunStruct<T, R> nil() {
        return _Casts.uncheckedCast(NIL);
    }


}
