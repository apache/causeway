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

package org.apache.isis.core.runtime.runner;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderDefault;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.IsisSystemFactory;
import org.apache.isis.core.runtime.systemusinginstallers.IsisSystemThatUsesInstallersFactory;

public class IsisInjectModule extends AbstractModule {

    private final DeploymentType deploymentType;
    private final InstallerLookup installerLookup;
    private final IsisConfigurationBuilder isisConfigurationBuilder;

    private static InstallerLookup defaultInstallerLookup() {
        return new InstallerLookup();
    }

    private static IsisConfigurationBuilderDefault defaultConfigurationBuilder() {
        return new IsisConfigurationBuilderDefault();
    }

    public IsisInjectModule(final DeploymentType deploymentType) {
        this(deploymentType, defaultConfigurationBuilder(), defaultInstallerLookup());
    }

    public IsisInjectModule(final DeploymentType deploymentType, final IsisConfigurationBuilder isisConfigurationBuilder) {
        this(deploymentType, isisConfigurationBuilder, defaultInstallerLookup());
    }

    public IsisInjectModule(final DeploymentType deploymentType, final InstallerLookup installerLookup) {
        this(deploymentType, defaultConfigurationBuilder(), installerLookup);
    }

    public IsisInjectModule(final DeploymentType deploymentType, final IsisConfigurationBuilder isisConfigurationBuilder, final InstallerLookup installerLookup) {
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
        return isisConfigurationBuilder;
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

    @Override
    protected void configure() {
        requireBinding(DeploymentType.class);
        requireBinding(IsisConfigurationBuilder.class);
        requireBinding(InstallerLookup.class);
        bind(AppManifest.class).toInstance(IsisSystemThatUsesInstallersFactory.APP_MANIFEST_NOOP);
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

    @Provides
    @Inject
    @Singleton
    protected IsisSystem provideIsisSystem(
            final DeploymentType deploymentType,
            final IsisSystemFactory systemFactory,
            final AppManifest appManifestIfAny) {
        final IsisSystem system = systemFactory.createSystem(deploymentType, appManifestIfAny);
        // as a side-effect, if the metamodel turns out to be invalid, then
        // this will push the MetaModelInvalidException into IsisContext.
        system.init();
        return system;
    }


}
