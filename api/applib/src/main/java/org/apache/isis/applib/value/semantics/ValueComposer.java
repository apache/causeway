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

import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.schema.common.v2.TypedTupleDto;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;

/**
 * Provides composition and decomposition for a given value-type
 * from and into its constituent fundamental parts.
 *
 * @param <T> - value-type
 *
 * @see DefaultsProvider
 * @see Parser
 * @see OrderRelation
 * @see ValueSemanticsProvider
 *
 * @since 2.x {@index}
 */
public interface ValueComposer<T> {

    public static final class ValueDecomposition extends _Either<ValueWithTypeDto, TypedTupleDto> {
        private static final long serialVersionUID = 1L;

        public static ValueDecomposition ofFundamental(final ValueWithTypeDto valueWithTypeDto) {
            return new ValueDecomposition(valueWithTypeDto, null);
        }

        public static ValueDecomposition ofComposite(final TypedTupleDto typedTupleDto) {
            return new ValueDecomposition(null, typedTupleDto);
        }

        private ValueDecomposition(final ValueWithTypeDto left, final TypedTupleDto right) {
            super(left, right);
        }
    }

    /**
     * Converts a value object into either a {@link ValueWithTypeDto}
     * or {@link TypedTupleDto}.
     */
    ValueDecomposition decompose(T value);

    /**
     * Converts either a {@link ValueWithTypeDto} or
     * a {@link TypedTupleDto}
     * to an instance of the object.
     *
     * @see #decompose(Object)
     */
    T compose(ValueDecomposition decomposition);


    // -- EXPERIMENTAL

    default Object getValueMixin(final T value) {
        return null;
    }

}
