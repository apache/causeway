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
 *
 */
package org.apache.causeway.persistence.querydsl.applib.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;

import lombok.experimental.UtilityClass;

/**
 * Static factory methods for various types of {@link Expression}s (in particular, {@link BooleanOperation}s and
 * {@link Predicate}s).
 *
 * @since 2.1 {@index}
 */
@UtilityClass
public class DslExpressions {

    public final static Pattern REGEX_PATTERN = Pattern.compile("\\(\\?i\\)"); // Pattern to recognize #wildcardToCaseInsensitiveRegex conversion

    /**
     * Creates {@link BooleanOperation} where the arguments use the operator 'startsWith'.
     * Equivalent to SQL clause '<argument>.startsWith(<path>)'.
     * This has a different outcome compared to the '<path>.startsWith(<argument>)'
     */
    public static <T> BooleanOperation startsWith(final T argument, final Path<T> path) {
        return Expressions.predicate(Ops.STARTS_WITH, constant(argument), path);
    }

    /**
     * Creates {@link BooleanOperation} where the arguments is checked for null.
     * Equivalent with SQL clause '<path> = <argument>' or '<path> IS NULL'.
     */
    public static <T> BooleanOperation eqOrNull(
            final Path<T> path,
            final T argument
    ) {
        return Optional.ofNullable(argument)
                .map(a -> Expressions.predicate(Ops.EQ, path, constant(a)))
                .orElse(Expressions.predicate(Ops.IS_NULL, path));
    }

    public static BooleanExpression searchAndReplace(
            final StringPath stringPath,
            final String searchPhrase,
            final CaseSensitivity caseSensitivity) {
        return search(stringPath, Wildcards.toAnsiSqlWildcard(searchPhrase), caseSensitivity);
    }

    public static BooleanExpression search(
            final StringPath stringPath,
            final String searchPhrase,
            final CaseSensitivity caseSensitivity
    ) {
        if (REGEX_PATTERN.matcher(searchPhrase).find()) {
            return stringPath.matches(searchPhrase);
        }
        if (caseSensitivity.isIgnoreCase()) {
            return stringPath.likeIgnoreCase(searchPhrase);
        }
        return stringPath.like(searchPhrase);
    }

    public static <T> Expression<T> constant(final T argument) {
        if (argument == null) return null;
        return Expressions.constant(argument);
    }

    public static Predicate and(List<? extends Predicate> predicates) {
        return and(predicates.toArray(new Predicate[0]));
    }

    public static Predicate and(Predicate... predicates) {
        if (predicates.length == 1) {
            return predicates[0];
        }
        if (predicates.length > 2) {
            Predicate[] predicate = new Predicate[]{and(Arrays.copyOf(predicates, 2))};
            Predicate[] remainder = Arrays.copyOfRange(predicates, 2, predicates.length);
            if (remainder.length == 1) {
                return and(addAll(predicate, remainder[0]));
            }
            if (remainder.length > 2) {
                return and(addAll(predicate, remainder));
            }
            return and(addAll(predicate, and(remainder)));
        }
        return Expressions.predicate(Ops.AND, Arrays.stream(predicates).map(ExpressionUtils::extract).toArray(Expression[]::new));
    }

    public static Predicate or(List<? extends Predicate> predicates) {
        return or(predicates.toArray(new Predicate[0]));
    }

    public static Predicate or(Predicate... predicates) {
        if (predicates.length == 1) {
            return predicates[0];
        }
        if (predicates.length > 2) {
            Predicate[] predicate = new Predicate[]{or(Arrays.copyOf(predicates, 2))};
            Predicate[] remainder = Arrays.copyOfRange(predicates, 2, predicates.length);
            if (remainder.length == 1) {
                return or(addAll(predicate, remainder[0]));
            }
            if (remainder.length > 2) {
                return or(addAll(predicate, remainder));
            }
            return or(addAll(predicate, or(remainder)));
        }
        return Expressions.predicate(Ops.OR, Arrays.stream(predicates).map(ExpressionUtils::extract).toArray(Expression[]::new));
    }

    private static <T> T[] addAll(
            final T[] array1,
            @SuppressWarnings("unchecked")
            final T... array2
    ) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final Class<?> type1 = array1.getClass().getComponentType();
        @SuppressWarnings("unchecked") // OK, because array is of type T
        final T[] joinedArray = (T[]) Array.newInstance(type1, array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        try {
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        } catch (final ArrayStoreException ase) {
            // Check if problem was due to incompatible types
            /*
             * We do this here, rather than before the copy because:
             * - it would be a wasted check most of the time
             * - safer, in case check turns out to be too strict
             */
            final Class<?> type2 = array2.getClass().getComponentType();
            if (!type1.isAssignableFrom(type2)) {
                throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of "
                        + type1.getName(), ase);
            }
            throw ase; // No, so rethrow original
        }
        return joinedArray;
    }

    private static <T> T[] clone(final T[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }
}
