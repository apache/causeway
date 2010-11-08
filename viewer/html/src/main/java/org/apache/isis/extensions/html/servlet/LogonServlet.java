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


package org.apache.isis.extensions.html.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.webapp.WebAppConstants;
import org.apache.isis.core.webapp.auth.AuthenticationSessionLookupStrategyDefault;
import org.apache.isis.extensions.html.component.html.HtmlComponentFactory;
import org.apache.isis.extensions.html.component.html.LogonFormPage;
import org.apache.isis.extensions.html.context.Context;
import org.apache.isis.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtime.authentication.AuthenticationRequest;
import org.apache.isis.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.runtime.authentication.standard.exploration.AuthenticationRequestExploration;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.internal.monitor.Monitor;


public class LogonServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(LogonServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
            IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
            IOException {
       
        
        AuthenticationSession authSession = new AuthenticationSessionLookupStrategyDefault().lookup(request, response);
        if (authSession != null) {
            boolean sessionValid = IsisContext.getAuthenticationManager().isSessionValid(authSession);
            if (sessionValid) {
                loggedIn(response, authSession.getUserName());
                return;
            }
        }

        String user = request.getParameter("username");
        final String password = request.getParameter("password");

        if (user == null && !IsisContext.getDeploymentType().isExploring()) {
            prompt(response, "", "", "");
            return;
        }

        authSession = authenticate(user, password);
        if (authSession == null) {
            prompt(response, user, password, "error");
            return;
        }

        final HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute(WebAppConstants.HTTP_SESSION_AUTHENTICATION_SESSION_KEY, authSession);

        final Context context = new Context(new HtmlComponentFactory());
        context.setSession(authSession);
        authSession.setAttribute(HtmlServletConstants.AUTHENTICATION_SESSION_CONTEXT_KEY, context);

        LOG.info("created session: " + httpSession);
        loggedIn(response, user);
    }

    private AuthenticationSession authenticate(String user, String password) {
        AuthenticationRequest request;
        if (IsisContext.getDeploymentType() == DeploymentType.EXPLORATION) { 
            request = new AuthenticationRequestExploration();
        }else {
            request = new AuthenticationRequestPassword(user, password);
        }
        return getAuthenticationManager().authenticate(request);
    }

    private void prompt(final HttpServletResponse response, final String user, final String password, final String message)
            throws IOException {
        response.setContentType("text/html");
        final HtmlComponentFactory factory = new HtmlComponentFactory();
        final LogonFormPage page = factory.createLogonPage(user, password);
        page.write(response.getWriter());
    }

    private void loggedIn(final HttpServletResponse response, final String user) throws IOException {
        Monitor.addEvent("Web", "Logon - " + user);
        response.sendRedirect("start.app");
    }

    // //////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////////

    private static AuthenticationManager getAuthenticationManager() {
        return IsisContext.getAuthenticationManager();
    }

}

