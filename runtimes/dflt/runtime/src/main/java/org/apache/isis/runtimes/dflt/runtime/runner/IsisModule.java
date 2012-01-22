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

package org.apache.isis.runtimes.dflt.runtime.runner;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderDefault;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderPrimer;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.InstallerLookup;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.IsisViewerInstaller;
import org.apache.isis.runtimes.dflt.runtime.installers.InstallerLookupDefault;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.IsisSystem;
import org.apache.isis.runtimes.dflt.runtime.system.IsisSystemFactory;
import org.apache.isis.runtimes.dflt.runtime.systemusinginstallers.IsisSystemThatUsesInstallersFactory;
import org.apache.isis.runtimes.dflt.runtime.viewer.IsisViewer;

public class IsisModule extends AbstractModule {

    private final DeploymentType deploymentType;
    private final InstallerLookup installerLookup;
    private final IsisConfigurationBuilder isisConfigurationBuilder;

    private final List<IsisConfigurationBuilderPrimer> isisConfigurationBuilderPrimers = Lists.newArrayList();
    private final List<String> viewerNames = Lists.newArrayList();

    private static InstallerLookupDefault defaultInstallerLookup() {
        return new InstallerLookupDefault();
    }

    private static IsisConfigurationBuilderDefault defaultConfigurationBuider() {
        return new IsisConfigurationBuilderDefault();
    }

    public IsisModule(final DeploymentType deploymentType) {
        this(deploymentType, defaultConfigurationBuider(), defaultInstallerLookup());
    }

    public IsisModule(final DeploymentType deploymentType, final IsisConfigurationBuilder isisConfigurationBuilder) {
        this(deploymentType, isisConfigurationBuilder, defaultInstallerLookup());
    }

    public IsisModule(final DeploymentType deploymentType, final InstallerLookup installerLookup) {
        this(deploymentType, defaultConfigurationBuider(), installerLookup);
    }

    public IsisModule(final DeploymentType deploymentType, final IsisConfigurationBuilder isisConfigurationBuilder, final InstallerLookup installerLookup) {
        this.installerLookup = installerLookup;
        this.isisConfigurationBuilder = isisConfigurationBuilder;
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
    private IsisConfigurationBuilder providesConfigurationBuilder() {
        return primeConfiguration(isisConfigurationBuilder);
    }

    private IsisConfigurationBuilder primeConfiguration(final IsisConfigurationBuilder configBuilder) {
        for (final IsisConfigurationBuilderPrimer isisConfigurationBuilderPrimer : isisConfigurationBuilderPrimers) {
            isisConfigurationBuilderPrimer.primeConfigurationBuilder(configBuilder);
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
    private InstallerLookup providesInstallerLookup(final IsisConfigurationBuilder configBuilder) {
        // wire up and initialize installer lookup
        configBuilder.injectInto(installerLookup);
        installerLookup.init();
        return installerLookup;
    }

    /**
     * Adjustment (as per GOOS book)
     */
    public void addConfigurationPrimers(final List<? extends IsisConfigurationBuilderPrimer> isisConfigurationBuilderPrimers) {
        this.isisConfigurationBuilderPrimers.addAll(isisConfigurationBuilderPrimers);
    }

    /**
     * Adjustment (as per GOOS book)
     */
    public void addViewerNames(final List<String> viewerNames) {
        this.viewerNames.addAll(viewerNames);
    }

    @Override
    protected void configure() {
        requireBinding(DeploymentType.class);
        requireBinding(IsisConfigurationBuilder.class);
        requireBinding(InstallerLookup.class);
    }

    @SuppressWarnings("unused")
    @Provides
    @Inject
    @Singleton
    private IsisSystemFactory provideIsisSystemFactory(final InstallerLookup installerLookup) {
        final IsisSystemThatUsesInstallersFactory systemFactory = new IsisSystemThatUsesInstallersFactory(installerLookup);
        systemFactory.init();
        return systemFactory;
    }

    @SuppressWarnings("unused")
    @Provides
    @Inject
    @Singleton
    private IsisSystem provideIsisSystem(final DeploymentType deploymentType, final IsisSystemFactory systemFactory) {
        final IsisSystem system = systemFactory.createSystem(deploymentType);
        system.init();
        return system;
    }

    public static class ViewerList {
        private final List<IsisViewer> viewers;

        public ViewerList(final List<IsisViewer> viewers) {
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
    private ViewerList lookupViewers(final InstallerLookup installerLookup, final DeploymentType deploymentType) {

        final List<String> viewersToStart = Lists.newArrayList(viewerNames);
        deploymentType.addDefaultViewer(viewersToStart);

        final List<IsisViewer> viewers = Lists.newArrayList();
        for (final String requestedViewer : viewersToStart) {
            final IsisViewerInstaller viewerInstaller = installerLookup.viewerInstaller(requestedViewer);
            final IsisViewer viewer = viewerInstaller.createViewer();
            viewers.add(viewer);
        }
        return new ViewerList(viewers);
    }

}