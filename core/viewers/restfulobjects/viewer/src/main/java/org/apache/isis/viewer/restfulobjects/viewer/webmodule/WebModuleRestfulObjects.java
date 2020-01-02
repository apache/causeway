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
package org.apache.isis.viewer.restfulobjects.viewer.webmodule;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import lombok.var;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.viewer.restfulobjects.viewer.webmodule.auth.AuthenticationSessionStrategyBasicAuth;
import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.context._Context.getDefaultClassLoader;
import static org.apache.isis.commons.internal.exceptions._Exceptions.unexpectedCodeReach;
import static org.apache.isis.commons.internal.resources._Resources.putRestfulPath;

/**
 * WebModule that provides the RestfulObjects Viewer.
 * 
 * @since 2.0
 */
@Service
@Named("isisRoViewer.WebModuleRestfulObjects")
@Order(OrderPrecedence.MIDPOINT - 80)
@Qualifier("RestfulObjects")
@Log4j2
public final class WebModuleRestfulObjects implements WebModule  {

    private final static String RESTEASY_BOOTSTRAPPER = "org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap";
    private final static String RESTEASY_DISPATCHER = "RestfulObjectsRestEasyDispatcher";
    public static final String ISIS_SESSION_FILTER_FOR_RESTFUL_OBJECTS = "IsisSessionFilterForRestfulObjects";

    private final IsisConfiguration isisConfiguration;
    private final String restfulPath;

    @Inject
    public WebModuleRestfulObjects(final IsisConfiguration isisConfiguration) {
        this.isisConfiguration = isisConfiguration;
        this.restfulPath = this.isisConfiguration.getViewer().getRestfulobjects().getBasePath();
    }


    @Getter
    private final String name = "RestEasy";

    @Override
    public void prepare(WebModuleContext ctx) {

        if(!isApplicable(ctx)) {
            return;
        }

        putRestfulPath(this.restfulPath);

        // register this module as a viewer
        ctx.addViewer("restfulobjects");
        ctx.addProtectedPath(this.restfulPath + "*");
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        // add IsisSessionFilters

        {
            val filter = ctx.addFilter(
                    ISIS_SESSION_FILTER_FOR_RESTFUL_OBJECTS, IsisRestfulObjectsSessionFilter.class);
            if(filter != null) {

                // this is mapped to the entire application;
                // however the IsisSessionFilter will
                // "notice" if the session filter has already been
                // executed for the request pipeline, and if so will do nothing
                filter.addMappingForServletNames(
                        null,
                        true,
                        RESTEASY_DISPATCHER);   // applies only to requests that are served by the RestEasy dispatcher

                filter.setInitParameter(
                        "authenticationSessionStrategy",
                        AuthenticationSessionStrategyBasicAuth.class.getName());
                filter.setInitParameter(
                        "whenNoSession", // what to do if no session was found ...
                        "auto"); // ... 401 and a basic authentication challenge if request originates from web browser
                filter.setInitParameter(
                        "passThru",
                        String.join(",", getRestfulPath()+"swagger", getRestfulPath()+"health"));
            }
        }

        {
            val filter = ctx.addFilter(RESTEASY_DISPATCHER,
                    IsisTransactionFilterForRestfulObjects.class);
            if(filter != null) {
                filter.addMappingForServletNames(
                        null,
                        true,
                        RESTEASY_DISPATCHER); // applies only to requests that are served by the RestEasy dispatcher
            }
        }



        // add RestEasy

        // used by RestEasy to determine the JAX-RS resources and other related configuration
        ctx.setInitParameter(
                "javax.ws.rs.Application", 
                org.apache.isis.viewer.restfulobjects.viewer.jaxrsapp.RestfulObjectsApplication.class.getName());

        ctx.setInitParameter("resteasy.servlet.mapping.prefix", getRestfulPath());

        var servlet = ctx.addServlet(RESTEASY_DISPATCHER,
                "org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher");
        if(servlet != null) {
            servlet.addMapping(getUrlPattern());
        }

        try {
            final Class<?> listenerClass = getDefaultClassLoader().loadClass(RESTEASY_BOOTSTRAPPER);
            return ctx.createListener(uncheckedCast(listenerClass));
        } catch (ClassNotFoundException e) {
            // guarded against by isAvailable()
            throw unexpectedCodeReach();
        }

    }


    // -- HELPER

    private String getUrlPattern() {
        return getRestfulPath() + "*";
    }

    private String getRestfulPath() {
        return this.restfulPath;
    }

}
