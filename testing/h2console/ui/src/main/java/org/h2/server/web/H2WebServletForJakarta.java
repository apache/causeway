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
package org.h2.server.web;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.h2.util.NetworkConnectionInfo;

/**
 * Adopted from {@link WebServlet} for Apache Causeway 3+.
 * <p>
 * Eventually replace with original WebServlet, once it supports Jakarta name-spaces.
 * @see WebServlet
 */
public class H2WebServletForJakarta extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private transient WebServer server;

    // -- MODIFICATIONS (OPEN UP API)

    public void setConnectionInfo(final ConnectionInfo connectionInfo) {
        server.updateSetting(connectionInfo);
    }

    public void setAllowOthers(final boolean b) {
        server.setAllowOthers(b);
    }

    public boolean getAllowOthers() {
        return server.getAllowOthers();
    }

    public void setAdminPassword(final String password) {
        server.setAdminPassword(password);
    }

    // --

    @Override
    public void init() {
        ServletConfig config = getServletConfig();
        Enumeration<?> en = config.getInitParameterNames();
        ArrayList<String> list = new ArrayList<>();
        while (en.hasMoreElements()) {
            String name = en.nextElement().toString();
            String value = config.getInitParameter(name);
            if (!name.startsWith("-")) {
                name = "-" + name;
            }
            list.add(name);
            if (!value.isEmpty()) {
                list.add(value);
            }
        }
        String[] args = list.toArray(new String[0]);
        server = new WebServer();
        server.setAllowChunked(false);
        server.init(args);
    }

    @Override
    public void destroy() {
        server.stop();
    }

    private boolean allow(final HttpServletRequest req) {
        if (server.getAllowOthers()) {
            return true;
        }
        String addr = req.getRemoteAddr();
        try {
            InetAddress address = InetAddress.getByName(addr);
            return address.isLoopbackAddress();
        } catch (UnknownHostException | NoClassDefFoundError e) {
            // Google App Engine does not allow java.net.InetAddress
            return false;
        }

    }

    private String getAllowedFile(final HttpServletRequest req, final String requestedFile) {
        if (!allow(req)) {
            return "notAllowed.jsp";
        }
        if (requestedFile.length() == 0) {
            return "index.do";
        }
        return requestedFile;
    }

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {
        req.setCharacterEncoding("utf-8");
        String file = req.getPathInfo();
        if (file == null) {
            resp.sendRedirect(req.getRequestURI() + "/");
            return;
        } else if (file.startsWith("/")) {
            file = file.substring(1);
        }
        file = getAllowedFile(req, file);

        // extract the request attributes
        Properties attributes = new Properties();
        Enumeration<?> en = req.getAttributeNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement().toString();
            String value = req.getAttribute(name).toString();
            attributes.put(name, value);
        }
        en = req.getParameterNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement().toString();
            String value = req.getParameter(name);
            attributes.put(name, value);
        }

        WebSession session = null;
        String sessionId = attributes.getProperty("jsessionid");
        if (sessionId != null) {
            session = server.getSession(sessionId);
        }
        WebApp app = new WebApp(server);
        app.setSession(session, attributes);
        String ifModifiedSince = req.getHeader("if-modified-since");

        String scheme = req.getScheme();
        StringBuilder builder = new StringBuilder(scheme).append("://").append(req.getServerName());
        int serverPort = req.getServerPort();
        if (!(serverPort == 80 && scheme.equals("http") || serverPort == 443 && scheme.equals("https"))) {
            builder.append(':').append(serverPort);
        }
        String path = builder.append(req.getContextPath()).toString();
        file = app.processRequest(file, new NetworkConnectionInfo(path, req.getRemoteAddr(), req.getRemotePort()));
        session = app.getSession();

        String mimeType = app.getMimeType();
        boolean cache = app.getCache();

        if (cache && server.getStartDateTime().equals(ifModifiedSince)) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        byte[] bytes = server.getFile(file);
        if (bytes == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            bytes = ("File not found: " + file).getBytes(StandardCharsets.UTF_8);
        } else {
            if (session != null && file.endsWith(".jsp")) {
                String page = new String(bytes, StandardCharsets.UTF_8);
                page = PageParser.parse(page, session.map);
                bytes = page.getBytes(StandardCharsets.UTF_8);
            }
            resp.setContentType(mimeType);
            if (!cache) {
                resp.setHeader("Cache-Control", "no-cache");
            } else {
                resp.setHeader("Cache-Control", "max-age=10");
                resp.setHeader("Last-Modified", server.getStartDateTime());
            }
        }
        if (bytes != null) {
            ServletOutputStream out = resp.getOutputStream();
            out.write(bytes);
        }
    }

    @Override
    public void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {
        doGet(req, resp);
    }

}

