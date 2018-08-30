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
import static org.apache.isis.commons.internal.base._Strings.prefix;
import static org.apache.isis.commons.internal.base._Strings.suffix;
import static org.apache.isis.commons.internal.base._With.ifPresentElse;
import static org.apache.isis.commons.internal.context._Context.getDefaultClassLoader;
import static org.apache.isis.commons.internal.exceptions._Exceptions.unexpectedCodeReach;
import static org.apache.isis.commons.internal.resources._Resource.getRestfulPathIfAny;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

/**
 * Package private mixin for WebModule implementing WebModule.
 * @since 2.0.0
 */
final class WebModule_LogOnExceptionLogger implements WebModule  {

    private final static String LOGONLOGGER_FILTER_CLASS_NAME = 
            "org.apache.isis.core.webapp.diagnostics.IsisLogOnExceptionFilter";

    private final static String LOGONLOGGER_FILTER_NAME = "IsisLogOnExceptionFilter";


    @Override
    public String getName() {
        return "Wicket WebFilter";
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

        reg.addMappingForUrlPatterns(null, true, getUrlPatterns() ); // filter is forced last

        return null; // does not provide a listener
    }

    @Override
    public boolean isAvailable(ServletContext ctx) {
        try {
            getDefaultClassLoader().loadClass(LOGONLOGGER_FILTER_CLASS_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // -- HELPER
    
    private String[] getUrlPatterns() {
        return new String[] {
                getRestfulUrlPattern(),
                getWicketUrlPattern()
            };
    }

    private String getWicketUrlPattern() {
        final String wicketPath = getWicketPath();
        final String wicketPathEnclosedWithSlashes = suffix(prefix(wicketPath, "/"), "/");
        return wicketPathEnclosedWithSlashes + "*";
    }

    private String getWicketPath() {
        // TODO[ISIS-1895] should be provided by a config value
        return "wicket";
    }

    private String getRestfulUrlPattern() {
        final String restfulPath = ifPresentElse(getRestfulPathIfAny(), "restful");
        final String restfulPathEnclosedWithSlashes = suffix(prefix(restfulPath, "/"), "/");
        return restfulPathEnclosedWithSlashes + "*";
    }
    

}
