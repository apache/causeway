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
package org.apache.isis.core.metamodel.spec;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.core.ResolvableType;

import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;

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

    public boolean isScalar() {
        return containerType.isEmpty();
    }

    public boolean isArray() {
        return containerType.map(Class::isArray).orElse(false);
    }

    // -- FACTORIES

    public static TypeOfAnyCardinality scalar(final @NonNull Class<?> scalarType) {
        return of(assertScalar(scalarType), Optional.empty());
    }

    public static TypeOfAnyCardinality nonScalar(
            final @NonNull Class<?> elementType,
            final @NonNull Class<?> nonScalarType) {
        return of(assertScalar(elementType), Optional.of(assertNonScalar(nonScalarType)));
    }

    public static TypeOfAnyCardinality forMethodReturn(
            final Class<?> implementationClass, final Method method) {
        val methodReturn = method.getReturnType();

        return ProgrammingModelConstants.CollectionType.valueOf(methodReturn)
        .map(collectionType->
            nonScalar(
                    inferElementTypeForMethodReturn(implementationClass, method),
                    methodReturn)
        )
        .orElseGet(()->scalar(methodReturn));
    }

    public static TypeOfAnyCardinality forMethodParameter(
            final Class<?> implementationClass, final Method method, final int paramIndex) {
        val paramType = method.getParameters()[paramIndex].getType();

        return ProgrammingModelConstants.CollectionType.valueOf(paramType)
        .map(collectionType->
            nonScalar(
                    inferElementTypeForMethodParameter(implementationClass, method, paramIndex),
                    paramType)
        )
        .orElseGet(()->scalar(paramType));
    }

    // -- WITHERS

    public TypeOfAnyCardinality withElementType(final @NonNull Class<?> elementType) {
        return of(assertScalar(elementType), this.getContainerType());
    }

    // -- HELPER

    private static Class<?> assertScalar(final @NonNull Class<?> scalarType) {
        _Assert.assertEquals(
                Optional.empty(),
                ProgrammingModelConstants.CollectionType.valueOf(scalarType),
                ()->String.format("%s should not match any supported non-scalar types", scalarType));
        return scalarType;
    }

    private static Class<?> assertNonScalar(final @NonNull Class<?> nonScalarType) {
        _Assert.assertTrue(
                ProgrammingModelConstants.CollectionType.valueOf(nonScalarType).isPresent(),
                ()->String.format("%s should match a supported non-scalar type", nonScalarType));
        return nonScalarType;
    }

    /** Return the element type as a resolved Class, falling back to Object if no specific class can be resolved. */
    private static Class<?> inferElementTypeForMethodReturn(
            final Class<?> implementationClass, final Method method) {
        val nonScalar = ResolvableType.forMethodReturnType(method, implementationClass);
        return toClass(nonScalar);
    }

    /** Return the element type as a resolved Class, falling back to Object if no specific class can be resolved. */
    private static Class<?> inferElementTypeForMethodParameter(
            final Class<?> implementationClass, final Method method, final int paramIndex) {
        val nonScalar = ResolvableType.forMethodParameter(method, paramIndex, implementationClass);
        return toClass(nonScalar);
    }

    private static Class<?> toClass(final ResolvableType nonScalar){
        val genericTypeArg = nonScalar.isArray()
                ? nonScalar.getComponentType()
                : nonScalar.getGeneric(0);
        return genericTypeArg.toClass();
    }



}
