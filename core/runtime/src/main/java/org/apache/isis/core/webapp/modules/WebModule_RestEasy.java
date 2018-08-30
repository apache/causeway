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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

/**
 * Package private mixin for WebModule implementing WebModule.
 * 
 * @since 2.0.0
 */
final class WebModule_RestEasy implements WebModule  {
    
    private final static String RESTEASY_BOOTSTRAPPER = 
            "org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap";
    
    private final static String RESTEASY_DISPATCHER = "RestfulObjectsRestEasyDispatcher";
    
    @Override
    public String getName() {
        return "RestEasy";
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {
        
        //  used by RestEasy to determine the JAX-RS resources and other related configuration
        ctx.setInitParameter(
                "javax.ws.rs.Application", 
                "org.apache.isis.viewer.restfulobjects.server.RestfulObjectsApplication");
        
        ctx.setInitParameter("resteasy.servlet.mapping.prefix", getRestfulPath());
        
        ctx.addServlet(RESTEASY_DISPATCHER, 
                "org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher");
        ctx.getServletRegistration(RESTEASY_DISPATCHER)
        .addMapping(getUrlPattern());
        
        try {
            final Class<?> listenerClass = getDefaultClassLoader().loadClass(RESTEASY_BOOTSTRAPPER);
            return ctx.createListener(uncheckedCast(listenerClass));
        } catch (ClassNotFoundException e) {
            // guarded against by isAvailable()
            throw unexpectedCodeReach();
        }
    }

    @Override
    public boolean isAvailable(ServletContext ctx) {
        try {
            getDefaultClassLoader().loadClass(RESTEASY_BOOTSTRAPPER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // -- HELPER
    
    private String getUrlPattern() {
        return getRestfulPath() + "*";
    }

    private String getRestfulPath() {
        final String restfulPath = ifPresentElse(getRestfulPathIfAny(), "restful");
        final String restfulPathEnclosedWithSlashes = suffix(prefix(restfulPath, "/"), "/");
        return restfulPathEnclosedWithSlashes;
    }
    
    
}
