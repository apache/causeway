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

import java.io.Serializable;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.applib.services.urlencoding.UrlEncodingService;

import lombok.Value;
import lombok.val;

class IdStringifierForSerializable_Test {

    private UrlEncodingService codec = UrlEncodingService.forTesting();

    // -- SCENARIO

    @Value
    static class CustomerPK implements Serializable{
        private static final long serialVersionUID = 1L;
        final int lower;
        final int upper;
    }

    // -- TEST

    static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(Byte.MAX_VALUE),
                Arguments.of(Byte.MIN_VALUE),
                Arguments.of((byte)0),
                Arguments.of((byte)12345),
                Arguments.of((byte)-12345),
                Arguments.of(new CustomerPK(5,6))
                // Arguments.of((Serializable)null) ... throws NPE as expected
        );
    }

    @ParameterizedTest
    @MethodSource()
    void roundtrip(final Serializable value) {

        val stringifier = new IdStringifierForSerializable(codec);

        String stringified = stringifier.enstring(value);
        Serializable parse = stringifier.destring(stringified);

        assertThat(parse).isEqualTo(value);
    }

}
