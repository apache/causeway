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
package org.apache.causeway.applib.services.exceprecog;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import jakarta.inject.Inject;

import org.apache.causeway.applib.exceptions.TranslatableException;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract implementation of {@link ExceptionRecognizer} that looks
 * exceptions meeting the {@link Predicate} supplied in the constructor
 * and, if found anywhere in the causal chain,
 * then returns a non-null message indicating that the exception has been recognized.
 *
 * <p>
 * If a messaging-parsing {@link Function} is provided through the constructor,
 * then the message can be altered.  Otherwise the exception's {@link Throwable#getMessage() message} is returned as-is.
 *
 * @since 1.x {@index}
 */
@Slf4j
public abstract class ExceptionRecognizerAbstract implements ExceptionRecognizer {

    @Inject protected TranslationService translationService;

    @Getter @Setter private boolean disabled = false;

    /**
     * Convenience for subclass implementations that always return a fixed message.
     */
    protected static Function<String, String> constant(final String message) {
        return input -> message;
    }

    /**
     * Convenience for subclass implementations that always prefixes the exception message
     * with the supplied text
     */
    protected static Function<Throwable, String> prefix(final String prefix) {
        return rootCause->prefix + ": " + rootCause.getMessage();
    }

    private final Category category;
    private final Predicate<Throwable> predicate;
    private final Function<Throwable, String> rootCauseMessageFormatter;

    protected boolean logRecognizedExceptions;

    public ExceptionRecognizerAbstract(
            final Category category,
            final Predicate<Throwable> predicate,
            final Function<Throwable, String> rootCauseMessageFormatter) {
        Objects.requireNonNull(predicate);
        this.category = category;
        this.predicate = predicate;
        this.rootCauseMessageFormatter = rootCauseMessageFormatter != null
                ? rootCauseMessageFormatter
                : Throwable::getMessage;
    }

    public ExceptionRecognizerAbstract(
            final Predicate<Throwable> predicate,
            final Function<Throwable, String> rootCauseMessageFormatter) {
        this(Category.OTHER, predicate, rootCauseMessageFormatter);
    }

    public ExceptionRecognizerAbstract(
            final Category category,
            final Predicate<Throwable> predicate) {
        this(category, predicate, null);
    }

    public ExceptionRecognizerAbstract(
            final Predicate<Throwable> predicate) {
        this(Category.OTHER, predicate);
    }

    private Optional<String> recognizeRootCause(final Throwable ex) {

        return _Exceptions.streamCausalChain(ex)
        .filter(predicate)
        .map(throwable->{
            if(logRecognizedExceptions) {
                log.info("Recognized exception, stacktrace : ", throwable);
            }
            if(ex instanceof TranslatableException) {
                var translatableException = (TranslatableException) ex;
                var translatableMessage = translatableException.getTranslatableMessage();
                var translationContext = translatableException.getTranslationContext();
                if(translatableMessage != null
                        && translationContext != null) {
                    return translatableMessage.translate(translationService, translationContext);
                }
            }
            final String formattedMessage = _Exceptions.getRootCause(throwable)
                .map(rootCauseMessageFormatter::apply)
                .orElse("");
            return formattedMessage;
        })
        .filter(_NullSafe::isPresent)
        .findFirst();
    }

    @Override
    public Optional<Recognition> recognize(final Throwable ex) {
        if(disabled) {
            return Optional.empty();
        }
        return Recognition.of(category, recognizeRootCause(ex).orElse(null));
    }

}
