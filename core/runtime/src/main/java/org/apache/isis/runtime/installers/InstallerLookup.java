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


package org.apache.isis.runtime.installers;

import org.apache.isis.commons.components.ApplicationScopedComponent;
import org.apache.isis.commons.components.Injectable;
import org.apache.isis.commons.components.Installer;
import org.apache.isis.metamodel.config.ConfigurationBuilder;
import org.apache.isis.metamodel.config.ConfigurationBuilderAware;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.specloader.FacetDecoratorInstaller;
import org.apache.isis.metamodel.specloader.ObjectReflectorInstaller;
import org.apache.isis.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.runtime.authorization.AuthorizationManagerInstaller;
import org.apache.isis.runtime.fixturesinstaller.FixturesInstaller;
import org.apache.isis.runtime.imageloader.TemplateImageLoaderInstaller;
import org.apache.isis.runtime.persistence.PersistenceMechanismInstaller;
import org.apache.isis.runtime.persistence.services.ServicesInstaller;
import org.apache.isis.runtime.remoting.ClientConnectionInstaller;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.IsisSystem;
import org.apache.isis.runtime.userprofile.UserProfileStoreInstaller;
import org.apache.isis.runtime.viewer.IsisViewerInstaller;
import org.apache.isis.runtime.web.EmbeddedWebServerInstaller;


/**
 * The installers correspond more-or-less to the configurable top-level components of
 * {@link IsisSystem}.
 * 
 * <p>
 * The methods of {@link InstallerRepository} may be called without {@link #init() initializing} this class,
 * but other methods may not.
 */
public interface InstallerLookup extends InstallerRepository, ApplicationScopedComponent, ConfigurationBuilderAware, Injectable {

    // /////////////////////////////////////////////////////////
    // metamodel
    // /////////////////////////////////////////////////////////

    ObjectReflectorInstaller reflectorInstaller(final String requested);

    // /////////////////////////////////////////////////////////
    // framework
    // /////////////////////////////////////////////////////////

    AuthenticationManagerInstaller authenticationManagerInstaller(String requested, boolean isExploring);

    AuthorizationManagerInstaller authorizationManagerInstaller(String requested, boolean isExploring);

    FixturesInstaller fixturesInstaller(String requested);

    ServicesInstaller servicesInstaller(final String requested);

    TemplateImageLoaderInstaller templateImageLoaderInstaller(String requested);

    PersistenceMechanismInstaller persistenceMechanismInstaller(final String requested, final DeploymentType deploymentType);

    UserProfileStoreInstaller userProfilePersistenceMechanismInstaller(final String requested, DeploymentType deploymentType);

    IsisViewerInstaller viewerInstaller(final String requested, final String defaultName);

    IsisViewerInstaller viewerInstaller(final String requested);

    /**
     * Client-side of <tt>remoting</tt>, specifying how to access the server.
     * 
     * <p>
     * Note that this lookup is called in three different contexts:
     * <ul>
     * <li>the <tt>IsisExecutionContextFactoryUsingInstallers</tt> uses this to lookup the
     * {@link PersistenceMechanismInstaller} (may be a <tt>ProxyPersistor</tt>)</li>
     * <li>the <tt>IsisExecutionContextFactoryUsingInstallers</tt> also uses this to lookup the
     * {@link FacetDecoratorInstaller}; adds in remoting facets.</li>
     * <li>the <tt>IsisSystemUsingInstallers</tt> uses this to lookup the
     * {@link AuthenticationManagerInstaller}.</li>
     * </ul>
     */
    ClientConnectionInstaller clientConnectionInstaller(final String requested);

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
     * Injects self into candidate
     */
    <T> T injectDependenciesInto(T candidate);

    /**
     * Returns a <i>snapshot</i> of the current {@link IsisConfiguration}.
     * 
     * <p>
     * The {@link IsisConfiguration} could subsequently be appended to if further {@link Installer}s
     * are loaded.
     */
    IsisConfiguration getConfiguration();

    // /////////////////////////////////////////////////////////
    // dependencies (injected)
    // /////////////////////////////////////////////////////////

    /**
     * Injected.
     */
    ConfigurationBuilder getConfigurationBuilder();

}

