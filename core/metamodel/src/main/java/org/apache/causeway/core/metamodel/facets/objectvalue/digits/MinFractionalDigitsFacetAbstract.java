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

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

public abstract class MinFractionalDigitsFacetAbstract
extends FacetAbstract
implements MinFractionalDigitsFacet {

    private static final Class<? extends Facet> type() {
        return MinFractionalDigitsFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private final int minFractionalDigits;

    protected MinFractionalDigitsFacetAbstract(
            final int minFractionalDigits,
            final FacetHolder holder) {
        super(type(), holder);
        this.minFractionalDigits = minFractionalDigits;
    }

    protected MinFractionalDigitsFacetAbstract(
            final int minFractionalDigits,
            final FacetHolder holder,
            final Facet.Precedence precedence) {
        super(type(), holder, precedence);
        this.minFractionalDigits = minFractionalDigits;
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof MinFractionalDigitsFacet
                ? Integer.compare(
                        this.getMinFractionalDigits(),
                        ((MinFractionalDigitsFacet)other).getMinFractionalDigits()) == 0
                : false;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("minFractionalDigits", String.valueOf(minFractionalDigits));
    }

    /**
     * If equal, first argument wins over second.
     */
    public static Optional<MinFractionalDigitsFacet> minimum(
            final Optional<MinFractionalDigitsFacet> a,
            final Optional<MinFractionalDigitsFacet> b) {
        if(b.isEmpty()) {
            return a;
        }
        if(a.isEmpty()) {
            return b;
        }
        return a.get().getMinFractionalDigits() <= b.get().getMinFractionalDigits()
                ? a
                : b;
    }

}
