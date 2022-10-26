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
package org.apache.causeway.testing.unittestsupport.applib.matchers;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static org.hamcrest.CoreMatchers.nullValue;

/**
 * Hamcrest {@link org.hamcrest.Matcher} implementations.
 *
 * @since 2.0 {@index}
 */
public final class StringMatchers {

    private StringMatchers() { }

    public static Matcher<String> nonEmptyString() {
        return new TypeSafeMatcher<>() {
            @Override
            public boolean matchesSafely(final String str) {
                return str != null && str.length() > 0;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("a non empty string");
            }

        };
    }

    public static Matcher<String> nonEmptyStringOrNull() {
        return CoreMatchers.anyOf(nullValue(String.class), nonEmptyString());
    }

    public static Matcher<String> matches(final String regex) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("string matching " + regex);
            }

            @Override
            public boolean matchesSafely(final String str) {
                return str.matches(regex);
            }
        };
    }

    public static Matcher<String> startsWith(final String expected) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(" starts with '" + expected + "'");
            }

            @Override
            public boolean matchesSafely(String actual) {
                return actual.startsWith(expected);
            }
        };
    }

    public static Matcher<String> contains(final String expected) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(" contains '" + expected + "'");
            }

            @Override
            public boolean matchesSafely(String actual) {
                return actual.contains(expected);
            }
        };
    }


}
