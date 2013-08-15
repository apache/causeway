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

package org.apache.isis.viewer.scimpi.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.UserManager;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugUsers;

public class DispatcherServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DispatcherServlet.class);
    private Dispatcher dispatcher;
    private DebugUsers debugUsers;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        LOG.info("http request from " + request.getRemoteAddr() + ": post " + request.getServletPath() + "?" + request.getQueryString()); 
        process(request, response);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        LOG.info("http request from " + request.getRemoteAddr() + ": get  " + request.getServletPath() + "?" + request.getQueryString()); 
        process(request, response);
    }

    private void process(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final ServletRequestContext context = new ServletRequestContext(debugUsers);
            context.startRequest(request, response, getServletContext());
            dispatcher.process(context, request.getServletPath());
        } catch (final RuntimeException e) {
            LOG.error("servlet exception", e);
            throw e;
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();

        // TODO get directory from servlet parameter
        ImageLookup.setImageDirectory(getServletContext(), "images");

        debugUsers = new DebugUsers();
        debugUsers.initialize();

        dispatcher = new Dispatcher();
        final Enumeration initParameterNames = getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            final String name = (String) initParameterNames.nextElement();
            final String value = getInitParameter(name);
            dispatcher.addParameter(name, value);
        }
        final String dir = getServletContext().getRealPath("/WEB-INF");
        dispatcher.init(dir, debugUsers);

        new UserManager(IsisContext.getAuthenticationManager());
    }
}
