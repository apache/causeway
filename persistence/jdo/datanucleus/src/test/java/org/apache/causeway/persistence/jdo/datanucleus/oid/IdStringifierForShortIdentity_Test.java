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

import javax.jdo.identity.ShortIdentity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.JdoShortIdentityValueSemantics;

import lombok.val;

class IdStringifierForShortIdentity_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(Short.MAX_VALUE),
                Arguments.of(Short.MIN_VALUE),
                Arguments.of((short)0),
                Arguments.of((short)12345),
                Arguments.of((short)-12345)
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(final short value) {

        val entityType = Customer.class;

        val stringifier = new JdoShortIdentityValueSemantics();

        val stringified = stringifier.enstring(new ShortIdentity(entityType, value));
        val parse = stringifier.destring(entityType, stringified);

        Assertions.assertThat(parse.getKeyAsObject()).isEqualTo(value);
        Assertions.assertThat(parse.getTargetClass()).isEqualTo(entityType);

        val decomposed = stringifier.decompose(new ShortIdentity(entityType, value));
        val composed = stringifier.compose(decomposed);

        Assertions.assertThat(composed.getKeyAsObject()).isEqualTo(value);
        Assertions.assertThat(composed.getTargetClass()).isEqualTo(entityType);
    }

}
