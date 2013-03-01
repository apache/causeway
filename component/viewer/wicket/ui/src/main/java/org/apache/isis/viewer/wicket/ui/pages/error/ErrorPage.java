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

import java.io.Serializable;
import java.util.List;

import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Web page representing the home page (showing a welcome message).
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class ErrorPage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    private static final String MAIN_MESSAGE_IF_NOT_RECOGNIZED = "Sorry, an unexpected error occurred.";
    
    private static final String ID_MAIN_MESSAGE = "mainMessage";
    
    private static final String ID_EXCEPTION_DETAIL = "exceptionDetail";
    private static final String ID_EXCEPTION_MESSAGE = "exceptionMessage";
    
    private static final String ID_STACK_TRACE_ELEMENT = "stackTraceElement";
    private static final String ID_LINE = "stackTraceElementLine";

    private static final JavaScriptResourceReference DIV_TOGGLE_JS = new JavaScriptResourceReference(ErrorPage.class, "div-toggle.js");

    private static class Detail implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        enum Type {
            EXCEPTION_CLASS_NAME,
            EXCEPTION_MESSAGE,
            STACKTRACE_ELEMENT
        }
        private Type type;
        private String line;
        Detail(Type type, String line) {
            this.type = type;
            this.line = line;
        }
    }

    /**
     * For recognized messages.
     */
    public ErrorPage(String message, Exception ex) {
        this(message, null, asStackTrace(ex));
    }


    /**
     * For non-recognized messages.
     */
    public ErrorPage(Exception ex) {
        this(MAIN_MESSAGE_IF_NOT_RECOGNIZED, ex.getMessage(), asStackTrace(ex));
    }

    private ErrorPage(String mainMessage, String exceptionMessage, List<Detail> stackTraceDetail) {
        super(new PageParameters());
        
        addBookmarkedPages();

        add(new Label(ID_MAIN_MESSAGE, mainMessage).setEscapeModelStrings(false));
        MarkupContainer container = new WebMarkupContainer(ID_EXCEPTION_DETAIL);
        add(container);
        
        container.add(
                new Label(ID_EXCEPTION_MESSAGE, Strings.nullToEmpty(exceptionMessage)).setVisible(exceptionMessage != null));
        
        container.add(new ListView<Detail>(ID_STACK_TRACE_ELEMENT, stackTraceDetail) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(ListItem<Detail> item) {
                final Detail detail = item.getModelObject();
                Label label = new Label(ID_LINE, detail.line);
                item.add(new AttributeAppender("class", detail.type.name().toLowerCase()));
                item.add(label);
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptReferenceHeaderItem.forReference(DIV_TOGGLE_JS));
    }
    

    private static List<Detail> asStackTrace(Throwable ex) {
        List<Detail> stackTrace = Lists.newArrayList();
        List<Throwable> causalChain = Throwables.getCausalChain(ex);
        for(Throwable cause: causalChain) {
            stackTrace.add(new Detail(Detail.Type.EXCEPTION_CLASS_NAME, cause.getClass().getName()));
            stackTrace.add(new Detail(Detail.Type.EXCEPTION_MESSAGE, cause.getMessage()));
            addStackTraceElements(cause, stackTrace);
            cause = cause.getCause();
        }
        return stackTrace;
    }

    private static void addStackTraceElements(Throwable ex, List<Detail> stackTrace) {
        for (StackTraceElement el : ex.getStackTrace()) {
            StringBuilder buf = new StringBuilder();
            buf .append("    ")
                .append(el.getClassName())
                .append("#")
                .append(el.getMethodName())
                .append("(")
                .append(el.getFileName())
                .append(":")
                .append(el.getLineNumber())
                .append(")\n")
                ;
            stackTrace.add(new Detail(Detail.Type.STACKTRACE_ELEMENT, buf.toString()));
        }
    }
}
