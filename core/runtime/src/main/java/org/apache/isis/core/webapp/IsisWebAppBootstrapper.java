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

import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderPrimer;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderResourceStreams;
import org.apache.isis.core.commons.config.NotFoundPolicy;
import org.apache.isis.core.commons.resource.ResourceStreamSourceComposite;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceFileSystem;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.runtime.runner.IsisInjectModule;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerInitParameters;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.webapp.config.ResourceStreamSourceForWebInf;

/**
 * Initialize the {@link IsisSystem} when the web application starts, and
 * destroys it when it ends.
 * <p>
 * Implementation note: we use a number of helper builders to keep this class as
 * small and focused as possible. The builders are available for reuse by other
 * bootstrappers.
 */
public class IsisWebAppBootstrapper implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(IsisWebAppBootstrapper.class);
    
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

            final String configLocation = servletContext.getInitParameter(WebAppConstants.CONFIG_DIR_PARAM);
            final ResourceStreamSourceComposite compositeSource = new ResourceStreamSourceComposite(
                    ResourceStreamSourceContextLoaderClassPath.create(), 
                    new ResourceStreamSourceForWebInf(servletContext)) ;

            if ( configLocation != null ) {
              LOG.info( "Config override location: " + configLocation );
              compositeSource.addResourceStreamSource(ResourceStreamSourceFileSystem.create(configLocation));
            } else {
              LOG.info( "Config override location: No override location configured" );
            }
            
            // will load either from WEB-INF, from the classpath or from config directory.
            final IsisConfigurationBuilder isisConfigurationBuilder = new IsisConfigurationBuilderResourceStreams(compositeSource);

            primeConfigurationBuilder(isisConfigurationBuilder, servletContext);

            isisConfigurationBuilder.addDefaultConfigurationResources();

            final DeploymentType deploymentType = determineDeploymentType(isisConfigurationBuilder, servletContext);

            addConfigurationResourcesForWebApps(isisConfigurationBuilder);
            addConfigurationResourcesForDeploymentType(isisConfigurationBuilder, deploymentType);
            IsisWebAppBootstrapperUtil.addConfigurationResourcesForViewers(isisConfigurationBuilder, servletContext);

            isisConfigurationBuilder.add(WebAppConstants.WEB_APP_DIR, webappDir);
            isisConfigurationBuilder.add(SystemConstants.NOSPLASH_KEY, "true");

            final InstallerLookup installerLookup = new InstallerLookup();

            injector = createGuiceInjector(isisConfigurationBuilder, deploymentType, installerLookup);

            final IsisSystem system = injector.getInstance(IsisSystem.class);

            isisConfigurationBuilder.lockConfiguration();
            isisConfigurationBuilder.dumpResourcesToLog();

            servletContext.setAttribute(WebAppConstants.ISIS_SYSTEM_KEY, system);
        } catch (final RuntimeException e) {
            LOG.error("startup failed", e);
            throw e;
        }
        LOG.info("server started");
    }

    private Injector createGuiceInjector(final IsisConfigurationBuilder isisConfigurationBuilder, final DeploymentType deploymentType, final InstallerLookup installerLookup) {
        final IsisInjectModule isisModule = new IsisInjectModule(deploymentType, isisConfigurationBuilder, installerLookup);
        return Guice.createInjector(isisModule);
    }

    @SuppressWarnings("unchecked")
    private static void primeConfigurationBuilder(final IsisConfigurationBuilder isisConfigurationBuilder, final ServletContext servletContext) {
        LOG.info("loading properties from option handlers");
        final List<IsisConfigurationBuilderPrimer> isisConfigurationBuilderPrimers = Lists.newArrayList();
        final List<IsisConfigurationBuilderPrimer> primers = (List<IsisConfigurationBuilderPrimer>) servletContext.getAttribute(WebAppConstants.CONFIGURATION_PRIMERS_KEY);
        if(primers != null) {
            isisConfigurationBuilderPrimers.addAll(primers);
        }
        // also support loading from init parameters (specifically, to support simplericity's jetty-console)
        isisConfigurationBuilderPrimers.add(new OptionHandlerInitParameters(servletContext));
        for (final IsisConfigurationBuilderPrimer isisConfigurationBuilderPrimer : isisConfigurationBuilderPrimers) {
            LOG.debug("priming configurations for " + isisConfigurationBuilderPrimer);
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
     * If no setting is found, defaults to {@link WebAppConstants#DEPLOYMENT_TYPE_DEFAULT}.
     */
    private DeploymentType determineDeploymentType(final IsisConfigurationBuilder isisConfigurationBuilder, final ServletContext servletContext) {
        String deploymentTypeStr = null;
        deploymentTypeStr = servletContext.getInitParameter(WebAppConstants.DEPLOYMENT_TYPE_KEY);
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
