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

package org.apache.isis.core.commons.ensure;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

/**
 * Uses the {@link Matcher Hamcrest API} as a means of verifying arguments and
 * so on.
 */
public final class Ensure {

    private Ensure() {
    }

    /**
     * To ensure that the provided assertion is true
     * 
     * @throws IllegalArgumentException
     */
    public static void ensure(final String expectation, final boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException("illegal argument, expected: " + expectation);
        }
    }

    /**
     * To ensure that the provided argument is correct.
     * 
     * @see #ensureThatArg(Object, Matcher, String)
     * @see #ensureThatState(Object, Matcher, String)
     * @see #ensureThatContext(Object, Matcher)
     * 
     * @throws IllegalArgumentException
     *             if matcher does not {@link Matcher#matches(Object) match}.
     */
    public static <T> T ensureThatArg(final T object, final Matcher<T> matcher) {
        if (!matcher.matches(object)) {
            throw new IllegalArgumentException("illegal argument, expected: " + descriptionOf(matcher));
        }
        return object;
    }


    /**
     * To ensure that the provided argument is correct.
     * 
     * @see #ensureThatArg(Object, Matcher)
     * @see #ensureThatState(Object, Matcher, String)
     * @see #ensureThatContext(Object, Matcher)
     * 
     * @throws IllegalArgumentException
     *             if matcher does not {@link Matcher#matches(Object) match}.
     */
    public static <T> T ensureThatArg(final T arg, final Matcher<T> matcher, final String message) {
        if (!matcher.matches(arg)) {
            throw new IllegalArgumentException(message);
        }
        return arg;
    }

    /**
     * To ensure that the current state of this object (instance fields) is
     * correct.
     * 
     * @see #ensureThatArg(Object, Matcher)
     * @see #ensureThatContext(Object, Matcher)
     * @see #ensureThatState(Object, Matcher, String)
     * 
     * @throws IllegalStateException
     *             if matcher does not {@link Matcher#matches(Object) match}.
     */
    public static <T> T ensureThatState(final T field, final Matcher<T> matcher) {
        if (!matcher.matches(field)) {
            throw new IllegalStateException("illegal argument, expected: " + descriptionOf(matcher));
        }
        return field;
    }

    /**
     * To ensure that the current state of this object (instance fields) is
     * correct.
     * 
     * @see #ensureThatArg(Object, Matcher)
     * @see #ensureThatContext(Object, Matcher)
     * @see #ensureThatState(Object, Matcher)
     * 
     * @throws IllegalStateException
     *             if matcher does not {@link Matcher#matches(Object) match}.
     */
    public static <T> T ensureThatState(final T field, final Matcher<T> matcher, final String message) {
        if (!matcher.matches(field)) {
            throw new IllegalStateException(message);
        }
        return field;
    }

    /**
     * To ensure that the current context (<tt>IsisContext</tt>) is correct.
     * 
     * @see #ensureThatArg(Object, Matcher)
     * @see #ensureThatState(Object, Matcher)
     * @see #ensureThatContext(Object, Matcher, String)
     * 
     * @throws IllegalThreadStateException
     *             if matcher does not {@link Matcher#matches(Object) match}.
     */
    public static <T> T ensureThatContext(final T contextProperty, final Matcher<T> matcher) {
        if (!matcher.matches(contextProperty)) {
            throw new IllegalThreadStateException("illegal argument, expected: " + descriptionOf(matcher));
        }
        return contextProperty;
    }

    /**
     * To ensure that the current context (<tt>IsisContext</tt>) is correct.
     * 
     * @see #ensureThatArg(Object, Matcher)
     * @see #ensureThatState(Object, Matcher)
     * @see #ensureThatContext(Object, Matcher, String)
     * 
     * @throws IllegalThreadStateException
     *             if matcher does not {@link Matcher#matches(Object) match}.
     */
    public static <T> T ensureThatContext(final T contextProperty, final Matcher<T> matcher, final String message) {
        if (!matcher.matches(contextProperty)) {
            throw new IllegalThreadStateException(message);
        }
        return contextProperty;
    }

    private static <T> String descriptionOf(final Matcher<T> matcher) {
        final StringDescription stringDescription = new StringDescription();
        matcher.describeTo(stringDescription);
        final String description = stringDescription.toString();
        return description;
    }

}
