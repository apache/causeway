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
package org.apache.isis.applib.value.semantics;

/**
 * Provides an ordering relation for a given value-type.
 * <p>
 * Does supports an <i>epsilon</i> parameter (measure of accuracy),
 * which has different meaning, depending on context
 * <ul>
 * <li>Numbers: accuracy is usually given as number eg. {@code 1E-12} - in case of integers should default to {@code 0}</li>
 * <li>Dates (temporal): accuracy is usually given as a number of days - should default to {@code 0}</li>
 * <li>Times (temporal): accuracy is usually given as a number of seconds - eg. {@code 1E-3} for millisecond resolution</li>
 * </ul>
 *
 * @param <T> - value-type
 * @param <D> - measure of accuracy
 *
 * @see DefaultsProvider
 * @see Parser
 * @see EncoderDecoder
 * @see ValueSemanticsProvider
 *
 * @since 2.x {@index}
 *
 */
public interface OrderRelation<T, D> {

    /**
     * Default epsilon (measure of accuracy).
     */
    D epsilon();

    /**
     * @param epsilon - measure of accuracy
     */
    int compare(T a, T b, D epsilon);

    default int compare(final T a, final T b) {
        return compare(a, b, epsilon());
    }

    /**
     * @param epsilon - measure of accuracy
     */
    boolean equals(T a, T b, D epsilon);

    default boolean equals(final T a, final T b) {
        return equals(a, b, epsilon());
    }

}
