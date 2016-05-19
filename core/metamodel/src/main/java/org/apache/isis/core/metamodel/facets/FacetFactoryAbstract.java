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

package org.apache.isis.core.metamodel.facets;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.runtimecontext.ConfigurationServiceInternal;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderAware;

public abstract class FacetFactoryAbstract implements FacetFactory, SpecificationLoaderAware, ServicesInjectorAware {

    private final List<FeatureType> featureTypes;

    public FacetFactoryAbstract(final List<FeatureType> featureTypes) {
        this.featureTypes = ImmutableList.copyOf(featureTypes);
    }

    @Override
    public List<FeatureType> getFeatureTypes() {
        return featureTypes;
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
    }

    // ////////////////////////////////////////////////////////////////
    // Dependencies (injected)
    // ////////////////////////////////////////////////////////////////

    private SpecificationLoader specificationLoader;

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    /**
     * Injected
     */
    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLookup) {
        this.specificationLoader = specificationLookup;
    }


    protected AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return servicesInjector.lookupService(AuthenticationSessionProvider.class);
    }

    protected DeploymentCategory getDeploymentCategory() {
        return servicesInjector.lookupService(DeploymentCategoryProvider.class).getDeploymentCategory();
    }

    protected IsisConfiguration getConfiguration() {
        final ConfigurationServiceInternal configurationServiceInternal = servicesInjector
                .lookupService(ConfigurationServiceInternal.class);
        return (IsisConfigurationDefault)configurationServiceInternal;
    }

    protected ServicesInjector servicesInjector;

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

}
