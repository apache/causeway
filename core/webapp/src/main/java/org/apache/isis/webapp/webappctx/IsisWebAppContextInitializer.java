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
package org.apache.isis.webapp.webappctx;

import java.util.EventListener;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Oneshot;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.viewer.wicket.WebAppContextPath;
import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;

import lombok.NonNull;
import lombok.Value;
import lombok.val;
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
public class IsisWebAppContextInitializer implements ServletContextInitializer {
    
    private final static _Oneshot oneshot = new _Oneshot();
    
    @Inject private ServiceRegistry serviceRegistry; // this dependency ensures Isis has been initialized/provisioned
    @Inject private IsisConfiguration isisConfiguration;
    @Inject private WebAppContextPath webAppContextPath;

    // -- INTERFACE IMPLEMENTATION
    
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        // onStartup(...) must be a one shot, otherwise just ignore 
        if(!oneshot.shoot()) {
            return;
        }
        
        if(!isIsisProvisioned()) {
            log.error("skipping initialization, Spring should already have provisioned all configured Beans");
            return;
        }
        
        // set the ServletContext initializing thread as preliminary default until overridden by
        // IsisWicketApplication#init() or others, that better know what ClassLoader to use as application default.
        _Context.setDefaultClassLoader(Thread.currentThread().getContextClassLoader(), false);
        
        val contextPath = servletContext.getContextPath();

        log.info("=== PHASE 1 === Setting up ServletContext parameters, contextPath = " + contextPath);

        webAppContextPath.setContextPath(contextPath);

        val webModuleContext = new WebModuleContext(servletContext, isisConfiguration, serviceRegistry);
        webModuleContext.prepare();

        log.info("=== PHASE 2 === Initializing the ServletContext");

        webModuleContext.init();
        servletContext.addListener(new ShutdownHook(webModuleContext));
        
        log.info("=== DONE === ServletContext initialized.");

    }

    public void contextDestroyed(WebModuleContext webModuleContext, ServletContextEvent event) {
        if(webModuleContext!=null) {
            log.info("about to destroy the context");
            webModuleContext.shutdown(event);
        }
        oneshot.reset();
        log.info("context destroyed");
    }

    // -- HELPER
    
    @Value
    private class ShutdownHook implements EventListener, ServletContextListener {
        @NonNull WebModuleContext webModuleContext;
        
        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            IsisWebAppContextInitializer.this.contextDestroyed(webModuleContext, sce);
        }
    }

    private boolean isIsisProvisioned() {
        return serviceRegistry!=null;
    }



}
