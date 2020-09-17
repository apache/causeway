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
package org.apache.isis.viewer.wicket.viewer.webmodule;

import static java.util.Objects.requireNonNull;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.wicket.protocol.http.WicketFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.webapp.modules.WebModuleAbstract;
import org.apache.isis.core.webapp.modules.WebModuleContext;

import lombok.Getter;

/**
 * WebModule that provides the Wicket Viewer.
 * @since 2.0
 */
@Service
@Named("isisWicketViewer.WebModuleWicket")
@Order(OrderPrecedence.MIDPOINT - 80)
@Qualifier("Wicket")
public final class WebModuleWicket extends WebModuleAbstract {

    private static final String WICKET_FILTER_NAME = "WicketFilter";

    private final IsisSystemEnvironment isisSystemEnvironment;
    private final IsisConfiguration isisConfiguration;

    private final String wicketBasePath;
    private final String deploymentMode;
    private final String wicketApp;
    private final String urlPattern;

    @Inject
    public WebModuleWicket(
            final IsisSystemEnvironment isisSystemEnvironment,
            final IsisConfiguration isisConfiguration,
            final ServiceInjector serviceInjector) {
        super(serviceInjector);

        this.isisSystemEnvironment = isisSystemEnvironment;
        this.isisConfiguration = isisConfiguration;

        this.wicketBasePath = this.isisConfiguration.getViewer().getWicket().getBasePath();

        deploymentMode = this.isisSystemEnvironment.isPrototyping()
                ? "development"
                : "deployment";

        wicketApp = isisConfiguration.getViewer().getWicket().getApp();

        requireNonNull(wicketBasePath, "Config property 'isis.viewer.wicket.base-path' is required.");
        requireNonNull(wicketApp, "Config property 'isis.viewer.wicket.app' is required.");

        this.urlPattern = wicketBasePath + "*";
    }

    @Getter
    private final String name = "Wicket";

    @Override
    public void prepare(final WebModuleContext ctx) {
        super.prepare(ctx);
        ctx.addProtectedPath(this.urlPattern);
    }

    @Override
    public Can<ServletContextListener> init(final ServletContext ctx) throws ServletException {

        registerFilter(ctx, WICKET_FILTER_NAME, WicketFilter.class)
            .ifPresent(filterReg -> {
                filterReg.setInitParameter("applicationClassName", wicketApp);
                filterReg.setInitParameter("filterMappingUrlPattern", urlPattern);
                filterReg.setInitParameter("configuration", deploymentMode);
                filterReg.addMappingForUrlPatterns(
                        null,
                        true,
                        urlPattern);
            });

        return Can.empty(); // registers no listeners
    }

}
