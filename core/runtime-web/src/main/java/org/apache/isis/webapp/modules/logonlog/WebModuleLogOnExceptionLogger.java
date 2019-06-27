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
package org.apache.isis.webapp.modules.logonlog;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.context._Context.getDefaultClassLoader;
import static org.apache.isis.commons.internal.exceptions._Exceptions.unexpectedCodeReach;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * WebModule to log log-on exceptions.
 * 
 * @since 2.0
 */
@Singleton @Order(Ordered.LOWEST_PRECEDENCE)
public final class WebModuleLogOnExceptionLogger implements WebModule  {

    private final static String LOGONLOGGER_FILTER_CLASS_NAME = 
            "org.apache.isis.webapp.diagnostics.IsisLogOnExceptionFilter";
    private final static String LOGONLOGGER_FILTER_NAME = 
            "IsisLogOnExceptionFilter";
    private WebModuleContext webModuleContext;


    @Override
    public String getName() {
        return "LogOn Exception Logger";
    }

    @Override
    public void prepare(WebModuleContext ctx) {
        this.webModuleContext = ctx;
    }
    
    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        final Filter filter;
        try {
            final Class<?> filterClass = getDefaultClassLoader().loadClass(LOGONLOGGER_FILTER_CLASS_NAME);
            filter = ctx.createFilter(uncheckedCast(filterClass));
        } catch (ClassNotFoundException e) {
            // guarded against by isAvailable()
            throw unexpectedCodeReach();
        }

        final Dynamic reg = ctx.addFilter(LOGONLOGGER_FILTER_NAME, filter);
        if(reg==null) {
            return null; // filter was already registered somewhere else (eg web.xml)
        }

        reg.addMappingForUrlPatterns(null, true, getProtectedUrlPatterns() ); // filter is forced last

        return null; // does not provide a listener
    }

    @Override
    public boolean isApplicable(WebModuleContext ctx) {
        try {
            getDefaultClassLoader().loadClass(LOGONLOGGER_FILTER_CLASS_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // -- HELPER
    
    private String[] getProtectedUrlPatterns() {
        return webModuleContext.streamProtectedPaths()
        .collect(_Arrays.toArray(String.class));
    }


    

}
