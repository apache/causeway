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
package org.apache.causeway.core.webapp.webappctx;

import java.util.EventListener;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.internal.base._Oneshot;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.core.webapp.modules.WebModule;
import org.apache.causeway.core.webapp.modules.WebModuleContext;

import lombok.extern.log4j.Log4j2;

/**
 *
 * Introduced to render web.xml Filter/Listener/Servlet configurations obsolete.
 * <p>
 * Acts as the single application entry-point for setting up the
 * ServletContext programmatically.
 * </p><p>
 * Installs {@link WebModule}s on the ServletContext.
 * </p>
 *
 * @since 2.0
 *
 */
@Component
@Log4j2
public class CausewayWebAppContextInitializer implements ServletContextInitializer {

    private static final _Oneshot oneshot = new _Oneshot();

    @Inject private ServiceRegistry serviceRegistry; // this dependency ensures Causeway has been initialized/provisioned
    @Inject private CausewayConfiguration causewayConfiguration;
    @Inject private WebAppContextPath webAppContextPath;

    // -- INTERFACE IMPLEMENTATION

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {

        // onStartup(...) must be a one shot, otherwise ignore with warning
        if(!oneshot.trigger()) {
            log.warn("Spring tries to startup this initializer more than once."
                    + " This is most likely a Spring configuration issue, check your bootstrapping setup.");
            return;
        }

        if(!isCausewayProvisioned()) {
            log.error("skipping initialization, Spring should already have provisioned all configured Beans");
            return;
        }

        // set the ServletContext initializing thread as preliminary default until overridden by
        // CausewayWicketApplication#init() or others, that better know what ClassLoader to use as application default.
        _Context.setDefaultClassLoader(Thread.currentThread().getContextClassLoader(), false);

        var contextPath = servletContext.getContextPath();

        log.info("=== PHASE 1 === Setting up ServletContext parameters, contextPath = " + contextPath);

        webAppContextPath.setContextPath(contextPath);

        var webModuleContext = new WebModuleContext(servletContext, causewayConfiguration, serviceRegistry);
        webModuleContext.prepare();

        log.info("=== PHASE 2 === Initializing the ServletContext");

        webModuleContext.init();
        servletContext.addListener(new ShutdownHook(this, webModuleContext));

        log.info("=== DONE === ServletContext initialized.");

    }

    public void contextDestroyed(final WebModuleContext webModuleContext, final ServletContextEvent event) {
        if(webModuleContext!=null) {
            log.info("about to destroy the context");
            webModuleContext.shutdown(event);
        }
        oneshot.reset();
        log.info("context destroyed");
    }

    // -- HELPER

    private record ShutdownHook(
            CausewayWebAppContextInitializer self,
            WebModuleContext webModuleContext) implements EventListener, ServletContextListener {

        @Override
        public void contextDestroyed(final ServletContextEvent sce) {
            self.contextDestroyed(webModuleContext, sce);
        }
    }

    private boolean isCausewayProvisioned() {
        return serviceRegistry!=null;
    }

}
