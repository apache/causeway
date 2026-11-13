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
package org.apache.causeway.viewer.commons.model.error;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.causeway.applib.exceptions.UnrecoverableException;
import org.apache.causeway.applib.services.error.ErrorDetails;
import org.apache.causeway.applib.services.error.ErrorReportingService;
import org.apache.causeway.applib.services.error.Ticket;
import org.apache.causeway.applib.services.exceprecog.Recognition;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

/**
 * Three cases: authorization exception, else recognized, else or not recognized.
 *
 * <p> Supports a {@link ErrorReportingService} if configured, that optionally may return a {@link Ticket}
 * to represent the fact that the error has been recorded.
 */
public record ExceptionModel(
    ExceptionType exceptionType,
    String mainMessage,
    List<Throwable> causalChain) implements Serializable {

    private static final String MAIN_MESSAGE_IF_NOT_RECOGNIZED = "Sorry, an unexpected error occurred.";

    public enum ExceptionType {
        AUTHORIZATION_EXCEPTION,
        RECOGNIZED,
        NOT_RECOGNIZED
    }

    public static ExceptionModel create(
            final MetaModelContext commonContext,
            final Optional<Recognition> recognition,
            final Exception ex) {

        var translationService = commonContext.getTranslationService();
        var recognizedMessage = recognition.map($->$.toMessage(translationService));

        final ObjectMember.AuthorizationException authorizationException =
            causalChainOf(ex, ObjectMember.AuthorizationException.class);

        String mainMessage = null;
        ExceptionType exceptionType = null;

        if(authorizationException != null) {
            exceptionType = ExceptionType.AUTHORIZATION_EXCEPTION;
            mainMessage = authorizationException.getMessage();
        } else {
            if(recognizedMessage.isPresent()) {
                exceptionType = ExceptionType.RECOGNIZED;
                mainMessage = recognizedMessage.get();
            } else {
                exceptionType = ExceptionType.NOT_RECOGNIZED;
                // see if we can find a NonRecoverableException in the stack trace

                UnrecoverableException nonRecoverableException =
                    _Exceptions.streamCausalChain(ex)
                        .filter(UnrecoverableException.class::isInstance)
                        .map(UnrecoverableException.class::cast)
                        .findFirst()
                        .orElse(null);

                mainMessage = nonRecoverableException != null
                        ? nonRecoverableException.getMessage()
                        : MAIN_MESSAGE_IF_NOT_RECOGNIZED;
            }
        }

        return new ExceptionModel(exceptionType, mainMessage, _Exceptions.getCausalChain(ex));
    }

    public boolean isRecognized() {
        return exceptionType==ExceptionType.RECOGNIZED;
    }

    public String getMainMessage() {
        return mainMessage;
    }

    /**
     * Whether this was an authorization exception (so UI can suppress information, eg stack trace).
     */
    public boolean isAuthorizationException() {
        return exceptionType==ExceptionType.AUTHORIZATION_EXCEPTION;
    }

    public ErrorDetails asErrorDetails() {
        return asErrorDetails(new ErrorFormatter() {});
    }

    public ErrorDetails asErrorDetails(ErrorFormatter formatter) {
        return new ErrorDetails(
            mainMessage,
            isRecognized(),
            isAuthorizationException(),
            flatten(causalChain, formatter),
            causalChain.stream()
                .map(formatter::toLines)
                .toList());
    }

    // -- HELPER

    private List<String> flatten(List<Throwable> causalChain, ErrorFormatter formatter) {
        var lines = new ArrayList<String>();
        int count = 0;
        for(var throwable : causalChain) {
            if(count>0) {
                lines.addAll(formatter.chainJoiningLines());
            }
            lines.addAll(formatter.toLines(throwable));
            count++;
        }
        return lines;
    }

    private static <T extends Exception> T causalChainOf(final Exception ex, final Class<T> exType) {
        for (Throwable cause : _Exceptions.getCausalChain(ex)) {
            if(exType.isAssignableFrom(cause.getClass())) {
                return _Casts.uncheckedCast(cause);
            }
        }
        return null;
    }

}
