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

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.context._Context.getDefaultClassLoader;
import static org.apache.isis.commons.internal.exceptions._Exceptions.unexpectedCodeReach;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

/**
 * Package private mixin for WebModule implementing WebModule.
 * @since 2.0.0
 */
final class WebModule_Shiro implements WebModule  {

    private final static String SHIRO_LISTENER_CLASS_NAME = 
            "org.apache.shiro.web.env.EnvironmentLoaderListener";

    private final static String SHIRO_FILTER_CLASS_NAME = 
            "org.apache.shiro.web.servlet.ShiroFilter";
    
    private final static String SHIRO_FILTER_NAME = "ShiroFilter";

    @Override
    public String getName() {
        return "Shiro";
    }
    
    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {
        
        final Filter filter;
        try {
            final Class<?> filterClass = getDefaultClassLoader().loadClass(SHIRO_FILTER_CLASS_NAME);
            filter = ctx.createFilter(uncheckedCast(filterClass));
        } catch (ClassNotFoundException e) {
            // guarded against by isAvailable()
            throw unexpectedCodeReach();
        }

        final Dynamic reg = ctx.addFilter(SHIRO_FILTER_NAME, filter);
        if(reg==null) {
            return null; // filter was already registered somewhere else (eg web.xml)
        }

        final String urlPattern = "/*";

        reg.addMappingForUrlPatterns(null, false, urlPattern); // filter is forced first
        
        try {
            final Class<?> listenerClass = getDefaultClassLoader().loadClass(SHIRO_LISTENER_CLASS_NAME);
            return ctx.createListener(uncheckedCast(listenerClass));
        } catch (ClassNotFoundException e) {
            // guarded against by isAvailable()
            throw unexpectedCodeReach();
        }
      
    }

    @Override
    public boolean isApplicable(ServletContext ctx) {
        try {
            getDefaultClassLoader().loadClass(SHIRO_LISTENER_CLASS_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
