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

package org.apache.causeway.persistence.jdo.datanucleus.oid;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.datanucleus.identity.LongId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.DnLongIdValueSemantics;

import lombok.val;

class IdStringifierForLongId_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(1L),
                Arguments.of(0L),
                Arguments.of(10L),
                Arguments.of(Long.MAX_VALUE),
                Arguments.of(Long.MIN_VALUE)
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(final long value) {

        val entityType = Customer.class;

        val stringifier = new DnLongIdValueSemantics();

        val stringified = stringifier.enstring(new LongId(entityType, value));
        val parse = stringifier.destring(entityType, stringified);

        Assertions.assertThat(parse.getKeyAsObject()).isEqualTo(value);
        Assertions.assertThat(parse.getTargetClassName()).isEqualTo(entityType.getName());

        val decomposed = stringifier.decompose(new LongId(entityType, value));
        val composed = stringifier.compose(decomposed);

        Assertions.assertThat(composed.getKeyAsObject()).isEqualTo(value);
        Assertions.assertThat(composed.getTargetClassName()).isEqualTo(entityType.getName());
    }

}
