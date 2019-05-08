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
package org.apache.isis.core.webapp.diagnostics;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simply logs the URL of any request that causes an exception to be thrown.
 */
public class IsisLogOnExceptionFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(IsisLogOnExceptionFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (IOException ex) {
            logRequestUrl(request, ex);
            throw ex;
        } catch (ServletException ex) {
            logRequestUrl(request, ex);
            throw ex;
        } catch (RuntimeException ex) {
            logRequestUrl(request, ex);
            throw ex;
        }
    }

    private static void logRequestUrl(ServletRequest request, Exception e) {
        if(!(request instanceof HttpServletRequest)) {
            return;
        }
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final StringBuffer buf = httpServletRequest.getRequestURL();
        final String queryString = httpServletRequest.getQueryString();
        if(queryString != null) {
            buf.append('?').append(queryString);
        }

        LOG.error("Request caused {}: {}", e.getClass().getName(), buf.toString(), e);
    }
}
