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
package org.apache.causeway.applib.util;

import java.util.Comparator;
import java.util.function.Function;

import org.apache.causeway.commons.internal.base._NullSafe;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides fluent composition for Objects' equals, hashCode and toString.
 * @since 1.x revised for 2.0 {@index}
 */
@Slf4j
public final class ObjectContracts {

    private ObjectContracts() {}

    public static <T> ToString<T> toString(final String name, final Function<T, ?> getter) {
        return ToString.toString(name, getter);
    }

    public static <T> Equality<T> checkEquals(final Function<T, ?> getter) {
        return Equality.checkEquals(getter);
    }

    public static <T> Hashing<T> hashing(final Function<T, ?> getter) {
        return Hashing.hashing(getter);
    }

    /**
     * WARNING Possible misuse because of forgetting respectively the last method
     * argument with {@code equals}, [@code hashCode} and {@code toString}!
     *
     * @since 2.0
     * @param <T>
     */
    public static interface ObjectContract<T> {

        public int compare(T obj, T other);

        public boolean equals(T obj, Object other);

        public int hashCode(T obj);

        public String toString(T obj);

        // -- TO STRING EVALUATION

        /**
         * True 'wither' (each call returns a new instance of ObjectContract)!
         * @param valueToStringFunction
         * @return ObjectContract with valueToStringFunction to apply to property values when
         * processing the toString algorithm.
         */
        public ObjectContract<T> withValueToStringFunction(Function<Object, String> valueToStringFunction);

        // -- COMPOSITION

        /**
         * Contract composition. (Any valueToStringFunction is 'copied over'.)
         *
         * @param propertyLabel a label to use for property to string output
         * @param getter function extracting the property value of an object
         * @param valueComparator
         */
        public <U> ObjectContract<T> thenUse(
                String propertyLabel,
                Function<? super T, ? extends U> getter,
                Comparator<? super U> valueComparator);

        /**
         * Contract composition using the naturalOrder comparator.
         * (Any valueToStringFunction is 'copied over'.)
         *
         * @param propertyLabel a label to use for property to string output
         * @param getter function extracting the property value of an object
         */
        public default <U extends Comparable<? super U>> ObjectContract<T> thenUse(
                final String propertyLabel,
                final Function<? super T, ? extends U> getter){
            return thenUse(propertyLabel, getter, Comparator.<U>naturalOrder());
        }

        public static <T> ObjectContract<T> empty(final Class<T> objectClass) {
            return new ObjectContract_Empty<>(objectClass);
        }

    }

    // -- COMPOSITION ENTRY POINTS

    public static <T> ObjectContract<T> contract(final Class<T> objectClass) {
        return ObjectContract.empty(objectClass);
    }

    public static <T> ObjectContract<T> parse(final Class<T> target, final String propertyNames) {
        return ObjectContract_Parser.parse(target, propertyNames);
    }

    // -- BACKWARDS COMPATIBILITY TO-STRING EVALUATOR

    public interface ToStringEvaluator {

        public boolean canEvaluate(Object o);
        public String evaluate(Object o);

        public static Function<Object, String> combineToFunction(final ToStringEvaluator ... evaluators){
            return value -> {
                if(value == null) {
                    return null;
                }
                if(!_NullSafe.isEmpty(evaluators)) {
                    for (ToStringEvaluator evaluator : evaluators) {
                        if(evaluator.canEvaluate(value)) {
                            return evaluator.evaluate(value);
                        }
                    }
                }
                return value.toString();
            };
        }
    }

}
