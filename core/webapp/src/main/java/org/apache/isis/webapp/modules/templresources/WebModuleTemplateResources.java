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
package org.apache.isis.webapp.modules.templresources;

import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;

/**
 * WebModule to provide static resources utilizing an in-memory cache.
 * 
 * @since 2.0
 */
@Service @Order(-100)
public final class WebModuleTemplateResources implements WebModule  {

    private final static String[] urlPatterns = { "*.thtml" };

    private final static int cacheTimeSeconds = 86400;

    private final static String SERVLET_NAME = "TemplateResourceServlet";

    @Override
    public String getName() {
        return "TemplateResources";
    }

    @Override
    public void prepare(WebModuleContext ctx) {
        // nothing special required
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        final Dynamic filter = ctx.addFilter("TemplateResourceCachingFilter", TemplateResourceCachingFilter.class);
        if(filter==null) {
            return null; // filter was already registered somewhere else (eg web.xml)
        }

        filter.setInitParameter(
                "CacheTime", 
                ""+cacheTimeSeconds);
        filter.addMappingForUrlPatterns(null, true, urlPatterns);

        ctx.addServlet(SERVLET_NAME, TemplateResourceServlet.class);
        ctx.getServletRegistration(SERVLET_NAME)
        .addMapping(urlPatterns);

        return null; // does not provide a listener
    }

    @Override
    public boolean isApplicable(WebModuleContext ctx) {
        return true;
    }


}
