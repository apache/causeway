/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.causeway.core.metamodel.facets.members.iconfa;

import java.util.function.BiConsumer;

import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.Getter;

/**
 * One of two bases for the {@link FaFacet}.
 *
 * @see FaImperativeFacetAbstract
 * @since 2.0
 */
public abstract class FaStaticFacetAbstract
extends FacetAbstract
implements FaStaticFacet {

    public static final Class<FaFacet> type() {
        return FaFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private final Either<FaStaticFacet, FaImperativeFacet> specialization = Either.left(this);

    @Getter(onMethod_ = {@Override})
    private final FontAwesomeLayers layers; // serializable

    protected FaStaticFacetAbstract(
            final String value,
            final CssClassFaPosition position,
            final FacetHolder holder) {
        this(value, position, holder, Precedence.DEFAULT);
    }

    protected FaStaticFacetAbstract(
            final String quickNotation,
            final CssClassFaPosition position,
            final FacetHolder holder,
            final Precedence precedence) {

        super(type(), holder, precedence);
        this.layers = position == null
                ? FontAwesomeLayers.fromQuickNotation(quickNotation)
                : FontAwesomeLayers.fromQuickNotation(quickNotation)
                    .withPosition(position);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("position", layers.getPosition());
        visitor.accept("classes", layers.toQuickNotation());
    }

}
