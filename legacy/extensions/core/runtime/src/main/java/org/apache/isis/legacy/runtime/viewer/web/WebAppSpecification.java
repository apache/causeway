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

package org.apache.isis.legacy.runtime.viewer.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.metamodel.commons.MapUtil;

/**
 * Defines what servlets, mappings etc are required from an embedded web server.
 */
public final class WebAppSpecification {

    private Map<String, String> contextParams = new LinkedHashMap<String, String>();
    private final List<Class<?>> servletContextListeners = new ArrayList<Class<?>>();
    private final List<ServletSpecification> servletSpecifications = new ArrayList<ServletSpecification>();
    private final List<FilterSpecification> filterSpecifications = new ArrayList<FilterSpecification>();
    private final List<String> resourcePaths = new ArrayList<String>();
    private final List<String> welcomeFiles = new ArrayList<String>();
    private String logHint;

    // ///////////////////////////////////////////////////////////
    // Context Params
    // ///////////////////////////////////////////////////////////

    public void addContextParams(final String... contextParams) {
        this.contextParams = MapUtil.asMap(contextParams);
    }

    public Map<String, String> getContextParams() {
        return Collections.unmodifiableMap(contextParams);
    }

    // ///////////////////////////////////////////////////////////
    // Servlet Context Listeners
    // ///////////////////////////////////////////////////////////

    public void addServletContextListener(final Class<?> servletContextListenerClass) {
        servletContextListeners.add(servletContextListenerClass);
    }

    public List<Class<?>> getServletContextListeners() {
        return servletContextListeners;
    }

    // ///////////////////////////////////////////////////////////
    // Servlet Mappings
    // ///////////////////////////////////////////////////////////

    public void addServletSpecification(final Class<?> servletClass, final String... pathSpecs) {
        servletSpecifications.add(new ServletSpecification(servletClass, pathSpecs));
    }

    public void addServletSpecification(final Class<?> servletClass, final Map<String, String> initParams, final String... pathSpecs) {
        servletSpecifications.add(new ServletSpecification(servletClass, initParams, pathSpecs));
    }

    public List<ServletSpecification> getServletSpecifications() {
        return servletSpecifications;
    }

    // ///////////////////////////////////////////////////////////
    // Filter Mappings
    // ///////////////////////////////////////////////////////////

    public void addFilterSpecification(final Class<?> filterClass, final String... pathSpecs) {
        filterSpecifications.add(new FilterSpecification(filterClass, pathSpecs));
    }

    public void addFilterSpecification(final Class<?> filterClass, final Map<String, String> initParams, final String... pathSpecs) {
        filterSpecifications.add(new FilterSpecification(filterClass, initParams, pathSpecs));
    }

    public List<FilterSpecification> getFilterSpecifications() {
        return filterSpecifications;
    }

    // ///////////////////////////////////////////////////////////
    // Resources
    // ///////////////////////////////////////////////////////////

    public void addResourcePath(final String path) {
        resourcePaths.add(path);
    }

    public List<String> getResourcePaths() {
        return resourcePaths;
    }

    // ///////////////////////////////////////////////////////////
    // Welcome Files
    // ///////////////////////////////////////////////////////////

    public void addWelcomeFile(final String path) {
        welcomeFiles.add(path);
    }

    public List<String> getWelcomeFiles() {
        return welcomeFiles;
    }

    // ///////////////////////////////////////////////////////////
    // Candifloss
    // ///////////////////////////////////////////////////////////

    public String getLogHint() {
        return logHint;
    }

    public void setLogHint(final String logHint) {
        this.logHint = logHint;
    }

}
