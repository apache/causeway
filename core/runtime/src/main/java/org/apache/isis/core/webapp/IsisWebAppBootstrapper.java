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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.config.NotFoundPolicy;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceCurrentClassClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceFileSystem;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.runtime.runner.IsisInjectModule;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerInitParameters;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryBuilder;
import org.apache.isis.core.webapp.config.ResourceStreamSourceForWebInf;

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

    @com.google.inject.Inject
    private IsisSessionFactory isisSessionFactory;


    /**
     * Convenience for servlets that need to obtain the {@link IsisSessionFactoryBuilder}.
     */
    public static IsisSessionFactoryBuilder getSystemBoundTo(final ServletContext servletContext) {
        final Object system = servletContext.getAttribute(WebAppConstants.ISIS_SESSION_FACTORY);
        return (IsisSessionFactoryBuilder) system;
    }

    // /////////////////////////////////////////////////////
    // Initialization
    // /////////////////////////////////////////////////////

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        try {
            final ServletContext servletContext = servletContextEvent.getServletContext();

            final String webInfDir = servletContext.getRealPath("/WEB-INF");
            loggingConfigurer.configureLogging(webInfDir, new String[0]);

            final IsisConfigurationBuilder isisConfigurationBuilder = obtainIsisConfigurationBuilder(servletContext);
            isisConfigurationBuilder.addDefaultConfigurationResourcesAndPrimers();

            final DeploymentType deploymentType = determineDeploymentType(servletContext, isisConfigurationBuilder);
            addConfigurationResourcesForDeploymentType(isisConfigurationBuilder, deploymentType);

            final String webappDir = servletContext.getRealPath("/");
            isisConfigurationBuilder.add(WebAppConstants.WEB_APP_DIR, webappDir);

            final IsisConfigurationDefault isisConfiguration = isisConfigurationBuilder.getConfiguration();
            final DeploymentCategory deploymentCategory = deploymentType.getDeploymentCategory();
            final IsisInjectModule isisModule = new IsisInjectModule(deploymentCategory, isisConfiguration);
            final Injector injector = Guice.createInjector(isisModule);
            injector.injectMembers(this);

            servletContext.setAttribute(WebAppConstants.ISIS_SESSION_FACTORY, isisSessionFactory);

        } catch (final RuntimeException e) {
            LOG.error("startup failed", e);
            throw e;
        }
        LOG.info("server started");
    }

    protected IsisConfigurationBuilder obtainIsisConfigurationBuilder(final ServletContext servletContext) {
        return obtainConfigBuilderFrom(servletContext);
    }

    /**
     * public so can also be used by Wicket viewer.
     */
    public static IsisConfigurationBuilder obtainConfigBuilderFrom(final ServletContext servletContext) {
        final IsisConfigurationBuilder isisConfigurationBuilder = lookupIsisConfigurationBuilder(servletContext);
        isisConfigurationBuilder.primeWith(new OptionHandlerInitParameters(servletContext));

        addResourceStreamSources(servletContext, isisConfigurationBuilder);
        return isisConfigurationBuilder;
    }

    public static IsisConfigurationBuilder lookupIsisConfigurationBuilder(final ServletContext servletContext) {
        IsisConfigurationBuilder isisConfigurationBuilder =
                (IsisConfigurationBuilder) servletContext.getAttribute(WebAppConstants.CONFIGURATION_BUILDER_KEY);
        if(isisConfigurationBuilder == null) {
            isisConfigurationBuilder = new IsisConfigurationBuilder();
        }
        return isisConfigurationBuilder;
    }

    private static void addResourceStreamSources(
            final ServletContext servletContext,
            final IsisConfigurationBuilder isisConfigurationBuilder) {


        // will load either from WEB-INF, from the classpath or from config directory.
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

        deploymentTypeStr = isisConfigurationBuilder.peekConfiguration().getString(SystemConstants.DEPLOYMENT_TYPE_KEY);
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
        final ServletContext servletContext = ev.getServletContext();

        try {
            final IsisSessionFactory isisSessionFactory = (IsisSessionFactory) servletContext.getAttribute(WebAppConstants.ISIS_SESSION_FACTORY);
            if (isisSessionFactory != null) {
                LOG.info("calling system shutdown");
                isisSessionFactory.destroyServicesAndShutdown();
            }
        } finally {
            servletContext.removeAttribute(WebAppConstants.ISIS_SESSION_FACTORY);
            LOG.info("server shut down");
        }
    }

}
