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

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceCurrentClassClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceFileSystem;
import org.apache.isis.core.commons.resource.ResourceStreamSourceForWebInf;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerInitParameters;

/**
 *  
 * @since 2.0.0-M2
 */
public final class IsisWebAppConfigHelper {
    
    private static final Logger LOG = LoggerFactory.getLogger(IsisWebAppConfigHelper.class);
    private IsisWebAppConfigHelper() {}
    
    /**
     * Initializes the IsisConfiguration subsystem with all currently available configuration values. 
     * @param servletContext
     * @return
     */
    public static void initConfigurationFrom(final ServletContext servletContext) {
        _Config.acceptBuilder(builder->{
            builder.primeWith(new OptionHandlerInitParameters(servletContext));
//            additionalConfig.forEach((k, v)->builder.put(k, v));
            addServletContextConstants(servletContext, builder);
            addResourceStreamSources(servletContext, builder);
            builder.addDefaultConfigurationResourcesAndPrimers();
        });
    }
    
  
    // -- HELPER

    private static void addServletContextConstants(
            final ServletContext servletContext,
            final IsisConfigurationBuilder isisConfigurationBuilder) {
        
        final String webappDir = servletContext.getRealPath("/");
        isisConfigurationBuilder.add(WebAppConstants.WEB_APP_DIR, webappDir);
    }
    
    private static void addResourceStreamSources(
            final ServletContext servletContext,
            final IsisConfigurationBuilder builder) {

        // will load either from WEB-INF, from the class-path or from config directory.
        final String configLocation = servletContext.getInitParameter(WebAppConstants.CONFIG_DIR_PARAM);
        if ( configLocation != null ) {
            LOG.info("Config override location: {}", configLocation );
            builder.addResourceStreamSource(ResourceStreamSourceFileSystem.create(configLocation));
        } else {
            LOG.info("Config override location: No override location configured" );

            builder.addResourceStreamSource(ResourceStreamSourceContextLoaderClassPath.create());
            builder.addResourceStreamSource(new ResourceStreamSourceCurrentClassClassPath());
            builder.addResourceStreamSource(new ResourceStreamSourceForWebInf(servletContext));
        }
    }

    
}
