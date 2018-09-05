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

package org.apache.isis.core.metamodel.facets.propcoll.accessor;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public abstract class PropertyOrCollectionAccessorFacetAbstract
extends FacetAbstract
implements PropertyOrCollectionAccessorFacet {

    private final ObjectSpecification onType;
    private final ObjectAdapterProvider adapterProvider;
    private final SpecificationLoader specificationLoader;
    private final IsisConfiguration configuration;
    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final DeploymentCategory deploymentCategory;

    public static Class<? extends Facet> type() {
        return PropertyOrCollectionAccessorFacet.class;
    }

    public PropertyOrCollectionAccessorFacetAbstract(
            final ObjectSpecification onType,
            final FacetHolder holder,
            final DeploymentCategory deploymentCategory,
            final IsisConfiguration configuration,
            final SpecificationLoader specificationLoader,
            final AuthenticationSessionProvider authenticationSessionProvider,
            final ObjectAdapterProvider adapterProvider) {
        super(type(), holder, Derivation.NOT_DERIVED);
        this.onType = onType;
        this.adapterProvider = adapterProvider;
        this.specificationLoader = specificationLoader;
        this.configuration = configuration;
        this.authenticationSessionProvider = authenticationSessionProvider;
        this.deploymentCategory = deploymentCategory;
    }

    @Override
    public ObjectSpecification getOnType() {
        return onType;
    }

    @Override
    public abstract Object getProperty(
            ObjectAdapter inObject,
            final InteractionInitiatedBy interactionInitiatedBy);

    protected ObjectSpecification getSpecification(final Class<?> type) {
        return type != null ? getSpecificationLoader().loadSpecification(type) : null;
    }

    // //////////////////////////////////////

    protected ObjectAdapterProvider getObjectAdapterProvider() {
        return adapterProvider;
    }

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    protected IsisConfiguration getConfiguration() {
        return configuration;
    }

    public DeploymentCategory getDeploymentCategory() {
        return deploymentCategory;
    }

    public AuthenticationSession getAuthenticationSession() {
        return authenticationSessionProvider.getAuthenticationSession();
    }

}