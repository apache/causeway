/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.core.metamodel.valuesemantics;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;

class IdStringifierForBigDecimal_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(BigDecimal.ONE),
                Arguments.of(BigDecimal.TEN),
                Arguments.of(BigDecimal.valueOf(Long.MAX_VALUE)),
                Arguments.of(BigDecimal.valueOf(Long.MIN_VALUE)),
                Arguments.of(BigDecimal.valueOf(Double.MAX_VALUE)),
                Arguments.of(BigDecimal.valueOf(Double.MIN_VALUE)),
                Arguments.of(BigDecimal.valueOf(10, 0)),
                Arguments.of(BigDecimal.valueOf(10, 1)),
                Arguments.of(BigDecimal.valueOf(10, 2)),
                Arguments.of(BigDecimal.valueOf(10, 3)),
                Arguments.of(new BigDecimal("1234567890123456789012345678901234567890.12345678901234567890"))
        );
    }

    @ParameterizedTest
    @MethodSource()
    void roundtrip(final BigDecimal bigDecimal) {

        val stringifier = new BigDecimalValueSemantics();

        String stringified = stringifier.enstring(bigDecimal);
        BigDecimal parse = stringifier.destring(stringified);

        assertThat(parse).isEqualTo(bigDecimal);
    }

}
