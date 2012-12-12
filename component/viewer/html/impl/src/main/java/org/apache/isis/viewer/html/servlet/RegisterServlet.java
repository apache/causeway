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

import com.google.common.base.Objects;

import org.apache.log4j.Logger;

import org.apache.isis.core.runtime.authentication.standard.RegistrationDetailsPassword;
import org.apache.isis.viewer.html.component.html.HtmlComponentFactory;
import org.apache.isis.viewer.html.component.html.RegisterFormPage;
import org.apache.isis.viewer.html.monitoring.servermonitor.Monitor;

public class RegisterServlet extends AbstractHtmlViewerServlet {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(RegisterServlet.class);

    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        // prompt
        final String user = request.getParameter("username");
        final String password = request.getParameter("password");
        final String password2 = request.getParameter("password2");
        if (user == null) {
            renderPrompt(response, "", "", "", "");
            return;
        }

        // register; re-prompt if required
        if (!Objects.equal(password, password2)) {
            renderPrompt(response, user, "", "", "passwords don't match");
            return;
        }

        // register; re-prompt if required
        final boolean registered = register(user, password, password2);
        if (!registered) {
            renderPrompt(response, user, "", "", "user name already taken");
            return;
        }

        // registered
        redirectToLogonPage(response, user);
    }

    private void redirectToLogonPage(final HttpServletResponse response, final String user) throws IOException {
        Monitor.addEvent("Web", "Logon - " + user);
        response.sendRedirect(pathTo(HtmlServletConstants.LOGON_PAGE));
    }

    private void renderPrompt(final HttpServletResponse response, final String user, final String password, final String password2, final String message) throws IOException {
        response.setContentType("text/html");
        final HtmlComponentFactory factory = new HtmlComponentFactory(getPathBuilder());
        final RegisterFormPage page = factory.createRegisterPage(user, password, message);
        page.write(response.getWriter());
    }

    private boolean register(final String user, final String password, final String password2) {
        return getAuthenticationManager().register(new RegistrationDetailsPassword(user, password));
    }

}
