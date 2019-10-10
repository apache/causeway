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

package org.apache.isis.commons.internal.assertions;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.apache.isis.commons.internal.base._With.requires;


/**
 * Utility for verifying arguments and so on.
 */
public final class _Ensure {

    private _Ensure() {
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
     * @throws IllegalArgumentException
     *             if predicate tests to false.
     * @deprecated might break build on OpenJDK-11 (or cross compilation builds in general)
     */
    @Deprecated
    public static <T> T ensureThatArg(
            final T arg, 
            final Predicate<? super T> predicate, 
            final Function<T, String> messageFunction) {
        
        requires(predicate, "predicate");
        if (!predicate.test(arg)) {
            requires(messageFunction, "messageFunction");
            throw new IllegalArgumentException(messageFunction.apply(arg));
        }
        return arg;
    }

//    public static <T> T ensureThatArg(final T arg, final Predicate<T> predicate, final Supplier<String> messageSupplier) {
//        requires(predicate, "predicate");
//        if (!predicate.test(arg)) {
//            requires(messageSupplier, "messageSupplier");
//            throw new IllegalArgumentException(messageSupplier.get());
//        }
//        return arg;
//    }

    /**
     * To ensure that the current state of this object (instance fields) is
     * correct.
     *
     * @throws IllegalStateException
     *             if predicate tests to false.
     */
    public static <T> T ensureThatState(final T field, final Predicate<? super T> predicate, final String message) {
        requires(predicate, "predicate");
        if (!predicate.test(field)) {
            throw new IllegalStateException(message);
        }
        return field;
    }

    /**
     * To ensure that the current context (<tt>IsisContext</tt>) is correct.
     *
     * @throws IllegalThreadStateException
     *             if predicate tests to false.
     */
    public static <T> T ensureThatContext(final T contextProperty, final Predicate<? super T> predicate, final String message) {
        requires(predicate, "predicate");
        if (!predicate.test(contextProperty)) {
            throw new IllegalThreadStateException(message);
        }
        return contextProperty;
    }


}
