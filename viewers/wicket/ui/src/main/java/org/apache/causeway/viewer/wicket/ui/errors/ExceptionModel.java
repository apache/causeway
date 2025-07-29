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
package org.apache.causeway.viewer.wicket.ui.errors;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.causeway.applib.exceptions.UnrecoverableException;
import org.apache.causeway.applib.services.error.ErrorDetails;
import org.apache.causeway.applib.services.exceprecog.Recognition;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

/**
 * Three cases: authorization exception, else recognized, else or not recognized.
 */
public record ExceptionModel(
    ExceptionType exceptionType,
    String mainMessage,
    List<StackTraceDetail> stackTraceDetailList,
    List<List<StackTraceDetail>> stackTraceDetailLists) implements Serializable {

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

        return new ExceptionModel(exceptionType, mainMessage, asStackTrace(ex), asStackTraces(ex));
    }


//    private Ticket ticket;
//    public Optional<Ticket> getTicket() {
//        return Optional.ofNullable(ticket);
//    }
//
//    /**
//     * Optionally called if an {@link ErrorReportingService} has been configured and returns a <tt>non-null</tt> ticket
//     * to represent the fact that the error has been recorded.
//     */
//    public void setTicket(final Ticket ticket) {
//        this.ticket = ticket;
//    }

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

    public List<StackTraceDetail> getStackTrace() {
        return stackTraceDetailList;
    }
    public List<List<StackTraceDetail>> getStackTraces() {
        return stackTraceDetailLists;
    }

    public ErrorDetails asErrorDetails(Function<StackTraceDetail, String> formatter) {
        final boolean recognized = isRecognized();
        final boolean authorizationException = isAuthorizationException();

        final List<String> stackDetailList = stackTraceDetailList
            .stream()
            .map(formatter)
            .toList();

        final List<List<StackTraceDetail>> stackTraces = getStackTraces();
        final List<List<String>> stackDetailLists = _Lists.newArrayList();
        for (List<StackTraceDetail> trace : stackTraces) {
            stackDetailLists.add(trace.stream()
                .map(formatter)
                .toList());
        }

        return new ErrorDetails(mainMessage, recognized, authorizationException, stackDetailList, stackDetailLists);
    }

    // -- HELPER

    private static <T extends Exception> T causalChainOf(final Exception ex, final Class<T> exType) {
        final List<Throwable> causalChain = _Exceptions.getCausalChain(ex);
        for (Throwable cause : causalChain) {
            if(exType.isAssignableFrom(cause.getClass())) {
                return _Casts.uncheckedCast(cause);
            }
        }
        return null;
    }

    private static List<StackTraceDetail> asStackTrace(final Throwable ex) {
        List<StackTraceDetail> stackTrace = _Lists.newArrayList();
        List<Throwable> causalChain = _Exceptions.getCausalChain(ex);
        boolean firstTime = true;
        for(Throwable cause: causalChain) {
            if(!firstTime) {
                stackTrace.add(StackTraceDetail.spacer());
                stackTrace.add(StackTraceDetail.causedBy());
                stackTrace.add(StackTraceDetail.spacer());
            } else {
                firstTime = false;
            }
            append(cause, stackTrace);
        }
        return stackTrace;
    }

    private static List<List<StackTraceDetail>> asStackTraces(final Throwable ex) {
        List<List<StackTraceDetail>> stackTraces = _Lists.newArrayList();

        List<Throwable> causalChain = _Exceptions.getCausalChain(ex);
        for(Throwable cause: causalChain) {
            List<StackTraceDetail> stackTrace = _Lists.newArrayList();
            append(cause, stackTrace);
            stackTraces.add(stackTrace);
        }
        return stackTraces;
    }

    private static void append(final Throwable cause, final List<StackTraceDetail> stackTrace) {
        stackTrace.add(StackTraceDetail.exceptionClassName(cause));
        stackTrace.add(StackTraceDetail.exceptionMessage(cause));
        for (StackTraceElement el : cause.getStackTrace()) {
            stackTrace.add(StackTraceDetail.element(el));
        }
    }

}
