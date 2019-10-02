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
package org.apache.isis.viewer.wicket.viewer;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.commons.internal.plugins.environment.IsisSystemEnvironment;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;

import static java.util.Objects.requireNonNull;
import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.base._Strings.prefix;
import static org.apache.isis.commons.internal.base._Strings.suffix;
import static org.apache.isis.commons.internal.context._Context.getDefaultClassLoader;
import static org.apache.isis.commons.internal.exceptions._Exceptions.unexpectedCodeReach;

import lombok.val;

/**
 * WebModule that provides the Wicket Viewer.
 * @since 2.0
 */
@Service @Order(-80)
public final class WebModuleWicket implements WebModule  {

    private final static String WICKET_FILTER_CLASS_NAME = 
            "org.apache.wicket.protocol.http.WicketFilter";

    private final static String WICKET_FILTER_NAME = "WicketFilter";

    private String pathConfigValue;
    private String deploymentMode;
    private String appConfigValue;

    @Inject IsisSystemEnvironment isisSystemEnvironment;

    @Override
    public String getName() {
        return "Wicket";
    }

    @Override
    public void prepare(WebModuleContext ctx) {

        if(!isAvailable()) {
            return;
        }

        val wicketConf = ctx.getConfiguration().getViewer().getWicket();
        
        pathConfigValue = wicketConf.getBasePath();

        deploymentMode = isisSystemEnvironment.isPrototyping()
                ? "development" 
                        : "deployment";

        appConfigValue = wicketConf.getApp();

        ctx.setHasBootstrapper();
        ctx.addViewer("wicket");
        ctx.addProtectedPath(suffix(prefix(pathConfigValue, "/"), "/") + "*");
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        final Filter filter;
        try {
            final Class<?> filterClass = getDefaultClassLoader().loadClass(WICKET_FILTER_CLASS_NAME);
            filter = ctx.createFilter(uncheckedCast(filterClass));
        } catch (ClassNotFoundException e) {
            // guarded against by isAvailable()
            throw unexpectedCodeReach();
        }

        final Dynamic reg = ctx.addFilter(WICKET_FILTER_NAME, filter);
        if(reg==null) {
            return null; // filter was already registered somewhere else (eg web.xml)
        }

        final String urlPattern = getUrlPattern();

        reg.setInitParameter("applicationClassName", getApplicationClassName());
        reg.setInitParameter("filterMappingUrlPattern", urlPattern);
        reg.setInitParameter("configuration", getWicketMode()); 
        reg.addMappingForUrlPatterns(null, true, urlPattern);

        return null; // does not provide a listener
    }

    @Override
    public boolean isApplicable(WebModuleContext ctx) {
        return isAvailable();
    }

    // -- HELPER

    private static boolean isAvailable() {
        try {
            getDefaultClassLoader().loadClass(WICKET_FILTER_CLASS_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getUrlPattern() {
        final String wicketPath = getWicketPath();
        final String wicketPathEnclosedWithSlashes = suffix(prefix(wicketPath, "/"), "/");
        final String urlPattern = wicketPathEnclosedWithSlashes + "*";
        return urlPattern;
    }

    private String getWicketPath() {
        return requireNonNull(pathConfigValue, "This web-module needs to be prepared first.");
    }

    private String getWicketMode() {
        return requireNonNull(deploymentMode, "This web-module needs to be prepared first.");
    }

    private String getApplicationClassName() {
        return requireNonNull(appConfigValue, "This web-module needs to be prepared first.");

    }

}
