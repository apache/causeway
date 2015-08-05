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

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorInstaller;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerInstaller;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.services.ServicesInstaller;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystemException;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.transaction.facetdecorator.standard.TransactionFacetDecoratorInstaller;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

public class IsisComponentProviderUsingInstallers implements IsisComponentProvider {

    private final DeploymentType deploymentType;
    private final InstallerLookup installerLookup;

    private AuthenticationManagerInstaller authenticationInstaller;
    private AuthorizationManagerInstaller authorizationInstaller;
    private ObjectReflectorInstaller reflectorInstaller;
    private ServicesInstaller servicesInstaller;
    private PersistenceMechanismInstaller persistenceMechanismInstaller;
    private FixturesInstaller fixtureInstaller;

    public IsisComponentProviderUsingInstallers(
            final DeploymentType deploymentType,
            final InstallerLookup installerLookup) {
        this.deploymentType = deploymentType;
        ensureThatArg(installerLookup, is(not(nullValue())));
        this.installerLookup = installerLookup;

        lookupAndSetInstallers(deploymentType);
    }

    private void lookupAndSetInstallers(final DeploymentType deploymentType) {

        this.authenticationInstaller = installerLookup.authenticationManagerInstaller(
                getConfiguration().getString(SystemConstants.AUTHENTICATION_INSTALLER_KEY),
                deploymentType);

        this.authorizationInstaller = installerLookup.authorizationManagerInstaller(
                getConfiguration().getString(SystemConstants.AUTHORIZATION_INSTALLER_KEY), deploymentType);

        this.fixtureInstaller = installerLookup.fixturesInstaller(
                getConfiguration().getString(SystemConstants.FIXTURES_INSTALLER_KEY));

        persistenceMechanismInstaller = installerLookup.persistenceMechanismInstaller(
                getConfiguration().getString(SystemConstants.OBJECT_PERSISTOR_INSTALLER_KEY),
                deploymentType);

        reflectorInstaller = installerLookup.reflectorInstaller(
                getConfiguration().getString(SystemConstants.REFLECTOR_KEY));

        servicesInstaller = installerLookup.servicesInstaller(null);

        TransactionFacetDecoratorInstaller transactionFacetDecoratorInstaller =
                installerLookup.getInstaller(TransactionFacetDecoratorInstaller.class);

        ensureThatState(authenticationInstaller, is(not(nullValue())), "authenticationInstaller could not be looked up");
        ensureThatState(authorizationInstaller, is(not(nullValue())), "authorizationInstaller could not be looked up");
        ensureThatState(fixtureInstaller, is(not(nullValue())), "fixtureInstaller could not be looked up");
        ensureThatState(persistenceMechanismInstaller, is(not(nullValue())), "persistenceMechanismInstaller could not be looked up");
        ensureThatState(reflectorInstaller, is(not(nullValue())), "reflectorInstaller could not be looked up");
        ensureThatState(transactionFacetDecoratorInstaller, is(not(nullValue())), "transactionFacetDecoratorInstaller could not be looked up");
        ensureThatState(servicesInstaller, is(not(nullValue())), "servicesInstaller could not be looked up");

        // add in transaction support
        reflectorInstaller.addFacetDecoratorInstaller(transactionFacetDecoratorInstaller);
    }

    //region > API

    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    /**
     * Returns a <i>snapshot</i> of the {@link IsisConfiguration configuration}.
     *
     * <p>
     *     ... as held by the internal {@link InstallerLookup}.
     * </p>
     *
     * @see InstallerLookup#getConfiguration()
     */
    @Override
    public IsisConfiguration getConfiguration() {
        return installerLookup.getConfiguration();
    }

    //endregion


    @Override
    public AuthenticationManager provideAuthenticationManager(final DeploymentType deploymentType) {
        return authenticationInstaller.createAuthenticationManager();
    }

    @Override
    public  AuthorizationManager provideAuthorizationManager(final DeploymentType deploymentType) {
        return authorizationInstaller.createAuthorizationManager();
    }

    @Override
    public FixturesInstaller obtainFixturesInstaller() throws IsisSystemException {
        return fixtureInstaller;
    }

    @Override
    public SpecificationLoaderSpi provideSpecificationLoaderSpi(
            final DeploymentType deploymentType,
            final Collection<MetaModelRefiner> metaModelRefiners) throws IsisSystemException {
        return reflectorInstaller.createReflector(metaModelRefiners);
    }

    @Override
    public List<Object> obtainServices() {
        return servicesInstaller.getServices(getDeploymentType());
    }

    @Override
    public PersistenceSessionFactory providePersistenceSessionFactory(
            final DeploymentType deploymentType,
            final ServicesInjectorSpi servicesInjectorSpi, final RuntimeContextFromSession runtimeContext) throws IsisSystemException {
        return persistenceMechanismInstaller.createPersistenceSessionFactory(deploymentType, servicesInjectorSpi, getConfiguration(),
                runtimeContext);
    }

}
