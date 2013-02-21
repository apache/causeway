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

package org.apache.isis.viewer.wicket.ui.pages.error;

import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Throwables;

/**
 * Web page representing the home page (showing a welcome message).
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class ErrorPage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_MESSAGE = "message";
    private static final String ID_STACK_TRACE = "stackTrace";

    public ErrorPage(Exception ex) {
        super(new PageParameters());
        add(new Label(ID_MESSAGE, ex.getMessage()));
        add(new Label(ID_STACK_TRACE, stackTraceAsString(ex)));
    }

    private static String stackTraceAsString(Throwable ex) {
        StringBuilder buf = new StringBuilder();
        appendStackTrace(ex, buf);
        Throwable cause = ex.getCause();
        while(cause != null) {
            buf.append("\n\nCaused by:\n");
            appendStackTrace(cause, buf);
            cause = cause.getCause();
        }
        return buf.toString();
    }

    private static void appendStackTrace(Throwable ex, StringBuilder buf) {
        for (StackTraceElement el : ex.getStackTrace()) {
            buf. append(el.getClassName())
                .append(el.getMethodName())
                .append("(")
                .append(el.getFileName())
                .append(":")
                .append(el.getLineNumber())
                .append(")\n")
                ;
        }
    }

}
