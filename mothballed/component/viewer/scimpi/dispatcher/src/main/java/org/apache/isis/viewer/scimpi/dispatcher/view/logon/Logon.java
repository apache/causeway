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

package org.apache.isis.viewer.scimpi.dispatcher.view.logon;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.authentication.AnonymousSession;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FieldEditState;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FormState;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.HiddenInputField;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.HtmlFormBuilder;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputField;

public class Logon extends AbstractElementProcessor {

    @Override
    public void process(final Request request) {
        String view = request.getOptionalProperty(VIEW);
        RequestContext context = request.getContext();
        if (view == null) {
            view = (String) context.getVariable("login-path");
        }

        final boolean isNotLoggedIn = IsisContext.getSession().getAuthenticationSession() instanceof AnonymousSession;
        if (isNotLoggedIn) {            
            loginForm(request, view);
        }
    }

    public static void loginForm(final Request request, final String view) {
        final String object = request.getOptionalProperty(OBJECT);
        final String method = request.getOptionalProperty(METHOD, "logon");
        final String result = request.getOptionalProperty(RESULT_NAME, "_user");
        final String resultScope = request.getOptionalProperty(SCOPE, Scope.SESSION.name());
        final String role = request.getOptionalProperty("field", "roles");
//        final String isisUser = request.getOptionalProperty("isis-user", "_web_default");
        final String formId = request.getOptionalProperty(FORM_ID, request.nextFormId());
        final String labelDelimiter = request.getOptionalProperty(LABEL_DELIMITER, ":");

        // TODO error if all values are not set (not if use type is not set and all others are still defaults);

        if (object != null) {
            RequestContext context = request.getContext();
            context.addVariable(LOGON_OBJECT, object, Scope.SESSION);
            context.addVariable(LOGON_METHOD, method, Scope.SESSION);
            context.addVariable(LOGON_RESULT_NAME, result, Scope.SESSION);
            context.addVariable(LOGON_SCOPE, resultScope, Scope.SESSION);
            context.addVariable(PREFIX + "roles-field", role, Scope.SESSION);
//            context.addVariable(PREFIX + "isis-user", isisUser, Scope.SESSION);
            context.addVariable(LOGON_FORM_ID, formId, Scope.SESSION);
        }
        
        final String error = request.getOptionalProperty(ERROR, request.getContext().getRequestedFile());
        final List<HiddenInputField> hiddenFields = new ArrayList<HiddenInputField>();
        hiddenFields.add(new HiddenInputField(ERROR, error));
        if (view != null) {
            hiddenFields.add(new HiddenInputField(VIEW, view));
        }
        hiddenFields.add(new HiddenInputField("_" + FORM_ID, formId));

        final FormState entryState = (FormState) request.getContext().getVariable(ENTRY_FIELDS);
        boolean isforThisForm = entryState != null && entryState.isForForm(formId);
        if (entryState != null && entryState.isForForm(formId)) {
        }
        final InputField nameField = createdField("username", "User Name", InputField.TEXT, isforThisForm ? entryState : null);
        final String width = request.getOptionalProperty("width");
        if (width != null) {
            final int w = Integer.valueOf(width).intValue();
            nameField.setWidth(w);
        }
        final InputField passwordField = createdField("password", "Password", InputField.PASSWORD, isforThisForm ? entryState : null);
        final InputField[] fields = new InputField[] { nameField, passwordField, };

        final String formTitle = request.getOptionalProperty(FORM_TITLE);
        final String loginButtonTitle = request.getOptionalProperty(BUTTON_TITLE, "Log in");
        final String className = request.getOptionalProperty(CLASS, "login");
        final String id = request.getOptionalProperty(ID, "logon");

        HtmlFormBuilder.createForm(request, "logon.app", hiddenFields.toArray(new HiddenInputField[hiddenFields.size()]), fields,
                className, id, formTitle, labelDelimiter, null, null, loginButtonTitle,
                isforThisForm && entryState != null ? entryState.getError() : null , null);        
    }

    protected static InputField createdField(final String fieldName, final String fieldLabel, final int type, final FormState entryState) {
        final InputField nameField = new InputField(fieldName);
        nameField.setType(type);
        nameField.setLabel(fieldLabel);
        if (entryState != null) {
            final FieldEditState fieldState = entryState.getField(fieldName);
            final String entry = fieldState == null ? "" : fieldState.getEntry();
            nameField.setValue(entry);
            final String error = fieldState == null ? "" : fieldState.getError();
            nameField.setErrorText(error);
        }
        return nameField;
    }

    @Override
    public String getName() {
        return "logon";
    }

}
