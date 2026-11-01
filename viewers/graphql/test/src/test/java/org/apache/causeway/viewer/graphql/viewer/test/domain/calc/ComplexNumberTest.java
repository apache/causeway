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
package org.apache.causeway.viewer.graphql.viewer.test.domain.calc;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComplexNumberTest {

    @Test
    void canonicalFormUsesAlgebraicNotation() {
        assertEquals("3+4i", new ComplexNumber(new BigDecimal("3"), new BigDecimal("4")).canonical());
        assertEquals(
                "1.25-2.500i",
                new ComplexNumber(new BigDecimal("1.25"), new BigDecimal("-2.500")).canonical());
    }

    @Test
    void algebraicFormRoundTripsAndLegacyCommaFormIsRejected() {
        assertEquals(
                new ComplexNumber(new BigDecimal("3"), new BigDecimal("4")),
                ComplexNumber.parse("3+4i"));
        assertEquals(
                new ComplexNumber(new BigDecimal("1.25"), new BigDecimal("-2.500")),
                ComplexNumber.parse("1.25-2.500i"));
        assertThrows(IllegalArgumentException.class, () -> ComplexNumber.parse("3,4"));
    }
}
