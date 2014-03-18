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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.lang.InputStreamExtensions;
import org.apache.isis.core.commons.lang.ResourceUtil;
import org.apache.isis.core.commons.lang.StringExtensions;

public class ResourceServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String servletPath = StringExtensions.stripLeadingSlash(request.getServletPath());
        if (LOG.isDebugEnabled()) {
            LOG.debug("request: " + servletPath);
        }

        // try to load from filesystem
        final InputStream is2 = getRealPath(request);
        if (is2 != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("request: " + servletPath + " loaded from filesystem");
            }
            writeContentType(request, response);
            InputStreamExtensions.copyTo(is2, response.getOutputStream());
            is2.close();
            return;
        }

        // otherwise, try to load from classpath
        final InputStream is = ResourceUtil.getResourceAsStream(servletPath);
        if (is != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("request: " + servletPath + " loaded from classpath");
            }
            writeContentType(request, response);
            InputStreamExtensions.copyTo(is, response.getOutputStream());
            is.close();
            return;
        }

        LOG.warn("failed to load resource from classpath or file system: " + servletPath);
    }

    private static void writeContentType(final HttpServletRequest request, final HttpServletResponse response) {
        final String contentType = guessContentType(request.getServletPath());
        if(contentType != null) {
            response.setContentType(contentType);
        }
    }

    private static String guessContentType(String servletPath) {
        if(servletPath.endsWith(".js")) {
            return "application/x-javascript";
        } else if(servletPath.endsWith(".css")) {
            return "text/css";
        } else if(servletPath.endsWith(".html")) {
            return "text/html";
        } else if(servletPath.endsWith(".png")) {
            return "image/png";
        } else if(servletPath.endsWith(".jpg")) {
            return "image/jpeg";
        } else if(servletPath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if(servletPath.endsWith(".gif")) {
            return "image/gif";
        }
        return null;
    }

    private FileInputStream getRealPath(final HttpServletRequest request) {
        final String realPath = request.getSession().getServletContext().getRealPath(request.getServletPath());
        if (realPath == null) {
            return null;
        }
        try {
            return new FileInputStream(realPath);
        } catch (final FileNotFoundException e) {
            return null;
        }
    }
}
