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

package org.apache.isis.core.webapp.content;

import static org.apache.isis.commons.internal.base._Strings.pair;
import static org.apache.isis.commons.internal.base._With.ifPresentElse;
import static org.apache.isis.commons.internal.base._With.ifPresentElseGet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.resources._Resource;
import org.apache.isis.core.commons.lang.InputStreamExtensions;
import org.apache.isis.core.commons.lang.ResourceUtil;
import org.apache.isis.core.commons.lang.StringExtensions;

/**
 * Serves static web-resources by class-path or file-system lookup.
 * Also handles HTML-templates, where template's placeholders get replaced by their values.
 */
@WebServlet(
        urlPatterns = { 
                "*.css", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.svg", "*.js", "*.html", "*.swf" }
)
public class ResourceServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceServlet.class);
    private static final long serialVersionUID = 1L;
    private ResourceServlet_HtmlTemplateVariables templateVariables;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        final String restfulPath = ifPresentElse(_Resource.getRestfulPathIfAny(), "restful"); 
        templateVariables = new ResourceServlet_HtmlTemplateVariables(pair("restful", restfulPath));
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    // -- HELPER

    private void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String servletPath = StringExtensions.stripLeadingSlash(request.getServletPath());
        LOG.debug("request: {}", servletPath);

        final InputStream is = ifPresentElseGet(
                ResourceUtil.getResourceAsStream(request), // try to load from file-system first 
                ()->ResourceUtil.getResourceAsStream(servletPath)); // otherwise, try to load from class-path  
        
        if (is != null) {
            LOG.debug("request: {} loaded from classpath", servletPath );
            
            try {
                writeContentType(request, response);
                processContent(is, request, response);
                return;
            } finally {
                is.close();    
            }
        }

        LOG.warn("failed to load resource from classpath or file system: {}", servletPath);
    }

    private void processContent(
            final InputStream is, 
            final HttpServletRequest request, 
            final HttpServletResponse response) 
                    throws IOException {
        
        if(request.getServletPath().endsWith(".template.html")) {
            
            final String templateContent = _Strings.ofBytes(_Bytes.of(is), StandardCharsets.UTF_8);
            final String htmlContent = templateVariables.applyTo(templateContent);
                        
            response.getWriter().append(htmlContent);
            
        } else {
            
            // direct copy
            InputStreamExtensions.copyTo(is, response.getOutputStream());
            
        }
    }

    private static void writeContentType(final HttpServletRequest request, final HttpServletResponse response) {
        final String contentType = guessContentType(request.getServletPath());
        if(contentType != null) {
            response.setContentType(contentType);
        }
    }

    private static String guessContentType(String servletPath) {
        if(servletPath.endsWith(".css")) {
            return "text/css";
        } else if(servletPath.endsWith(".png")) {
            return "image/png";
        } else if(servletPath.endsWith(".jpg")) {
            return "image/jpeg";
        } else if(servletPath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if(servletPath.endsWith(".gif")) {
            return "image/gif";
        } else if(servletPath.endsWith(".svg")) {
            return "image/svg+xml";
        } else if(servletPath.endsWith(".js")) {
            return "application/x-javascript";
        } else if(servletPath.endsWith(".html")) {
            return "text/html";
        } else if(servletPath.endsWith(".swf")) {
            return "application/x-shockwave-flash";
        }
        return null;
    }

}
