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
package org.apache.isis.webapp.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.webapp.webappctx.IsisWebAppContextInitializer.ServletContextResource;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @since 2.0
 *
 */
@Log4j2 @RequiredArgsConstructor
public class WebModuleContext {

    private boolean hasBootstrapper = false;
    private final StringBuilder viewers = new StringBuilder();
    private final StringBuilder protectedPath = new StringBuilder();

    @NonNull @Getter private final ServletContextResource servletContextResource;
    @NonNull @Getter private final IsisConfiguration configuration;
    @NonNull @Getter private final ServiceRegistry serviceRegistry;
    
    private List<WebModule> webModules;
    private final List<ServletContextListener> activeListeners = new ArrayList<>();

    /**
     * Tell other modules that a bootstrapper is present.
     */
    public void setHasBootstrapper() {
        hasBootstrapper = true;
    }

    /**
     * @return whether this context has a bootstrapper
     */
    public boolean hasBootstrapper() {
        return hasBootstrapper;    
    }

    /**
     *  Adds to the list of viewer names (<tt>isis.viewers</tt> context param)
     * @param viewerName
     */
    public void addViewer(String viewerName) {
        if(viewers.length()>0) {
            viewers.append(",");
        } 
        viewers.append(viewerName);
    }

    /**
     *  Adds to the list of protected path names (<tt>isis.protected</tt> context param)
     * @param path
     */
    public void addProtectedPath(String path) {
        if(protectedPath.length()>0) {
            protectedPath.append(",");
        } 
        protectedPath.append(path);
    }

    /**
     * Streams the protected path names (<tt>isis.protected</tt> context param)
     */
    public Stream<String> streamProtectedPaths() {
        final String list = protectedPath.toString();
        return _Strings.splitThenStream(list, ",");
    }
    public String[] getProtectedPaths() {
        return streamProtectedPaths().collect(_Arrays.toArray(String.class));
    }

    public void prepare() {
        webModules =
                WebModule.discoverWebModules(serviceRegistry)
                .peek(module->module.prepare(this)) // prepare context
                .collect(Collectors.toList());
    }

    public void init() {

        val event = new ServletContextEvent(servletContextResource.getServletContextOneShot());

        webModules.stream()
        .filter(module->module.isApplicable(this)) // filter those WebModules that are applicable
        .forEach(module->addListener(event.getServletContext(), module));

        activeListeners.forEach(listener->listener.contextInitialized(event));
    }

    public void shutdown(ServletContextEvent event) {
        activeListeners.forEach(listener->shutdownListener(event, listener));
        activeListeners.clear();
    }

    // -- HELPER

    private void addListener(ServletContext context, WebModule module) {
        log.info(String.format("Setup ServletContext, adding WebModule '%s'", module.getName()));
        try {
            final List<ServletContextListener> listeners = module.init(context);
            if(listeners != null) {
                activeListeners.addAll(listeners);
            }
        } catch (ServletException e) {
            log.error(String.format("Failed to add WebModule '%s' to the ServletContext.", module.getName()), e);
        }  
    }

    private void shutdownListener(ServletContextEvent event, ServletContextListener listener) {
        try {
            listener.contextDestroyed(event);
        } catch (Exception e) {
            log.error(String.format("Failed to shutdown WebListener '%s'.", listener.getClass().getName()), e);
        }
    }

}
