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
package org.apache.causeway.core.metamodel.facets.actions.semantics;

import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

/**
 * Represents the semantics of an action.
 * <p>
 * Specifically, whether it is safe, idempotent or non-idempotent.
 */
public record ActionSemanticsFacet(
    @NonNull String origin,
    @NonNull SemanticsOf value,
    @NonNull FacetHolder facetHolder,
    Facet.@NonNull Precedence precedence
    ) implements Facet {

    @Override public Class<? extends Facet> facetType() { return getClass(); }
    @Override public Precedence getPrecedence() { return precedence(); }
    @Override public FacetHolder getFacetHolder() { return facetHolder(); }

    public ActionSemanticsFacet(final String origin, final SemanticsOf of, final FacetHolder holder) {
        this(origin, of, holder, Precedence.DEFAULT);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("origin", origin());
        visitor.accept("precedence", precedence().name());
        visitor.accept("value", value);
    }

}
