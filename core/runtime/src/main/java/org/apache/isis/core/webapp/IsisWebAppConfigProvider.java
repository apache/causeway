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

import static org.apache.isis.commons.internal.context._Context.getOrThrow;
import static org.apache.isis.commons.internal.context._Context.putSingleton;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceCurrentClassClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceFileSystem;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerInitParameters;
import org.apache.isis.core.webapp.config.ResourceStreamSourceForWebInf;

/**
 * Provides IsisConfigurationBuilder instances.
 *  
 * @since 2.0.0
 */
public class IsisWebAppConfigProvider {
    
    private static final Logger LOG = LoggerFactory.getLogger(IsisWebAppConfigProvider.class);
    
    /**
     * Removes any cashed IsisConfigurationBuilder instance from the ServletContext.
     * @param servletContext
     */
    public void invalidate(final ServletContext servletContext) {
        servletContext.setAttribute(WebAppConstants.CONFIGURATION_BUILDER_KEY, null);
    }
    
    /**
     * If available reuses the IsisConfigurationBuilder instance that is already on the ServletContext or
     * creates a new one. 
     * 
     * @param servletContext
     * @return
     */
    public IsisConfigurationBuilder getConfigurationBuilder(final ServletContext servletContext) {
        IsisConfigurationBuilder isisConfigurationBuilder =
                (IsisConfigurationBuilder) servletContext.getAttribute(WebAppConstants.CONFIGURATION_BUILDER_KEY);
        if(isisConfigurationBuilder == null) {
            isisConfigurationBuilder = newIsisConfigurationBuilder(servletContext);
        }
        return isisConfigurationBuilder;
    }
    
    /**
     * Shortcut for {@code getConfigurationBuilder(servletContext).peekConfiguration()}
     * @param servletContext
     * @return a configuration copy
     */
    public IsisConfiguration peekConfiguration(final ServletContext servletContext) {
        return getConfigurationBuilder(servletContext).peekConfiguration();
    }

    /**
     * Returns a new IsisConfigurationBuilder populated with all currently available configuration values. 
     * @param servletContext
     * @return
     */
    protected IsisConfigurationBuilder newIsisConfigurationBuilder(final ServletContext servletContext) {
        IsisConfigurationBuilder isisConfigurationBuilder = new IsisConfigurationBuilder();
        isisConfigurationBuilder.primeWith(new OptionHandlerInitParameters(servletContext));
        addResourceStreamSources(servletContext, isisConfigurationBuilder);
        return isisConfigurationBuilder;
    }
    
    // -- LOOKUP
    
    /**
     * Register an instance of IsisWebAppConfigProvider as an application-scoped singleton.
     * @param configProvider
     */
    public static void register(IsisWebAppConfigProvider configProvider) {
        putSingleton(IsisWebAppConfigProvider.class, configProvider);
    }
    
    /**
     * @return the application-scoped singleton instance of IsisWebAppConfigProvider
     * @throws IllegalStateException if no such singleton was registered
     */
    public static IsisWebAppConfigProvider getInstance() {
        return getOrThrow(IsisWebAppConfigProvider.class, 
                ()->new IllegalStateException("No config provider registered on this context."));
    }
    
    // -- HELPER

    private static void addResourceStreamSources(
            final ServletContext servletContext,
            final IsisConfigurationBuilder isisConfigurationBuilder) {


        // will load either from WEB-INF, from the class-path or from config directory.
        final String configLocation = servletContext.getInitParameter(WebAppConstants.CONFIG_DIR_PARAM);
        if ( configLocation != null ) {
            LOG.info("Config override location: {}", configLocation );
            isisConfigurationBuilder.addResourceStreamSource(ResourceStreamSourceFileSystem.create(configLocation));
        } else {
            LOG.info("Config override location: No override location configured" );

            isisConfigurationBuilder.addResourceStreamSource(ResourceStreamSourceContextLoaderClassPath.create());
            isisConfigurationBuilder.addResourceStreamSource(new ResourceStreamSourceCurrentClassClassPath());
            isisConfigurationBuilder.addResourceStreamSource(new ResourceStreamSourceForWebInf(servletContext));
        }
    }
    
}
