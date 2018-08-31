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

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.config.NotFoundPolicy;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.runtime.runner.IsisInjectModule;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.SystemConstants;
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

            final DeploymentType deploymentType = determineDeploymentType(servletContext, isisConfigurationBuilder);
            addConfigurationResourcesForDeploymentType(isisConfigurationBuilder, deploymentType);

            final IsisConfigurationDefault isisConfiguration = isisConfigurationBuilder.getConfiguration();
            final DeploymentCategory deploymentCategory = deploymentType.getDeploymentCategory();
            final IsisInjectModule isisModule = new IsisInjectModule(deploymentCategory, isisConfiguration);
            final Injector injector = Guice.createInjector(isisModule);
            injector.injectMembers(this);

        } catch (final RuntimeException e) {
            LOG.error("startup failed", e);
            throw e;
        }
        LOG.info("server started");
    }

    /**
     * Checks {@link IsisConfigurationBuilder configuration} for
     * {@value SystemConstants#DEPLOYMENT_TYPE_KEY}, (that is, from the command
     * line), but otherwise searches in the {@link ServletContext}, first for
     * {@value WebAppConstants#DEPLOYMENT_TYPE_KEY} and also
     * {@value SystemConstants#DEPLOYMENT_TYPE_KEY}.
     * <p>
     * If no setting is found, defaults to {@link WebAppConstants#DEPLOYMENT_TYPE_DEFAULT}.
     */
    protected DeploymentType determineDeploymentType(
            final ServletContext servletContext,
            final IsisConfigurationBuilder isisConfigurationBuilder) {
        String deploymentTypeStr = determineDeploymentTypeStr(servletContext, isisConfigurationBuilder);
        return DeploymentType.lookup(deploymentTypeStr);
    }

    private String determineDeploymentTypeStr(
            final ServletContext servletContext,
            final IsisConfigurationBuilder isisConfigurationBuilder) {

        String deploymentTypeStr;

        deploymentTypeStr = servletContext.getInitParameter(WebAppConstants.DEPLOYMENT_TYPE_KEY);
        if (deploymentTypeStr != null) {
            return deploymentTypeStr;
        }

        deploymentTypeStr = servletContext.getInitParameter(SystemConstants.DEPLOYMENT_TYPE_KEY);
        if (deploymentTypeStr != null) {
            return deploymentTypeStr;
        }

        deploymentTypeStr = isisConfigurationBuilder.peekAt(SystemConstants.DEPLOYMENT_TYPE_KEY);
        if (deploymentTypeStr != null) {
            return deploymentTypeStr;
        }

        return WebAppConstants.DEPLOYMENT_TYPE_DEFAULT;
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
