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
package org.apache.isis.core.webapp.modules.templresources;

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
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.webapp.modules.WebModuleAbstract;

import lombok.Getter;

/**
 * WebModule to provide static resources utilizing an in-memory cache.
 *
 * @since 2.0
 */
@Service
@Named("isis.webapp.WebModuleTemplateResources")
@javax.annotation.Priority(OrderPrecedence.MIDPOINT - 100)
@Qualifier("TemplateResources")
public final class WebModuleTemplateResources extends WebModuleAbstract {

    private static final String[] urlPatterns = { "*.thtml" };
    private static final int cacheTimeSeconds = 86400;

    private static final String FILTER_NAME = "TemplateResourceCachingFilter";
    private static final String SERVLET_NAME = "TemplateResourceServlet";

    @Getter
    private final String name = "TemplateResources";

    @Inject
    public WebModuleTemplateResources(final ServiceInjector serviceInjector) {
        super(serviceInjector);
    }

    @Override
    public Can<ServletContextListener> init(ServletContext ctx) throws ServletException {

        registerFilter(ctx, FILTER_NAME, TemplateResourceCachingFilter.class)
            .ifPresent(filterReg -> {
                filterReg.setInitParameter(
                        "CacheTime",
                        ""+cacheTimeSeconds);
                filterReg.addMappingForUrlPatterns(
                        null,
                        true,
                        urlPatterns);

            });

        registerServlet(ctx, SERVLET_NAME, TemplateResourceServlet.class)
            .ifPresent(servletReg -> {
                servletReg.addMapping(urlPatterns);
            });

        return Can.empty(); // registers no listeners
    }

}
