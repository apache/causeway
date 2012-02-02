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
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
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
        if (view == null) {
            view = (String) request.getContext().getVariable("login-path");
        }

        final boolean isNotLoggedIn = IsisContext.getSession().getAuthenticationSession() instanceof AnonymousSession;
        if (isNotLoggedIn) {
            loginForm(request, view);
        }
    }

    public static void loginForm(final Request request, final String view) {
        // String message = (String)
        // request.getContext().examplegetVariable("login-failure");

        final String error = request.getOptionalProperty(ERROR, request.getContext().getRequestedFile());
        final List<HiddenInputField> hiddenFields = new ArrayList<HiddenInputField>();
        hiddenFields.add(new HiddenInputField(ERROR, error));
        if (view != null) {
            hiddenFields.add(new HiddenInputField(VIEW, view));
        }

        final FormState entryState = (FormState) request.getContext().getVariable(ENTRY_FIELDS);
        final InputField nameField = createdField("username", "User Name", InputField.TEXT, entryState);
        final String width = request.getOptionalProperty("width");
        if (width != null) {
            final int w = Integer.valueOf(width).intValue();
            nameField.setWidth(w);
        }
        final InputField passwordField = createdField("password", "Password", InputField.PASSWORD, entryState);
        final InputField[] fields = new InputField[] { nameField, passwordField, };

        final String formTitle = request.getOptionalProperty(FORM_TITLE);
        final String loginButtonTitle = request.getOptionalProperty(BUTTON_TITLE, "Log in");
        final String className = request.getOptionalProperty(CLASS, "login");
        final String id = request.getOptionalProperty(ID);

        HtmlFormBuilder.createForm(request, "logon.app", hiddenFields.toArray(new HiddenInputField[hiddenFields.size()]), fields, className, id, formTitle, null, null, loginButtonTitle, entryState == null ? null : entryState.getError(), null);
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
