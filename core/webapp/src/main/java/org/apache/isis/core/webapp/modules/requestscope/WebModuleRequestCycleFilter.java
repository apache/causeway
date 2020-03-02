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
package org.apache.isis.core.webapp.modules.requestscope;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.webapp.modules.WebModuleAbstract;

import lombok.Getter;

/**
 * WebModule to hook into request-cycles of protected servlets.
 * Allows the framework to make a unique IsisSession instance available for each request-cycle. 
 * 
 * @implSpec filter must be configured with the servlet context filter chain after 
 * an AuthenticationSession was made available  
 * 
 * @since 2.0
 */
@Service
@Named("isisWebapp.WebModuleLogOnExceptionLogger")
@Order(OrderPrecedence.EARLY - 99)
@Qualifier("WebModuleRequestCycleFilter")
public final class WebModuleRequestCycleFilter
extends WebModuleAbstract {

    private static final String FILTER_NAME = "IsisRequestCycleFilter";


    @Getter
    private final String name = "Request Cycle Filter";

    @Inject
    public WebModuleRequestCycleFilter(final ServiceInjector serviceInjector) {
        super(serviceInjector);
    }


    @Override
    public Can<ServletContextListener> init(ServletContext ctx) throws ServletException {

        registerFilter(ctx, FILTER_NAME, IsisRequestCycleFilter.class)
            .ifPresent(filterReg -> {
                filterReg.addMappingForUrlPatterns(
                        null,
                        true, // filterReg is forced last
                        webModuleContext.getProtectedPaths().toArray(String.class));
            });

        return Can.empty(); // registers no listeners
    }


    // -- HELPER


}
