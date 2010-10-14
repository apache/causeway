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


package org.apache.isis.webapp.view.logon;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.webapp.AbstractElementProcessor;
import org.apache.isis.webapp.processor.Request;
import org.apache.isis.webapp.view.form.HiddenInputField;
import org.apache.isis.webapp.view.form.InputField;
import org.apache.isis.webapp.view.form.InputForm;


public class Logon extends AbstractElementProcessor {

    public void process(Request request) {
        String view = request.getOptionalProperty(VIEW);
        if (view == null) {
            view = (String) request.getContext().getVariable("login-path");
        }

        String error = request.getOptionalProperty(ERRORS, request.getContext().getRequestedFile());
        List<HiddenInputField> hiddenFields = new ArrayList<HiddenInputField>();
        hiddenFields.add(new HiddenInputField(ERRORS, error));
        if (view != null) {
            hiddenFields.add(new HiddenInputField(VIEW, view));
        }

        InputField nameField = new InputField("username");
        nameField.setType(InputField.TEXT);
        nameField.setLabel("User Name");

        InputField passwordField = new InputField("password");
        passwordField.setType(InputField.PASSWORD);
        passwordField.setLabel("Password");

        InputField[] fields = new InputField[] { nameField, passwordField, };

        String legend = request.getOptionalProperty(LEGEND);
        String loginButtonTitle = request.getOptionalProperty(TITLE, "Log in");
        String className = request.getOptionalProperty(CLASS, "login");
        String  id = request.getOptionalProperty(ID);
        InputForm.createForm(request, "logon.app", loginButtonTitle, fields, hiddenFields.toArray(new HiddenInputField[hiddenFields.size()]), legend, className, id);
    }

    public String getName() {
        return "logon";
    }

}

