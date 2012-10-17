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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.scimpi.dispatcher.DispatchException;
import org.apache.isis.viewer.scimpi.dispatcher.ErrorCollator;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiNotFoundException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugUsers;

public class ServletRequestContext extends RequestContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;
    private boolean isAborted;

    public ServletRequestContext(final DebugUsers debugUsers) {
        super(debugUsers);
    }

    public void append(final DebugBuilder view) {
        super.append(view);

        /*
         * view.divider("System"); Runtime.getRuntime().
         */
        view.startSection("HTTP Serviet Request");
        view.appendTitle("General");
        view.appendln("Auth type", request.getAuthType());
        view.appendln("Character encoding", request.getCharacterEncoding());
        view.appendln("Class", request.getClass());
        view.appendln("Content type", request.getContentType());
        view.appendln("Context path", getContextPath());
        view.appendln("Locale", request.getLocale());
        view.appendln("Method", request.getMethod());
        view.appendln("Path info", request.getPathInfo());
        view.appendln("Path translated", request.getPathTranslated());
        view.appendln("Protocol", request.getProtocol());
        view.appendln("Query string", request.getQueryString());
        view.appendln("Remote host", request.getRemoteHost());
        view.appendln("Remote user", request.getRemoteUser());
        view.appendln("Real path", servletContext.getRealPath("/"));
        view.appendln("Scheme", request.getScheme());
        view.appendln("Server name", request.getServerName());
        view.appendln("Servlet path", request.getServletPath());
        view.appendln("Session", request.getSession());
        view.appendln("Session ID", request.getRequestedSessionId());
        view.appendln("URI", request.getRequestURI());
        view.appendln("URL", request.getRequestURL());
        view.appendln("User principle", request.getUserPrincipal());

        
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            view.appendTitle("Cookies");
            for (final Cookie cookie : cookies) {
                view.appendln(cookie.getName(), cookie.getValue());
            }
        }
        
        final Enumeration attributeNames = request.getAttributeNames();
        if (attributeNames.hasMoreElements()) {
            view.appendTitle("Attributes");
            while (attributeNames.hasMoreElements()) {
                final String name = (String) attributeNames.nextElement();
                view.appendln(name, request.getAttribute(name));
            }
        }

        view.appendTitle("Headers");
        final Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = (String) headerNames.nextElement();
            view.appendln(name, request.getHeader(name));
        }

        view.appendTitle("Parameters");
        final Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = (String) parameterNames.nextElement();
            view.appendln(name, request.getParameter(name));
        }

        view.appendTitle("Servlet Context");
        final ServletContext context = getServletContext();
        view.appendln("Name", context.getServletContextName());
        view.appendln("Server Info", context.getServerInfo());
        view.appendln("Version", context.getMajorVersion() + "." + context.getMinorVersion());
        view.appendln("Attributes", getAttributes(context));
        view.appendln("Init parameters", getParameters(context));
        view.appendln("Real path", context.getRealPath("/"));
    }

    private String getAttributes(final ServletContext context) {
        final StringBuffer buf = new StringBuffer();
        final Enumeration names = context.getAttributeNames();
        while (names.hasMoreElements()) {
            final String name = (String) names.nextElement();
            buf.append(name + "=" + context.getAttribute(name));
        }
        return buf.toString();
    }

    private String getParameters(final ServletContext context) {
        final StringBuffer buf = new StringBuffer();
        final Enumeration names = context.getInitParameterNames();
        while (names.hasMoreElements()) {
            final String name = (String) names.nextElement();
            buf.append(name + "=" + context.getInitParameter(name));
        }
        return buf.toString();
    }

    public void startRequest(final HttpServletRequest request, final HttpServletResponse response, final ServletContext servletContext) {
        this.request = request;
        this.response = response;
        this.servletContext = servletContext;
        final Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = (String) parameterNames.nextElement();
            addParameter(name, request.getParameter(name));
        }
        initSession();
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

    @Override
    public PrintWriter getWriter() {
        try {
            return response.getWriter();
        } catch (final IOException e) {
            throw new ScimpiException(e);
        }
    }

    @Override
    public String findFile(final String fileName) {
        try {
            if (getServletContext().getResource(fileName) == null) {
                return null;
            } else {
                return fileName;
            }
        } catch (final MalformedURLException e) {
            throw new ScimpiException(e);
        }
    }

    @Override
    public String getErrorDetails() {
        return (String) getRequest().getAttribute("com.planchaser.error.details");
    }

    @Override
    public String getErrorMessage() {
        return (String) getRequest().getAttribute("com.planchaser.error.message");
    }

    @Override
    public String getErrorReference() {
        return (String) getRequest().getAttribute("com.planchaser.error.reference");
    }

    @Override
    public InputStream openStream(final String path) {
        final InputStream in = servletContext.getResourceAsStream(path);

        if (in == null) {
            servletContext.getResourcePaths("/");
            try {
                servletContext.getResource(path);
            } catch (final MalformedURLException e) {
                throw new ScimpiException(e);
            }

            throw new ScimpiNotFoundException("Cannot find file " + path);
        }
        return in;
    }
    
    @Override
    public void startHttpSession() {
        addVariable("_auth_session", getSession(), Scope.SESSION); 
    }

    private void initSession(){
        final HttpSession httpSession = request.getSession(true);
        // TODO when using version 3.0 of Servlet API use the HttpOnly setting for improved security
        if (httpSession.getAttribute("scimpi-context") == null) {
            final Map<String, Object> sessionData = getSessionData();
            httpSession.setAttribute("scimpi-context", sessionData);
        } else {
            final HashMap<String, Object> data = (HashMap<String, Object>) httpSession.getAttribute("scimpi-context");
            if (data != null) {
                setSessionData(data);
            }
        }
    }

    @Override
    protected String getSessionId() {
        return request.getSession().getId();
    }

    @Override
    public String clearSession() {
        request.getSession().invalidate();
        return null;
    }

    @Override
    public void reset() {
        try {
            response.getWriter().print("<h1>RESET</h1>");
        } catch (final IOException e) {
            throw new DispatchException(e);
        }
        response.reset();
    }

    @Override
    public void forward(final String view) {
        try {
            isAborted = true;
            getRequest().getRequestDispatcher(view).forward(getRequest(), getResponse());
        } catch (final IOException e) {
            throw new DispatchException(e);
        } catch (final ServletException e) {
            throw new DispatchException(e);
        }
    }

    @Override
    public void redirectTo(final String view) {
        try {
            isAborted = true;
            getResponse().sendRedirect(view);
        } catch (final IOException e) {
            throw new DispatchException(e);
        }
    }

    @Override
    public void raiseError(final int status, final ErrorCollator errorDetails) {
        try {
            isAborted = true;
            getRequest().setAttribute("com.planchaser.error.reference", errorDetails.getReference()); 
            getRequest().setAttribute("com.planchaser.error.message", errorDetails.getMessage());
            getRequest().setAttribute("com.planchaser.error.details", errorDetails.getDetails());
            getResponse().sendError(status);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isAborted() {
        return isAborted;
    }

    @Override
    public void setContentType(final String string) {
        getResponse().setContentType(string);
    }

    @Override
    public String imagePath(final ObjectAdapter object) {
        final String contextPath = getContextPath();
        return ImageLookup.imagePath(object, contextPath);
    }

    @Override
    public String imagePath(final ObjectSpecification specification) {
        final String contextPath = getContextPath();
        return ImageLookup.imagePath(specification, contextPath);
    }

    @Override
    public String getContextPath() {
        return request.getContextPath();
    }

    @Override
    public String getHeader(final String name) {
        return request.getHeader(name);
    }

    @Override
    public String getQueryString() {
        return request.getQueryString();
    }

    @Override
    public String getUri() {
        return request.getRequestURI() + "?" + request.getQueryString();
    }

    @Override
    public String getUrlBase() {
        // return request.getScheme() + request.getServerName() +
        // request.getServerPort();
        final StringBuffer url = request.getRequestURL();
        url.setLength(url.length() - request.getRequestURI().length());
        return url.toString();
    }

    @Override
    public void addCookie(final String name, final String value, final int minutesUtilExpires) {
        final Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(minutesUtilExpires * 60);
        response.addCookie(cookie);
    }

    @Override
    public String getCookie(final String name) {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
