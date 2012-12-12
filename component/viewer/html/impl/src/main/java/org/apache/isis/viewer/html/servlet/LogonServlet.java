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

package org.apache.isis.viewer.html.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.authentication.exploration.AuthenticationRequestExploration;
import org.apache.isis.core.runtime.authentication.standard.RegistrationDetailsPassword;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.webapp.IsisSessionFilter;
import org.apache.isis.core.webapp.auth.AuthenticationSessionStrategy;
import org.apache.isis.viewer.html.component.html.HtmlComponentFactory;
import org.apache.isis.viewer.html.component.html.LogonFormPage;
import org.apache.isis.viewer.html.context.Context;
import org.apache.isis.viewer.html.monitoring.servermonitor.Monitor;

public class LogonServlet extends AbstractHtmlViewerServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(LogonServlet.class);

    private AuthenticationSessionStrategy authenticationSessionStrategy;

    @Override
    public void init() throws ServletException {
        authenticationSessionStrategy = IsisSessionFilter.lookup(getServletConfig().getInitParameter(IsisSessionFilter.AUTHENTICATION_SESSION_STRATEGY_KEY));
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        // look for existing valid session
        final AuthenticationSession existingAuthSession = authenticationSessionStrategy.lookupValid(request, response);
        if (existingAuthSession != null) {
            redirectToStartPage(response, existingAuthSession.getUserName());
            return;
        }

        // prompt
        final String user = request.getParameter("username");
        final String password = request.getParameter("password");
        if (user == null && !getDeploymentType().isExploring()) {
            renderPrompt(response, "", "", null);
            return;
        }

        // authenticate; re-prompt if required
        final AuthenticationSession authSession = authenticate(user, password);
        if (authSession == null) {
            renderPrompt(response, user, password, "user/password invalid");
            return;
        }

        // authenticated
        authenticationSessionStrategy.bind(request, response, authSession);

        final Context context = new Context(getHtmlComponentFactory());
        context.setSession(authSession);
        authSession.setAttribute(HtmlServletConstants.AUTHENTICATION_SESSION_CONTEXT_KEY, context);

        LOG.info("created session");
        redirectToStartPage(response, user);
    }

    protected HtmlComponentFactory getHtmlComponentFactory() {
        return new HtmlComponentFactory(getPathBuilder());
    }

    private void redirectToStartPage(final HttpServletResponse response, final String user) throws IOException {
        Monitor.addEvent("Web", "Logon - " + user);
        response.sendRedirect(pathTo(HtmlServletConstants.START_PAGE));
    }

    private void renderPrompt(final HttpServletResponse response, final String user, final String password, final String error) throws IOException {
        response.setContentType("text/html");
        final HtmlComponentFactory factory = getHtmlComponentFactory();
        final boolean registerLink = getAuthenticationManager().supportsRegistration(RegistrationDetailsPassword.class);
        final LogonFormPage page = factory.createLogonPage(user, password, registerLink, error);
        page.write(response.getWriter());
    }

    protected AuthenticationSession authenticate(final String user, final String password) {
        AuthenticationRequest request;
        if (getDeploymentType() == DeploymentType.EXPLORATION) {
            request = new AuthenticationRequestExploration();
        } else {
            request = new AuthenticationRequestPassword(user, password);
        }
        return getAuthenticationManager().authenticate(request);
    }

}
