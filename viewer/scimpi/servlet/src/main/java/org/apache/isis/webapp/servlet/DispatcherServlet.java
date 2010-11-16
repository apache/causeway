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

package org.apache.isis.webapp.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.webapp.Dispatcher;
import org.apache.isis.webapp.UserManager;
import org.apache.log4j.Logger;

public class DispatcherServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(DispatcherServlet.class);
    private Dispatcher dispatcher;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        LOG.info("POST " + request.getServletPath() + "  " + request.getQueryString());
        process(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOG.info("GET  " + request.getServletPath() + "  " + request.getQueryString());
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: NOF's AuthenticationSession now has its own #getAttribute() and #setAttribute() methods,
        // so an alternative is to bind this info onto the AuthSession.
        // The core/webapp will (hopefully) evolve to define a standard approach here...
        try {
            HttpSession httpSession = request.getSession(true);
            ServletRequestContext context = (ServletRequestContext) httpSession.getAttribute("scimpi-context");
            if (context == null || !context.isValid()) {
                context = new ServletRequestContext();
                httpSession.setAttribute("scimpi-context", context);
            }
            context.startRequest(request, response, getServletContext());
            dispatcher.process(context, request.getServletPath());
        } catch (RuntimeException e) {
            LOG.error("servlet exception", e);
            throw e;
        }

    }

    @Override
    public void init() throws ServletException {
        super.init();

        // TODO get directory from servlet parameter
        ImageLookup.setImageDirectory(getServletContext(), "images");

        dispatcher = new Dispatcher();
        Enumeration initParameterNames = getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String name = (String) initParameterNames.nextElement();
            String value = getInitParameter(name);
            dispatcher.addParameter(name, value);
        }
        String dir = getServletContext().getRealPath("/WEB-INF");
        dispatcher.init(dir);

        new UserManager(IsisContext.getAuthenticationManager());
    }
}
