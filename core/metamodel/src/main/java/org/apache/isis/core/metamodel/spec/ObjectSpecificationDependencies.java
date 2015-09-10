/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.spec;

import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;

public class ObjectSpecificationDependencies {

    private final DeploymentCategory deploymentCategory;
    private final ServicesInjector servicesInjector;
    private final SpecificationLoader specificationLoader;
    private final FacetProcessor facetProcessor;
    private final AdapterManager adapterManager;

    public ObjectSpecificationDependencies(
            final DeploymentCategory deploymentCategory,
            final ServicesInjector servicesInjector,
            final SpecificationLoader specificationLoader,
            final FacetProcessor facetProcessor,
            final AdapterManager adapterManager) {
        this.deploymentCategory = deploymentCategory;
        this.servicesInjector = servicesInjector;
        this.specificationLoader = specificationLoader;
        this.facetProcessor = facetProcessor;
        this.adapterManager = adapterManager;
    }

    public DeploymentCategory getDeploymentCategory() {
        return deploymentCategory;
    }
    
    public ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

    public SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    public FacetProcessor getFacetProcessor() {
        return facetProcessor;
    }

    public AdapterManager getAdapterManager() {
        return adapterManager;
    }
}