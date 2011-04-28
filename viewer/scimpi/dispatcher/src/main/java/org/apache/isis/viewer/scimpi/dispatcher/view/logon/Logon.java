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

import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.UserlessSession;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FieldEditState;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FormState;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.HiddenInputField;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputField;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.HtmlFormBuilder;


public class Logon extends AbstractElementProcessor {

    public void process(Request request) {
        String view = request.getOptionalProperty(VIEW);
        if (view == null) {
            view = (String) request.getContext().getVariable("login-path");
        }

        boolean isNotLoggedIn = IsisContext.getSession().getAuthenticationSession() instanceof UserlessSession;
        if (isNotLoggedIn) {
            loginForm(request, view);
        }
    }

    public static void loginForm(Request request, String view) {
    //    String message = (String) request.getContext().examplegetVariable("login-failure");
           
        String error = request.getOptionalProperty(ERRORS, request.getContext().getRequestedFile());
        List<HiddenInputField> hiddenFields = new ArrayList<HiddenInputField>();
        hiddenFields.add(new HiddenInputField(ERRORS, error));
        if (view != null) {
            hiddenFields.add(new HiddenInputField(VIEW, view));
        }

        FormState entryState = (FormState) request.getContext().getVariable(ENTRY_FIELDS);
        InputField nameField = createdField("username", "User Name", InputField.TEXT, entryState);
        String width = request.getOptionalProperty("width");
        if (width != null) {
            int w = Integer.valueOf(width).intValue();
            nameField.setWidth(w);
        }
        InputField passwordField = createdField("password", "Password", InputField.PASSWORD, entryState); 
        InputField[] fields = new InputField[] { nameField, passwordField, };
   
        String formTitle = request.getOptionalProperty(FORM_TITLE);
        String loginButtonTitle = request.getOptionalProperty(BUTTON_TITLE, "Log in");
        String className = request.getOptionalProperty(CLASS, "action login full");
        String  id = request.getOptionalProperty(ID);
        
        HtmlFormBuilder.createForm(request, "logon.app", hiddenFields.toArray(new HiddenInputField[hiddenFields.size()]), fields,
                className, id, formTitle, null, null, loginButtonTitle, entryState == null ? null : entryState.getError());
    }

    protected static InputField createdField(String fieldName, String fieldLabel, int type, FormState entryState) {
        InputField nameField = new InputField(fieldName);
        nameField.setType(type);
        nameField.setLabel(fieldLabel);
        if (entryState != null) {
            FieldEditState fieldState = entryState.getField(fieldName);
            String entry = fieldState == null ? "" : fieldState.getEntry();
            nameField.setValue(entry);
            String error =  fieldState == null ? "" : fieldState.getError();
            nameField.setErrorText(error);
        }
        return nameField;
    }

    public String getName() {
        return "logon";
    }

}

