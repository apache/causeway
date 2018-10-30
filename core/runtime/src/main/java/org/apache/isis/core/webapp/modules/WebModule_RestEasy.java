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

import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.core.webapp.IsisSessionFilter;
import org.apache.isis.core.webapp.IsisWebAppConfigProvider;

import static java.util.Objects.requireNonNull;
import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.base._Strings.prefix;
import static org.apache.isis.commons.internal.base._Strings.suffix;
import static org.apache.isis.commons.internal.context._Context.getDefaultClassLoader;
import static org.apache.isis.commons.internal.exceptions._Exceptions.unexpectedCodeReach;
import static org.apache.isis.commons.internal.resources._Resources.putRestfulPath;

/**
 * Package private mixin for WebModule implementing WebModule.
 * 
 * @since 2.0.0
 */
final class WebModule_RestEasy implements WebModule  {
    
    public static final String KEY_RESTFUL_BASE_PATH = "isis.viewer.restfulobjects.basePath";
    public static final String KEY_RESTFUL_BASE_PATH_DEFAULT = "/restful";
    
    private final static String RESTEASY_BOOTSTRAPPER = 
            "org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap";
    
    private final static String RESTEASY_DISPATCHER = "RestfulObjectsRestEasyDispatcher";
    
    String restfulPathConfigValue;
    
    @Override
    public String getName() {
        return "RestEasy";
    }
    
    @Override
    public void prepare(ServletContext ctx) {
        
        if(!isApplicable(ctx)) {
            return;
        }
        
        // try to fetch restfulPath from config else fallback to default
        final String restfulPath = 
                IsisWebAppConfigProvider.getInstance()
                .peekAtOrDefault(ctx, KEY_RESTFUL_BASE_PATH, KEY_RESTFUL_BASE_PATH_DEFAULT);
                
        putRestfulPath(restfulPath);
        
        this.restfulPathConfigValue = restfulPath; // store locally for reuse
        
        // register this module as a viewer
        ContextUtil.registerViewer(ctx, "restfulobjects");
        ContextUtil.registerProtectedPath(ctx, suffix(prefix(restfulPath, "/"), "/") + "*" );
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        // add IsisSessionFilters
        
        {
            final Dynamic filter = ctx.addFilter("IsisSessionFilterForRestfulObjects", IsisSessionFilter.class);

            // this is mapped to the entire application; 
            // however the IsisSessionFilter will 
            // "notice" if the session filter has already been
            // executed for the request pipeline, and if so will do nothing
            filter.addMappingForServletNames(null, true, RESTEASY_DISPATCHER); 
            
            filter.setInitParameter(
                    "authenticationSessionStrategy", 
                    "org.apache.isis.viewer.restfulobjects.server.authentication.AuthenticationSessionStrategyBasicAuth");
            filter.setInitParameter(
                    "whenNoSession", // what to do if no session was found ...
                    "auto"); // ... 401 and a basic authentication challenge if request originates from web browser
            filter.setInitParameter(
                    "passThru", 
                    String.join(",", getRestfulPath()+"swagger", getRestfulPath()+"health"));
            
        }
        
        {
            final Dynamic filter = ctx.addFilter("RestfulObjectsRestEasyDispatcher", 
                    "org.apache.isis.viewer.restfulobjects.server.webapp.IsisTransactionFilterForRestfulObjects");
            filter.addMappingForServletNames(null, true, RESTEASY_DISPATCHER); 
        }
        
        

        // add RestEasy
        
        // used by RestEasy to determine the JAX-RS resources and other related configuration
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
    public boolean isApplicable(ServletContext ctx) {
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
        requireNonNull(restfulPathConfigValue, "This web-module needs to be prepared first.");
        final String restfulPathEnclosedWithSlashes = suffix(prefix(restfulPathConfigValue, "/"), "/");
        return restfulPathEnclosedWithSlashes;
    }
    
    

    
}
