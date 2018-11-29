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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.AppConfigLocator;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.NotFoundPolicy;
import org.apache.isis.config.IsisConfiguration.ContainsPolicy;
import org.apache.isis.config.builder.IsisConfigurationBuilder;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.webapp.modules.WebModule;
import org.apache.isis.core.webapp.modules.WebModuleContext;

import static org.apache.isis.commons.internal.base._With.acceptIfPresent;
import static org.apache.isis.commons.internal.resources._Resources.putContextPathIfPresent;

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
 * @since 2.0.0-M2
 *
 */
//@WebListener //[ahuber] to support Servlet 3.0 annotations @WebFilter, @WebListener or others 
//with skinny war deployment requires additional configuration, so for now we disable this annotation
public class IsisWebAppContextListener implements ServletContextListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(IsisWebAppContextListener.class);
    
    private final List<ServletContextListener> activeListeners = new ArrayList<>();

    // -- INTERFACE IMPLEMENTATION
    
    @Override
    public void contextInitialized(ServletContextEvent event) {

        final ServletContext servletContext = event.getServletContext();
        
        LOG.info("=== PHASE 1 === Setting up ServletContext parameters");
  
        //[ahuber] set the ServletContext initializing thread as preliminary default until overridden by
        // IsisWicketApplication#init() or others that better know what ClassLoader to use as application default.
        _Context.setDefaultClassLoader(Thread.currentThread().getContextClassLoader(), false);
        
        _Context.putSingleton(ServletContext.class, servletContext);
        
        putContextPathIfPresent(servletContext.getContextPath());
        
        @SuppressWarnings("unused") // finalize the config (build and regard immutable)
        IsisConfiguration isisConfiguration = AppConfigLocator.getAppConfig().isisConfiguration();
        
        //[2039] environment priming no longer suppoerted
        //_Config.acceptBuilder(IsisContext.EnvironmentPrimer::primeEnvironment);

        final WebModuleContext webModuleContext = new WebModuleContext(servletContext);
        
        final List<WebModule> webModules =
                 WebModule.discoverWebModules()
                 .peek(module->module.prepare(webModuleContext)) // prepare context
                 .collect(Collectors.toList());

        LOG.info("=== PHASE 2 === Initializing the ServletContext");
        
        webModules.stream()
        .filter(module->module.isApplicable(webModuleContext)) // filter those WebModules that are applicable
        .forEach(module->addListener(servletContext, module));
        
        activeListeners.forEach(listener->listener.contextInitialized(event));
        
        LOG.info("=== DONE === ServletContext initialized.");
    }

    void addConfigurationResourcesForDeploymentType(
            final IsisConfigurationBuilder isisConfigurationBuilder) {
        final String resourceName = 
                IsisContext.getEnvironment().getDeploymentType().name().toLowerCase() + ".properties";
        isisConfigurationBuilder.addConfigurationResource(resourceName, NotFoundPolicy.CONTINUE, ContainsPolicy.IGNORE);
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
