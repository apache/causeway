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
package org.apache.isis.core.webapp;

import static org.apache.isis.commons.internal.base._With.acceptIfPresent;
import static org.apache.isis.commons.internal.context._Context.setDefaultClassLoader;
import static org.apache.isis.commons.internal.resources._Resource.putContextPathIfPresent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.webapp.modules.WebModule;

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
 * @since 2.0.0
 *
 */
@WebListener
public class IsisWebAppContextListener implements ServletContextListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(IsisWebAppContextListener.class);
    
    private final List<ServletContextListener> activeListeners = new ArrayList<>();

    // -- INTERFACE IMPLEMENTATION
    
    @Override
    public void contextInitialized(ServletContextEvent event) {

        final ServletContext servletContext = event.getServletContext();
        
        LOG.info("=== PHASE 1 === Setting up ServletContext parameters");
        
        setDefaultClassLoader(this.getClass().getClassLoader(), true);
        putContextPathIfPresent(servletContext.getContextPath());
        
        final IsisWebAppConfigProvider configProvider = new IsisWebAppConfigProvider();
        IsisWebAppConfigProvider.register(configProvider);

        final List<WebModule> webModules =
                 WebModule.discoverWebModules()
                 .peek(module->module.prepare(servletContext)) // prepare context
                 .collect(Collectors.toList());

        // put the list of viewer names "isis.viewers" into a context parameter
        WebModule.ContextUtil.commitViewers(servletContext);
        // invalidate config such that next IsisConfigurationBuilder that gets obtained is reinitialized
        configProvider.invalidate(servletContext);  
        
        LOG.info("=== PHASE 2 === Initializing the ServletContext");
        
        webModules.stream()
        .filter(module->module.isApplicable(servletContext)) // filter those WebModules that are applicable
        .forEach(module->addListener(servletContext, module));
        
        activeListeners.forEach(listener->listener.contextInitialized(event));
        
        LOG.info("=== DONE === ServletContext initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        activeListeners.forEach(listener->shutdownListener(event, listener));
        activeListeners.clear();
    }
    
    // -- HELPER
    
    private void addListener(ServletContext context, WebModule module) {
        LOG.info(String.format("Setup ServletContext, adding WebModule '%s'", module.getName()));
        try {
            acceptIfPresent(module.init(context), activeListeners::add);
        } catch (ServletException e) {
            LOG.error(String.format("Failed to add WebModule '%s' to the ServletContext.", module.getName()), e);
        }  
    }
    
    private void shutdownListener(ServletContextEvent event, ServletContextListener listener) {
        try {
            listener.contextDestroyed(event);
        } catch (Exception e) {
            LOG.error(String.format("Failed to shutdown WebListener '%s'.", listener.getClass().getName()), e);
        }
    }

}
