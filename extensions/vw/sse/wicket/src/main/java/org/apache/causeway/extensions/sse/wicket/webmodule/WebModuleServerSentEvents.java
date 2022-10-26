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
package org.apache.causeway.extensions.sse.wicket.webmodule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.webapp.modules.WebModuleAbstract;

import lombok.Getter;

/**
 * WebModule providing support for Server Sent Events.
 *
 * @since 2.0 {@index}
 */
@Service
@Named("causeway.val.WebModuleServerSentEvents")
@Qualifier("ServerSentEvents")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
public final class WebModuleServerSentEvents extends WebModuleAbstract {

    private static final String SERVLET_NAME = "ServerSentEventsServlet";

    @Getter
    private final String name = "ServerSentEvents";

    @Inject
    public WebModuleServerSentEvents(ServiceInjector serviceInjector) {
        super(serviceInjector);
    }


    @Override
    public Can<ServletContextListener> init(ServletContext ctx) throws ServletException {

        registerServlet(ctx, SERVLET_NAME, ServerSentEventsServlet.class)
            .ifPresent(servletReg -> {
                servletReg.setAsyncSupported(true);
                servletReg.addMapping("/sse");
            });

        return Can.empty(); // registers no listeners
    }


}
