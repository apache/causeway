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

import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.webapp.AbstractElementProcessor;
import org.apache.isis.webapp.Dispatcher;
import org.apache.isis.webapp.processor.Request;


public class User extends AbstractElementProcessor {
    private static final String LOGIN_VIEW = "login-view";
    private static final String DEFAULT_LOGIN_VIEW = "login." + Dispatcher.EXTENSION;
    private static final String LOGOUT_VIEW = "logout-view";
    private static final String DEFAULT_LOGOUT_VIEW = "logout." + Dispatcher.EXTENSION;

    public void process(Request request) {
        boolean isLoggedIn = IsisContext.getSession() != null;
        request.appendHtml("<div class=\"user\">");
        if (isLoggedIn) {
            String user = request.getOptionalProperty(NAME);
            if (user == null) {
                user = IsisContext.getAuthenticationSession().getUserName();
            }
            request.appendHtml("Welcome <span class=\"name\">" + user + "</span>, ");
            String logoutView = request.getOptionalProperty(LOGOUT_VIEW, DEFAULT_LOGOUT_VIEW);
            request.appendHtml("<a class=\"link\" href=\"logout.app?view=" + logoutView + "\">Log out</a>");
        } else {
            String loginView = request.getOptionalProperty(LOGIN_VIEW, DEFAULT_LOGIN_VIEW);
            request.appendHtml("<a div class=\"link\" href=\"" + loginView + "\">Log in</a>");
        }
        request.appendHtml("</div>");
    }

    public String getName() {
        return "user";
    }

}

