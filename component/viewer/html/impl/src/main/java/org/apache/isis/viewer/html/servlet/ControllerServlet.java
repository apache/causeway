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
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.viewer.html.PathBuilder;
import org.apache.isis.viewer.html.component.Page;
import org.apache.isis.viewer.html.context.Context;
import org.apache.isis.viewer.html.request.Request;
import org.apache.isis.viewer.html.request.ServletRequest;
import org.apache.isis.viewer.html.servlet.internal.WebController;

public class ControllerServlet extends AbstractHtmlViewerServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ControllerServlet.class);

    private String encoding;
    private WebController controller;

    // //////////////////////////////////////////////////////////////////
    // init
    // //////////////////////////////////////////////////////////////////

    @Override
    public void init(final ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        encoding =
            getConfiguration().getString(HtmlServletConstants.ENCODING_KEY, HtmlServletConstants.ENCODING_DEFAULT);

        controller = getWebController(getPathBuilder());

        final boolean debugEnabled = getConfiguration().getBoolean(HtmlServletConstants.DEBUG_KEY);
        controller.setDebug(debugEnabled);

        controller.init();
    }

    // Don't remove this - It allows other implementations of HtmlViewer to replace the WebController
    protected WebController getWebController(PathBuilder pathBuilder) {
        if (controller == null) {
            controller = new WebController(getPathBuilder());
        }
        return controller;
    }

    // //////////////////////////////////////////////////////////////////
    // doGet, doPost
    // //////////////////////////////////////////////////////////////////

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
        request.setCharacterEncoding(encoding);
        processRequest(request, response);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
        IOException {
        processRequest(request, response);
    }

    private void processRequest(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
        LOG.info("request: " + request.getServletPath() + "?" + request.getQueryString());

        final Request req = new ServletRequest(request);

        if (req.getRequestType() == null) {
            throw new ServletException("No action specified");
        } else if (!controller.actionExists(req)) {
            throw new ServletException("No such action " + req.getRequestType());
        } else {
            try {
                final Context context = getContextForRequest(request);
                processRequest(request, response, req, context);
            } catch (final Exception e) {
                LOG.error("exception during request handling", e);
                throw new ServletException("Internal exception", e);
            }
        }
    }

    private Context getContextForRequest(final HttpServletRequest request) {
        final AuthenticationSession authSession = getAuthenticationSession();
        Context context = (Context) authSession.getAttribute(HtmlServletConstants.AUTHENTICATION_SESSION_CONTEXT_KEY);
        if (context == null || !context.isValid()) {
            context = new Context(getHtmlComponentFactory());
            authSession.setAttribute(HtmlServletConstants.AUTHENTICATION_SESSION_CONTEXT_KEY, context);
        }
        return context;
    }

    private void processRequest(final HttpServletRequest request, final HttpServletResponse response,
        final Request req, final Context context) throws IOException, ServletException {
        response.setContentType("text/html");
        response.setCharacterEncoding(encoding);
        // no need to check if logged in; the IsisSessionFilter would
        // have prevented us from getting here.

        final Page page = controller.generatePage(context, req);
        if (context.isValid()) {
            if (controller.isDebug()) {
                controller.addDebug(page, req);
                addDebug(request, page);
            }
            PrintWriter writer;
            writer = response.getWriter();
            page.write(writer);
        } else {
            response.sendRedirect(getLogonPage());
        }
    }

    protected String getLogonPage() {
        return pathTo(HtmlServletConstants.LOGON_PAGE);
    }

    private void addDebug(final HttpServletRequest request, final Page page) {
        page.addDebug("Servlet path", request.getServletPath());
        page.addDebug("Query string", request.getQueryString());
        page.addDebug("Context path", request.getContextPath());
        page.addDebug("Path info", request.getPathInfo());
    }

}
