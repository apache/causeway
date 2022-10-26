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
package org.apache.causeway.commons.collections;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 * Provides a subset of the functionality that the Java {@link Collection}
 * interface has, focusing on immutability.
 */
public interface ImmutableCollection<E>
extends Iterable<E> {

    /**
     * Returns the number of elements in this collection.  If this collection
     * contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this collection
     */
    int size();

    /**
     * Returns {@code true} if this collection contains no elements.
     *
     * @return {@code true} if this collection contains no elements
     */
    boolean isEmpty();

    /**
     * @return either 'empty', 'one' or 'multi'
     */
    Cardinality getCardinality();

    /**
     * @return whether this Can contains given {@code element}, that is, at least one contained element
     * passes the {@link Objects#equals(Object, Object)} test with respect to the given element.
     */
    boolean contains(@Nullable E element);

    /**
     * @return this collection's single element or an empty Optional,
     * if this collection has any cardinality other than ONE
     */
    Optional<E> getSingleton();

    /**
     * Shortcut for {@code getSingleton().orElseThrow(_Exceptions::noSuchElement)}
     * @throws NoSuchElementException if result is empty
     */
    default E getSingletonOrFail() {
        return getSingleton().orElseThrow(_Exceptions::noSuchElement);
    }

    /**
     * @return Stream of elements this collection contains
     */
    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * @return possibly concurrent Stream of elements this collection contains
     */
    default Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

}
