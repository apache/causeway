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
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract.DeprecatedPolicy;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.ReflectorConstants;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfigurationAndAnnotation;
import org.apache.isis.core.runtime.system.IsisSystemException;
import org.apache.isis.progmodels.dflt.JavaReflectorHelper;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

import static org.apache.isis.commons.internal.base._With.requires;
import static org.apache.isis.config.internal._Config.getConfiguration;

/**
 * 
 */
public final class IsisComponentProvider {
    
    // -- BUILDER - DEFAULT
    
    public static IsisComponentProviderBuilder builder(AppManifest appManifest) {
        // ensure we the appmanifest on the config
        
        return new IsisComponentProviderBuilder()
                .appManifest(appManifest);
    }
    
    // -- BUILDER - USING INSTALLERS
    
    public static IsisComponentProviderBuilder builderUsingInstallers(AppManifest appManifest) {
        
        final IsisComponentProviderHelper_UsingInstallers helper = 
                new IsisComponentProviderHelper_UsingInstallers(appManifest);
        
        return builder(appManifest)
                .authenticationManager(helper.authenticationManager)
                .authorizationManager(helper.authorizationManager);
    }
    

    // -- CONSTRUCTOR

    private final AppManifest appManifest;
    protected final List<Object> services;
    protected final AuthenticationManager authenticationManager;
    protected final AuthorizationManager authorizationManager;

    IsisComponentProvider(
            final AppManifest appManifest,
            final AuthenticationManager authenticationManager,
            final AuthorizationManager authorizationManager) {

        this.appManifest = requires(appManifest, "appManifest");

        this.services = new ServicesInstallerFromConfigurationAndAnnotation().getServices();

        this.authenticationManager = authenticationManager;
        this.authorizationManager = authorizationManager;
    }

    // --
    
    public AppManifest getAppManifest() {
        return appManifest;
    }

    // -- provideAuth*

    public AuthenticationManager provideAuthenticationManager() {
        return authenticationManager;
    }

    public AuthorizationManager provideAuthorizationManager() {
        return authorizationManager;
    }

    // -- provideServiceInjector

    public ServicesInjector provideServiceInjector() {
        return ServicesInjector.builder()
                .addServices(services)
                .build();
    }

    // -- provideSpecificationLoader

    public SpecificationLoader provideSpecificationLoader(
            final ServicesInjector servicesInjector,
            final Collection<MetaModelRefiner> metaModelRefiners)  throws IsisSystemException {

        final IsisConfiguration configuration = getConfiguration();
        
        final ProgrammingModel programmingModel = createProgrammingModel(configuration);
        final MetaModelValidator mmv = createMetaModelValidator(configuration);

        return JavaReflectorHelper.createObjectReflector(
                programmingModel, metaModelRefiners,
                mmv,
                servicesInjector);
    }

    protected MetaModelValidator createMetaModelValidator(IsisConfiguration configuration) {
        
        final String metaModelValidatorClassName =
                configuration.getString(
                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME,
                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(metaModelValidatorClassName, MetaModelValidator.class);
    }

    protected ProgrammingModel createProgrammingModel(IsisConfiguration configuration) {
        
        final DeprecatedPolicy deprecatedPolicy = DeprecatedPolicy.parse(configuration);

        final ProgrammingModel programmingModel = new ProgrammingModelFacetsJava5(deprecatedPolicy);
        ProgrammingModel.Util.includeFacetFactories(configuration, programmingModel);
        ProgrammingModel.Util.excludeFacetFactories(configuration, programmingModel);
        return programmingModel;
    }





}
