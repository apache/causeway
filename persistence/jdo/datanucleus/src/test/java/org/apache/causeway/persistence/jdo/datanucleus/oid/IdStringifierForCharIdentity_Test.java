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

import javax.jdo.identity.CharIdentity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.core.metamodel.valuesemantics.CharacterValueSemantics;
import org.apache.causeway.persistence.jdo.datanucleus.valuetypes.JdoCharIdentityValueSemantics;

class IdStringifierForCharIdentity_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of('a'),
                Arguments.of('b'),
                Arguments.of(' '),
                Arguments.of('^'), // shouldn't need ecoding
                Arguments.of('*'),
                Arguments.of('('),
                Arguments.of(')'),
                Arguments.of('_'),
                Arguments.of('-'),
                Arguments.of('['),
                Arguments.of(']'),
                Arguments.of(','),
                Arguments.of('.'),
                Arguments.of(';'),
                Arguments.of('/'), // should need encoding
                Arguments.of('\\'),
                Arguments.of('?'),
                Arguments.of(':'),
                Arguments.of('&'),
                Arguments.of('+'),
                Arguments.of('%')
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(final char c) {

        var entityType = Customer.class;

        var stringifier = JdoCharIdentityValueSemantics.builder()
                .idStringifierForCharacter(new CharacterValueSemantics())
                .build();

        var value = new CharIdentity(entityType, c);
        var stringified = stringifier.enstring(value);
        var parse = stringifier.destring(entityType, stringified);

        assertThat(parse).isEqualTo(value);
    }

}
