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
package org.apache.causeway.core.metamodel.commons;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class StringUtils_NormalizedTest {

    @ParameterizedTest
    @MethodSource("data")
    public void normalizesOk(final String input, final String expected) {
        assertThat(StringExtensions.normalized(input), is(expected));
    }

    private static Stream<Arguments> data() {
        return Stream.of(
          Arguments.of(null, null), // null
          Arguments.of("", ""), // empty string
          Arguments.of("yada Foobar", "yada Foobar"), // alreadyNormalized
          Arguments.of("Yada\tFoobar", "Yada Foobar"), // tab
          Arguments.of("Yada\t Foobar", "Yada Foobar"), // tab and space
          Arguments.of("Yada  foobar", "Yada foobar"), // two spaces
          Arguments.of("Yada\nfoobar", "Yada foobar"), // new line
          Arguments.of("Yada\n Foobar", "Yada Foobar"), // newline and space
          Arguments.of("Yada\r\n Foobar", "Yada Foobar"), // windows newline
          Arguments.of("Yada\r Foobar", "Yada Foobar"), // macos newline
          Arguments.of("Yada\r \tFoo \n\tbar  Baz", "Yada Foo bar Baz") // multiple
        );
    }

}
