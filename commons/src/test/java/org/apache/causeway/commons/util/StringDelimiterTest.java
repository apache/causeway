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
package org.apache.causeway.commons.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.io.TextUtils;

class StringDelimiterTest {

    @Test
    void parsing() {

        var delimiter = TextUtils.delimiter(".");

        assertThatDelimiterEquals(delimiter, new String[0]);
        assertThatDelimiterEquals(delimiter.parse(null), new String[0]);
        assertThatDelimiterEquals(delimiter.parse(""), new String[0]);
        assertThatDelimiterEquals(delimiter.parse("a"), "a");

        // single prefix and/or suffix
        assertThatDelimiterEquals(delimiter.parse(".a"), "a");
        assertThatDelimiterEquals(delimiter.parse("a."), "a");
        assertThatDelimiterEquals(delimiter.parse(".a."), "a");

        // multi prefix and/or suffix
        assertThatDelimiterEquals(delimiter.parse("..a"), "a");
        assertThatDelimiterEquals(delimiter.parse("a.."), "a");
        assertThatDelimiterEquals(delimiter.parse("..a.."), "a");

        assertThatDelimiterEquals(delimiter.parse("a.b"), "a", "b");

        // multi infix
        assertThatDelimiterEquals(delimiter.parse("a..b"), "a", "b");
    }

    @Test
    void joining() {

        var delimiter = TextUtils.delimiter(".");

        assertThatDelimiterEquals(
                delimiter.parse(null).join(null), new String[0]);

        assertThatDelimiterEquals(
                delimiter.parse("a.b").join(null), "a", "b");

        assertThatDelimiterEquals(
                delimiter.parse(null).join(delimiter.parse("a.b")), "a", "b");

        assertThatDelimiterEquals(
                delimiter.join(delimiter.parse("a.b")), "a", "b");

        assertThatDelimiterEquals(
                delimiter.parse("a.b").join(delimiter.parse("c.d")), "a", "b", "c", "d");

        assertThatDelimiterEquals(
                delimiter.parse("a.b")
                    .join(delimiter.parse("c"))
                    .join(delimiter.parse("d")), "a", "b", "c", "d");
    }

    @Test
    void asDelimitedString() {
        var delimiter = TextUtils.delimiter(".");
        assertEquals("", delimiter.asDelimitedString());
        assertEquals("a.b", delimiter.parse("a.b").asDelimitedString());
    }

    @Test
    void withDelimiter() {
        var delimiter = TextUtils.delimiter(".");
        assertEquals("", delimiter.withDelimiter("/").asDelimitedString());
        assertEquals("a/b", delimiter.parse("a.b").withDelimiter("/").asDelimitedString());
    }

    @Test
    void toStringMethod() {
        var delimiter = TextUtils.delimiter(".");
        assertEquals("StringDelimiter[delimiter=.,elements=[]]", delimiter.toString());
        assertEquals("StringDelimiter[delimiter=.,elements=[a, b]]", delimiter.parse("a.b").toString());
    }

    // -- HELPER

    private static void assertThatDelimiterEquals(final TextUtils.StringDelimiter delimiter, final String...parts) {
        assertEquals(Can.ofArray(parts),
                delimiter.stream().collect(Can.toCan()));
    }

}
