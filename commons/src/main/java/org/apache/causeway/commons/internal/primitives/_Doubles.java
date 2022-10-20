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
package org.apache.causeway.commons.internal.primitives;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalDouble;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Double Utility
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
@UtilityClass
public class _Doubles {

    /**
     * Ignores precision loss.
     * @throws ArithmeticException
     *      if {@code decimal} cannot be approximated by {@link Double}.
     * @apiNote specifically not using {@link OptionalDouble} as result type here,
     * based on how this method is used from value semantics
     */
    public Optional<Double> convertToDouble(final @NonNull Optional<BigDecimal> decimal) {
        return decimal
                .map(_Doubles::convertToDouble);
    }

    /**
     * Ignores precision loss.
     * @throws ArithmeticException
     *      if {@code decimal} cannot be approximated by {@link Double}.
     */
    public double convertToDouble(final @NonNull BigDecimal decimal) {
        val doubleValue = decimal.doubleValue();
        // overflow detection
        if(!Double.isFinite(doubleValue)) {
            throw new ArithmeticException("non finite BigDecimal to double conversion");
        }
        return doubleValue;
    }

}
