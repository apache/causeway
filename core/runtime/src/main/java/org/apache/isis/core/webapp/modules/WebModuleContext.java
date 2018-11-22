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
package org.apache.isis.core.webapp.modules;

import java.util.stream.Stream;

import javax.servlet.ServletContext;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.config.internal._Config;

/**
 * 
 * @since 2.0.0-M2
 *
 */
public class WebModuleContext {

    /**
     * This key was deprecated from config, but we still use it for reference. It is auto-populated 
     * such that it can be looked up, to see what viewers have been discovered by the framework.
     */
    private final static String ISIS_VIEWERS = "isis.viewers";
    private final static String ISIS_PROTECTED = "isis.protected";
    
    private boolean hasBootstrapper = false;
    private final StringBuilder viewers = new StringBuilder();
    private final StringBuilder protectedPath = new StringBuilder();
    
    private final ServletContext servletContext;
    
    public WebModuleContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Tell other modules that a bootstrapper is present.
     */
    public void setHasBootstrapper() {
        hasBootstrapper = true;
    }
    
    /**
     * @return whether this context has a bootstrapper
     */
    public boolean hasBootstrapper() {
        return hasBootstrapper;    
    }
    
    /**
     *  Adds to the list of viewer names "isis.viewers"
     * @param viewerName
     */
    public void addViewer(String viewerName) {
        if(viewers.length()>0) {
            viewers.append(",");
        } 
        viewers.append(viewerName);
    }
    
    /**
     *  Adds to the list of protected path names "isis.protected"
     * @param path
     */
    public void addProtectedPath(String path) {
        if(protectedPath.length()>0) {
            protectedPath.append(",");
        } 
        protectedPath.append(path);
    }
    
    /**
     * Streams the protected path names "isis.protected".
     * @param ctx
     */
    public Stream<String> streamProtectedPaths() {
        final String list = protectedPath.toString();
        return _Strings.splitThenStream(list, ",");
    }
    
    /**
     * Commits all properties to current life-cycle's config.
     */
    public void commit() {
        _Config.acceptBuilder(builder->{
            builder.add(ISIS_VIEWERS, viewers.toString());
            builder.add(ISIS_PROTECTED, protectedPath.toString());
        });
    }
    
}
