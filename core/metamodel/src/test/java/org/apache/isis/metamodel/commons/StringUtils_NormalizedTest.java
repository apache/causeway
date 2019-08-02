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

package org.apache.isis.metamodel.commons;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class StringUtils_NormalizedTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { null, null, }, // null
                { "", "", }, // empty string
                { "yada Foobar", "yada Foobar", }, // alreadyNormalized
                { "Yada\tFoobar", "Yada Foobar", }, // tab
                { "Yada\t Foobar", "Yada Foobar", }, // tab and space
                { "Yada  foobar", "Yada foobar", }, // two spaces
                { "Yada\nfoobar", "Yada foobar", }, // new line
                { "Yada\n Foobar", "Yada Foobar", }, // newline and space
                { "Yada\r\n Foobar", "Yada Foobar", }, // windows newline
                { "Yada\r Foobar", "Yada Foobar", }, // macos newline
                { "Yada\r \tFoo \n\tbar  Baz", "Yada Foo bar Baz", }, // multiple
        });
    }

    private final String input;
    private final String expected;

    public StringUtils_NormalizedTest(final String input, final String expected) {
        this.input = input;
        this.expected = expected;
    }

    @Test
    public void normalizesOk() {
        assertThat(StringExtensions.normalized(input), is(expected));
    }

}
