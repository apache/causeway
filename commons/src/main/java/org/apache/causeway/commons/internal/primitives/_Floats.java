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

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * t Utility
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
@UtilityClass
public class _Floats {

    /**
     * Ignores precision loss.
     * @throws ArithmeticException
     *      if {@code decimal} cannot not be approximated by {@link Float}.
     */
    public Optional<Float> convertToFloat(final @NonNull Optional<BigDecimal> decimal) {
        return decimal
                .map(_Floats::convertToFloat);
    }

    /**
     * Ignores precision loss.
     * @throws ArithmeticException
     *      if {@code decimal} cannot not be approximated by {@link Double}.
     */
    public float convertToFloat(final @NonNull BigDecimal decimal) {
        val floatValue = decimal.floatValue();
        // overflow detection
        if(!Float.isFinite(floatValue)) {
            throw new ArithmeticException("non finite BigDecimal to float conversion");
        }
        return floatValue;
    }

}
