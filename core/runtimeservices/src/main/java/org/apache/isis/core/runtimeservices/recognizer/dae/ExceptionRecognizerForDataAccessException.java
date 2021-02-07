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
package org.apache.isis.core.runtimeservices.recognizer.dae;

import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerForType;
import org.apache.isis.core.config.IsisConfiguration;

/**
 * Common to those that can be disabled via IsisConfiguration.
 */
public abstract class ExceptionRecognizerForDataAccessException 
extends ExceptionRecognizerForType {

    protected ExceptionRecognizerForDataAccessException(
            final IsisConfiguration isisConfiguration,
            final Category category,
            final Predicate<Throwable> predicate,
            final Function<Throwable, String> rootCauseMessageFormatter) {
        super(category, predicate, rootCauseMessageFormatter);
        
        super.setDisabled(
                isisConfiguration.getCore().getRuntimeServices()
                .getExceptionRecognizer().getDae().isDisable());
    }

    protected ExceptionRecognizerForDataAccessException(
            final IsisConfiguration isisConfiguration,
            final Category category,
            final Class<? extends Exception> exceptionType,
            final Function<Throwable, String> rootCauseMessageFormatter) {
        this(isisConfiguration, category, ofType(exceptionType), rootCauseMessageFormatter);
    }

}
