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
package org.apache.isis.config.builder;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.WebAppConstants;
import org.apache.isis.config.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.config.resource.ResourceStreamSourceCurrentClassClassPath;
import org.apache.isis.config.resource.ResourceStreamSourceFileSystem;
import org.apache.isis.config.resource.ResourceStreamSourceForWebInf;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;

import static org.apache.isis.commons.internal.base._With.ifPresentElseGet;

class PrimerForServletContext implements IsisConfigurationBuilder.Primer {

    private static final Logger LOG = LoggerFactory.getLogger(PrimerForServletContext.class);
    
    @Override
    public void prime(final IsisConfigurationBuilder builder) {
        final ServletContext servletContext  = _Context.getIfAny(ServletContext.class);
        if(servletContext==null) {
            LOG.info("No servlet context found to prime configuration from.");
            return;
        }
        LOG.info("Priming configuration from servlet context ...");
        
        asMap(servletContext).forEach((k, v)->builder.put(k, v));
        addServletContextConstants(servletContext, builder);
        addResourceStreamSources(servletContext, builder);
        
        final String loggingPropertiesDir = 
                ifPresentElseGet(
                        servletContext.getInitParameter("isis.config.dir"),
                        ()->servletContext.getRealPath("/WEB-INF"));
    
        final IsisLoggingConfigurer loggingConfigurer = new IsisLoggingConfigurer();
        loggingConfigurer.configureLogging(loggingPropertiesDir, new String[0]);

    }

    // -- HELPER

    private static Map<String, String> asMap(ServletContext servletContext) {
        Enumeration<String> initParameterNames = servletContext.getInitParameterNames();
        final Map<String,String> map = _Maps.newTreeMap();
        while(initParameterNames.hasMoreElements()) {
            final String initParameterName = initParameterNames.nextElement();
            final String initParameterValue = servletContext.getInitParameter(initParameterName);
            if (initParameterName.startsWith("isis.")) {
                map.put(initParameterName, initParameterValue);
            }
        }
        return map;
    }
    
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
