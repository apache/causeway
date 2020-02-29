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
package org.apache.isis.core.webapp.modules.logonlog;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lombok.val;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerForType;
//import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerService;
//import org.apache.isis.core.commons.collections.Can;
//
//import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Simply logs the URL of any request that causes an exception to be thrown.
 */
@Log4j2
public class IsisLogOnExceptionFilter implements Filter {
    
    //@Autowired private ExceptionRecognizerService exceptionRecognizerService;

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
        } catch (Exception ex) {
            
            if(ex instanceof IOException) {
                val url = ((HttpServletRequest) request).getRequestURL().toString();
                if(url.endsWith(".css")
                        || url.endsWith(".js")
                        || url.endsWith(".woff2")) {
                    throw ex; // don't log
                }    
            }
            
            if(ex instanceof IOException
                    || ex instanceof ServletException
                    || ex instanceof RuntimeException) {
                logRequestUrl(request, ex);
            }
            
            throw ex;
        } 
    }
    
    // -- HELPER

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

        log.error("Request caused {}: {}", e.getClass().getName(), buf.toString(), e);
    }
}
