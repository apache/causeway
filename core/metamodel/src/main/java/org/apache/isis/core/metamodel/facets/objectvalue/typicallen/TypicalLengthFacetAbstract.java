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

package org.apache.isis.core.metamodel.facets.objectvalue.typicallen;

import java.util.function.BiConsumer;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.NonNull;

public abstract class TypicalLengthFacetAbstract
extends FacetAbstract
implements TypicalLengthFacet {

    private static final Class<? extends Facet> type() {
        return TypicalLengthFacet.class;
    }

    public TypicalLengthFacetAbstract(
            final int typicalLength,
            final FacetHolder holder) {
        super(type(), holder);
        this.typicalLength = typicalLength;
    }

    public TypicalLengthFacetAbstract(
            final int typicalLength,
            final FacetHolder holder,
            final Facet.Precedence precedence) {
        super(type(), holder, precedence);
        this.typicalLength = typicalLength;
    }

    // -- IMPL

    private final int typicalLength;
    @Override
    public final int value() {
        return typicalLength;
    }

    @Override
    public final boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof TypicalLengthFacet
                ? this.value() == ((TypicalLengthFacet)other).value()
                : false;
    }

    // -- REPORTING

    @Override
    protected String toStringValues() {
        final int typicalLength = value();
        return typicalLength == 0
                ? "default"
                : String.valueOf(typicalLength);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("typicalLength", value());
    }


}
