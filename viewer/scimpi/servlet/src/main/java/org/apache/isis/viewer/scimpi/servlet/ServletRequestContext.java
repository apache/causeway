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
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.scimpi.dispatcher.DispatchException;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiNotFoundException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugView;


public class ServletRequestContext extends RequestContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;

    public void append(DebugView view) {
        view.divider("System");
     //   Runtime.getRuntime().

        view.divider("Request");
        view.appendRow("Auth type", request.getAuthType());
        view.appendRow("Character encoding", request.getCharacterEncoding());
        view.appendRow("Class", request.getClass());
        view.appendRow("Content type", request.getContentType());
        view.appendRow("Context path", getContextPath());
        view.appendRow("Locale", request.getLocale());
        view.appendRow("Method", request.getMethod());
        view.appendRow("Path info", request.getPathInfo());
        view.appendRow("Path translated", request.getPathTranslated());
        view.appendRow("Protocol", request.getProtocol());
        view.appendRow("Query string", request.getQueryString());
        view.appendRow("Remote host", request.getRemoteHost());
        view.appendRow("Remote user", request.getRemoteUser());
        view.appendRow("Real path", servletContext.getRealPath("/"));
        view.appendRow("Scheme", request.getScheme());
        view.appendRow("Server name", request.getServerName());
        view.appendRow("Servlet path", request.getServletPath());
        view.appendRow("Session", request.getSession());
        view.appendRow("Session ID", request.getRequestedSessionId());
        view.appendRow("URI", request.getRequestURI());
        view.appendRow("URL", request.getRequestURL());
        view.appendRow("User principle", request.getUserPrincipal());

        view.divider("Cookies");
        Cookie[] cookies = request.getCookies();
        for (int i = 0; i < cookies.length; i++) {
            view.appendRow(cookies[i].getName(), cookies[i].getValue());
        }

        Enumeration attributeNames = request.getAttributeNames();
        if (attributeNames.hasMoreElements()) {
            view.divider("Attributes");
            while (attributeNames.hasMoreElements()) {
                String name = (String) attributeNames.nextElement();
                view.appendRow(name, request.getAttribute(name));
            }
        }

        view.divider("Headers");
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            view.appendRow(name, request.getHeader(name));
        }

        view.divider("Parameters");
        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = (String) parameterNames.nextElement();
            view.appendRow(name, request.getParameter(name));
        }

        view.divider("Servlet Context");
        ServletContext context = getServletContext();
        view.appendRow("Name", context.getServletContextName());
        view.appendRow("Server Info", context.getServerInfo());
        view.appendRow("Version", context.getMajorVersion() + "." + context.getMinorVersion());
        view.appendRow("Attributes", getAttributes(context));
        view.appendRow("Init parameters", getParameters(context));
        view.appendRow("Real path", context.getRealPath("/"));

        super.append(view);
    }

    private String getAttributes(ServletContext context) {
        StringBuffer buf = new StringBuffer();
        Enumeration names = context.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            buf.append(name + "=" + context.getAttribute(name));
        }
        return buf.toString();
    }

    private String getParameters(ServletContext context) {
        StringBuffer buf = new StringBuffer();
        Enumeration names = context.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            buf.append(name + "=" + context.getInitParameter(name));
        }
        return buf.toString();
    }

    public void startRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        this.request = request;
        this.response = response;
        this.servletContext = servletContext;
        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = (String) parameterNames.nextElement();
            addParameter(name, request.getParameter(name));
        }
        
        
        
        // TODO move this
     //   response.sendError(403);
     //   response.setContentType("text/html");
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public PrintWriter getWriter() {
        try {
            return response.getWriter();
        } catch (IOException e) {
            throw new ScimpiException(e);
        }
    }

    public String findFile(String fileName) {
        try {
            if (getServletContext().getResource(fileName) == null) {
                return null;
            } else {
                return fileName;
            }
        } catch (MalformedURLException e) {
            throw new ScimpiException(e);
        }
    }

    public InputStream openStream(String path) {
        InputStream in = servletContext.getResourceAsStream(path);

        if (in == null) {
            servletContext.getResourcePaths("/");
            try {
                servletContext.getResource(path);
            } catch (MalformedURLException e) {
                throw new ScimpiException(e);
            }

            throw new ScimpiNotFoundException("Cannot find file " + path);
        }
        return in;
    }

    protected String getSessionId() {
        return request.getSession().getId();
    }

    public String clearSession() {
        request.getSession().invalidate();
        return null;
    }
    
    public void redirectTo(String view) {
        try {
            getResponse().sendRedirect(view);
        } catch (IOException e) {
            throw new DispatchException(e);
        }
    }
    
    public void setStatus(int status) {
        /*
        try {
            getResponse().sendError(status);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */
        getResponse().setStatus(status);
    }
    
    public void setContentType(String string) {
        getResponse().setContentType(string);
    }

    public String imagePath(ObjectAdapter object) {
        String contextPath = getContextPath();
        return ImageLookup.imagePath(object, contextPath);
    }
    
    public String imagePath(ObjectSpecification specification) {
        String contextPath = getContextPath();
        return ImageLookup.imagePath(specification, contextPath);
    }

    public String getContextPath() {
        return request.getContextPath();
    }
    
    public String getQueryString() {
        return request.getQueryString();
    }
    
    public String getUri() {
        return request.getRequestURI() + "?" + request.getQueryString();
    }
    
    public String getUrlBase() {
        //return request.getScheme() + request.getServerName() + request.getServerPort();
        StringBuffer url = request.getRequestURL();
        url.setLength(url.length() - request.getRequestURI().length());
        return url.toString();
    }
    
    public void addCookie(String name, String value, int minutesUtilExpires) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(minutesUtilExpires * 60);
        response.addCookie(cookie);
    }
    
    public String getCookie(String name) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if(cookies[i].getName().equals(name)) {
                    return cookies[i].getValue();
                }
            }
        }
        return null;
    }
}

