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

import java.util.List;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import org.apache.isis.viewer.wicket.model.models.ModelAbstract;

public class ExceptionModel extends ModelAbstract<Exception> {

    private static final long serialVersionUID = 1L;

    private static final String MAIN_MESSAGE_IF_NOT_RECOGNIZED = "Sorry, an unexpected error occurred.";
    
    private Exception exception;
    private boolean recognized;

    private final String mainMessage;
    

    public static ExceptionModel create(String recognizedMessageIfAny, Exception ex) {
        return recognizedMessageIfAny != null
                ? new ExceptionModel(recognizedMessageIfAny, true, ex)
                : new ExceptionModel(MAIN_MESSAGE_IF_NOT_RECOGNIZED, false, ex);
    }

    private ExceptionModel(String mainMessage, boolean recognized, Exception ex) {
        this.mainMessage = mainMessage;
        this.recognized = recognized;
        this.exception = ex;
    }

    @Override
    protected Exception load() {
        return exception;
    }

    @Override
    public void setObject(Exception ex) {
        if(ex == null) {
            return;
        }
        this.exception = ex;
    }

    public boolean isRecognized() {
        return recognized;
    }

    public String getMainMessage() {
        return mainMessage;
    }
    
    public List<StackTraceDetail> getStackTrace() {
        return asStackTrace(exception);
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
