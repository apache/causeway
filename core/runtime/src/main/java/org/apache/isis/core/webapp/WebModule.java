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
package org.apache.isis.core.webapp;

import java.util.stream.Stream;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

/**
 * @since 2.0.0
 */
public interface WebModule {
    
    // -- INTERFACE

    /**
     * @return (display-) name of this provider
     */
    public String getName();
    
    /**
     * Sets the WebListener up and registers it with the given ServletContext {@code ctx}.
     * @param ctx ServletContext
     */
    public ServletContextListener init(ServletContext ctx) throws ServletException;
    
    /**
     * @param ctx ServletContext
     * @return whether this provider is available on the class-path and also applicable 
     */
    public boolean isAvailable(ServletContext ctx);
    
    // -- DISCOVERY 
    
    /**
     * Searches the class-path for 'bootstrappers'. 
     * @return stream of 'bootstrappers'
     */
    static Stream<WebModule> discoverWebModules() {
        
        return Stream.of(
                new WebModule_Shiro(),
                new WebModule_Wicket(),
                new WebModule_NoWicket(), // not required if the Wicket viewer is in use
                new WebModule_RestEasy(), // default REST provider
                new WebModule_LogOnExceptionLogger() // log any logon exceptions
                );
    }
    
}
