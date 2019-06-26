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
package org.apache.isis.core.webapp.modules.resources;

import javax.inject.Singleton;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.core.webapp.modules.WebModule;
import org.apache.isis.core.webapp.modules.WebModuleContext;
import org.springframework.core.annotation.Order;

/**
 * WebModule to provide static resources utilizing an in-memory cache.
 * 
 * @since 2.0.0
 */
@Singleton @Order(-100)
public final class WebModuleStaticResources implements WebModule  {
    
    private final static String[] urlPatterns = { 
          "*.css", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.svg", "*.js", "*.html", "*.swf" };
    
    private final static int cacheTimeSeconds = 86400;
    
    private final static String RESOURCE_SERVLET_NAME = "ResourceServlet";
    
    @Override
    public String getName() {
        return "StaticResources";
    }
    
    @Override
    public void prepare(WebModuleContext ctx) {
        // nothing special required
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        final Dynamic filter = ctx.addFilter("ResourceCachingFilter", ResourceCachingFilter.class);
        if(filter==null) {
            return null; // filter was already registered somewhere else (eg web.xml)
        }

        filter.setInitParameter(
                "CacheTime", 
                ""+cacheTimeSeconds);
        filter.addMappingForUrlPatterns(null, true, urlPatterns);
        
        ctx.addServlet(RESOURCE_SERVLET_NAME, ResourceServlet.class);
        ctx.getServletRegistration(RESOURCE_SERVLET_NAME)
        .addMapping(urlPatterns);
        
        return null; // does not provide a listener
    }

    @Override
    public boolean isApplicable(WebModuleContext ctx) {
        return true;
    }

    
}
