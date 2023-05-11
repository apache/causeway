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
package org.apache.causeway.commons.internal.base;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.apache.causeway.commons.internal._Constants;

class StringsTest {

    @Test
    void isEmpty() throws Exception {
        assertThat(_Strings.isEmpty(" 12 aBc"), is(false));
        assertThat(_Strings.isEmpty(""), is(true));
        assertThat(_Strings.isEmpty(null), is(true));
    }

    @Test
    void isNotEmpty() throws Exception {
        assertThat(_Strings.isNotEmpty(" 12 aBc"), is(true));
        assertThat(_Strings.isNotEmpty(""), is(false));
        assertThat(_Strings.isNotEmpty(null), is(false));
    }

    @Test
    void substring() throws Exception {
        assertThat(_Strings.substring("12abc", 0, 5), is("12abc"));
        assertThat(_Strings.substring("12abc", 1, 5), is("2abc"));
        assertThat(_Strings.substring("12abc", 0, 4), is("12ab"));
        assertThat(_Strings.substring("12abc", 0, -1), is("12ab"));
        assertThat(_Strings.substring("12abc", 10, -10), is("")); // index overflow
        assertThat(_Strings.substring(null, 1, 1), nullValue());
    }

    @Test
    void lowerWithNull() throws Exception {
        assertThat(
                _Strings.lower(null),
                nullValue());
    }

    @Test
    void lowerMixed() throws Exception {
        assertThat(
                _Strings.lower("12aBc"),
                is("12abc"));
    }


    @Test
    void upperWithNull() throws Exception {
        assertThat(
                _Strings.upper(null),
                nullValue());
    }

    @Test
    void upperMixed() throws Exception {
        assertThat(
                _Strings.upper("12aBc"),
                is("12ABC"));
    }

    @Test
    void capitalizeWithNull() throws Exception {
        assertThat(
                _Strings.capitalize(null),
                nullValue());
    }

    @Test
    void capitalizeSize0() throws Exception {
        assertThat(
                _Strings.capitalize(""),
                is(""));
    }

    @Test
    void capitalizeSize1() throws Exception {
        assertThat(
                _Strings.capitalize("a"),
                is("A"));
    }

    @Test
    void capitalizeSize2() throws Exception {
        assertThat(
                _Strings.capitalize("ab"),
                is("Ab"));
    }

    @Test
    void trimWithNull() throws Exception {
        assertThat(
                _Strings.trim(null),
                nullValue());
    }

    @Test
    void trimMixed() throws Exception {
        assertThat(
                _Strings.trim(" 12 aBc"),
                is("12 aBc"));
    }

    @Test
    void splitThenStreamWithNull() throws Exception {
        assertThat(
                _Strings.splitThenStream(null, "$")
                .collect(Collectors.joining("|")),
                is(""));
    }

    @Test
    void splitThenStreamSingle() throws Exception {
        assertThat(
                _Strings.splitThenStream(" 12 aBc ", "$")
                .collect(Collectors.joining("|")),
                is(" 12 aBc "));
    }

    @Test
    void splitThenStreamMultipleWithSeparatorAtBegin() throws Exception {
        assertThat(
                _Strings.splitThenStream("$ 1$2 a$Bc ", "$")
                .collect(Collectors.joining("|")),
                is("| 1|2 a|Bc "));
    }

    @Test
    void splitThenStreamMultipleWithSeparatorAtEnd() throws Exception {
        assertThat(
                _Strings.splitThenStream(" 1$2 a$Bc $", "$")
                .collect(Collectors.joining("|")),
                is(" 1|2 a|Bc |"));
    }

    @Test
    void splitThenStreamMultipleWithSeparatorsInSequence() throws Exception {
        assertThat(
                _Strings.splitThenStream(" 1$2 a$$Bc ", "$")
                .collect(Collectors.joining("|")),
                is(" 1|2 a||Bc "));
    }

    @Test
    void splitThenStreamWithnewLine() throws Exception {
        assertThat(
                _Strings.splitThenStream(" 1\n2 a\n\nBc ", "\n")
                .collect(Collectors.joining("|")),
                is(" 1|2 a||Bc "));
    }

    @Test
    void condenseWhitespacesWithNull() throws Exception {
        assertThat(
                _Strings.condenseWhitespaces(null,"|"),
                nullValue());
    }

    @Test
    void condenseWhitespaces() throws Exception {
        assertThat(
                _Strings.condenseWhitespaces("  12 aBc","|"),
                is("|12|aBc"));
    }

    // -- TO BYTE CONVERSION

    @Test
    void toByteConvertWithNull() throws Exception {
        assertThat(
                _Strings.toBytes(null, StandardCharsets.UTF_8),
                nullValue());
    }

    @Test
    void toByteConvertWithEmpty() throws Exception {
        assertArrayEquals(
                _Constants.emptyBytes,
                _Strings.toBytes("", StandardCharsets.UTF_8));
    }

    @Test
    void toByteConvert() throws Exception {
        assertArrayEquals(
                new byte[] {48,49,50,51},
                _Strings.toBytes("0123", StandardCharsets.UTF_8));
    }

    // -- FROM BYTE CONVERSION

    @Test
    void fromByteConvertWithNull() throws Exception {
        assertThat(
                _Strings.ofBytes(null, StandardCharsets.UTF_8),
                nullValue());
    }

    @Test
    void fromByteConvertWithEmpty() throws Exception {
        assertThat(
                _Strings.ofBytes(_Constants.emptyBytes, StandardCharsets.UTF_8),
                is(""));
    }

    @Test
    void fromByteConvert() throws Exception {
        assertThat(
                _Strings.ofBytes(new byte[] {48,49,50,51}, StandardCharsets.UTF_8),
                is("0123"));
    }

    // -- BYTE MANIPULATION

    @Test
    void convertIdentity() throws Exception {

        assertThat(
                _Strings.convert(null, _Bytes.operator(), StandardCharsets.UTF_8),
                nullValue());

        assertThat(
                _Strings.convert("0123", _Bytes.operator(), StandardCharsets.UTF_8),
                is("0123"));
    }

    // -- OPERATOR COMPOSITION

    @Test
    void composeIdentityWithNull() throws Exception {
        assertThat(
                _Strings.operator().apply(null),
                nullValue());
    }

    @Test
    void composeIdentity() throws Exception {
        assertThat(
                _Strings.operator().apply(" 12 aBc"),
                is(" 12 aBc"));
    }

    @Test
    void compose2WithNull() throws Exception {
        assertThat(
                _Strings.operator()
                .andThen(_Strings::lower)
                .apply(null),
                nullValue());
    }

    @Test
    void compose2() throws Exception {
        assertThat(
                _Strings.operator()
                .andThen(_Strings::lower)
                .apply(" 12 aBc"),
                is(" 12 abc"));
    }

    @Test
    void composeOperatorSequency_LastShouldWin() throws Exception {
        assertThat(
                _Strings.operator()
                .andThen(_Strings::lower)
                .andThen(_Strings::upper)
                .apply(" 12 aBc"),
                is(" 12 ABC"));
    }

    // -- BASE64

    @ParameterizedTest
    @ValueSource(strings = {
            "-12-abc !@#$%^&*()_-+={}[]<>,./?\\:\"",
            ""})
    void base64UrlZlibCompressedRoundtrip(final String input) throws Exception {
        assertThat(
                _Strings.base64UrlDecodeZlibCompressed(_Strings.base64UrlEncodeZlibCompressed(input)),
                is(input));
    }

    // -- SPECIAL COMPOSITES

    @Test
    void asLowerDashed() throws Exception {
        assertThat(
                _Strings.asLowerDashed
                .apply(" 12    aBc"),
                is("-12-abc"));
    }

    @Test
    void asNormalized() throws Exception {
        assertThat(
                _Strings.asNormalized
                .apply(" 12 a B         c"),
                is(" 12 a B c"));
    }

    @Test
    void asNaturalName2() throws Exception {
        assertThat(
                _Strings.asNaturalName
                .apply("NextAvailableDate"),
                is("Next Available Date"));
    }

}
