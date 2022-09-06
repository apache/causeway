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
package org.apache.isis.core.metamodel.facets.actcoll.typeof;

import java.lang.reflect.Method;
import java.util.Optional;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.CollectionType;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleTypeValueFacet;
import org.apache.isis.core.metamodel.spec.TypeOfAnyCardinality;

import lombok.val;

/**
 * The type of the collection or the action.
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating the
 * collection's accessor or the action's invoker method with the
 * {@link Collection#typeOf} annotation.
 */
public interface TypeOfFacet extends SingleTypeValueFacet {

    // -- FACTORIES

    static Optional<TypeOfFacet> inferFromMethodParameter(
            final Class<?> implementationClass,
            final Method method,
            final int paramIndex,
            final FacetHolder holder) {
        val type = TypeOfAnyCardinality.forMethodParameter(implementationClass, method, paramIndex);
        return toInferredFrom(type, holder);
    }

    static Optional<TypeOfFacet> inferFromMethodReturnType(
            final Class<?> implementationClass,
            final Method method,
            final FacetHolder holder) {
        val type = TypeOfAnyCardinality.forMethodReturn(implementationClass, method);
        return toInferredFrom(type, holder);
    }

    static Optional<TypeOfFacet> inferFromNonScalarType(
            final CollectionType collectionType, final Class<?> nonScalarType, final FacetHolder holder) {
        val type = TypeOfAnyCardinality.forNonScalarType(nonScalarType);
        return toInferredFrom(type, holder);
    }

    // -- INTERNAL

    private static Optional<TypeOfFacet> toInferredFrom(
            final TypeOfAnyCardinality type,
            final FacetHolder holder) {
        return type.isScalar()
            ? Optional.empty()
            : Optional.of(type.isArray()
                    ? inferredFromArray(type, holder)
                    : inferredFromGenerics(type, holder));
    }

    private static TypeOfFacet inferredFromArray(
            final TypeOfAnyCardinality type,
            final FacetHolder holder) {
        return new TypeOfFacetFromArray(type, holder);
    }

    private static TypeOfFacet inferredFromGenerics(
            final TypeOfAnyCardinality type,
            final FacetHolder holder) {
        return new TypeOfFacetFromGenerics(type, holder);
    }

}
