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
package org.apache.causeway.core.webapp.routing;

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.causeway.commons.internal.resources._Resources;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedirectServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private String redirectTo;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        redirectTo = config.getInitParameter("redirectTo");
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        try {
            response.sendRedirect(_Resources.combinePath(request.getContextPath(), redirectTo));
        } catch (Exception e) {
            log.error("failed to redirect request to {}", redirectTo, e);
        }

    }

}
