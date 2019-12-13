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

/**
 * Details of the error (obtained from the thrown exception), passed as part of the request to the
 * {@link ErrorReportingService}.
 *
 * <p>
 *     Implementation note: a class has been used here so that additional fields might be added in the future.
 * </p>
 */
public class ErrorDetails {

    private final String mainMessage;
    private final boolean recognized;
    private final boolean authorizationCause;
    private final List<String> stackTraceDetailListCombined;
    private final List<List<String>> stackDetailListPerCause;

    public ErrorDetails(
            final String mainMessage,
            final boolean recognized,
            final boolean authorizationCause,
            final List<String> stackTraceDetailListCombined,
            final List<List<String>> stackDetailListPerCause) {
        this.mainMessage = mainMessage;
        this.recognized = recognized;
        this.authorizationCause = authorizationCause;
        this.stackTraceDetailListCombined = stackTraceDetailListCombined;
        this.stackDetailListPerCause = stackDetailListPerCause;
    }

    public String getMainMessage() {
        return mainMessage;
    }

    public boolean isRecognized() {
        return recognized;
    }

    public boolean isAuthorizationCause() {
        return authorizationCause;
    }

    /**
     * @deprecated  - renamed to {@link #getStackTraceDetailCombined()}.
     */
    @Deprecated
    public List<String> getStackTraceDetailList() {
        return stackTraceDetailListCombined;
    }

    public List<String> getStackTraceDetailCombined() {
        return stackTraceDetailListCombined;
    }

    /**
     * One per exception cause.
     */
    public List<List<String>> getStackTraceDetailPerCause() {
        return stackDetailListPerCause;
    }
}
