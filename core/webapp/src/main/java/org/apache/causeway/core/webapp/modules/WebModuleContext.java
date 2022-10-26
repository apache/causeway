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
package org.apache.causeway.core.webapp.modules;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration;

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
@RequiredArgsConstructor
@Log4j2
public class WebModuleContext {

    private final StringBuilder protectedPath = new StringBuilder();

    @NonNull @Getter private final ServletContext servletContext;
    @NonNull @Getter private final CausewayConfiguration configuration;
    @NonNull @Getter private final ServiceRegistry serviceRegistry;

    private Can<WebModule> webModules;
    private final List<ServletContextListener> activeListeners = new ArrayList<>();

    /**
     *  Adds to the list of protected path names (<tt>causeway.protected</tt> context param)
     * @param path
     */
    public void addProtectedPath(String path) {
        if(protectedPath.length()>0) {
            protectedPath.append(",");
        }
        protectedPath.append(path);
    }

    /**
     * immutable list of protected path names (<tt>causeway.protected</tt> context param)
     */
    public Can<String> getProtectedPaths() {
        final String listLiteral = protectedPath.toString();
        return Can.<String>ofStream(_Strings.splitThenStream(listLiteral, ","));
    }

    public void prepare() {
        webModules = WebModule.discoverWebModules(serviceRegistry);
        webModules.forEach(module->module.prepare(this));
    }

    public void init() {

        val event = new ServletContextEvent(servletContext);

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

    private void addListener(ServletContext servletContext, WebModule webModule) {
        log.info(String.format("Setup ServletContext, adding WebModule '%s'", webModule.getName()));
        try {
            final Can<ServletContextListener> listeners = webModule.init(servletContext);
            if(listeners != null && !listeners.isEmpty()) {
                activeListeners.addAll(listeners.toList());
            }
        } catch (ServletException e) {
            log.error(String.format("Failed to add WebModule '%s' to the ServletContext.", webModule.getName()), e);
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
