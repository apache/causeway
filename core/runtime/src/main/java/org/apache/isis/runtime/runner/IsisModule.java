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


package org.apache.isis.runtime.runner;

import java.util.Collections;
import java.util.List;

import org.apache.isis.core.metamodel.config.ConfigurationBuilder;
import org.apache.isis.core.metamodel.config.ConfigurationBuilderDefault;
import org.apache.isis.core.metamodel.config.ConfigurationPrimer;
import org.apache.isis.runtime.installers.InstallerLookup;
import org.apache.isis.runtime.installers.InstallerLookupDefault;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.IsisSystemFactory;
import org.apache.isis.runtime.system.IsiSsystemUsingInstallersFactory;
import org.apache.isis.runtime.system.IsisSystem;
import org.apache.isis.runtime.viewer.IsisViewer;
import org.apache.isis.runtime.viewer.IsisViewerInstaller;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.internal.Lists;

public class IsisModule extends AbstractModule {

    private final DeploymentType deploymentType;
    private final InstallerLookup installerLookup;
    private final ConfigurationBuilder configurationBuilder;

    private final List<ConfigurationPrimer> configurationPrimers = Lists.newArrayList();
    private final List<String> viewerNames = Lists.newArrayList();

    private static InstallerLookupDefault defaultInstallerLookup() {
        return new InstallerLookupDefault(IsisModule.class);
    }
    
    private static ConfigurationBuilderDefault defaultConfigurationBuider() {
        return new ConfigurationBuilderDefault();
    }
    
    public IsisModule(DeploymentType deploymentType) {
        this(deploymentType, defaultConfigurationBuider(), defaultInstallerLookup());
    }

    public IsisModule(DeploymentType deploymentType, ConfigurationBuilder configurationBuilder) {
        this(deploymentType, configurationBuilder, defaultInstallerLookup());
    }
    
    public IsisModule(DeploymentType deploymentType, InstallerLookup installerLookup) {
        this(deploymentType, defaultConfigurationBuider(), installerLookup);
    }
    
    public IsisModule(DeploymentType deploymentType,
            ConfigurationBuilder configurationBuilder,
            InstallerLookup installerLookup) {
        this.installerLookup = installerLookup;
        this.configurationBuilder = configurationBuilder;
        this.deploymentType = deploymentType;
    }

    /**
     * As passed in or defaulted by the constructors.
     */
    @SuppressWarnings("unused")
    @Provides
    @Singleton
    private DeploymentType provideDeploymentsType() {
        return deploymentType;
    }

    /**
     * As passed in or defaulted by the constructors.
     */
    @SuppressWarnings("unused")
    @Provides
    @Singleton
    private ConfigurationBuilder providesConfigurationBuilder() {
        return primeConfiguration(configurationBuilder);
    }

    private ConfigurationBuilder primeConfiguration(final ConfigurationBuilder configBuilder) {
        for (ConfigurationPrimer configurationPrimer : configurationPrimers) {
            configurationPrimer.primeConfigurationBuilder(configBuilder);
        }
        return configBuilder;
    }

    
    /**
     * As passed in or defaulted by the constructors.
     */
    @SuppressWarnings("unused")
    @Provides
    @Singleton
    @Inject
    private InstallerLookup providesInstallerLookup(
            ConfigurationBuilder configBuilder) {
        // wire up and initialize installer lookup
        configBuilder.injectInto(installerLookup);
        installerLookup.init();
        return installerLookup;
    }
    

    /**
     * Adjustment (as per GOOS book)
     */
    public void addConfigurationPrimers(List<? extends ConfigurationPrimer> configurationPrimers) {
        this.configurationPrimers.addAll(configurationPrimers);
    }

    /**
     * Adjustment (as per GOOS book)
     */
    public void addViewerNames(List<String> viewerNames) {
        this.viewerNames.addAll(viewerNames);
    }

    
    @Override
    protected void configure() {
        requireBinding(DeploymentType.class);
        requireBinding(ConfigurationBuilder.class);
        requireBinding(InstallerLookup.class);
    }

    
    
    @SuppressWarnings("unused")
    @Provides
    @Inject
    @Singleton
    private IsisSystemFactory provideIsisSystemFactory(final InstallerLookup installerLookup) {
        IsiSsystemUsingInstallersFactory systemFactory = new IsiSsystemUsingInstallersFactory(installerLookup);
        systemFactory.init();
        return systemFactory;
    }

    @SuppressWarnings("unused")
    @Provides
    @Inject
    @Singleton
    private IsisSystem provideIsisSystem(final DeploymentType deploymentType, IsisSystemFactory systemFactory) {
        final IsisSystem system = systemFactory.createSystem(deploymentType);
        system.init();
        return system;
    }


    public static class ViewerList {
        private List<IsisViewer> viewers;
        public ViewerList(List<IsisViewer> viewers) {
            this.viewers = Collections.unmodifiableList(viewers);
        }

        public List<IsisViewer> getViewers() {
            return viewers;
        }
    }
    
    @SuppressWarnings("unused")
    @Provides
    @Inject
    @Singleton
    private ViewerList lookupViewers(InstallerLookup installerLookup, DeploymentType deploymentType) {
        
        List<String> viewersToStart = Lists.newArrayList(viewerNames);
        deploymentType.addDefaultViewer(viewersToStart);

        List<IsisViewer> viewers = Lists.newArrayList();
        for (String requestedViewer : viewersToStart) {
            final IsisViewerInstaller viewerInstaller = installerLookup
                    .viewerInstaller(requestedViewer);
            final IsisViewer viewer = viewerInstaller.createViewer();
            viewers.add(viewer);
        }
        return new ViewerList(viewers);
    }


    
}