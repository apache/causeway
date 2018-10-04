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

import static org.apache.isis.commons.internal._Constants.emptyStringArray;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.config.NotFoundPolicy;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryBuilder;

/**
 * Initialize the {@link IsisSessionFactoryBuilder} when the web application starts, and
 * destroys it when it ends.
 * <p>
 * Implementation note: we use a number of helper builders to keep this class as
 * small and focused as possible. The builders are available for reuse by other
 * bootstrappers.
 */
public class IsisWebAppBootstrapper implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(IsisWebAppBootstrapper.class);

    private final IsisLoggingConfigurer loggingConfigurer = new IsisLoggingConfigurer();

    // /////////////////////////////////////////////////////
    // Initialization
    // /////////////////////////////////////////////////////

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        try {
            final ServletContext servletContext = servletContextEvent.getServletContext();

            final String webInfDir = servletContext.getRealPath("/WEB-INF");
            loggingConfigurer.configureLogging(webInfDir, emptyStringArray);

            final IsisConfigurationBuilder isisConfigurationBuilder = 
                    IsisWebAppConfigProvider.getInstance().getConfigurationBuilder(servletContext);
            isisConfigurationBuilder.addDefaultConfigurationResourcesAndPrimers();

            final DeploymentType deploymentType = DeploymentType.get(); 
                    
            addConfigurationResourcesForDeploymentType(isisConfigurationBuilder, deploymentType);

            //FIXME[1892]
//            final IsisConfigurationDefault isisConfiguration = isisConfigurationBuilder.getConfiguration();
//            final IsisInjectModule isisModule = new IsisInjectModule(isisConfiguration);
//            final Injector injector = Guice.createInjector(isisModule);
//            injector.injectMembers(this);

        } catch (final RuntimeException e) {
            LOG.error("startup failed", e);
            throw e;
        }
        LOG.info("server started");
    }

    protected void addConfigurationResourcesForDeploymentType(
            final IsisConfigurationBuilder isisConfigurationBuilder,
            final DeploymentType deploymentType) {
        final String resourceName = deploymentType.name().toLowerCase() + ".properties";
        isisConfigurationBuilder.addConfigurationResource(resourceName, NotFoundPolicy.CONTINUE, IsisConfigurationDefault.ContainsPolicy.IGNORE);
    }


    // /////////////////////////////////////////////////////
    // Destroy
    // /////////////////////////////////////////////////////

    @Override
    public void contextDestroyed(final ServletContextEvent ev) {
        LOG.info("server shutting down");
        try {
            _Context.clear();
        } finally {
            LOG.info("server shut down");
        }
    }

}
