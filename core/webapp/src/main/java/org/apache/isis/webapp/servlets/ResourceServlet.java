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

package org.apache.isis.webapp.servlets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.isis.commons.io.IoUtils;
import org.apache.isis.commons.lang.Resources;
import org.apache.isis.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author Dan Haywood
 * 
 */
public class ResourceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final Logger LOG = Logger.getLogger(ResourceServlet.class);

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
        IOException {
        processRequest(request, response);
    }

    private void processRequest(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
        final String servletPath = StringUtils.stripLeadingSlash(request.getServletPath());
        LOG.info("request: " + servletPath);

        // try to load from filesystem
        InputStream is2 = getRealPath(request);
        if (is2 != null) {
            LOG.debug("request: " + servletPath + " loaded from filesystem");
            IoUtils.copy(is2, response.getOutputStream());
            is2.close();
            return;
        }

        // otherwise, try to load from classpath
        InputStream is = Resources.getResourceAsStream(servletPath);
        if (is != null) {
            LOG.debug("request: " + servletPath + " loaded from classpath");
            IoUtils.copy(is, response.getOutputStream());
            is.close();
            return;
        }

        LOG.warn("failed to load resource from classpath or file system: " + servletPath);
    }

    private FileInputStream getRealPath(final HttpServletRequest request) {
        String realPath = request.getSession().getServletContext().getRealPath(request.getServletPath());
        if (realPath == null) {
            return null;
        }
        try {
            return new FileInputStream(realPath);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
