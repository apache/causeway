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


package org.apache.isis.webapp;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.isis.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.metamodel.config.ConfigurationBuilder;
import org.apache.isis.metamodel.config.ConfigurationBuilderResourceStreams;
import org.apache.isis.metamodel.config.ConfigurationPrimer;
import org.apache.isis.metamodel.config.NotFoundPolicy;
import org.apache.isis.runtime.installers.InstallerLookup;
import org.apache.isis.runtime.installers.InstallerLookupDefault;
import org.apache.isis.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.runtime.runner.IsisModule;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.IsisSystem;
import org.apache.isis.runtime.system.SystemConstants;

import com.google.inject.Guice;
import com.google.inject.Injector;


/**
 * Initialize the {@link IsisSystem} when the web application starts,
 * and destroys it when it ends.
 * <p>
 * Implementation note: we use a number of helper builders to keep this class as
 * small and focused as possible. The builders are available for reuse by other
 * bootstrappers.
 */
public class IsisWebAppBootstrapper implements ServletContextListener {

    private static final Logger LOG = Logger
            .getLogger(IsisWebAppBootstrapper.class);
    private final IsisLoggingConfigurer loggingConfigurer = new IsisLoggingConfigurer();
    private Injector injector;

    /**
     * Convenience for servlets that need to obtain the
     * {@link IsisSystem}.
     */
    public static IsisSystem getSystemBoundTo(
            ServletContext servletContext) {
        Object system = servletContext
                .getAttribute(WebAppConstants.ISIS_SYSTEM_KEY);
        return (IsisSystem) system;
    }

    // /////////////////////////////////////////////////////
    // Initialization
    // /////////////////////////////////////////////////////

    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        try {
        ServletContext servletContext = servletContextEvent.getServletContext();

        String webappDir = servletContext.getRealPath("/");
        String webInfDir = servletContext.getRealPath("/WEB-INF");
        loggingConfigurer.configureLogging(webInfDir, new String[0]);

        // will load either from WEB-INF or from the classpath.
        final ConfigurationBuilder configurationBuilder = new ConfigurationBuilderResourceStreams(
                new ResourceStreamSourceServletContext(servletContext),
                new ResourceStreamSourceContextLoaderClassPath());

        primeConfigurationBuilder(configurationBuilder, servletContext);

        final DeploymentType deploymentType = determineDeploymentType(
                configurationBuilder, servletContext);

        addConfigurationResourcesForWebApps(configurationBuilder);
        addConfigurationResourcesForDeploymentType(configurationBuilder,
                deploymentType);
        configurationBuilder.add(WebAppConstants.WEB_APP_DIR, webappDir);
        configurationBuilder.add(SystemConstants.NOSPLASH_KEY, "true");

        InstallerLookup installerLookup = new InstallerLookupDefault(
                getClass());

        injector = createGuiceInjector(configurationBuilder,
                deploymentType, installerLookup);

        IsisSystem system = injector.getInstance(IsisSystem.class);

        servletContext.setAttribute(WebAppConstants.ISIS_SYSTEM_KEY,
                system);
        } catch (RuntimeException e) {
            LOG.error("startup failed", e);
            throw e;
        }
        LOG.info("server started");
    }

    private Injector createGuiceInjector(
            final ConfigurationBuilder configurationBuilder,
            final DeploymentType deploymentType, InstallerLookup installerLookup) {
        IsisModule isisModule = new IsisModule(deploymentType, configurationBuilder, installerLookup);
        return Guice.createInjector(isisModule);
    }

    @SuppressWarnings("unchecked")
    private void primeConfigurationBuilder(
            ConfigurationBuilder configurationBuilder,
            ServletContext servletContext) {
        List<ConfigurationPrimer> configurationPrimers = (List<ConfigurationPrimer>) servletContext
                .getAttribute(WebAppConstants.CONFIGURATION_PRIMERS_KEY);
        if (configurationPrimers == null) {
            return;
        }
        for (ConfigurationPrimer configurationPrimer : configurationPrimers) {
            configurationPrimer.primeConfigurationBuilder(configurationBuilder);
        }
    }

    /**
     * Checks {@link ConfigurationBuilder configuration} for
     * {@value SystemConstants#DEPLOYMENT_TYPE_KEY},
     * (that is, from the command line), but otherwise searches in the
     * {@link ServletContext}, first for
     * {@value WebAppConstants#DEPLOYMENT_TYPE_KEY} and also
     * {@value SystemConstants#DEPLOYMENT_TYPE_KEY}.
     * <p>
     * If no setting is found, defaults to
     * {@value WebAppConstants#DEPLOYMENT_TYPE_DEFAULT}.
     */
    private DeploymentType determineDeploymentType(
            ConfigurationBuilder configurationBuilder,
            final ServletContext servletContext) {
        String deploymentTypeStr = configurationBuilder.getConfiguration()
                .getString(SystemConstants.DEPLOYMENT_TYPE_KEY);
        if (deploymentTypeStr == null) {
            deploymentTypeStr = servletContext
                    .getInitParameter(WebAppConstants.DEPLOYMENT_TYPE_KEY);
        }
        if (deploymentTypeStr == null) {
            deploymentTypeStr = servletContext
                    .getInitParameter(SystemConstants.DEPLOYMENT_TYPE_KEY);
        }
        if (deploymentTypeStr == null) {
            deploymentTypeStr = WebAppConstants.DEPLOYMENT_TYPE_DEFAULT;
        }
        return DeploymentType.lookup(deploymentTypeStr);
    }

    private void addConfigurationResourcesForDeploymentType(
            final ConfigurationBuilder configurationLoader,
            final DeploymentType deploymentType) {
        String type = deploymentType.name().toLowerCase();
        configurationLoader.addConfigurationResource(type + ".properties",
                NotFoundPolicy.CONTINUE);
    }

    private void addConfigurationResourcesForWebApps(
            final ConfigurationBuilder configurationLoader) {
        for (String config : (new String[] { "web.properties", "war.properties" })) {
            if (config != null) {
                configurationLoader.addConfigurationResource(config,
                        NotFoundPolicy.CONTINUE);
            }
        }
    }

    // /////////////////////////////////////////////////////
    // Destroy
    // /////////////////////////////////////////////////////

    public void contextDestroyed(final ServletContextEvent ev) {
        LOG.info("server shutting down");
        ServletContext servletContext = ev.getServletContext();

        try {
            final IsisSystem system = (IsisSystem) servletContext
                    .getAttribute(WebAppConstants.ISIS_SYSTEM_KEY);
            if (system != null) {
                LOG.info("calling system shutdown");
                system.shutdown();
            }
        } finally {
            servletContext
                    .removeAttribute(WebAppConstants.ISIS_SYSTEM_KEY);
            LOG.info("server shut down");
        }
    }

}

