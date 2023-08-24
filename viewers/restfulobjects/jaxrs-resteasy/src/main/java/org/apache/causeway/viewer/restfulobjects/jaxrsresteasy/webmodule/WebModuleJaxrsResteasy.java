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
package org.apache.causeway.viewer.restfulobjects.jaxrsresteasy.webmodule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.jboss.resteasy.core.providerfactory.ResteasyProviderFactoryImpl;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.RestEasyConfiguration;
import org.apache.causeway.core.webapp.modules.WebModuleAbstract;
import org.apache.causeway.core.webapp.modules.WebModuleContext;
import org.apache.causeway.viewer.restfulobjects.applib.CausewayModuleViewerRestfulObjectsApplib;
import org.apache.causeway.viewer.restfulobjects.viewer.webmodule.CausewayRestfulObjectsInteractionFilter;

import lombok.Getter;
import lombok.val;

/**
 * WebModule that provides the RestfulObjects Viewer.
 *
 * @since 2.0 {@index}
 *
 * @implNote CDI feels responsible to resolve injection points for any Servlet or Filter
 * we register programmatically on the ServletContext.
 * As long as injection points are considered to be resolved by Spring, we can workaround this fact:
 * By replacing annotations {@code @Inject} with {@code @Autowire} for any Servlet or Filter,
 * that get contributed by a WebModule, these will be ignored by CDI.
 *
 */
@Service
//CAUTION: SwaggerServiceMenu refers to this name
@Named(CausewayModuleViewerRestfulObjectsApplib.NAMESPACE + ".WebModuleJaxrsRestEasy")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT - 80)
@Qualifier("JaxrsRestEasy")
public final class WebModuleJaxrsResteasy extends WebModuleAbstract {

    private static final String INTERACTION_FILTER_NAME = "CausewayRestfulObjectsInteractionFilter";

    private final CausewayConfiguration causewayConfiguration;
    private final RestEasyConfiguration restEasyConfiguration;

    private final String restfulPath;
    private final String urlPattern;

    @Inject
    public WebModuleJaxrsResteasy(
            final CausewayConfiguration causewayConfiguration,
            final RestEasyConfiguration restEasyConfiguration,
            final ServiceInjector serviceInjector) {
        super(serviceInjector);
        this.causewayConfiguration = causewayConfiguration;
        this.restEasyConfiguration = restEasyConfiguration;
        this.restfulPath = this.restEasyConfiguration.getJaxrs().getDefaultPath() + "/";
        this.urlPattern = this.restfulPath + "*";
    }

    @Getter
    private final String name = "JaxrsRestEasy";

    @Override
    public void prepare(final WebModuleContext ctx) {

        // forces RuntimeDelegate.getInstance() to be provided by RestEasy
        // (and not by eg. the JEE container if any)
        ResteasyProviderFactory.setInstance(new ResteasyProviderFactoryImpl());

        super.prepare(ctx);

        if(!isApplicable(ctx)) {
            return;
        }

        ctx.addProtectedPath(urlPattern);
    }

    @Override
    public Can<ServletContextListener> init(final ServletContext ctx) throws ServletException {

        val authenticationStrategyClassName = causewayConfiguration.getViewer()
                .getRestfulobjects().getAuthentication().getStrategyClassName();

        registerFilter(ctx, INTERACTION_FILTER_NAME, CausewayRestfulObjectsInteractionFilter.class)
        .ifPresent(filterReg -> {
            // this is mapped to the entire application;
            // however the CausewayRestfulObjectsInteractionFilter will
            // "notice" if the session filter has already been
            // executed for the request pipeline, and if so will do nothing
            filterReg.addMappingForUrlPatterns(
                    null,
                    true,
                    this.urlPattern);

            filterReg.setInitParameter(
                    "authenticationStrategy",
                    authenticationStrategyClassName);
            filterReg.setInitParameter(
                    "whenNoSession", // what to do if no session was found ...
                    "auto"); // ... 401 and a basic authentication challenge if request originates from web browser
            filterReg.setInitParameter(
                    "passThru",
                    String.join(",",
                            this.restfulPath + "swagger",
                            this.restfulPath + "health"));

        } );

        return Can.empty(); // registers no listeners
    }


}
