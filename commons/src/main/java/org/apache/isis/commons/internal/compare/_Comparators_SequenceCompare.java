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
package org.apache.isis.commons.internal.compare;

import java.util.StringTokenizer;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.primitives._Ints;

import lombok.val;

/**
 *
 * package private mixin for utility class {@link _Comparators}
 *
 */
final class _Comparators_SequenceCompare {

    private _Comparators_SequenceCompare(){}

    public static int compareNullLast(
            @Nullable final String sequence1,
            @Nullable final String sequence2,
            final String separator) {

        if(_Strings.isEmpty(separator))
            throw new IllegalArgumentException("a non empty separator is required");

        if (sequence1 == null && sequence2 == null) {
            return 0;
        }

        if (sequence1 == null && sequence2 != null) {
            return +1; // non-null before null
        }
        if (sequence1 != null && sequence2 == null) {
            return -1; // non-null before null
        }

        final StringTokenizer components1 = tokenizerFor(sequence1, separator);
        final StringTokenizer components2 = tokenizerFor(sequence2, separator);

        final int length1 = components1.countTokens();
        final int length2 = components2.countTokens();

        // shouldn't happen but just in case.
        if (length1 == 0 && length2 == 0) {
            return 0;
        }

        // continue to loop until we run out of components.
        int n = 0;
        while (true) {
            final int length = n + 1;
            // check if run out of components in either side
            if (length1 < length && length2 >= length) {
                return -1; // o1 before o2
            }
            if (length2 < length && length1 >= length) {
                return +1; // o2 before o1
            }
            if (length1 < length && length2 < length) {
                // run out of components
                return 0;
            }
            // we have this component on each side

            val token1 = components1.nextToken();
            val token2 = components2.nextToken();

            int componentCompare = 0;

            val int1 = _Ints.parseInt(token1, 10);
            val int2 = _Ints.parseInt(token2, 10);

            if(int1.isPresent() && int2.isPresent()) {
                componentCompare = Integer.compare(int1.getAsInt(), int2.getAsInt());
            } else {
                // not integers compare as strings
                componentCompare = token1.compareTo(token2);
            }

            if (componentCompare != 0) {
                return componentCompare;
            }
            // this component is the same; lets look at the next
            n++;
        }
    }

    private static StringTokenizer tokenizerFor(final String sequence, final String separator) {
        return new StringTokenizer(sequence, separator, false);
    }


}
