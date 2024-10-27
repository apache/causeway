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
package org.apache.causeway.core.webapp.modules.logonlog;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.apache.causeway.applib.services.exceprecog.ExceptionRecognizerForType;
//import org.apache.causeway.applib.services.exceprecog.ExceptionRecognizerService;
//import org.apache.causeway.commons.collections.Can;
//
//
import lombok.extern.log4j.Log4j2;

/**
 * Simply logs the URL of any request that causes an exception to be thrown (but will swallow any
 * ClientAbortExceptions, as these represent the end user navigating away without waiting for a response)
 */
@Log4j2
public class CausewayLogOnExceptionFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    /**
     *
     * eg:
     * <pre>
     *     yyyy-MM-ddT11:37:40.905753834Z
     *     yyyy-MM-ddT11:37:40.905761428Z org.apache.catalina.connector.ClientAbortException: java.io.IOException: Broken pipe
     *     yyyy-MM-ddT11:37:40.905767047Z 	at org.apache.catalina.connector.OutputBuffer.doFlush(OutputBuffer.java:309) ~[tomcat-embed-core-9.0.83.jar:9.0.83]
     *     yyyy-MM-ddT11:37:40.905772622Z 	at org.apache.catalina.connector.OutputBuffer.flush(OutputBuffer.java:271) ~[tomcat-embed-core-9.0.83.jar:9.0.83]
     *     yyyy-MM-ddT11:37:40.905778518Z 	at org.apache.catalina.connector.Response.flushBuffer(Response.java:494) ~[tomcat-embed-core-9.0.83.jar:9.0.83]
     *     yyyy-MM-ddT11:37:40.905784572Z 	at org.apache.catalina.connector.ResponseFacade.flushBuffer(ResponseFacade.java:256) ~[tomcat-embed-core-9.0.83.jar:9.0.83]
     *     yyyy-MM-ddT11:37:40.905790091Z 	at org.apache.wicket.protocol.http.servlet.ServletWebResponse.flush(ServletWebResponse.java:315) ~[wicket-core-9.16.0.jar:9.16.0]
     *     yyyy-MM-ddT11:37:40.905795414Z 	at org.apache.wicket.protocol.http.HeaderBufferingWebResponse.flush(HeaderBufferingWebResponse.java:98) ~[wicket-core-9.16.0.jar:9.16.0]
     *     yyyy-MM-ddT11:37:40.905800948Z 	at org.apache.wicket.protocol.http.WicketFilter.processRequestCycle(WicketFilter.java:280) ~[wicket-core-9.16.0.jar:9.16.0]
     *     yyyy-MM-ddT11:37:40.905806754Z 	at org.apache.wicket.protocol.http.WicketFilter.processRequest(WicketFilter.java:208) ~[wicket-core-9.16.0.jar:9.16.0]
     *     yyyy-MM-ddT11:37:40.905850671Z 	at org.apache.wicket.protocol.http.WicketFilter.doFilter(WicketFilter.java:307) ~[wicket-core-9.16.0.jar:9.16.0]
     *     yyyy-MM-ddT11:37:40.905861372Z 	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:178) ~[tomcat-embed-core-9.0.83.jar:9.0.83]
     *     yyyy-MM-ddT11:37:40.905883937Z 	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:153) ~[tomcat-embed-core-9.0.83.jar:9.0.83]
     *     yyyy-MM-ddT11:37:40.905886000Z 	at org.apache.isis.core.webapp.modules.logonlog.IsisLogOnExceptionFilter.doFilter(IsisLogOnExceptionFilter.java:60)
     * </pre>
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception ex) {

            // browser has navigated away.
            if(ex.getClass().getCanonicalName().equals("org.apache.catalina.connector.ClientAbortException")) {
                return; // don't log or even throw
            }

            if(ex instanceof IOException) {
                var url = ((HttpServletRequest) request).getRequestURL().toString();
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
