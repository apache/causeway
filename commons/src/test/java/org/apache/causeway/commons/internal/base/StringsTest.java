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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
                _Strings.StringOperator.identity().apply(null),
                nullValue());
    }

    @Test
    void composeIdentity() throws Exception {
        assertThat(
                _Strings.StringOperator.identity().apply(" 12 aBc"),
                is(" 12 aBc"));
    }

    @Test
    void compose2WithNull() throws Exception {
        assertThat(
                _Strings.StringOperator.identity()
                .andThen(_Strings::lower)
                .apply(null),
                nullValue());
    }

    @Test
    void compose2() throws Exception {
        assertThat(
                _Strings.StringOperator.identity()
                .andThen(_Strings::lower)
                .apply(" 12 aBc"),
                is(" 12 abc"));
    }

    @Test
    void composeOperatorSequency_LastShouldWin() throws Exception {
        assertThat(
                _Strings.StringOperator.identity()
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
    void asNormalized() throws Exception {
        assertNull(asNormalized(null)); // null
        assertThat(asNormalized(""), is("")); // empty string
        assertThat(asNormalized("yada Foobar"), is("yada Foobar")); // alreadyNormalized
        assertThat(asNormalized("Yada\tFoobar"), is("Yada Foobar")); // tab
        assertThat(asNormalized("Yada\t Foobar"), is("Yada Foobar")); // tab and space
        assertThat(asNormalized("Yada  foobar"), is("Yada foobar")); // two spaces
        assertThat(asNormalized("Yada\nfoobar"), is("Yada foobar")); // new line
        assertThat(asNormalized("Yada\n Foobar"), is("Yada Foobar")); // newline and space
        assertThat(asNormalized("Yada\r\n Foobar"), is("Yada Foobar")); // windows newline
        assertThat(asNormalized("Yada\r Foobar"), is("Yada Foobar")); // mac-os newline
        assertThat(asNormalized("Yada\r \tFoo \n\tbar  Baz"), is("Yada Foo bar Baz")); // multiple
        assertThat(asNormalized(" 12 a B         c"), is(" 12 a B c"));
    }
    private String asNormalized(final String string) {
        return _Strings.asNormalized.apply(string);
    }

    @Test
    void asNaturalName() throws Exception {
        assertThat(asNaturalName("NextAvailableDate"), is("Next Available Date"));
    }
    @Test
    void naturalNameAddsSpacesToCamelCaseWords() {
        assertEquals("Camel Case Word", asNaturalName("CamelCaseWord"));
    }
    @Test
    void naturalNameAddsSpacesBeforeNumbers() {
        assertEquals("One 2 One", asNaturalName("One2One"));
        assertEquals("Type 123", asNaturalName("Type123"));
        assertEquals("4321 Go", asNaturalName("4321Go"));
    }
    @Test
    void naturalNameRecognisesAcronymns() {
        assertEquals("TNT Power", asNaturalName("TNTPower"));
        assertEquals("Spam RAM Can", asNaturalName("SpamRAMCan"));
        assertEquals("DOB", asNaturalName("DOB"));
    }
    @Test
    void naturalNameWithShortNames() {
        assertEquals("At", asNaturalName("At"));
        assertEquals("I", asNaturalName("I"));
    }
    @Test
    void naturalNameNoChange() {
        assertEquals("Camel Case Word", asNaturalName("CamelCaseWord"));
        assertEquals("Almost Normal english sentence", asNaturalName("Almost Normal english sentence"));
    }
    private static String asNaturalName(final String string) {
        return _Strings.asNaturalName.apply(string);
    }

    @Test
    void asCamelCase() {
        assertThat(asCamelCase("An Upper Case"), is("AnUpperCase"));
        assertThat(asCamelCase("An_Upper.Case"), is("AnUpperCase"));
        assertThat(asCamelCase("a Lower Case"), is("aLowerCase"));
        assertThat(asCamelCase("a_Lower.Case"), is("aLowerCase"));
        // special use-case in org.apache.causeway.viewer.commons.model.components.UiComponentType#getId()
        assertThat(asCamelCase("OBJECT_EDIT".toLowerCase()), is("objectEdit"));
    }
    private String asCamelCase(final String string) {
        return _Strings.asCamelCase.apply(string);
    }

    @Test
    void asCamelCaseDecapitalized() {
        assertThat(asCamelCaseDecapitalized("An Upper Case"), is("anUpperCase"));
        assertThat(asCamelCaseDecapitalized("a Lower Case"), is("aLowerCase"));
        assertThat(asCamelCaseDecapitalized("AnUpperCase"), is("anUpperCase"));
        assertThat(asCamelCaseDecapitalized("aLowerCase"), is("aLowerCase"));
        assertThat(asCamelCaseDecapitalized("a  Lower  Case"), is("aLowerCase"));
    }
    private String asCamelCaseDecapitalized(final String string) {
        return _Strings.asCamelCaseDecapitalized.apply(string);
    }

    @Test
    void asLowerDashed() {
        assertThat(asLowerDashed(" 12    aBc"), is("-12-abc"));
        assertThat(asLowerDashed("An Upper Case"), is("an-upper-case"));
        assertThat(asLowerDashed("An   Upper   Case"), is("an-upper-case"));
        assertThat(asLowerDashed("An\nUpper\tCase"), is("an-upper-case"));
    }
    private String asLowerDashed(final String string) {
        return _Strings.asLowerDashed.apply(string);
    }

    // -- PREFIX/SUFFIX

    @Test
    void shouldStripIfThereIsOne() {
        assertThat(stripLeadingSlash("/foobar"), is("foobar"));
    }
    @Test
    void shouldLeaveUnchangedIfThereIsNone() {
        assertThat(stripLeadingSlash("foobar"), is("foobar"));
    }
    @Test
    void shouldConvertSolitarySlashToEmptyString() {
        assertThat(stripLeadingSlash("/"), is(""));
    }
    @Test
    void identityOnNull() {
        assertNull(stripLeadingSlash(null));
    }
    private static String stripLeadingSlash(final String input) {
        return _Strings.removePrefix(input, "/");
    }

}
