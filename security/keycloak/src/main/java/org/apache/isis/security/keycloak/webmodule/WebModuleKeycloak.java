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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.webapp.modules.WebModuleAbstract;

import lombok.Getter;

/**
 * WebModule to enable support for Keycloak.
 *
 * @since 2.0 {@index}
 */
@Service
@Named("isis.security.WebModuleKeycloak")
@Order(OrderPrecedence.FIRST + 100)
@Qualifier("Keycloak")
public final class WebModuleKeycloak extends WebModuleAbstract {

    private static final String KEYCLOAK_FILTER_NAME = "KeycloakFilter";

    @Getter
    private final String name = "Keycloak";

    @Inject
    public WebModuleKeycloak(ServiceInjector serviceInjector) {
        super(serviceInjector);
    }

    @Override
    public Can<ServletContextListener> init(ServletContext ctx) throws ServletException {

        registerFilter(ctx, KEYCLOAK_FILTER_NAME, KeycloakFilter.class)
            .ifPresent(filterReg -> {
                filterReg.addMappingForUrlPatterns(
                        null,
                        false, // filter is forced first
                        "/*");

            });

        return Can.empty(); // registers no listeners
    }

}
