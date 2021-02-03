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
package org.apache.isis.applib.services.exceprecog;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.apache.isis.commons.internal.exceptions._Exceptions.containsAnyOfTheseMessages;
import static org.apache.isis.commons.internal.exceptions._Exceptions.getCausalChain;

/**
 * A specific implementation of {@link ExceptionRecognizer} that looks for an
 * exception of the type provided in the constructor
 * and, if found anywhere in the causal chain,
 * then returns a non-null message indicating that the exception has been recognized.
 *
 * <p>
 * If a messaging-parsing {@link Function} is provided through the constructor,
 * then the message can be altered.  Otherwise the exception's {@link Throwable#getMessage() message} is returned as-is.
 *
 * @since 1.x {@index}
 */
public class ExceptionRecognizerForType extends ExceptionRecognizerAbstract {

    protected static final Predicate<Throwable> ofType(
            final Class<? extends Throwable> exceptionType) {

        return ex->exceptionType.isAssignableFrom(ex.getClass());
    }

    /**
     * A {@link Predicate} that {@link Predicate#test(Object) applies} only if the message(s)
     * supplied do <i>NOT</i> appear in the {@link Throwable} or any of its {@link Throwable#getCause() cause}s
     * (recursively).
     *
     * <p>
     * Intended to prevent too eager matching of an overly general exception type.
     */
    protected static final Predicate<Throwable> excluding(
            final String... messages) {

        return ex->{

            for (final Throwable throwable : getCausalChain(ex)) {
                if(containsAnyOfTheseMessages(throwable, messages)) {
                    return false;
                }
            }

            return true;
        };
    }


    /**
     * A {@link Predicate} that {@link Predicate#test(Object) applies} only if at least one of the message(s)
     * supplied <i>DO</i> appear in the {@link Throwable} or any of its {@link Throwable#getCause() cause}s
     * (recursively).
     *
     * <p>
     * Intended to prevent more precise matching of a specific general exception type.
     */
    protected static final Predicate<Throwable> including(
            final String... messages) {

        return ex->{

            for (final Throwable throwable : getCausalChain(ex)) {
                if(containsAnyOfTheseMessages(throwable, messages)) {
                    return true;
                }
            }

            return false;
        };
    }

    public ExceptionRecognizerForType(
            final Category category,
            final Class<? extends Exception> exceptionType,
            final UnaryOperator<String> messageParser) {
        this(category, ofType(exceptionType), messageParser);
    }

    public ExceptionRecognizerForType(
            final Category category,
            final Predicate<Throwable> predicate,
            final UnaryOperator<String> messageParser) {
        super(category, predicate, messageParser);
    }

    public ExceptionRecognizerForType(
            final Category category,
            final Class<? extends Exception> exceptionType) {
        this(category, exceptionType, null);
    }

    public ExceptionRecognizerForType(
            final Class<? extends Exception> exceptionType,
            final UnaryOperator<String> messageParser) {
        this(Category.OTHER, exceptionType, messageParser);
    }

    public ExceptionRecognizerForType(
            final Predicate<Throwable> predicate,
            final UnaryOperator<String> messageParser) {
        this(Category.OTHER, predicate, messageParser);
    }

    public ExceptionRecognizerForType(Class<? extends Exception> exceptionType) {
        this(Category.OTHER, exceptionType);
    }

}
