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
package org.apache.isis.core.webapp.modules.sse;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.core.webapp.modules.WebModule;
import org.apache.isis.core.webapp.modules.WebModuleContext;
import org.springframework.core.annotation.Order;

import lombok.val;

/**
 * WebModule providing support for Server Sent Events.
 * 
 * @since 2.0.0-M3
 */
@Singleton @Order(-99)
public final class WebModuleServerSentEvents implements WebModule  {

    private final static String SERVLET_NAME = "ServerSentEventsServlet";
    
    @Override
    public String getName() {
        return "ServerSentEvents";
    }
    
    @Override
    public void prepare(WebModuleContext ctx) {
        // nothing special required
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        val servlet = ctx.addServlet(SERVLET_NAME, ServerSentEventsServlet.class);
        servlet.setAsyncSupported(true);
        
        ctx.getServletRegistration(SERVLET_NAME)
        .addMapping("/sse");
        
        return null; // does not provide a listener
    }

    @Override
    public boolean isApplicable(WebModuleContext ctx) {
        return true;
    }

}
