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
package org.apache.causeway.commons.internal.compare;

import java.util.Comparator;
import java.util.Objects;

import org.springframework.lang.Nullable;

import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides some ordering algorithms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
@UtilityClass
public final class _Comparators {

    // -- NULLSAFE COMPARE

    /**
     * Compares two {@link Comparable} (and nulls-frist).
     * @apiNote consider using {@link Comparator#naturalOrder()} combined with
     * {@link Comparator#nullsFirst(Comparator)}.
     * @implNote this utility method does not produce objects on the heap
     * @return {@code -1} if {@code a < b}, {@code 1} if {@code a > b} else {@code 0}
     * @see Comparable#compareTo(Object)
     */
    public <T> int compareNullsFirst(final @Nullable Comparable<T> a, final @Nullable T b) {
        if(Objects.equals(a, b)) {
            return 0;
        }
        // at this point not both can be null, so which ever is null wins
        if(a==null) {
            return -1;
        }
        if(b==null) {
            return 1;
        }
        // at this point neither can be null
        return a.compareTo(b);
    }

    /**
     * Compares two {@link Comparable}s (and nulls-last).
     * @apiNote consider using {@link Comparator#naturalOrder()} combined with
     * {@link Comparator#nullsFirst(Comparator)}.
     * @implNote this utility method does not produce objects on the heap
     * @return {@code -1} if {@code a < b}, {@code 1} if {@code a > b} else {@code 0}
     * @see Comparable#compareTo(Object)
     */
    public <T> int compareNullsLast(final @Nullable Comparable<T> a, final @Nullable T b) {
        if(Objects.equals(a, b)) {
            return 0;
        }
        // at this point not both can be null, so which ever is null wins
        if(a==null) {
            return 1;
        }
        if(b==null) {
            return -1;
        }
        // at this point neither can be null
        return a.compareTo(b);
    }

    // -- DEWEY ORDER

    private static final String DEWEY_SEPERATOR = ".";

    public int deweyOrderCompare(
            final @Nullable String sequence1,
            final @Nullable String sequence2) {
        return _Comparators_SequenceCompare.compareNullLast(sequence1, sequence2, DEWEY_SEPERATOR);
    }

    private static final Comparator<String> DEWEY_ORDER_COMPARATOR = _Comparators::deweyOrderCompare;
    public Comparator<String> deweyOrderComparator() {
        return DEWEY_ORDER_COMPARATOR;
    }

}
