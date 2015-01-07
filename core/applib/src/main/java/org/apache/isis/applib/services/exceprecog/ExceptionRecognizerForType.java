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

import java.util.List;
import javax.jdo.JDODataStoreException;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import org.apache.isis.applib.annotation.Hidden;

/**
 * An specific implementation of {@link ExceptionRecognizer} that looks for an
 * exception of the type provided in the constructor
 * and, if found anywhere in the {@link Throwables#getCausalChain(Throwable) causal chain},
 * then returns a non-null message indicating that the exception has been recognized.
 * 
 * <p>
 * If a messaging-parsing {@link Function} is provided through the constructor,
 * then the message can be altered.  Otherwise the exception's {@link Throwable#getMessage() message} is returned as-is.
 */
@Hidden
public class ExceptionRecognizerForType extends ExceptionRecognizerAbstract {

    protected final static Predicate<Throwable> ofTypeExcluding(final Class<? extends Throwable> exceptionType, final String... messages) {
        return Predicates.and(ofType(exceptionType), excluding(messages));
    }

    protected final static Predicate<Throwable> ofTypeIncluding(final Class<? extends Throwable> exceptionType, final String... messages) {
        return Predicates.and(ofType(exceptionType), including(messages));
    }
    
    protected final static Predicate<Throwable> ofType(final Class<? extends Throwable> exceptionType) {
        return new Predicate<Throwable>() {
            @Override
            public boolean apply(Throwable input) {
                return exceptionType.isAssignableFrom(input.getClass());
            }
        };
    }
    
    /**
     * A {@link Predicate} that {@link Predicate#apply(Object) applies} only if the message(s)
     * supplied do <i>NOT</i> appear in the {@link Throwable} or any of its {@link Throwable#getCause() cause}s
     * (recursively).
     * 
     * <p>
     * Intended to prevent too eager matching of an overly general exception type.
     */
    protected final static Predicate<Throwable> excluding(final String... messages) {
        return new Predicate<Throwable>() {
            @Override
            public boolean apply(Throwable input) {
                final List<Throwable> causalChain = Throwables.getCausalChain(input);
                for (String message : messages) {
                    for (Throwable throwable : causalChain) {
                        final String throwableMessage = throwable.getMessage();
                        if(throwableMessage != null && throwableMessage.contains(message)) {
                            return false;
                        }
                        if(throwable instanceof JDODataStoreException) {
                            final JDODataStoreException jdoDataStoreException = (JDODataStoreException) throwable;
                            final Throwable[] nestedExceptions = jdoDataStoreException.getNestedExceptions();
                            for (Throwable nestedException : nestedExceptions) {
                                final String nestedThrowableMessage = nestedException.getMessage();
                                if(nestedThrowableMessage != null && nestedThrowableMessage.contains(message)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
                return true;
            }
        };
    }

    /**
     * A {@link Predicate} that {@link Predicate#apply(Object) applies} only if at least one of the message(s)
     * supplied <i>DO</i> appear in the {@link Throwable} or any of its {@link Throwable#getCause() cause}s
     * (recursively).
     * 
     * <p>
     * Intended to prevent more precise matching of a specific general exception type.
     */
    protected final static Predicate<Throwable> including(final String... messages) {
        return new Predicate<Throwable>() {
            @Override
            public boolean apply(Throwable input) {
                final List<Throwable> causalChain = Throwables.getCausalChain(input);
                for (String message : messages) {
                    for (Throwable throwable : causalChain) {
                        final String throwableMessage = throwable.getMessage();
                        if(throwableMessage != null && throwableMessage.contains(message)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    public ExceptionRecognizerForType(Category category, final Class<? extends Exception> exceptionType, final Function<String,String> messageParser) {
        this(category, ofType(exceptionType), messageParser);
    }
    
    public ExceptionRecognizerForType(Category category, final Predicate<Throwable> predicate, final Function<String,String> messageParser) {
        super(category, predicate, messageParser);
    }
    
    public ExceptionRecognizerForType(Category category, Class<? extends Exception> exceptionType) {
        this(category, exceptionType, null);
    }

    public ExceptionRecognizerForType(final Class<? extends Exception> exceptionType, final Function<String,String> messageParser) {
        this(Category.OTHER, exceptionType, messageParser);
    }

    public ExceptionRecognizerForType(final Predicate<Throwable> predicate, final Function<String,String> messageParser) {
        this(Category.OTHER, predicate, messageParser);
    }

    public ExceptionRecognizerForType(Class<? extends Exception> exceptionType) {
        this(Category.OTHER, exceptionType);
    }

}
