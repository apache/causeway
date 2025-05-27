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
package org.apache.causeway.core.webapp.modules.templresources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._StringInterpolation;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.RestEasyConfiguration;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.core.metamodel.commons.InputStreamExtensions;
import org.apache.causeway.core.metamodel.commons.ResourceUtil;

import static org.apache.causeway.commons.internal.base._Strings.pair;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles HTML-templates, where template's placeholders get replaced by their values.
 */
@Slf4j
public class TemplateResourceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private _StringInterpolation templateVariables;

    @Autowired private RestEasyConfiguration restEasyConfiguration;
    @Autowired private WebAppContextPath webAppContextPath;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        final String restfulPath = this.restEasyConfiguration.getJaxrs().getDefaultPath();
        final String restfulBase = webAppContextPath.prependContextPath(restfulPath);
        templateVariables = new _StringInterpolation(pair("restful-base", restfulBase));
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

    private void processRequest(final HttpServletRequest request, final HttpServletResponse response) {
        final String servletPath = _Strings.removePrefix(request.getServletPath(), "/");
        log.debug("request: {}", servletPath);

        var resourceInputStream = Optional
                .ofNullable(loadFromFileSystem(request)) // try to load from file-system first
                .orElseGet(()->loadFromClassPath(servletPath)); // otherwise, try to load from class-path

        if (resourceInputStream != null) {
            try {
                writeContentType(request, response);
                processContent(resourceInputStream, request, response);
                return;
            } catch (Exception e) {
                // fall through
            } finally {
                try {
                    resourceInputStream.close();
                } catch (IOException e) {
                    // fall through
                }
            }
        }

        log.warn("failed to load resource from classpath or file system: {}", servletPath);
    }

    private InputStream loadFromFileSystem(final HttpServletRequest request) {
        var inputStream = _Util.getResourceAsStream(request);

        if(log.isDebugEnabled()) {
            var realPath = request.getSession().getServletContext().getRealPath(request.getServletPath());
            if(inputStream!=null) {
                log.debug("request: {} loaded from fileSystem {}", request.getServletPath(), realPath);
            } else {
                log.debug("request: {} not found in fileSystem {}", request.getServletPath(), realPath);
            }
        }

        return inputStream;
    }

    private InputStream loadFromClassPath(final String path) {
        var inputStream = ResourceUtil.getResourceAsStream(path);
        if(log.isDebugEnabled()) {
            if(inputStream!=null) {
                log.debug("request: {} loaded from classpath", path);
            }
        }
        return inputStream;
    }

    private void processContent(
            final InputStream is,
            final HttpServletRequest request,
            final HttpServletResponse response)
                    throws IOException {

        if(request.getServletPath().endsWith(".thtml")) {

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

    private static String guessContentType(final String servletPath) {
        if(servletPath.endsWith(".html")) {
            return "text/html";
        }
        return null;
    }

}
