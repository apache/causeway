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
package org.apache.isis.testing.h2console.ui.webmodule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.h2.server.web.WebServlet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.webapp.modules.WebModuleAbstract;
import org.apache.isis.core.webapp.modules.WebModuleContext;

import lombok.Getter;
import lombok.val;

@Service
@Named("isisTstH2Console.WebModuleH2Console")
@Order(OrderPrecedence.MIDPOINT)
@Qualifier("H2Console")
public class WebModuleH2Console extends WebModuleAbstract {

    private final static String SERVLET_NAME = "H2Console";
    private final static String CONSOLE_PATH = "/db";

    @Getter
    private final LocalResourcePath localResourcePathIfEnabled;

    private final IsisSystemEnvironment isisSystemEnvironment;
    private final IsisConfiguration isisConfiguration;

    private final boolean applicable;

    @Inject
    public WebModuleH2Console(
            final IsisSystemEnvironment isisSystemEnvironment,
            final IsisConfiguration isisConfiguration,
            final ServiceInjector serviceInjector) {
        super(serviceInjector);
        this.isisSystemEnvironment = isisSystemEnvironment;
        this.isisConfiguration = isisConfiguration;

        this.applicable = isPrototyping() && isUsesH2MemConnection();
        this.localResourcePathIfEnabled = applicable ? new LocalResourcePath(CONSOLE_PATH) : null;
    }

    @Getter
    private final String name = "H2Console";


    @Override
    public Can<ServletContextListener> init(final ServletContext ctx) throws ServletException {

        registerServlet(ctx, SERVLET_NAME, WebServlet.class)
            .ifPresent(servletReg -> {
                servletReg.addMapping(CONSOLE_PATH + "/*");
                servletReg.setInitParameter("webAllowOthers", "true");
            });

        return Can.empty(); // registers no listeners
    }

    @Override
    public boolean isApplicable(WebModuleContext ctx) {
        return applicable;
    }

    // -- HELPER

    private boolean isPrototyping() {
        return isisSystemEnvironment.getDeploymentType().isPrototyping();
    }

    private boolean isUsesH2MemConnection() {
        val connectionUrl = isisConfiguration.getPersistence().getJdoDatanucleus().getImpl().getJavax().getJdo().getOption().getConnectionUrl();
        return !_Strings.isNullOrEmpty(connectionUrl) && connectionUrl.contains(":h2:mem:");
    }

}
