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
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.fixtures.InstallableFixture;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader;
import org.apache.isis.core.metamodel.layoutmetadata.json.LayoutMetadataReaderFromJson;
import org.apache.isis.core.metamodel.metamodelvalidator.dflt.MetaModelValidatorDefault;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.runtime.authentication.standard.Authenticator;
import org.apache.isis.core.runtime.authentication.standard.AuthenticatorAbstract;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfiguration;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfigurationAndAnnotation;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystemException;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.transaction.facetdecorator.standard.StandardTransactionFacetDecorator;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusPersistenceMechanismInstaller;
import org.apache.isis.progmodels.dflt.JavaReflectorHelper;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

public class IsisComponentProviderDefault2 extends IsisComponentProviderAbstract {

    private final ProgrammingModel programmingModel;
    private final MetaModelValidator metaModelValidator;

    public IsisComponentProviderDefault2(
            final DeploymentType deploymentType,
            final AppManifest appManifestIfAny,
            final List<Object> servicesOverride,
            final List<InstallableFixture> fixturesOverride,
            final IsisConfiguration configurationOverride,
            final ProgrammingModel programmingModelOverride,
            final MetaModelValidator metaModelValidatorOverride) {
        super(deploymentType, appManifestIfAny);

        this.configuration = elseDefault(configurationOverride);

        final String fixtureClassNamesCsv;
        if(appManifest != null) {

            putAppManifestKey(appManifest);
            registerPackageNames(appManifest);
            specifyServicesAndRegisteredEntitiesUsing(appManifest);

            List<Class<? extends FixtureScript>> fixtureClasses = appManifest.getFixtures();
            fixtureClassNamesCsv = classNamesFrom(fixtureClasses);

            overrideConfigurationUsing(appManifest);

            this.services = createServices(configuration);

        } else {
            fixtureClassNamesCsv = classNamesFrom(fixturesOverride);

            this.services = elseDefault(servicesOverride, configuration);
        }

        putConfigurationProperty(FixturesInstallerFromConfiguration.FIXTURES, fixtureClassNamesCsv);
        this.fixturesInstaller = createFixturesInstaller(configuration);

        // integration tests ignore appManifest for authentication and authorization.
        this.authenticationManager = createAuthenticationManager(configuration);
        this.authorizationManager = createAuthorizationManager(configuration);

        this.programmingModel = elseDefault(programmingModelOverride, configuration);
        this.metaModelValidator = elseDefault(metaModelValidatorOverride);

    }



    //region > appManifest

    private List<Object> createServices(final IsisConfiguration configuration) {
        final ServicesInstallerFromConfigurationAndAnnotation servicesInstaller =
                new ServicesInstallerFromConfigurationAndAnnotation();
        servicesInstaller.setConfiguration(configuration);
        return servicesInstaller.getServices();
    }


    @Override
    protected void doPutConfigurationProperty(final String key, final String value) {
        // bit hacky :-(
        IsisConfigurationDefault configurationDefault = (IsisConfigurationDefault) this.configuration;
        configurationDefault.put(key, value);
    }

    //endregion

    /**
     * Default will read <tt>isis.properties</tt> (and other optional property files) from the &quot;config&quot;
     * package on the current classpath.
     */
    private static IsisConfigurationDefault elseDefault(final IsisConfiguration configuration) {
        return configuration != null
                ? (IsisConfigurationDefault) configuration
                : new IsisConfigurationDefault(ResourceStreamSourceContextLoaderClassPath.create("config"));
    }

    private static List<Object> elseDefault(
            final List<Object> servicesOverride,
            final IsisConfiguration configuration) {
        return servicesOverride != null
                ? servicesOverride
                : createDefaultServices(configuration);
    }

    private static List<Object> createDefaultServices(
            final IsisConfiguration configuration) {
        final ServicesInstallerFromConfiguration servicesInstaller = new ServicesInstallerFromConfiguration();
        servicesInstaller.setConfiguration(configuration);
        return servicesInstaller.getServices();
    }


    private static ProgrammingModel elseDefault(final ProgrammingModel programmingModel, final IsisConfiguration configuration) {
        return programmingModel != null
                ? programmingModel
                : createDefaultProgrammingModel(configuration);
    }

    // TODO: this is duplicating logic in JavaReflectorInstallerNoDecorators; need to unify.
    private static ProgrammingModel createDefaultProgrammingModel(final IsisConfiguration configuration) {
        final ProgrammingModelFacetsJava5 programmingModel = new ProgrammingModelFacetsJava5();

        ProgrammingModel.Util.includeFacetFactories(configuration, programmingModel);
        ProgrammingModel.Util.excludeFacetFactories(configuration, programmingModel);
        return programmingModel;
    }

    private static MetaModelValidator elseDefault(final MetaModelValidator metaModelValidator) {
        return metaModelValidator != null
                ? metaModelValidator
                : new MetaModelValidatorDefault();
    }

    private static FixturesInstaller createFixturesInstaller(final IsisConfiguration configuration) {
        final FixturesInstallerFromConfiguration fixturesInstallerFromConfiguration = new FixturesInstallerFromConfiguration();
        fixturesInstallerFromConfiguration.setConfiguration(configuration);
        return fixturesInstallerFromConfiguration;
    }

    /**
     * The standard authentication manager, configured with the default authenticator (allows all requests through).
     */
    private static AuthenticationManager createAuthenticationManager(final IsisConfiguration configuration) {
        final AuthenticationManagerStandard authenticationManager = new AuthenticationManagerStandard(configuration);
        Authenticator authenticator = new AuthenticatorBypass(configuration);
        authenticationManager.addAuthenticator(authenticator);
        return authenticationManager;
    }

    private static class AuthenticatorBypass extends AuthenticatorAbstract {

        public AuthenticatorBypass(final IsisConfiguration configuration) {
            super(configuration);
        }

        @Override
        public boolean isValid(final AuthenticationRequest request) {
            return true;
        }

        @Override
        public boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
            return true;
        }

    }


    /**
     * The standard authorization manager, allowing all access.
     */
    private static AuthorizationManager createAuthorizationManager(final IsisConfiguration configuration) {
        return new AuthorizationManagerStandard(configuration);
    }


    @Override
    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    @Override
    public IsisConfigurationDefault getConfiguration() {
        return configuration;
    }

    @Override
    public List<Object> provideServices() {
        return services;
    }

    @Override
    public FixturesInstaller provideFixturesInstaller()  {
        return fixturesInstaller;
    }

    @Override
    public SpecificationLoaderSpi provideSpecificationLoaderSpi(
            final DeploymentType deploymentType,
            Collection<MetaModelRefiner> metaModelRefiners) throws IsisSystemException {

        final DeploymentCategory deploymentCategory = deploymentType.getDeploymentCategory();

        final Set<FacetDecorator> facetDecorators =
                Sets.newHashSet((FacetDecorator) new StandardTransactionFacetDecorator(getConfiguration()));
        final List<LayoutMetadataReader> layoutMetadataReaders =
                Lists.<LayoutMetadataReader>newArrayList(new LayoutMetadataReaderFromJson());

        return JavaReflectorHelper
                .createObjectReflector(
                        deploymentCategory, getConfiguration(), programmingModel,
                        metaModelRefiners,
                        facetDecorators, layoutMetadataReaders,
                        metaModelValidator
                );
    }

    @Override
    public AuthenticationManager provideAuthenticationManager(DeploymentType deploymentType) {
        return authenticationManager;
    }

    @Override
    public AuthorizationManager provideAuthorizationManager(DeploymentType deploymentType) {
        return authorizationManager;
    }

    @Override
    public PersistenceSessionFactory providePersistenceSessionFactory(
            DeploymentType deploymentType,
            final ServicesInjectorSpi servicesInjectorSpi,
            final SpecificationLoaderSpi specificationLoader) {
        DataNucleusPersistenceMechanismInstaller installer =
                new DataNucleusPersistenceMechanismInstaller();
        return installer.createPersistenceSessionFactory(
                deploymentType, configuration, servicesInjectorSpi
        );
    }

}
