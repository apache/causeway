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
package org.apache.isis.extensions.cors.webmodule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.ebaysf.web.cors.CORSFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import lombok.var;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.context._Context.getDefaultClassLoader;
import static org.apache.isis.commons.internal.exceptions._Exceptions.unexpectedCodeReach;

/**
 * WebModule providing support for CORS
 * 
 * @since 2.0
 */
@Service
@Named("isisExtCors.WebModuleServerCors")
@Qualifier("CORS")
@Order(OrderPrecedence.HIGH)
@Log4j2
public final class WebModuleCors implements WebModule  {

    private final static String CORS_FILTER_NAME = "CORS Filter";

    @Getter
    private final String name = "CORS";

    private final ServiceInjector serviceInjector;

    private WebModuleContext webModuleContext;

    @Inject
    public WebModuleCors(ServiceInjector serviceInjector) {
        this.serviceInjector = serviceInjector;
    }


    @Override
    public void prepare(final WebModuleContext webModuleContext) {
        this.webModuleContext = webModuleContext;
    }

    /*
    <filter>
        <filter-name>CORS Filter</filter-name>
        <filter-class>org.ebaysf.web.cors.CORSFilter</filter-class>
        <init-param>
            <param-name>cors.allowed.origins</param-name>
            <param-value>*</param-value>
        </init-param>
        <init-param>
            <param-name>cors.allowed.headers</param-name>
            <param-value>Content-Type,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,Cache-Control,If-Modified-Since,Pragma</param-value>
        </init-param>
        <init-param>
            <param-name>cors.exposed.headers</param-name>
            <param-value>Authorization</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>CORS Filter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
     */
    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        var filter = ctx.addFilter(CORS_FILTER_NAME, CORSFilter.class);
        if (filter != null) {
            serviceInjector.injectServicesInto(filter);
            filter.setInitParameter("cors.allowed.origins", "*");
            filter.setInitParameter("cors.allowed.headers", "Content-Type,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,Cache-Control,If-Modified-Since,Pragma");
            filter.setInitParameter("cors.exposed.headers", "Authorization");

            filter.addMappingForUrlPatterns(
                    null,
                    false,
                    this.webModuleContext.getProtectedPaths());

        } else {
            // was already registered, eg in web.xml.
        }

        return null;
    }


}
