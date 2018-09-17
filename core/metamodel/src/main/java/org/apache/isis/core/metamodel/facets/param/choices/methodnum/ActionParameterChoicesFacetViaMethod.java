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

package org.apache.isis.core.metamodel.facets.param.choices.methodnum;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.CollectionUtils;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacetAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public class ActionParameterChoicesFacetViaMethod extends ActionParameterChoicesFacetAbstract implements ImperativeFacet {

    private final Method method;
    private final Class<?> choicesType;

    public ActionParameterChoicesFacetViaMethod(
            final Method method,
            final Class<?> choicesType,
            final FacetHolder holder,
            final DeploymentCategory deploymentCategory,
            final SpecificationLoader specificationLookup,
            final AuthenticationSessionProvider authenticationSessionProvider,
            final ObjectAdapterProvider adapterProvider) {
        super(holder, deploymentCategory, specificationLookup, authenticationSessionProvider, adapterProvider);
        this.method = method;
        this.choicesType = choicesType;
    }

    /**
     * Returns a singleton list of the {@link Method} provided in the
     * constructor.
     */
    @Override
    public List<Method> getMethods() {
        return Collections.singletonList(method);
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.CHOICES_OR_AUTOCOMPLETE;
    }

    @Override
    public Object[] getChoices(
            final ObjectAdapter adapter,
            final List<ObjectAdapter> argumentsIfAvailable,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final Object choices =
                ObjectAdapter.InvokeUtils.invokeAutofit(
                        method, adapter, argumentsIfAvailable);
        if (choices == null) {
            return _Constants.emptyObjects;
        }
        final ObjectAdapter objectAdapter = getObjectAdapterProvider().adapterFor(choices);
        final FacetedMethodParameter facetedMethodParameter = (FacetedMethodParameter) getFacetHolder();
        final Class<?> parameterType = facetedMethodParameter.getType();

        final List<ObjectAdapter> visibleAdapters =
                ObjectAdapter.Util.visibleAdapters(
                        objectAdapter,
                        interactionInitiatedBy);
        final List<Object> visibleObjects =
                _Lists.map(visibleAdapters, ObjectAdapter.Functions.getObject());

        final ObjectSpecification parameterSpec = getSpecification(parameterType);
        return CollectionUtils.getCollectionAsObjectArray(visibleObjects, parameterSpec, getObjectAdapterProvider());
    }

    @Override
    protected String toStringValues() {
        return "method=" + method + ",type=" + choicesType;
    }

}
