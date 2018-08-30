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
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.core.webapp.IsisWebAppContextListener;

/**
 * Introduced to render web.xml listener configurations obsolete.
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
     * Sets the WebListener up and registers it with the given ServletContext {@code ctx}.
     * @param ctx ServletContext
     */
    public ServletContextListener init(ServletContext ctx) throws ServletException;
    
    /**
     * @param ctx ServletContext
     * @return whether this module is available (on the class-path) and also applicable 
     */
    public boolean isAvailable(ServletContext ctx);
    
    // -- DISCOVERY 
    
    /**
     * @return Stream of 'known' WebModules, whether applicable or not is not decided here 
     */
    static Stream<WebModule> discoverWebModules() {
        
        //TODO [ahuber] instead of providing a static list of modules, modules could be discovered on 
        // the class-path (in case we have plugins that provide such modules).
        // We need yet to decide a mechanism, that enforces a certain ordering of these modules, since
        // this influences the order in which filters are processed.
        
        return Stream.of(
                new WebModule_Shiro(), // filters before all others
                new WebModule_Wicket(),
                new WebModule_NoWicket(), // not required if the Wicket viewer is in use
                new WebModule_RestEasy(), // default REST provider
                new WebModule_LogOnExceptionLogger() // log any logon exceptions, filters after all others
                );
    }
    
}
