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
package org.apache.isis.security.keycloak.webmodule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.var;

/**
 * WebModule to enable support for Keycloak.
 */
@Service
@Named("isisSecurityKeycloak.WebModuleKeycloak")
@Order(OrderPrecedence.HIGHEST + 100)
@Qualifier("Keycloak")
@Log4j2
public final class WebModuleKeycloak implements WebModule  {

    private final static String KEYCLOAK_FILTER_NAME = "KeycloakFilter";

    @Getter
    private final String name = "Keycloak";

    private final ServiceInjector serviceInjector;

    @Inject
    public WebModuleKeycloak(ServiceInjector serviceInjector) {
        this.serviceInjector = serviceInjector;
    }

    @Override
    public void prepare(WebModuleContext ctx) {
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        var filter = ctx.addFilter(KEYCLOAK_FILTER_NAME, KeycloakFilter.class);
        if (filter != null) {
            filter.addMappingForUrlPatterns(
                    null,
                    false, // filter is forced first
                    "/*");
        } else {
            // was already registered, eg in web.xml.
        }

        return null;
    }

}
