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
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.scimpi.dispatcher.DispatchException;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiNotFoundException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugWriter;


public class ServletRequestContext extends RequestContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;
    private boolean isAborted;

    public void append(DebugWriter view) {
        /*
        view.divider("System");
        Runtime.getRuntime().
		*/
        view.appendTitle("Request");
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

        view.appendTitle("Cookies");
        Cookie[] cookies = request.getCookies();
        for (int i = 0; i < cookies.length; i++) {
            view.appendln(cookies[i].getName(), cookies[i].getValue());
        }

        Enumeration attributeNames = request.getAttributeNames();
        if (attributeNames.hasMoreElements()) {
            view.appendTitle("Attributes");
            while (attributeNames.hasMoreElements()) {
                String name = (String) attributeNames.nextElement();
                view.appendln(name, request.getAttribute(name));
            }
        }

        view.appendTitle("Headers");
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            view.appendln(name, request.getHeader(name));
        }

        view.appendTitle("Parameters");
        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = (String) parameterNames.nextElement();
            view.appendln(name, request.getParameter(name));
        }

        view.appendTitle("Servlet Context");
        ServletContext context = getServletContext();
        view.appendln("Name", context.getServletContextName());
        view.appendln("Server Info", context.getServerInfo());
        view.appendln("Version", context.getMajorVersion() + "." + context.getMinorVersion());
        view.appendln("Attributes", getAttributes(context));
        view.appendln("Init parameters", getParameters(context));
        view.appendln("Real path", context.getRealPath("/"));

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
    
    public void startHttpSession() {
        addVariable("_auth_session", getSession(), Scope.SESSION);
        HttpSession httpSession = request.getSession(true);
        Map<String, Object> sessionData = getSessionData();
        httpSession.setAttribute("scimpi-context", sessionData);
    }

    protected String getSessionId() {
        return request.getSession().getId();
    }

    public String clearSession() {
        request.getSession().invalidate();
        return null;
    }
    
    public void reset() {
        try {
            response.getWriter().print("<h1>RESET</h1>");
        } catch (IOException e) {
            throw new DispatchException(e);
        }
        response.reset();
    }
    
    public void forward(String view) {
        try {
            isAborted = true;
            getRequest().getRequestDispatcher(view).forward(getRequest(), getResponse());
        } catch (IOException e) {
            throw new DispatchException(e);
        } catch (ServletException e) {
            throw new DispatchException(e);
        }
    }

    public void redirectTo(String view) {
        try {
            isAborted = true;
            getResponse().sendRedirect(view);
        } catch (IOException e) {
            throw new DispatchException(e);
        }
    }
    
    public void raiseError(int status) {
        try {
            isAborted = true;
            getResponse().sendError(status);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getResponse().setStatus(status);
    }
    
    public boolean isAborted() {
        return isAborted;
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
    
    public String getHeader(String name) {
        return request.getHeader(name);
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

