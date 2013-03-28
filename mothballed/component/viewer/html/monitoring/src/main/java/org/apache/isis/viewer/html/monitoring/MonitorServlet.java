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

package org.apache.isis.viewer.html.monitoring;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.isis.viewer.html.monitoring.servermonitor.MonitorListenerImpl;

public class MonitorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MonitorListenerImpl monitor;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String queryString = request.getQueryString();
        final String query = queryString == null ? "Overview" : URLDecoder.decode(queryString, "UTF-8");
        response.setContentType("text/html");
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream()));
        if (query.equals("Sessions")) {
            writer.println("<HTML><HEAD><TITLE>NOF System Monitor - " + "Sessions" + "</TITLE></HEAD>");
            writer.println("<BODY>");

            writer.println("<h1>" + "Sessions" + "</h1>");
            writer.println("<pre>");
            writer.println(listSessions());
            writer.println("</pre>");
            writer.println("</BODY></HTML>");
        } else {
            monitor.writeHtmlPage(query, writer);
        }
        writer.flush();
    }

    private static String listSessions() {
        final StringBuffer str = new StringBuffer();
        /*
         * final Iterator<?> it = SessionAccess.getSessions().iterator(); while
         * (it.hasNext()) { final HttpSession session = (HttpSession) it.next();
         * final String id = session.getId(); str.append(id); str.append(" \t");
         * 
         * final long creationTime = session.getCreationTime(); str.append(new
         * Date(creationTime)); str.append(" \t");
         * 
         * final long lastAccessedTime = session.getLastAccessedTime();
         * str.append(new Date(lastAccessedTime)); str.append(" \t");
         * 
         * final AuthenticationSession nofSession = (AuthenticationSession)
         * session.getAttribute("NOF_SESSION_ATTRIBUTE"); if (nofSession !=
         * null) { str.append(nofSession.getUserName()); }
         * 
         * str.append("\n"); }
         */
        return str.toString();
    }

    @Override
    public void init(final ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        monitor = new MonitorListenerImpl();
    }
}
