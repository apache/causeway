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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
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
public class ExceptionRecognizerForType extends ExceptionRecognizerDelegating {

    private final static Predicate<Throwable> ofType(final Class<? extends Throwable> exceptionType) {
        return new Predicate<Throwable>() {
            @Override
            public boolean apply(Throwable input) {
                return exceptionType.isAssignableFrom(input.getClass());
            }
        };
    }

    public ExceptionRecognizerForType(final Class<? extends Exception> exceptionType, final Function<String,String> messageParser) {
        super(new ExceptionRecognizerGeneral(ofType(exceptionType), messageParser));
    }
    
    public ExceptionRecognizerForType(Class<? extends Exception> exceptionType) {
        this(exceptionType, null);
    }
}
