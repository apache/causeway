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
package org.apache.isis.testing.unittestsupport.applib.matchers;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Hamcrest {@link Matcher} implementations.
 *
 * @since 2.0 {@index}
 */
public final class ListMatchers {

    private ListMatchers() { }

    public static Matcher<List<?>> containsElementThat(final Matcher<?> elementMatcher) {
        return new TypeSafeMatcher<>() {
            @Override
            public boolean matchesSafely(final List<?> list) {
                for (final Object o : list) {
                    if (elementMatcher.matches(o)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("contains element that ").appendDescriptionOf(elementMatcher);
            }
        };
    }

    public static <T> Matcher<List<T>> sameContentsAs(final List<T> expected) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("same sequence as " + expected);
            }

            @Override
            public boolean matchesSafely(final List<T> actual) {
                return actual.containsAll(expected) && expected.containsAll(actual);
            }
        };
    }

    public static <T> Matcher<List<T>> listContaining(final T t) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("list containing ").appendValue(t);
            }

            @Override
            public boolean matchesSafely(List<T> arg0) {
                return arg0.contains(t);
            }
        };
    }

    @SafeVarargs
    public static <T> Matcher<List<T>> listContainingAll(final T... items) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("has items ").appendValue(items);

            }

            @Override
            public boolean matchesSafely(List<T> arg0) {
                return arg0.containsAll(Arrays.asList(items));
            }
        };
    }

    public static Matcher<List<Object>> containsObjectOfType(final Class<?> cls) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(final Description desc) {
                desc.appendText("contains instance of type " + cls.getName());
            }

            @Override
            public boolean matchesSafely(final List<Object> items) {
                for (final Object object : items) {
                    if (cls.isAssignableFrom(object.getClass())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }



}
