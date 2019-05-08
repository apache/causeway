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
package org.apache.isis.core.webapp.routing;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter attempts to ensure that would-be users of the framework are
 * directed to the bundled documentation, rather than just hitting the REST API
 * (ie the json viewer).
 *
 * <p>
 * Specifically, if the request is to "/" but the Accept header is anything
 * other than "application/json" (eg is set to "text/html" and suggesting that
 * the user is using a browser to access the webapp) then the filter redirects
 * to /index.html (the documentation pages).
 *
 * <p>
 * Only if the Accept header is set to application/json is the request allowed
 * to continue through.
 */
public class RedirectToDocsFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RedirectToDocsFilter.class);

    private static final String REDIRECT_TO_KEY = "redirectTo";
    private static final String REDIRECT_TO_DEFAULT = "/index.html";

    private static final String ACCEPT_HEADER = "Accept";
    private static final String APPLICATION_JSON_MIME_TYPE = "application/json";

    private String redirectTo;

    @Override
    public void init(final FilterConfig cfg) throws ServletException {
        redirectTo = cfg.getInitParameter(REDIRECT_TO_KEY);
        if (redirectTo == null) {
            redirectTo = REDIRECT_TO_DEFAULT;
        }
        LOG.info("redirectToDocsFilter: redirectTo={}", redirectTo);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // do nothing if not requesting "/"
        final String servletPath = httpServletRequest.getServletPath();
        LOG.info("redirectToDocsFilter: servletPath: {}", servletPath);

        if (!"/".equals(servletPath)) {
            chain.doFilter(request, response);
            return;
        }

        final String acceptHeader = httpServletRequest.getHeader(ACCEPT_HEADER);
        if (acceptHeader != null && acceptHeader.startsWith(APPLICATION_JSON_MIME_TYPE)) {
            // let request through
            chain.doFilter(request, response);
            return;
        }

        // otherwise redirect
        final String redirect = combine(httpServletRequest.getContextPath(), redirectTo);
        LOG.info("redirectToDocsFilter: redirecting to: {}", redirect);

        httpServletResponse.sendRedirect(redirect);
    }

    private static String combine(final String str1, final String str2) {
        final StringBuilder buf = new StringBuilder(str1);
        if (!str2.startsWith("/")) {
            buf.append("/");
        }
        buf.append(str2);
        return buf.toString();
    }

}
