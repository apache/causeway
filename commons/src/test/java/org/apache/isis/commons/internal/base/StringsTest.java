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

package org.apache.isis.commons.internal.base;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.apache.isis.commons.internal._Constants;

class StringsTest {

    @Test
    public void isEmpty() throws Exception {
        assertThat(_Strings.isEmpty(" 12 aBc"), is(false));
        assertThat(_Strings.isEmpty(""), is(true));
        assertThat(_Strings.isEmpty(null), is(true));
    }

    @Test
    public void isNotEmpty() throws Exception {
        assertThat(_Strings.isNotEmpty(" 12 aBc"), is(true));
        assertThat(_Strings.isNotEmpty(""), is(false));
        assertThat(_Strings.isNotEmpty(null), is(false));
    }


    @Test
    public void lowerWithNull() throws Exception {
        assertThat(
                _Strings.lower(null), 
                nullValue());
    }

    @Test
    public void lowerMixed() throws Exception {
        assertThat(
                _Strings.lower("12aBc"), 
                is("12abc"));
    }


    @Test
    public void upperWithNull() throws Exception {
        assertThat(
                _Strings.upper(null), 
                nullValue());
    }

    @Test
    public void upperMixed() throws Exception {
        assertThat(
                _Strings.upper("12aBc"), 
                is("12ABC"));
    }

    @Test
    public void capitalizeWithNull() throws Exception {
        assertThat(
                _Strings.capitalize(null), 
                nullValue());
    }

    @Test
    public void capitalizeSize0() throws Exception {
        assertThat(
                _Strings.capitalize(""), 
                is(""));
    }

    @Test
    public void capitalizeSize1() throws Exception {
        assertThat(
                _Strings.capitalize("a"), 
                is("A"));
    }

    @Test
    public void capitalizeSize2() throws Exception {
        assertThat(
                _Strings.capitalize("ab"), 
                is("Ab"));
    }

    @Test
    public void trimWithNull() throws Exception {
        assertThat(
                _Strings.trim(null), 
                nullValue());
    }

    @Test
    public void trimMixed() throws Exception {
        assertThat(
                _Strings.trim(" 12 aBc"), 
                is("12 aBc"));
    }

    @Test
    public void splitThenStreamWithNull() throws Exception {
        assertThat(
                _Strings.splitThenStream(null, "$")
                .collect(Collectors.joining("|")),
                is(""));
    }

    @Test
    public void splitThenStreamSingle() throws Exception {
        assertThat(
                _Strings.splitThenStream(" 12 aBc ", "$")
                .collect(Collectors.joining("|")),
                is(" 12 aBc "));
    }

    @Test
    public void splitThenStreamMultipleWithSeparatorAtBegin() throws Exception {
        assertThat(
                _Strings.splitThenStream("$ 1$2 a$Bc ", "$")
                .collect(Collectors.joining("|")),
                is("| 1|2 a|Bc "));
    }

    @Test
    public void splitThenStreamMultipleWithSeparatorAtEnd() throws Exception {
        assertThat(
                _Strings.splitThenStream(" 1$2 a$Bc $", "$")
                .collect(Collectors.joining("|")),
                is(" 1|2 a|Bc |"));
    }

    @Test
    public void splitThenStreamMultipleWithSeparatorsInSequence() throws Exception {
        assertThat(
                _Strings.splitThenStream(" 1$2 a$$Bc ", "$")
                .collect(Collectors.joining("|")),
                is(" 1|2 a||Bc "));
    }
    
    @Test
    public void splitThenStreamWithnewLine() throws Exception {
        assertThat(
                _Strings.splitThenStream(" 1\n2 a\n\nBc ", "\n")
                .collect(Collectors.joining("|")),
                is(" 1|2 a||Bc "));
    }

    @Test
    public void condenseWhitespacesWithNull() throws Exception {
        assertThat(
                _Strings.condenseWhitespaces(null,"|"), 
                nullValue());
    }

    @Test
    public void condenseWhitespaces() throws Exception {
        assertThat(
                _Strings.condenseWhitespaces("  12 aBc","|"), 
                is("|12|aBc"));
    }

    // -- TO BYTE CONVERSION

    @Test
    public void toByteConvertWithNull() throws Exception {
        assertThat(
                _Strings.toBytes(null, StandardCharsets.UTF_8), 
                nullValue());
    }

    @Test
    public void toByteConvertWithEmpty() throws Exception {
        assertArrayEquals(
                _Constants.emptyBytes,
                _Strings.toBytes("", StandardCharsets.UTF_8));
    }

    @Test
    public void toByteConvert() throws Exception {
        assertArrayEquals(
                new byte[] {48,49,50,51},
                _Strings.toBytes("0123", StandardCharsets.UTF_8));
    }

    // -- FROM BYTE CONVERSION

    @Test
    public void fromByteConvertWithNull() throws Exception {
        assertThat(
                _Strings.ofBytes(null, StandardCharsets.UTF_8), 
                nullValue());
    }

    @Test
    public void fromByteConvertWithEmpty() throws Exception {
        assertThat(
                _Strings.ofBytes(_Constants.emptyBytes, StandardCharsets.UTF_8), 
                is(""));
    }

    @Test
    public void fromByteConvert() throws Exception {
        assertThat(
                _Strings.ofBytes(new byte[] {48,49,50,51}, StandardCharsets.UTF_8), 
                is("0123"));
    }

    // -- BYTE MANIPULATION

    @Test
    public void convertIdentity() throws Exception {

        assertThat(
                _Strings.convert(null, _Bytes.operator(), StandardCharsets.UTF_8), 
                nullValue());

        assertThat(
                _Strings.convert("0123", _Bytes.operator(), StandardCharsets.UTF_8), 
                is("0123"));
    }

    // -- OPERATOR COMPOSITION

    @Test
    public void composeIdentityWithNull() throws Exception {
        assertThat(
                _Strings.operator().apply(null), 
                nullValue());
    }

    @Test
    public void composeIdentity() throws Exception {
        assertThat(
                _Strings.operator().apply(" 12 aBc"), 
                is(" 12 aBc"));
    }

    @Test
    public void compose2WithNull() throws Exception {
        assertThat(
                _Strings.operator()
                .andThen(_Strings::lower)
                .apply(null), 
                nullValue());
    }

    @Test
    public void compose2() throws Exception {
        assertThat(
                _Strings.operator()
                .andThen(_Strings::lower)
                .apply(" 12 aBc"), 
                is(" 12 abc"));
    }

    @Test
    public void composeOperatorSequency_LastShouldWin() throws Exception {
        assertThat(
                _Strings.operator()
                .andThen(_Strings::lower)
                .andThen(_Strings::upper)
                .apply(" 12 aBc"), 
                is(" 12 ABC"));
    }

    // -- SPECIAL COMPOSITES

    @Test
    public void asLowerDashed() throws Exception {
        assertThat(
                _Strings.asLowerDashed
                .apply(" 12    aBc"), 
                is("-12-abc"));
    }

    @Test
    public void asNormalized() throws Exception {
        assertThat(
                _Strings.asNormalized
                .apply(" 12 a B         c"), 
                is(" 12 a B c"));
    }

    @Test
    public void asNaturalName2() throws Exception {
        assertThat(
                _Strings.asNaturalName2
                .apply("NextAvailableDate"), 
                is("Next Available Date"));
    }


}
