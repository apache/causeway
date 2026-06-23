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
package org.apache.causeway.core.metamodel.facets.objectvalue.digits;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.core.metamodel.facetapi.Facet;

/**
 * The maximum allowed number of digits to the left of the decimal place
 * (integer/integral part) for this number.
 *
 * <p> For example:
 * <ul>
 * <li><tt>12345.789</tt> has 5 integer/integral digits</li>
 * <li><tt>0.123</tt> has 1 integer/integral digit</li>
 * </ul>
 * @see MinIntegerDigitsFacet
 */
public interface MaxIntegerDigitsFacet
extends Facet {

    /**
     * eg. as provided by {@link ValueSemantics#maxIntegerDigits()}
     */
    int maxIntegerDigits();

    @Override
    default boolean semanticEquals(final @NonNull Facet facet) {
        return facet instanceof MaxIntegerDigitsFacet other
            ? this.maxIntegerDigits() == other.maxIntegerDigits()
            : false;
    }

    @Override
    default void visitAttributes(final BiConsumer<String, Object> visitor) {
        Facet.super.visitAttributes(visitor);
        visitor.accept("maxIntegerDigits", String.valueOf(maxIntegerDigits()));
    }

    /**
     * The stronger constraint wins. If equal, first argument wins over second.
     */
    public static Optional<MaxIntegerDigitsFacet> strongestConstraint(
            final Optional<MaxIntegerDigitsFacet> a,
            final Optional<MaxIntegerDigitsFacet> b) {
        if(b.isEmpty())
            return a;
        if(a.isEmpty())
            return b;
        return a.get().maxIntegerDigits() <= b.get().maxIntegerDigits()
            ? a
            : b;
    }

}
