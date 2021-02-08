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
package org.apache.isis.applib.services.error;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Details of the error (obtained from the thrown exception), passed as part of the request to the
 * {@link ErrorReportingService}.
 *
 * <p>
 *     Implementation note: a class has been used here so that additional fields might be added in the future.
 * </p>
 *
 * @since 2.0 {@index}
 */
@AllArgsConstructor
public class ErrorDetails {

    /**
     * The main message to be displayed to the end-user.
     *
     * <p>
     * The service is responsible for translating this into the language of the end-user (it can use xref:refguide:applib-svc:LocaleProvider.adoc[`LocaleProvider`] if required).
     * </p>
     *
     */
    @Getter
    private final String mainMessage;

    /**
     * Whether this message has already been recognized by an {@link org.apache.isis.applib.services.exceprecog.ExceptionRecognizer} implementation.
     *
     * <p>
     * Generally this converts potentially non-recoverable (fatal) exceptions into recoverable exceptions (warnings) as well providing an alternative mechanism for generating user-friendly error messages.
     * </p>
     */
    @Getter
    private final boolean recognized;

    /**
     * Whether the cause of the exception was due to a lack of privileges.
     *
     * <p>
     * In such cases the UI restricts the information shown to the end-user, to avoid leaking potentially sensitive information
     * </p>
     */
    @Getter
    private final boolean authorizationCause;

    /**
     * The stack trace of the exception, including the trace of any exceptions in the causal chain.
     *
     * <p>
     * These technical details are hidden from the user and only shown for non-recoverable exceptions.
     * </p>
     */
    @Getter
    private final List<String> stackTraceDetailListCombined;

    /**
     * The stack trace, broken out by exception cause.
     */
    @Getter
    private final List<List<String>> stackTraceDetailPerCause;

}
