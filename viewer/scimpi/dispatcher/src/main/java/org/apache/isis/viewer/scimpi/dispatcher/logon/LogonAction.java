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


package org.apache.isis.viewer.scimpi.dispatcher.logon;

import java.io.IOException;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.runtimes.dflt.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.viewer.scimpi.dispatcher.Action;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.UserManager;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FieldEditState;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FormState;


// TODO this should work like EditAction so that logon page is repopulated
public class LogonAction implements Action {

    public void process(RequestContext context) throws IOException {
        String username = context.getParameter("username");
        String password = context.getParameter("password");
        AuthenticationSession session = UserManager.authenticate(new AuthenticationRequestPassword(username, password));

        String view;
        if (session == null) {
            FormState formState = new FormState();
            formState.setError("Failed to login. Check the username and ensure that your password was entered correctly");
            FieldEditState fieldState = formState.createField("username", username);
            if (username.length() == 0) {
                fieldState.setError("User Name required");
            }
            fieldState = formState.createField("password", password);
            if (password.length() == 0) {
                fieldState.setError("Password required");
            }
            if (username.length() == 0 || password.length() == 0) {
                formState.setError("Both the user name and password must be entered");
            }
            context.addVariable(ENTRY_FIELDS, formState, Scope.REQUEST);
            
            view = context.getParameter("error");
            context.setRequestPath("/" + view, Dispatcher.ACTION);
        } else {
            context.setSession(session);
            context.startHttpSession();
            view = context.getParameter("view");
            if (view == null) {
                // REVIEW this is duplicated in Logon.java
                view = "start." + Dispatcher.EXTENSION;
            }
            context.redirectTo(view);
        }
    }

    public String getName() {
        return "logon";
    }

    public void init() {}

    public void debug(DebugBuilder debug) {}
}

