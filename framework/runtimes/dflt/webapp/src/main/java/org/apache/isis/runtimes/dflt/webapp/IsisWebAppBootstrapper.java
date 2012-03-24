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

package org.apache.isis.runtimes.dflt.webapp;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderPrimer;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderResourceStreams;
import org.apache.isis.core.commons.config.NotFoundPolicy;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.webapp.config.ResourceStreamSourceForWebInf;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.InstallerLookup;
import org.apache.isis.runtimes.dflt.runtime.installers.InstallerLookupDefault;
import org.apache.isis.runtimes.dflt.runtime.runner.IsisModule;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.IsisSystem;
import org.apache.isis.runtimes.dflt.runtime.system.SystemConstants;

/**
 * Initialize the {@link IsisSystem} when the web application starts, and
 * destroys it when it ends.
 * <p>
 * Implementation note: we use a number of helper builders to keep this class as
 * small and focused as possible. The builders are available for reuse by other
 * bootstrappers.
 */
public class IsisWebAppBootstrapper implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(IsisWebAppBootstrapper.class);
    private final IsisLoggingConfigurer loggingConfigurer = new IsisLoggingConfigurer();
    private Injector injector;

    /**
     * Convenience for servlets that need to obtain the {@link IsisSystem}.
     */
    public static IsisSystem getSystemBoundTo(final ServletContext servletContext) {
        final Object system = servletContext.getAttribute(WebAppConstants.ISIS_SYSTEM_KEY);
        return (IsisSystem) system;
    }

    // /////////////////////////////////////////////////////
    // Initialization
    // /////////////////////////////////////////////////////

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        try {
            final ServletContext servletContext = servletContextEvent.getServletContext();

            final String webappDir = servletContext.getRealPath("/");
            final String webInfDir = servletContext.getRealPath("/WEB-INF");
            loggingConfigurer.configureLogging(webInfDir, new String[0]);

            // will load either from WEB-INF or from the classpath.
            final IsisConfigurationBuilder isisConfigurationBuilder = new IsisConfigurationBuilderResourceStreams(new ResourceStreamSourceForWebInf(servletContext), ResourceStreamSourceContextLoaderClassPath.create());

            primeConfigurationBuilder(isisConfigurationBuilder, servletContext);

            final DeploymentType deploymentType = determineDeploymentType(isisConfigurationBuilder, servletContext);

            addConfigurationResourcesForWebApps(isisConfigurationBuilder);
            addConfigurationResourcesForDeploymentType(isisConfigurationBuilder, deploymentType);
            addConfigurationResourcesForViewers(isisConfigurationBuilder, servletContext);

            isisConfigurationBuilder.add(WebAppConstants.WEB_APP_DIR, webappDir);
            isisConfigurationBuilder.add(SystemConstants.NOSPLASH_KEY, "true");

            final InstallerLookup installerLookup = new InstallerLookupDefault();

            injector = createGuiceInjector(isisConfigurationBuilder, deploymentType, installerLookup);

            final IsisSystem system = injector.getInstance(IsisSystem.class);
            
            isisConfigurationBuilder.dumpResourcesToLog();

            servletContext.setAttribute(WebAppConstants.ISIS_SYSTEM_KEY, system);
        } catch (final RuntimeException e) {
            LOG.error("startup failed", e);
            throw e;
        }
        LOG.info("server started");
    }

    private Injector createGuiceInjector(final IsisConfigurationBuilder isisConfigurationBuilder, final DeploymentType deploymentType, final InstallerLookup installerLookup) {
        final IsisModule isisModule = new IsisModule(deploymentType, isisConfigurationBuilder, installerLookup);
        return Guice.createInjector(isisModule);
    }

    @SuppressWarnings("unchecked")
    private void primeConfigurationBuilder(final IsisConfigurationBuilder isisConfigurationBuilder, final ServletContext servletContext) {
        final List<IsisConfigurationBuilderPrimer> isisConfigurationBuilderPrimers = (List<IsisConfigurationBuilderPrimer>) servletContext.getAttribute(WebAppConstants.CONFIGURATION_PRIMERS_KEY);
        if (isisConfigurationBuilderPrimers == null) {
            return;
        }
        for (final IsisConfigurationBuilderPrimer isisConfigurationBuilderPrimer : isisConfigurationBuilderPrimers) {
            isisConfigurationBuilderPrimer.primeConfigurationBuilder(isisConfigurationBuilder);
        }
    }

    /**
     * Checks {@link IsisConfigurationBuilder configuration} for
     * {@value SystemConstants#DEPLOYMENT_TYPE_KEY}, (that is, from the command
     * line), but otherwise searches in the {@link ServletContext}, first for
     * {@value WebAppConstants#DEPLOYMENT_TYPE_KEY} and also
     * {@value SystemConstants#DEPLOYMENT_TYPE_KEY}.
     * <p>
     * If no setting is found, defaults to
     * {@value WebAppConstants#DEPLOYMENT_TYPE_DEFAULT}.
     */
    private DeploymentType determineDeploymentType(final IsisConfigurationBuilder isisConfigurationBuilder, final ServletContext servletContext) {
        String deploymentTypeStr = null;
        if (deploymentTypeStr == null) {
            deploymentTypeStr = servletContext.getInitParameter(WebAppConstants.DEPLOYMENT_TYPE_KEY);
        }
        if (deploymentTypeStr == null) {
            deploymentTypeStr = servletContext.getInitParameter(SystemConstants.DEPLOYMENT_TYPE_KEY);
        }
        if (deploymentTypeStr == null) {
            deploymentTypeStr = isisConfigurationBuilder.getConfiguration().getString(SystemConstants.DEPLOYMENT_TYPE_KEY);
        }
        if (deploymentTypeStr == null) {
            deploymentTypeStr = WebAppConstants.DEPLOYMENT_TYPE_DEFAULT;
        }
        return DeploymentType.lookup(deploymentTypeStr);
    }

    private void addConfigurationResourcesForDeploymentType(final IsisConfigurationBuilder configurationLoader, final DeploymentType deploymentType) {
        final String type = deploymentType.name().toLowerCase();
        configurationLoader.addConfigurationResource(type + ".properties", NotFoundPolicy.CONTINUE);
    }

    private void addConfigurationResourcesForWebApps(final IsisConfigurationBuilder configurationLoader) {
        for (final String config : (new String[] { "web.properties", "war.properties" })) {
            if (config != null) {
                configurationLoader.addConfigurationResource(config, NotFoundPolicy.CONTINUE);
            }
        }
    }

    private void addConfigurationResourcesForViewers(final IsisConfigurationBuilder configurationLoader, final ServletContext servletContext) {
        addConfigurationResourcesForContextParam(configurationLoader, servletContext, "isis.viewers");
        addConfigurationResourcesForContextParam(configurationLoader, servletContext, "isis.viewer");
    }

    private void addConfigurationResourcesForContextParam(final IsisConfigurationBuilder configurationLoader, final ServletContext servletContext, final String name) {
        final String viewers = servletContext.getInitParameter(name);
        if (viewers == null) {
            return;
        }
        for (final String viewer : viewers.split(",")) {
            configurationLoader.addConfigurationResource("viewer_" + viewer + ".properties", NotFoundPolicy.CONTINUE);
        }
    }

    // /////////////////////////////////////////////////////
    // Destroy
    // /////////////////////////////////////////////////////

    @Override
    public void contextDestroyed(final ServletContextEvent ev) {
        LOG.info("server shutting down");
        final ServletContext servletContext = ev.getServletContext();

        try {
            final IsisSystem system = (IsisSystem) servletContext.getAttribute(WebAppConstants.ISIS_SYSTEM_KEY);
            if (system != null) {
                LOG.info("calling system shutdown");
                system.shutdown();
            }
        } finally {
            servletContext.removeAttribute(WebAppConstants.ISIS_SYSTEM_KEY);
            LOG.info("server shut down");
        }
    }

}
