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

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorInstaller;
import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerInstaller;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.services.ServicesInstaller;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfigurationAndAnnotation;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.transaction.facetdecorator.standard.TransactionFacetDecoratorInstaller;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

public class IsisComponentProviderUsingInstallers extends IsisComponentProviderAbstract {

    private final InstallerLookup installerLookup;

    private ObjectReflectorInstaller reflectorInstaller;
    private PersistenceMechanismInstaller persistenceMechanismInstaller;

    public IsisComponentProviderUsingInstallers(
            final DeploymentType deploymentType,
            final InstallerLookup installerLookup) {
        super(deploymentType, appManifestIfAny(installerLookup));

        ensureThatArg(deploymentType, is(not(nullValue())));
        ensureThatArg(installerLookup, is(not(nullValue())));

        this.installerLookup = installerLookup;

        if(appManifest != null) {

            specifyServicesAndRegisteredEntitiesUsing(appManifest);

            putConfigurationProperty(SystemConstants.SERVICES_INSTALLER_KEY, ServicesInstallerFromConfigurationAndAnnotation.NAME);

            final String authenticationMechanism = appManifest.getAuthenticationMechanism();
            putConfigurationProperty(SystemConstants.AUTHENTICATION_INSTALLER_KEY, authenticationMechanism);

            final String authorizationMechanism = appManifest.getAuthorizationMechanism();
            putConfigurationProperty(SystemConstants.AUTHORIZATION_INSTALLER_KEY, authorizationMechanism);

            List<Class<? extends FixtureScript>> fixtureClasses = appManifest.getFixtures();
            final String fixtureClassNamesCsv = classNamesFrom(fixtureClasses);
            putConfigurationProperty(FixturesInstallerFromConfiguration.FIXTURES, fixtureClassNamesCsv);

            overrideConfigurationUsing(appManifest);
        }

        // loading installers causes the configuration to be appended to successively
        AuthenticationManagerInstaller authenticationInstaller = this.installerLookup.authenticationManagerInstaller(
                configProperty(SystemConstants.AUTHENTICATION_INSTALLER_KEY));

        AuthorizationManagerInstaller authorizationInstaller = this.installerLookup.authorizationManagerInstaller(
                configProperty(SystemConstants.AUTHORIZATION_INSTALLER_KEY));

        this.fixturesInstaller = this.installerLookup.fixturesInstaller(
                configProperty(SystemConstants.FIXTURES_INSTALLER_KEY));

        ServicesInstaller servicesInstaller = this.installerLookup.servicesInstaller(null);

        // although there is only one implementation of PersistenceMechanismInstaller, we still do the lookup
        // because this will add the persistor_datanucleus.properties and persistor.properties to the set of
        // config files from which we read configuration properties.
        persistenceMechanismInstaller = this.installerLookup.persistenceMechanismInstaller(
                configProperty(SystemConstants.OBJECT_PERSISTOR_INSTALLER_KEY)
        );

        reflectorInstaller = this.installerLookup.reflectorInstaller(
                configProperty(SystemConstants.REFLECTOR_KEY));

        TransactionFacetDecoratorInstaller transactionFacetDecoratorInstaller =
                this.installerLookup.getInstaller(TransactionFacetDecoratorInstaller.class);


        // ensure we were able to load all components via InstallerLookup (fail fast)
        ensureThatState(authenticationInstaller, is(not(nullValue())),
                "authenticationInstaller could not be looked up");
        ensureThatState(authorizationInstaller, is(not(nullValue())),
                "authorizationInstaller could not be looked up");
        ensureThatState(servicesInstaller, is(not(nullValue())),
                "servicesInstaller could not be looked up");
        ensureThatState(fixturesInstaller, is(not(nullValue())),
                "fixtureInstaller could not be looked up");
        ensureThatState(transactionFacetDecoratorInstaller, is(not(nullValue())),
                "transactionFacetDecoratorInstaller could not be looked up");
        ensureThatState(persistenceMechanismInstaller, is(not(nullValue())),
                "persistenceMechanismInstaller could not be looked up");
        ensureThatState(reflectorInstaller, is(not(nullValue())),
                "reflectorInstaller could not be looked up");


        // add in transaction support
        reflectorInstaller.addFacetDecoratorInstaller(transactionFacetDecoratorInstaller);

        // capture the final configuration once all components have been loaded
        configuration = this.installerLookup.getConfiguration();

        // eagerly calculate
        authenticationManager = authenticationInstaller.createAuthenticationManager();
        authorizationManager = authorizationInstaller.createAuthorizationManager();
        services = servicesInstaller.getServices();

        ensureInitialized();
    }

    private static AppManifest appManifestIfAny(final InstallerLookup installerLookup) {
        final String appManifestIfAny = installerLookup.getConfiguration().getString(SystemConstants.APP_MANIFEST_KEY);
        return appManifestIfAny != null? InstanceUtil.createInstance(appManifestIfAny, AppManifest.class): null;
    }

    protected void doPutConfigurationProperty(final String key, final String value) {
        this.installerLookup.putConfigurationProperty(key, value);
    }
    //endregion



    @Override
    public SpecificationLoaderSpi provideSpecificationLoaderSpi(
            final Collection<MetaModelRefiner> metaModelRefiners) {
        return reflectorInstaller.createReflector(metaModelRefiners);
    }


    @Override
    public PersistenceSessionFactory providePersistenceSessionFactory(
            final DeploymentType deploymentType,
            final ServicesInjectorSpi servicesInjectorSpi,
            final RuntimeContextFromSession runtimeContext) {
        return persistenceMechanismInstaller.createPersistenceSessionFactory(deploymentType, servicesInjectorSpi,
                getConfiguration(),
                runtimeContext);
    }

    //region > helpers

    /**
     * Returns the current value of the configuration property.
     *
     * Note that this may change over time as new installers are loaded (= new config property files).
     */
    private String configProperty(final String key) {
        return this.installerLookup.getConfiguration().getString(key);
    }
    //endregion


}
