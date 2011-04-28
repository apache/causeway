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


package org.apache.isis.runtimes.dflt.runtime.viewer.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.commons.lang.MapUtils;


/**
 * Defines what servlets, mappings etc are required from an embedded web server.
 */
public final class WebAppSpecification {
	
	private Map<String,String> contextParams = new LinkedHashMap<String, String>();
    private List<Class<?>> servletContextListeners = new ArrayList<Class<?>>();
    private List<ServletSpecification> servletSpecifications = new ArrayList<ServletSpecification>();
    private List<FilterSpecification> filterSpecifications = new ArrayList<FilterSpecification>();
    private List<String> resourcePaths = new ArrayList<String>();
    private List<String> welcomeFiles = new ArrayList<String>();
	private String logHint;

    

    /////////////////////////////////////////////////////////////
    // Context Params
    /////////////////////////////////////////////////////////////

	public void addContextParams(String... contextParams) {
		this.contextParams = MapUtils.asMap(contextParams);
	}
	
	public Map<String, String> getContextParams() {
		return Collections.unmodifiableMap(contextParams);
	}

	
    /////////////////////////////////////////////////////////////
    // Servlet Context Listeners
    /////////////////////////////////////////////////////////////
    
    public void addServletContextListener(Class<?> servletContextListenerClass) {
        servletContextListeners.add(servletContextListenerClass);        
    }
    
    public List<Class<?>> getServletContextListeners() {
        return servletContextListeners;
    }


    /////////////////////////////////////////////////////////////
    // Servlet Mappings
    /////////////////////////////////////////////////////////////
    
    public void addServletSpecification(Class<?> servletClass, String... pathSpecs) {
        servletSpecifications.add(new ServletSpecification(servletClass, pathSpecs));        
    }
    
    public void addServletSpecification(Class<?> servletClass, Map<String,String> initParams, String... pathSpecs) {
        servletSpecifications.add(new ServletSpecification(servletClass, initParams, pathSpecs));        
    }
    
    public List<ServletSpecification> getServletSpecifications() {
        return servletSpecifications;
    }

    /////////////////////////////////////////////////////////////
    // Filter Mappings
    /////////////////////////////////////////////////////////////

    public void addFilterSpecification(Class<?> filterClass, String... pathSpecs) {
        filterSpecifications.add(new FilterSpecification(filterClass, pathSpecs));
    }

    public void addFilterSpecification(Class<?> filterClass, Map<String,String> initParams, String... pathSpecs) {
        filterSpecifications.add(new FilterSpecification(filterClass, initParams, pathSpecs));
    }

    public List<FilterSpecification> getFilterSpecifications() {
        return filterSpecifications;
    }
    
    /////////////////////////////////////////////////////////////
    // Resources
    /////////////////////////////////////////////////////////////

    public void addResourcePath(String path) {
        resourcePaths.add(path);
    }
    public List<String> getResourcePaths() {
        return resourcePaths;
    }


    /////////////////////////////////////////////////////////////
    // Welcome Files
    /////////////////////////////////////////////////////////////

    public void addWelcomeFile(String path) {
        welcomeFiles.add(path);
    }
    
    public List<String> getWelcomeFiles() {
        return welcomeFiles;
    }

    
    /////////////////////////////////////////////////////////////
    // Candifloss
    /////////////////////////////////////////////////////////////

	public String getLogHint() {
		return logHint;
	}

	public void setLogHint(String logHint) {
		this.logHint = logHint;
	}

}

