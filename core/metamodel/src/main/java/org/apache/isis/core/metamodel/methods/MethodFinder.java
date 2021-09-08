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
package org.apache.isis.core.metamodel.methods;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.ReturnTypeCategory;

/**
 * An extension to {@link MethodFinderPAT} in support of multiple simultaneous naming conventions.
 * @apiNote each method name candidate is processed in sequence as given by {@code Can<String> names}
 */
//@Log4j2
public final class MethodFinder {

    public static Predicate<Method> hasReturnType(final Class<?> expectedReturnType) {
        return method->expectedReturnType.isAssignableFrom(method.getReturnType());
    }

    public static Predicate<Method> hasReturnTypeAnyOf(final Can<Class<?>> allowedReturnTypes) {
        return method->allowedReturnTypes.stream()
                .anyMatch(allowedReturnType->allowedReturnType.isAssignableFrom(method.getReturnType()));
    }

    public static Stream<Method> findMethod(
            final MethodFinderOptions options,
            final Class<?> expectedReturnType,
            final Class<?>[] paramTypes) {
        return options.streamMethodsMatchingSignature(paramTypes)
                .filter(hasReturnType(expectedReturnType));
    }

    // -- SEARCH FOR MULTIPLE NAME CANDIDATES

    public static Stream<Method> findMethod_returningCategory(
            final MethodFinderOptions options,
            final ReturnTypeCategory returnTypeCategory,
            final Class<?>[] paramTypes) {

        return options.streamMethodsMatchingSignature(paramTypes)
                .filter(hasReturnTypeAnyOf(returnTypeCategory.getReturnTypes()));
    }

    public static Stream<Method> findMethod_returningBoolean(
            final MethodFinderOptions options,
            final Class<?>[] paramTypes) {

        return findMethod_returningCategory(options, ReturnTypeCategory.BOOLEAN, paramTypes);
    }

    public static Stream<Method> findMethod_returningText(
            final MethodFinderOptions options,
            final Class<?>[] paramTypes) {

        return findMethod_returningCategory(options, ReturnTypeCategory.TRANSLATABLE, paramTypes);
    }

    public static Stream<Method> findMethod_returningNonScalar(
            final MethodFinderOptions options,
            final Class<?> elementReturnType,
            final Class<?>[] paramTypes) {

        return options.streamMethodsMatchingSignature(paramTypes)
                .filter(hasReturnTypeAnyOf(ReturnTypeCategory.nonScalar(elementReturnType)));
    }

}
