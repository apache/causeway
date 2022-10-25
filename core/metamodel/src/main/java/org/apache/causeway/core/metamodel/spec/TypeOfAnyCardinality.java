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
package org.apache.causeway.core.metamodel.spec;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.core.ResolvableType;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.reflection._Reflect.MethodAndImplementingClass;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.CollectionSemantics;

import lombok.NonNull;
import lombok.val;

@lombok.Value(staticConstructor = "of")
public class TypeOfAnyCardinality {

    /**
     * The type either contained or not.
     */
    private final @NonNull  Class<?> elementType;

    /**
     * Optionally the container type, the {@link #getElementType()} is contained in,
     * such as {@link List}, {@link Collection}, etc.
     */
    private final @NonNull Optional<Class<?>> containerType;
    private final @NonNull Optional<CollectionSemantics> collectionSemantics;

    public boolean isScalar() {
        return containerType.isEmpty();
    }

    public boolean isArray() {
        return containerType.map(Class::isArray).orElse(false);
    }

    /**
     * Always <code>true</code> for <i>scalar</i> or <i>array</i>.
     * Otherwise, whether {@link #getContainerType()} exactly matches
     * the container type from {@link #getCollectionSemantics()}.
     */
    public boolean isSupportedForActionParameter() {
        return isScalar()
                || isArray()
                ? true
                : Objects.equals(
                        getContainerType().orElse(null),
                        getCollectionSemantics().map(CollectionSemantics::getContainerType).orElse(null));

    }

    // -- FACTORIES

    public static TypeOfAnyCardinality scalar(final @NonNull Class<?> scalarType) {
        return of(assertScalar(scalarType), Optional.empty(), Optional.empty());
    }

    public static TypeOfAnyCardinality nonScalar(
            final @NonNull Class<?> elementType,
            final @NonNull Class<?> nonScalarType,
            final @NonNull CollectionSemantics collectionSemantics) {
        return of(assertScalar(elementType),
                Optional.of(assertNonScalar(nonScalarType)),
                Optional.of(collectionSemantics));
    }

    public static TypeOfAnyCardinality forMethodReturn(
            final Class<?> _implementationClass, final Method _method) {
        val methodReturnGuess = _method.getReturnType();
        return ProgrammingModelConstants.CollectionSemantics.valueOf(methodReturnGuess)
        .map(__->{
            // adopt into default class loader context

            val origin = MethodAndImplementingClass.of(_method, _implementationClass);
            val adopted = origin
                    .adoptIntoDefaultClassLoader()
                    .getValue()
                    .orElse(origin);

            val method = adopted.getMethod();
            val methodReturn = method.getReturnType();

            return ProgrammingModelConstants.CollectionSemantics.valueOf(methodReturn)
            .map(collectionSemantics->
                nonScalar(
                        adopted.resolveFirstGenericTypeArgumentOnMethodReturn(),
                        methodReturn,
                        collectionSemantics)
            )
            .orElseGet(()->scalar(methodReturn));
        })
        .orElseGet(()->scalar(methodReturnGuess));
    }

    public static TypeOfAnyCardinality forMethodParameter(
            final Class<?> _implementationClass, final Method _method, final int paramIndex) {
        val paramTypeGuess = _method.getParameters()[paramIndex].getType();
        return ProgrammingModelConstants.CollectionSemantics.valueOf(paramTypeGuess)
        .map(__->{
            // adopt into default class loader context

            val origin = MethodAndImplementingClass.of(_method, _implementationClass);
            val adopted = origin
                    .adoptIntoDefaultClassLoader()
                    .getValue()
                    .orElse(origin);

            val method = adopted.getMethod();
            val paramType = method.getParameters()[paramIndex].getType();

            return ProgrammingModelConstants.CollectionSemantics.valueOf(paramType)
            .map(collectionSemantics->
                nonScalar(
                        adopted.resolveFirstGenericTypeArgumentOnParameter(paramIndex),
                        paramType,
                        collectionSemantics)
            )
            .orElseGet(()->scalar(paramType));
        })
        .orElseGet(()->scalar(paramTypeGuess));
    }

    public static TypeOfAnyCardinality forNonScalarType(
            final @NonNull Class<?> nonScalarType,
            final @NonNull CollectionSemantics collectionSemantics) {
        return nonScalar(
                toClass(ResolvableType.forClass(nonScalarType)),
                nonScalarType,
                collectionSemantics);
    }

    // -- WITHERS

    public TypeOfAnyCardinality withElementType(final @NonNull Class<?> elementType) {
        return of(assertScalar(elementType), this.getContainerType(), this.getCollectionSemantics());
    }

    // -- HELPER

    private static Class<?> assertScalar(final @NonNull Class<?> scalarType) {
        _Assert.assertEquals(
                Optional.empty(),
                ProgrammingModelConstants.CollectionSemantics.valueOf(scalarType),
                ()->String.format("%s should not match any supported non-scalar types", scalarType));
        return scalarType;
    }

    private static Class<?> assertNonScalar(final @NonNull Class<?> nonScalarType) {
        _Assert.assertTrue(
                ProgrammingModelConstants.CollectionSemantics.valueOf(nonScalarType).isPresent(),
                ()->String.format("%s should match a supported non-scalar type", nonScalarType));
        return nonScalarType;
    }

    private static Class<?> toClass(final ResolvableType nonScalar){
        val genericTypeArg = nonScalar.isArray()
                ? nonScalar.getComponentType()
                : nonScalar.getGeneric(0);
        return genericTypeArg.toClass();
    }

}
