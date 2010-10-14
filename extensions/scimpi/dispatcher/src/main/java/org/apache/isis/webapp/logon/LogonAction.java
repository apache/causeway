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


package org.apache.isis.webapp.logon;

import java.io.IOException;

import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.webapp.Action;
import org.apache.isis.webapp.Dispatcher;
import org.apache.isis.webapp.UserManager;
import org.apache.isis.webapp.context.RequestContext;
import org.apache.isis.webapp.debug.DebugView;


// TODO this should work like EditAction so that logon page is repopulated
public class LogonAction implements Action {

    public void process(RequestContext context) throws IOException {
        String password = context.getParameter("password");
        String username = context.getParameter("username");
        AuthenticationSession session = UserManager.authenticate(new AuthenticationRequestPassword(username, password));

        String view;
        if (session == null) {
            view = context.getParameter("error");
        } else {
            context.setSession(session);
//            UserManager.logonUser(session);
            view = context.getParameter("view");
            if (view == null) {
                // REVIEW this is duplicated in Logon.java
                view = "start." + Dispatcher.EXTENSION;
            }
        }
        context.redirectTo(view);
    }

    public String getName() {
        return "logon";
    }

    public void init() {}

    public void debug(DebugView view) {}
}

