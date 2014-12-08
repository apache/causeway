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
import java.util.Collections;
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
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.objectstore.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.runtime.authentication.standard.Authenticator;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;
import org.apache.isis.core.runtime.fixtures.FixturesInstaller;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfiguration;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystemException;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.systemusinginstallers.IsisSystemAbstract;
import org.apache.isis.core.runtime.transaction.facetdecorator.standard.StandardTransactionFacetDecorator;
import org.apache.isis.core.security.authentication.AuthenticatorBypass;
import org.apache.isis.progmodels.dflt.JavaReflectorHelper;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

public class IsisSystemDefault extends IsisSystemAbstract {

    private final IsisConfigurationDefault configuration;
    private final List<Object> servicesIfAny;

    public IsisSystemDefault(Object... servicesIfAny) {
        this(DeploymentType.SERVER, servicesIfAny);
    }

    public IsisSystemDefault(List<Object> services) {
        this(DeploymentType.SERVER, services);
    }

    public IsisSystemDefault(DeploymentType deploymentType, Object... servicesIfAny) {
        this(deploymentType, asList(servicesIfAny));
    }

    public IsisSystemDefault(DeploymentType deploymentType, List<Object> services) {
        super(deploymentType);
        this.configuration = new IsisConfigurationDefault(ResourceStreamSourceContextLoaderClassPath.create("config"));
        this.servicesIfAny = services;
    }

    private static List<Object> asList(Object... objects) {
        return objects != null? Collections.unmodifiableList(Lists.newArrayList(objects)): null;
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
    protected List<Object> obtainServices() {
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
    protected FixturesInstaller obtainFixturesInstaller() throws IsisSystemException {
        final FixturesInstallerFromConfiguration fixturesInstallerFromConfiguration = new FixturesInstallerFromConfiguration();
        fixturesInstallerFromConfiguration.setConfiguration(getConfiguration());
        return fixturesInstallerFromConfiguration;
    }


    /**
     * Optional hook method, to create the reflector with defaults (Java5, with cglib, and only the transaction facet decorators)
     * 
     * <p>
     * Each of the subcomponents can be overridden if required.
     * 
     * @see #obtainReflectorFacetDecoratorSet()
     * @see #obtainReflectorMetaModelValidator()
     * @see #obtainReflectorProgrammingModel()
     */
    @Override
    protected SpecificationLoaderSpi obtainSpecificationLoaderSpi(DeploymentType deploymentType, Collection<MetaModelRefiner> metaModelRefiners) throws IsisSystemException {
        
        final ProgrammingModel programmingModel = obtainReflectorProgrammingModel();
        final Set<FacetDecorator> facetDecorators = obtainReflectorFacetDecoratorSet();
        final MetaModelValidator mmv = obtainReflectorMetaModelValidator();
        final List<LayoutMetadataReader> layoutMetadataReaders = obtainLayoutMetadataReaders();
        return JavaReflectorHelper.createObjectReflector(programmingModel, metaModelRefiners, facetDecorators, layoutMetadataReaders, mmv, getConfiguration());
    }


    /**
     * Optional hook method.
     */
    protected ProgrammingModel obtainReflectorProgrammingModel() {
        return new ProgrammingModelFacetsJava5();
    }

    /**
     * Optional hook method.
     */
    protected Set<FacetDecorator> obtainReflectorFacetDecoratorSet() {
        return Sets.newHashSet((FacetDecorator)new StandardTransactionFacetDecorator(getConfiguration()));
    }

    /**
     * Optional hook method.
     */
    protected MetaModelValidator obtainReflectorMetaModelValidator() {
        return new MetaModelValidatorDefault();
    }

    /**
     * Optional hook method.
     */
    protected List<LayoutMetadataReader> obtainLayoutMetadataReaders() {
        return Lists.<LayoutMetadataReader>newArrayList(new LayoutMetadataReaderFromJson());
    }


    /**
     * The standard authentication manager, configured with the default authenticator (allows all requests through).
     */
    @Override
    protected AuthenticationManager obtainAuthenticationManager(DeploymentType deploymentType) throws IsisSystemException {
        final AuthenticationManagerStandard authenticationManager = new AuthenticationManagerStandard(getConfiguration());
        Authenticator authenticator = new AuthenticatorBypass(configuration);
        authenticationManager.addAuthenticator(authenticator);
        return authenticationManager;
    }

    /**
     * The standard authorization manager, allowing all access.
     */
    @Override
    protected AuthorizationManager obtainAuthorizationManager(DeploymentType deploymentType) {
        return new AuthorizationManagerStandard(getConfiguration());
    }

    /**
     * The in-memory object store (unless overridden by {@link #obtainPersistenceMechanismInstaller(IsisConfiguration)}).
     */
    @Override
    protected PersistenceSessionFactory obtainPersistenceSessionFactory(DeploymentType deploymentType) throws IsisSystemException {
        PersistenceMechanismInstaller installer = obtainPersistenceMechanismInstaller(getConfiguration());
        if(installer == null) {
            final InMemoryPersistenceMechanismInstaller inMemoryPersistenceMechanismInstaller = new InMemoryPersistenceMechanismInstaller();
            inMemoryPersistenceMechanismInstaller.setConfiguration(getConfiguration());
            installer = inMemoryPersistenceMechanismInstaller;
        }
        return installer.createPersistenceSessionFactory(deploymentType);
    }


    /**
     * Optional hook; if returns <tt>null</tt> then the {@link #obtainPersistenceSessionFactory(DeploymentType)} is used.
     */
    protected PersistenceMechanismInstaller obtainPersistenceMechanismInstaller(IsisConfiguration configuration) throws IsisSystemException {
        InMemoryPersistenceMechanismInstaller installer = new InMemoryPersistenceMechanismInstaller();
        installer.setConfiguration(getConfiguration());
        return installer;
    }

}
