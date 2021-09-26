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
 * The {@link Composition} type represents a binary tree data structure.
 *
 * @since 2.0 {@index}
 */
@RequiredArgsConstructor(access=AccessLevel.PRIVATE, staticName="ofBranches")
@ToString @EqualsAndHashCode
public final class Composition<T> {

    private final _Either<T, Composition<T>> left;
    private final _Either<T, Composition<T>> right;
    private final int elementCount;

    // -- FACTORIES

    public static <T> Composition<T> ofElement(final @NonNull T left) {
        return Composition.ofBranches(_Either.left(left), _Either.right(nil()), 1);
    }

    public static <T> Composition<T> ofElements(final @NonNull T left, final @NonNull T right) {
        return Composition.ofBranches(_Either.left(left), _Either.left(right), 2);
    }

    public static <T> Composition<T> ofLeftElement(
            final @NonNull T left,
            final @NonNull Composition<T> right) {
        return Composition.ofBranches(
                _Either.left(left),
                _Either.right(right),
                1 + right.elementCount);
    }

    public static <T> Composition<T> ofRightElement(
            final @NonNull Composition<T> left,
            final @NonNull T right) {
        return Composition.ofBranches(
                _Either.right(left),
                _Either.left(right),
                left.elementCount + 1);
    }

    public static <T> Composition<T> of(
            final @NonNull Composition<T> left,
            final @NonNull Composition<T> right) {
        return Composition.ofBranches(
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

    public Stream<T> streamDepthFirstPostorder() {
        return elementCount!=0 // intercept NIL
                ? Stream.concat(
                        left.fold(Stream::of, Composition::streamDepthFirstPostorder),
                        right.fold(Stream::of, Composition::streamDepthFirstPostorder))
                : Stream.empty();
    }

    // -- EXTRACTION

    public List<T> flatten() {
        return streamDepthFirstPostorder()
                .collect(Collectors.toCollection(()->new ArrayList<>(elementCount)));
    }

    // -- MAPPING

    // -- NIL / THE EMPTY COMPOSITION

    private final static Composition<?> NIL = Composition.ofBranches(null, null, 0);
    public static <T> Composition<T> nil() {
        return _Casts.uncheckedCast(NIL);
    }

}
