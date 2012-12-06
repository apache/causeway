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

package org.apache.isis.runtimes.dflt.runtime.installerregistry;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderAware;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorInstaller;
import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerInstaller;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoaderInstaller;
import org.apache.isis.runtimes.dflt.runtime.fixtures.FixturesInstaller;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.EmbeddedWebServerInstaller;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.IsisViewerInstaller;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.services.ServicesInstaller;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.IsisSystem;
import org.apache.isis.runtimes.dflt.runtime.systemdependencyinjector.SystemDependencyInjector;
import org.apache.isis.runtimes.dflt.runtime.userprofile.UserProfileStoreInstaller;

/**
 * The installers correspond more-or-less to the configurable top-level
 * components of {@link IsisSystem}.
 * 
 * <p>
 * The methods of {@link InstallerRepository} may be called without
 * {@link #init() initializing} this class, but other methods may not.
 */
public interface InstallerLookup extends InstallerRepository, ApplicationScopedComponent, IsisConfigurationBuilderAware, Injectable, SystemDependencyInjector {

    // /////////////////////////////////////////////////////////
    // metamodel
    // /////////////////////////////////////////////////////////

    ObjectReflectorInstaller reflectorInstaller(final String requested);

    // /////////////////////////////////////////////////////////
    // framework
    // /////////////////////////////////////////////////////////

    AuthenticationManagerInstaller authenticationManagerInstaller(String requested, final DeploymentType deploymentType);

    AuthorizationManagerInstaller authorizationManagerInstaller(String requested, final DeploymentType deploymentType);

    FixturesInstaller fixturesInstaller(String requested);

    ServicesInstaller servicesInstaller(final String requested);

    TemplateImageLoaderInstaller templateImageLoaderInstaller(String requested);

    PersistenceMechanismInstaller persistenceMechanismInstaller(final String requested, final DeploymentType deploymentType);

    UserProfileStoreInstaller userProfilePersistenceMechanismInstaller(final String requested, DeploymentType deploymentType);

    IsisViewerInstaller viewerInstaller(final String requested, final String defaultName);

    IsisViewerInstaller viewerInstaller(final String requested);

    EmbeddedWebServerInstaller embeddedWebServerInstaller(final String requested);

    // /////////////////////////////////////////////////////////
    // framework - generic
    // /////////////////////////////////////////////////////////

    <T extends Installer> T getInstaller(final Class<T> cls, final String requested);

    <T extends Installer> T getInstaller(final Class<T> cls);

    <T extends Installer> T getInstaller(final String implClassName);

    // /////////////////////////////////////////////////////////
    // configuration
    // /////////////////////////////////////////////////////////

    /**
     * Returns a <i>snapshot</i> of the current {@link IsisConfiguration}.
     * 
     * <p>
     * The {@link IsisConfiguration} could subsequently be appended to if
     * further {@link Installer}s are loaded.
     */
    IsisConfiguration getConfiguration();

    // /////////////////////////////////////////////////////////
    // dependencies (injected)
    // /////////////////////////////////////////////////////////

    /**
     * Injected.
     */
    IsisConfigurationBuilder getConfigurationBuilder();

}
