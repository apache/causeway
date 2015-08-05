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

package org.apache.isis.core.integtestsupport;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
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
import org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.runtime.authentication.standard.Authenticator;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfiguration;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystemException;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;
import org.apache.isis.core.runtime.transaction.facetdecorator.standard.StandardTransactionFacetDecorator;
import org.apache.isis.core.security.authentication.AuthenticatorBypass;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusPersistenceMechanismInstaller;
import org.apache.isis.progmodels.dflt.JavaReflectorHelper;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

public class IsisComponentProviderDefault implements IsisComponentProvider {

    private final DeploymentType deploymentType;

    private final IsisConfiguration configuration;
    private final List<Object> servicesIfAny;
    private final ProgrammingModel programmingModelOverride;
    private final MetaModelValidator metaModelValidatorOverride;

    public IsisComponentProviderDefault(
            final DeploymentType deploymentType,
            final List<Object> services,
            final IsisConfiguration configuration,
            final ProgrammingModel programmingModelOverride,
            final MetaModelValidator metaModelValidatorOverride) {
        this.deploymentType = deploymentType;
        this.configuration = configuration;
        this.servicesIfAny = services;
        this.programmingModelOverride = programmingModelOverride;
        this.metaModelValidatorOverride = metaModelValidatorOverride;
    }

    static IsisConfiguration defaultConfiguration() {
        return new IsisConfigurationDefault(ResourceStreamSourceContextLoaderClassPath.create("config"));
    }


    @Override
    public DeploymentType getDeploymentType() {
        return deploymentType;
    }


    /**
     * Reads <tt>isis.properties</tt> (and other optional property files) from the &quot;config&quot; package on the current classpath.
     */
    @Override
    public IsisConfiguration getConfiguration() {
        return configuration;
    }


    /**
     * Either the services explicitly provided by a constructor, otherwise reads from the configuration.
     */
    @Override
    public List<Object> obtainServices() {
        if(servicesIfAny != null) {
            return servicesIfAny;
        }
        // else
        final ServicesInstallerFromConfiguration servicesInstaller = new ServicesInstallerFromConfiguration();
        return servicesInstaller.getServices(getDeploymentType());
    }

    /**
     * Install fixtures from configuration.
     */
    @Override
    public FixturesInstaller obtainFixturesInstaller() throws IsisSystemException {
        final FixturesInstallerFromConfiguration fixturesInstallerFromConfiguration = new FixturesInstallerFromConfiguration();
        fixturesInstallerFromConfiguration.setConfiguration(getConfiguration());
        return fixturesInstallerFromConfiguration;
    }


    /**
     * <p>
     * Each of the subcomponents can be overridden if required.
     *
     * @see #obtainReflectorFacetDecoratorSet()
     * @see #obtainReflectorMetaModelValidator()
     * @see #obtainReflectorProgrammingModel()
     */
    @Override
    public SpecificationLoaderSpi provideSpecificationLoaderSpi(
            DeploymentType deploymentType,
            Collection<MetaModelRefiner> metaModelRefiners) throws IsisSystemException {


        final ProgrammingModel programmingModel = obtainReflectorProgrammingModel();
        final Set<FacetDecorator> facetDecorators = obtainReflectorFacetDecoratorSet();
        final MetaModelValidator mmv = obtainReflectorMetaModelValidator();
        final List<LayoutMetadataReader> layoutMetadataReaders = obtainLayoutMetadataReaders();
        return JavaReflectorHelper
                .createObjectReflector(programmingModel, metaModelRefiners, facetDecorators, layoutMetadataReaders, mmv,
                        getConfiguration());
    }


    private ProgrammingModel obtainReflectorProgrammingModel() {

        if (programmingModelOverride != null) {
            return programmingModelOverride;
        }

        final ProgrammingModelFacetsJava5 programmingModel = new ProgrammingModelFacetsJava5();

        // TODO: this is duplicating logic in JavaReflectorInstallerNoDecorators; need to unify.

        ProgrammingModel.Util.includeFacetFactories(getConfiguration(), programmingModel);
        ProgrammingModel.Util.excludeFacetFactories(getConfiguration(), programmingModel);
        return programmingModel;
    }

    /**
     * Optional hook method.
     */
    private Set<FacetDecorator> obtainReflectorFacetDecoratorSet() {
        return Sets.newHashSet((FacetDecorator) new StandardTransactionFacetDecorator(getConfiguration()));
    }

    /**
     * Optional hook method.
     */
    protected MetaModelValidator obtainReflectorMetaModelValidator() {
        if(metaModelValidatorOverride != null) {
            return metaModelValidatorOverride;
        }
        return new MetaModelValidatorDefault();
    }

    protected List<LayoutMetadataReader> obtainLayoutMetadataReaders() {
        return Lists.<LayoutMetadataReader>newArrayList(new LayoutMetadataReaderFromJson());
    }

    /**
     * The standard authentication manager, configured with the default authenticator (allows all requests through).
     */
    @Override
    public AuthenticationManager provideAuthenticationManager(DeploymentType deploymentType) throws IsisSystemException {
        final AuthenticationManagerStandard authenticationManager = new AuthenticationManagerStandard(getConfiguration());
        Authenticator authenticator = new AuthenticatorBypass(configuration);
        authenticationManager.addAuthenticator(authenticator);
        return authenticationManager;
    }

    /**
     * The standard authorization manager, allowing all access.
     */
    @Override
    public AuthorizationManager provideAuthorizationManager(DeploymentType deploymentType) {
        return new AuthorizationManagerStandard(getConfiguration());
    }

    @Override
    public PersistenceSessionFactory providePersistenceSessionFactory(
            DeploymentType deploymentType,
            final ServicesInjectorSpi servicesInjectorSpi,
            final RuntimeContextFromSession runtimeContext) throws IsisSystemException {
        DataNucleusPersistenceMechanismInstaller installer = new DataNucleusPersistenceMechanismInstaller();
        return installer.createPersistenceSessionFactory(deploymentType, servicesInjectorSpi, getConfiguration(), runtimeContext);
    }

}
