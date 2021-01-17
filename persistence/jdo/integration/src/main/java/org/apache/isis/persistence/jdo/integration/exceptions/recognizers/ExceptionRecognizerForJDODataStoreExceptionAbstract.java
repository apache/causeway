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
package org.apache.isis.persistence.jdo.integration.exceptions.recognizers;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerForType;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.val;

/**
 * common to those that can be disabled via IsisConfiguration
 */
abstract class ExceptionRecognizerForJDODataStoreExceptionAbstract extends ExceptionRecognizerForType {

    @Inject private IsisConfiguration isisConfiguration;

    protected ExceptionRecognizerForJDODataStoreExceptionAbstract(
            Category category,
            final Predicate<Throwable> predicate,
            final UnaryOperator<String> messageParser) {
        super(category, predicate, messageParser);
    }

    protected ExceptionRecognizerForJDODataStoreExceptionAbstract(
            Category category,
            final Class<? extends Exception> exceptionType,
            final UnaryOperator<String> messageParser) {
        this(category, ofType(exceptionType), messageParser);
    }

    @PostConstruct
    public void init() {
        val disabled = isisConfiguration
                .getCore().getRuntimeServices().getExceptionRecognizer().getJdo().isDisable();
        super.setDisabled(disabled);
    }


}
