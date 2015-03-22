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
package org.apache.isis.viewer.wicket.ui.errors;

import java.util.Iterator;
import java.util.List;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.wicket.model.models.ModelAbstract;

public class ExceptionModel extends ModelAbstract<List<StackTraceDetail>> {

    private static final long serialVersionUID = 1L;

    private static final String MAIN_MESSAGE_IF_NOT_RECOGNIZED = "Sorry, an unexpected error occurred.";
    
    private List<StackTraceDetail> stackTraceDetailList;
    private boolean recognized;
    private boolean authorizationCause;

    private final String mainMessage;

    public static ExceptionModel create(String recognizedMessageIfAny, Exception ex) {
        return new ExceptionModel(recognizedMessageIfAny, ex);
    }

    /**
     * Three cases: authorization exception, else recognized, else or not recognized.
     * @param recognizedMessageIfAny
     * @param ex
     */
    private ExceptionModel(String recognizedMessageIfAny, Exception ex) {

        final ObjectMember.AuthorizationException authorizationException = causalChainOf(ex, ObjectMember.AuthorizationException.class);
        if(authorizationException != null) {
            this.authorizationCause = true;
            this.mainMessage = authorizationException.getMessage();
        } else {
            this.authorizationCause = false;
            if(recognizedMessageIfAny != null) {
                this.recognized = true;
                this.mainMessage = recognizedMessageIfAny;
            } else {
                this.recognized =false;

                // see if we can find a NonRecoverableException in the stack trace
                Iterable<NonRecoverableException> appEx = Iterables.filter(Throwables.getCausalChain(ex), NonRecoverableException.class);
                Iterator<NonRecoverableException> iterator = appEx.iterator();
                NonRecoverableException nonRecoverableException = iterator.hasNext() ? iterator.next() : null;

                this.mainMessage = nonRecoverableException != null? nonRecoverableException.getMessage() : MAIN_MESSAGE_IF_NOT_RECOGNIZED;
            }
        }
        stackTraceDetailList = asStackTrace(ex);
    }


    @Override
    protected List<StackTraceDetail> load() {
        return stackTraceDetailList;
    }

    private static <T extends Exception> T causalChainOf(Exception ex, Class<T> exType) {

        final List<Throwable> causalChain = Throwables.getCausalChain(ex);
        for (Throwable cause : causalChain) {
            if(exType.isAssignableFrom(cause.getClass())) {
                return (T)cause;
            }
        }
        return null;
    }

    @Override
    public void setObject(List<StackTraceDetail> stackTraceDetail) {
        if(stackTraceDetail == null) {
            return;
        }
        this.stackTraceDetailList = stackTraceDetail;
    }

    public boolean isRecognized() {
        return recognized;
    }

    public String getMainMessage() {
        return mainMessage;
    }

    /**
     * Whether this was an authorization exception (so UI can suppress information, eg stack trace).
     */
    public boolean isAuthorizationException() {
        return authorizationCause;
    }


    public List<StackTraceDetail> getStackTrace() {
        return stackTraceDetailList;
    }

    private static List<StackTraceDetail> asStackTrace(Throwable ex) {
        List<StackTraceDetail> stackTrace = Lists.newArrayList();
        List<Throwable> causalChain = Throwables.getCausalChain(ex);
        for(Throwable cause: causalChain) {
            stackTrace.add(StackTraceDetail.exceptionClassName(cause));
            stackTrace.add(StackTraceDetail.exceptionMessage(cause));
            addStackTraceElements(cause, stackTrace);
            cause = cause.getCause();
        }
        return stackTrace;
    }

    private static void addStackTraceElements(Throwable ex, List<StackTraceDetail> stackTrace) {
        for (StackTraceElement el : ex.getStackTrace()) {
            stackTrace.add(StackTraceDetail.element(el));
        }
    }

}
