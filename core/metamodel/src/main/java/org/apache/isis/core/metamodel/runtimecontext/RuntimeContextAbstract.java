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

package org.apache.isis.core.metamodel.runtimecontext;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProviderAbstract;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;

public abstract class RuntimeContextAbstract implements RuntimeContext {

    private final DeploymentCategory deploymentCategory;
    private final IsisConfigurationDefault configuration;
    private final ServicesInjector servicesInjector;
    private final SpecificationLoaderSpi specificationLoader;

    public RuntimeContextAbstract(
            final DeploymentCategory deploymentCategory,
            final IsisConfigurationDefault configuration,
            final ServicesInjector servicesInjector,
            final SpecificationLoaderSpi specificationLoader) {
        this.deploymentCategory = deploymentCategory;
        this.configuration = configuration;
        this.servicesInjector = servicesInjector;
        this.specificationLoader = specificationLoader;
    }


    @Override
    public DeploymentCategoryProvider getDeploymentCategoryProvider() {
        return new DeploymentCategoryProviderAbstract() {

            @Override
            public DeploymentCategory getDeploymentCategory() {
                return deploymentCategory;
            }
        };
    }


    @Override
    public ConfigurationService getConfigurationService() {
        return configuration;
    }

    @Override
    public ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

    @Override
    public SpecificationLoaderSpi getSpecificationLoader() {
        return specificationLoader;
    }


    //@Override
    public void injectInto(final Object candidate) {
        if (RuntimeContextAware.class.isAssignableFrom(candidate.getClass())) {
            final RuntimeContextAware cast = RuntimeContextAware.class.cast(candidate);
            cast.setRuntimeContext(this);
        }
        injectSubcomponentsInto(candidate);
    }

    protected void injectSubcomponentsInto(final Object candidate) {
        getAuthenticationSessionProvider().injectInto(candidate);
        getDeploymentCategoryProvider().injectInto(candidate);
        getTransactionStateProvider().injectInto(candidate);
        getServicesInjector().injectInto(candidate);
        getConfigurationService().injectInto(candidate);
        getLocalizationProvider().injectInto(candidate);
        getPersistenceSessionService().injectInto(candidate);
        getMessageBrokerService().injectInto(candidate);
        getSpecificationLoader().injectInto(candidate);
    }

}
