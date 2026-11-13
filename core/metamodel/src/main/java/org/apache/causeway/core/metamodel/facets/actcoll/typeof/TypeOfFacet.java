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
package org.apache.causeway.core.metamodel.facets.actcoll.typeof;

import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedType;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/**
 * The type of the collection or the action.
 * <p>
 * In the standard Apache Causeway Programming Model, corresponds to annotating the
 * collection's accessor or the action's invoker method with the
 * {@link Collection#typeOf} annotation.
 */
public interface TypeOfFacet extends Facet {

    ResolvedType value();

    /**
     * Convenience to return the {@link ObjectSpecification} corresponding to
     * this facet's {@link #value() type's} {@link ResolvedType#elementType()}.
     */
    ObjectSpecification elementSpec();

    Optional<CollectionSemantics> getCollectionSemantics();

    // -- FACTORIES

    static Optional<TypeOfFacet> inferFromMethodParameter(
            final MethodFacade method,
            final int paramIndex,
            final FacetHolder holder) {
        var type = method.resolveParameter(paramIndex);
        return toInferredFrom(TypeOfFacet::inferredFromFeature, type, holder);
    }

    static Optional<TypeOfFacet> inferFromMethodReturnType(
            final MethodFacade method,
            final FacetHolder holder) {
        var type = method.resolveMethodReturn();
        return toInferredFrom(TypeOfFacet::inferredFromFeature, type, holder);
    }

    static Optional<TypeOfFacet> inferFromPluralType(
            final CollectionSemantics collectionSemantics, final Class<?> pluralType, final FacetHolder holder) {
        var type = _GenericResolver.forPluralType(pluralType, collectionSemantics);
        return toInferredFrom(TypeOfFacet::inferredFromType, type, holder);
    }

    // -- INTERNAL

    private static Optional<TypeOfFacet> toInferredFrom(
            final BiFunction<ResolvedType, FacetHolder, TypeOfFacet> factory,
            final ResolvedType type,
            final FacetHolder holder) {
        return type.isSingular()
            ? Optional.empty()
            : Optional.of(factory.apply(type, holder));
    }

    private static TypeOfFacet inferredFromFeature(
            final ResolvedType type,
            final FacetHolder holder) {
        return new TypeOfFacetFromFeature(type, holder);
    }

    private static TypeOfFacet inferredFromType(
            final ResolvedType type,
            final FacetHolder holder) {
        return new TypeOfFacetFromType(type, holder);
    }

}
