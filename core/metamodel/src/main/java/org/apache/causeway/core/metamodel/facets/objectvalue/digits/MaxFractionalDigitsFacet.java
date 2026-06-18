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

import jakarta.validation.constraints.Digits;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.core.metamodel.facetapi.Facet;

/**
 * The maximum allowed number of digits to the right of the decimal place (fractional part)
 * for this number.
 *
 * <p>
 * For example:
 * <ul>
 * <li><tt>12345.789</tt> has 3 fractional digits</li>
 * <li><tt>12345</tt> has 0 fractional digits</li>
 * <li><tt>12345.0</tt> has 1 fractional digit</li>
 * </ul>
 */
public interface MaxFractionalDigitsFacet
extends Facet {

    /**
     * eg. as provided by {@link Digits#fraction()}
     * and {@link ValueSemantics#maxFractionalDigits()}
     */
    int maxFractionalDigits();

    @Override
    default boolean semanticEquals(final @NonNull Facet facet) {
        return facet instanceof MaxFractionalDigitsFacet other
                ? this.maxFractionalDigits() == other.maxFractionalDigits()
                : false;
    }

    @Override
    default void visitAttributes(final BiConsumer<String, Object> visitor) {
        Facet.super.visitAttributes(visitor);
        visitor.accept("maxFractionalDigits", maxFractionalDigits() <0
                ? "unlimited"
                : String.valueOf(maxFractionalDigits()));
    }

    /**
     * The stronger constraint wins. If equal, first argument wins over second.
     */
    public static Optional<MaxFractionalDigitsFacet> strongestConstraint(
            final Optional<MaxFractionalDigitsFacet> a,
            final Optional<MaxFractionalDigitsFacet> b) {
        if(b.isEmpty())
            return a;
        if(a.isEmpty())
            return b;
        return a.get().maxFractionalDigits() <= b.get().maxFractionalDigits()
                ? a
                : b;
    }

}
