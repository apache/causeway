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
package org.apache.causeway.core.metamodel.facets.object.layout;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.HasImperativeAspect;
import org.apache.causeway.core.metamodel.facets.ImperativeAspect;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;

public record LayoutPrefixFacetViaMethod(
    @NonNull String origin,
    @NonNull ImperativeAspect imperativeAspect,
    @NonNull FacetHolder facetHolder,
    Facet.@NonNull Precedence precedence
) implements LayoutPrefixFacet, HasImperativeAspect {

    // -- FACTORIES

    public static Optional<LayoutPrefixFacetViaMethod> create(
        final @Nullable ResolvedMethod methodIfAny,
        final FacetHolder holder) {

        System.err.printf("methodIfAny %s%n", methodIfAny);

        return Optional.ofNullable(methodIfAny)
            .map(method->ImperativeAspect.singleRegularMethod(method, Intent.UI_HINT))
            .map(imperativeAspect->
                new LayoutPrefixFacetViaMethod("LayoutMethod", imperativeAspect, holder, Precedence.DEFAULT));
    }

    // -- METHODS

    @Override public Class<? extends Facet> facetType() { return LayoutPrefixFacet.class; }
    @Override public Precedence getPrecedence() { return precedence(); }
    @Override public FacetHolder getFacetHolder() { return facetHolder(); }

    @Override
    public String layoutPrefix(final ManagedObject managedObject) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(managedObject)) return null;
        try {
            return (String) imperativeAspect.invokeSingleMethod(managedObject);
        } catch (final RuntimeException ex) {
            return null;
        }
    }

    @Override
    public ImperativeAspect getImperativeAspect() {
        return imperativeAspect();
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("origin", origin());
        visitor.accept("precedence", getPrecedence().name());
        imperativeAspect.visitAttributes(visitor);
    }

}
