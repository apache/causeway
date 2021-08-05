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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * @since 2.0 {@index}
 */
public class ThrowableMatchers {

    ThrowableMatchers(){}

    /**
     * Matches when the exception's causal chain contains the given {@code type}
     * @param type
     */
    public static TypeSafeMatcher<Throwable> causedBy(final Class<? extends Throwable> type) {
        return new TypeSafeMatcher<Throwable>() {

            @Override
            protected boolean matchesSafely(final Throwable throwable) {
                return _Exceptions.getCausalChain(throwable) // non null result
                .stream()
                .anyMatch(t->t.getClass().equals(type));
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Caused by " + type.getName());
            }

        };
    }

    public Matcher<Throwable> causalChainHasMessageWith(final String messageFragment) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("causal chain has message with " + messageFragment);
            }

            @Override
            protected boolean matchesSafely(Throwable e) {
                for (Throwable ex : _Exceptions.getCausalChain(e)) {
                    if (ex.getMessage().contains(messageFragment)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

}
