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

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;

import org.apache.isis.core.webapp.IsisWebAppContextListener;

/**
 * Introduced to render web.xml Filter/Listener/Servlet configurations obsolete.
 * <p>
 * WebModule instances are used by the {@link IsisWebAppContextListener} to setup 
 * the ServletContext programmatically.
 * </p>
 * 
 * @since 2.0.0
 */
public interface WebModule {
    
    // -- INTERFACE

    /**
     * @return (display-) name of this module
     */
    public String getName();
    
    /**
     * Before initializing any WebModule we call each WebModule's prepare method 
     * to allow for a WebModule to leave information useful for other modules on 
     * the shared WebModuleContext.
     * 
     * @param ctx WebModuleContext
     */
    default public void prepare(WebModuleContext ctx) {}
    
    /**
     * Expected to be called after all WebModules had a chance to prepare the WebModuleContext.
     * Sets this WebModule's {@link Filter}s, {@link Servlet}s or {@link WebListener}s 
     * up and registers them with the {@link ServletContext} as provided via {@code ctx}.
     * @param ctx ServletContext
     */
    public ServletContextListener init(ServletContext ctx) throws ServletException;
    
    /**
     * Expected to be called after all WebModules had a chance to prepare the WebModuleContext.
     * @param ctx WebModuleContext
     * @return whether this module is applicable/usable
     */
    public boolean isApplicable(WebModuleContext ctx);
    
    // -- DISCOVERY 
    
    /**
     * @return Stream of 'known' WebModules, whether applicable or not is not decided here 
     */
    static Stream<WebModule> discoverWebModules() {
        
        //TODO [ahuber] instead of providing a static list of modules, modules could be discovered on 
        // the class-path (in case we have plugins that provide such modules). 
        // We could use the AppManifest instead.
        // We need yet to decide a mechanism, that enforces a certain ordering of these modules, since
        // this influences the order in which filters are processed.
        
        return Stream.of(
                new WebModule_Shiro(), // filters before all others
                new WebModule_StaticResources(),
                new WebModule_Wicket(),
                new WebModule_RestEasy(), // default REST provider
                new WebModule_LogOnExceptionLogger() // log any logon exceptions, filters after all others
                );
    }
    
    
}
