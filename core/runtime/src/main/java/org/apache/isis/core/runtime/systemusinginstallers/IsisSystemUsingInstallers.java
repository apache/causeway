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

package org.apache.isis.core.runtime.systemusinginstallers;

import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorInstaller;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerInstaller;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.services.ServicesInstaller;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystemException;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.systemdependencyinjector.SystemDependencyInjector;
import org.apache.isis.core.runtime.transaction.facetdecorator.standard.TransactionFacetDecoratorInstaller;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.*;

public class IsisSystemUsingInstallers extends IsisSystemAbstract {

    public static final Logger LOG = LoggerFactory.getLogger(IsisSystemUsingInstallers.class);

    private final InstallerLookup installerLookup;

    private AuthenticationManagerInstaller authenticationInstaller;
    private AuthorizationManagerInstaller authorizationInstaller;
    private ObjectReflectorInstaller reflectorInstaller;
    private ServicesInstaller servicesInstaller;
    private PersistenceMechanismInstaller persistenceMechanismInstaller;
    private FixturesInstaller fixtureInstaller;

    // ///////////////////////////////////////////
    // Constructors
    // ///////////////////////////////////////////

    public IsisSystemUsingInstallers(final DeploymentType deploymentType, final InstallerLookup installerLookup) {
        super(deploymentType);
        ensureThatArg(installerLookup, is(not(nullValue())));
        this.installerLookup = installerLookup;
    }

    // ///////////////////////////////////////////
    // InstallerLookup
    // ///////////////////////////////////////////

    /**
     * As per
     * {@link #IsisSystemUsingInstallers(DeploymentType, InstallerLookup)
     * constructor}.
     */
    public SystemDependencyInjector getInstallerLookup() {
        return installerLookup;
    }

    // ///////////////////////////////////////////
    // Create context hooks
    // ///////////////////////////////////////////


    // ///////////////////////////////////////////
    // Configuration
    // ///////////////////////////////////////////

    /**
     * Returns a <i>snapshot</i> of the {@link IsisConfiguration configuration}
     * held by the {@link #getInstallerLookup() installer lookup}.
     * 
     * @see InstallerLookup#getConfiguration()
     */
    @Override
    public IsisConfiguration getConfiguration() {
        return installerLookup.getConfiguration();
    }

    // ///////////////////////////////////////////
    // Authentication & Authorization
    // ///////////////////////////////////////////

    public void lookupAndSetAuthenticatorAndAuthorization(final DeploymentType deploymentType) {

        //final IsisConfiguration configuration = installerLookup.getConfiguration();

        // use the one specified in configuration
        final String authenticationManagerKey = getConfiguration().getString(SystemConstants.AUTHENTICATION_INSTALLER_KEY);
        final AuthenticationManagerInstaller authenticationInstaller = installerLookup.authenticationManagerInstaller(authenticationManagerKey, deploymentType);
        if (authenticationInstaller != null) {
            setAuthenticationInstaller(authenticationInstaller);
        }
        
        // use the one specified in configuration
        final String authorizationManagerKey = getConfiguration().getString(SystemConstants.AUTHORIZATION_INSTALLER_KEY);
        final AuthorizationManagerInstaller authorizationInstaller = installerLookup.authorizationManagerInstaller(authorizationManagerKey, deploymentType);
        if (authorizationInstaller != null) {
            setAuthorizationInstaller(authorizationInstaller);
        }
    }

    public void setAuthenticationInstaller(final AuthenticationManagerInstaller authenticationManagerInstaller) {
        this.authenticationInstaller = authenticationManagerInstaller;
    }

    public void setAuthorizationInstaller(final AuthorizationManagerInstaller authorizationManagerInstaller) {
        this.authorizationInstaller = authorizationManagerInstaller;
    }

    @Override
    protected AuthenticationManager obtainAuthenticationManager(final DeploymentType deploymentType) {
        return authenticationInstaller.createAuthenticationManager();
    }

    @Override
    protected AuthorizationManager obtainAuthorizationManager(final DeploymentType deploymentType) {
        return authorizationInstaller.createAuthorizationManager();
    }

    // ///////////////////////////////////////////
    // Fixtures
    // ///////////////////////////////////////////

    public void lookupAndSetFixturesInstaller() {
        final IsisConfiguration configuration = installerLookup.getConfiguration();
        final String fixture = configuration.getString(SystemConstants.FIXTURES_INSTALLER_KEY);

        final FixturesInstaller fixturesInstaller = installerLookup.fixturesInstaller(fixture);
        if (fixturesInstaller != null) {
            this.fixtureInstaller = fixturesInstaller;
        }
    }

    public void setFixtureInstaller(final FixturesInstaller fixtureInstaller) {
        this.fixtureInstaller = fixtureInstaller;
    }

    @Override
    protected FixturesInstaller obtainFixturesInstaller() throws IsisSystemException {
        return fixtureInstaller;
    }

    // ///////////////////////////////////////////
    // Reflector
    // ///////////////////////////////////////////

    public void setReflectorInstaller(final ObjectReflectorInstaller reflectorInstaller) {
        this.reflectorInstaller = reflectorInstaller;
    }

    @Override
    protected SpecificationLoaderSpi obtainSpecificationLoaderSpi(final DeploymentType deploymentType, final Collection<MetaModelRefiner> metaModelRefiners) throws IsisSystemException {
        if (reflectorInstaller == null) {
            final String fromCmdLine = getConfiguration().getString(SystemConstants.REFLECTOR_KEY);
            reflectorInstaller = installerLookup.reflectorInstaller(fromCmdLine);
        }
        ensureThatState(reflectorInstaller, is(not(nullValue())), "reflector installer has not been injected and could not be looked up");

        // add in transaction support (if already in set then will be ignored)
        reflectorInstaller.addFacetDecoratorInstaller(installerLookup.getInstaller(TransactionFacetDecoratorInstaller.class));

        return reflectorInstaller.createReflector(metaModelRefiners);
    }

    // ///////////////////////////////////////////
    // Container and Services
    // ///////////////////////////////////////////

    public void setServicesInstaller(final ServicesInstaller servicesInstaller) {
        this.servicesInstaller = servicesInstaller;
    }

    @Override
    protected List<Object> obtainServices() {
        if (servicesInstaller == null) {
            servicesInstaller = installerLookup.servicesInstaller(null);
        }
        ensureThatState(servicesInstaller, is(not(nullValue())), "services installer has not been injected and could not be looked up");

        return servicesInstaller.getServices(getDeploymentType());
    }


    // ///////////////////////////////////////////
    // PersistenceSessionFactory
    // ///////////////////////////////////////////

    public void setPersistenceMechanismInstaller(final PersistenceMechanismInstaller persistenceMechanismInstaller) {
        this.persistenceMechanismInstaller = persistenceMechanismInstaller;
    }

    @Override
    protected PersistenceSessionFactory obtainPersistenceSessionFactory(final DeploymentType deploymentType) throws IsisSystemException {

        // look for a object store persistor
        if (persistenceMechanismInstaller == null) {
            final String persistenceMechanism = getConfiguration().getString(SystemConstants.OBJECT_PERSISTOR_INSTALLER_KEY);
            persistenceMechanismInstaller = installerLookup.persistenceMechanismInstaller(persistenceMechanism, deploymentType);
        }

        ensureThatState(persistenceMechanismInstaller, is(not(nullValue())), "persistor installer has not been injected and could not be looked up");
        return persistenceMechanismInstaller.createPersistenceSessionFactory(deploymentType);
    }
}
