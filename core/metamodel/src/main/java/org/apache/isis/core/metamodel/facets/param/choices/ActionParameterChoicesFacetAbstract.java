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

package org.apache.isis.core.metamodel.facets.param.choices;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public abstract class ActionParameterChoicesFacetAbstract extends FacetAbstract implements ActionParameterChoicesFacet {

    public static Class<? extends Facet> type() {
        return ActionParameterChoicesFacet.class;
    }

    private final DeploymentCategory deploymentCategory;
    private final SpecificationLoader specificationLoader;
    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final ObjectAdapterProvider adapterProvider;

    public ActionParameterChoicesFacetAbstract(
            final FacetHolder holder,
            final DeploymentCategory deploymentCategory,
            final SpecificationLoader specificationLoader,
            final AuthenticationSessionProvider authenticationSessionProvider,
            final ObjectAdapterProvider adapterProvider) {
        super(type(), holder, Derivation.NOT_DERIVED);
        this.deploymentCategory = deploymentCategory;
        this.specificationLoader = specificationLoader;
        this.authenticationSessionProvider = authenticationSessionProvider;
        this.adapterProvider = adapterProvider;
    }

    protected ObjectSpecification getSpecification(final Class<?> type) {
        return type != null ? getSpecificationLoader().loadSpecification(type) : null;
    }

    // /////////////////////////////////////////////////////////
    // Dependencies
    // /////////////////////////////////////////////////////////

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    protected ObjectAdapterProvider getObjectAdapterProvider() {
        return adapterProvider;
    }

    protected DeploymentCategory getDeploymentCategory() {
        return deploymentCategory;
    }

    protected AuthenticationSession getAuthenticationSession() {
        return authenticationSessionProvider.getAuthenticationSession();
    }
}
