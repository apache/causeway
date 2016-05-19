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
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.IsisSystemFactory;

public class IsisInjectModule extends AbstractModule {

    private final DeploymentType deploymentType;
    private final InstallerLookup installerLookup;
    private final IsisConfiguration isisConfiguration;

    public IsisInjectModule(
            final DeploymentType deploymentType,
            final IsisConfigurationDefault isisConfiguration) {
        this.installerLookup = new InstallerLookup(isisConfiguration);
        this.isisConfiguration = isisConfiguration;
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
    private IsisConfiguration providesConfiguration() {
        return isisConfiguration;
    }

    /**
     * As passed in or defaulted by the constructors.
     */
    @SuppressWarnings("unused")
    @Provides
    @Singleton
    @Inject
    private InstallerLookup providesInstallerLookup() {
        return installerLookup;
    }

    @Override
    protected void configure() {
        requireBinding(DeploymentType.class);
        requireBinding(IsisConfiguration.class);
        requireBinding(InstallerLookup.class);
        bind(AppManifest.class).toInstance(IsisSystemFactory.APP_MANIFEST_NOOP);
    }


    @Provides
    @Inject
    @Singleton
    protected IsisSystem provideIsisSystem(
            final DeploymentType deploymentType,
            final InstallerLookup installerLookup,
            final AppManifest appManifestIfAny) {

        final IsisSystemFactory systemFactory = new IsisSystemFactory(installerLookup);
        final IsisSystem system = systemFactory.createSystem(deploymentType, appManifestIfAny);

        // as a side-effect, if the metamodel turns out to be invalid, then
        // this will push the MetaModelInvalidException into IsisContext.
        system.init();
        return system;
    }


}
