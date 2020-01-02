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
package org.apache.isis.extensions.sse.webmodule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.val;

/**
 * WebModule providing support for Server Sent Events.
 * 
 * @since 2.0
 */
@Service
@Named("isisExtSse.WebModuleServerSentEvents")
@Qualifier("ServerSentEvents")
@Order(OrderPrecedence.MIDPOINT)
@Log4j2
public final class WebModuleServerSentEvents implements WebModule  {

    private final static String SERVLET_NAME = "ServerSentEventsServlet";

    @Getter
    private final String name = "ServerSentEvents";

    private final ServiceInjector serviceInjector;

    @Inject
    public WebModuleServerSentEvents(ServiceInjector serviceInjector) {
        this.serviceInjector = serviceInjector;
    }

    @Override
    public void prepare(WebModuleContext ctx) {
        // nothing special required
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        val servlet = ctx.addServlet(SERVLET_NAME, ServerSentEventsServlet.class);
        if(servlet != null) {
            serviceInjector.injectServicesInto(servlet);
            servlet.setAsyncSupported(true);
            servlet.addMapping("/sse");
        } else {
            // was already registered, eg in web.xml.
        }

        return null; // does not provide a listener
    }


}
