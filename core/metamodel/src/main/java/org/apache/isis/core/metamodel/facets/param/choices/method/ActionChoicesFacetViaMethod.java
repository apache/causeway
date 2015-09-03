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

package org.apache.isis.core.metamodel.facets.param.choices.method;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionChoicesFacetAbstract;
import org.apache.isis.core.metamodel.spec.DomainModelException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.facets.CollectionUtils;

public class ActionChoicesFacetViaMethod extends ActionChoicesFacetAbstract implements ImperativeFacet {

    private final Method method;
    private final Class<?> choicesType;
    private final SpecificationLoader specificationLoader;
    private final AdapterManager adapterManager;

    public ActionChoicesFacetViaMethod(
            final Method method,
            final Class<?> choicesType,
            final FacetHolder holder,
            final SpecificationLoader specificationLoader,
            final AdapterManager adapterManager) {
        super(holder);
        this.method = method;
        this.choicesType = choicesType;
        this.specificationLoader = specificationLoader;
        this.adapterManager = adapterManager;
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
    public Object[][] getChoices(
            final ObjectAdapter owningAdapter,
            final AuthenticationSession authenticationSession,
            final DeploymentCategory deploymentCategory,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final Object objectOrCollection = ObjectAdapter.InvokeUtils.invoke(method, owningAdapter);
        if (!(objectOrCollection instanceof Object[])) {
            throw new DomainModelException(String.format(
                    "Expected an array of collections (Object[]) containing choices for all parameters, "
                            + "but got %s instead. Perhaps the parameter number is missing?",
                    objectOrCollection));
        }
        final Object[] options = (Object[]) objectOrCollection;
        final Object[][] results = new Object[options.length][];
        for (int i = 0; i < results.length; i++) {
            final Class<?> parameterType = method.getParameterTypes()[i];
            results[i] = handleResults(options[i], parameterType, authenticationSession, deploymentCategory,
                    interactionInitiatedBy);
        }
        return results;
    }

    private Object[] handleResults(
            final Object collectionOrArray,
            final Class<?> parameterType,
            final AuthenticationSession authenticationSession,
            final DeploymentCategory deploymentCategory,
            final InteractionInitiatedBy interactionInitiatedBy) {
        if (collectionOrArray == null) {
            return null;
        }

        final ObjectAdapter collectionAdapter = getAdapterManager().adapterFor(collectionOrArray);

        final List<ObjectAdapter> visibleAdapters =
                ObjectAdapter.Util.visibleAdapters(
                        collectionAdapter,
                        authenticationSession, deploymentCategory,
                        interactionInitiatedBy);
        final List<Object> filteredObjects = Lists.newArrayList(
                Iterables.transform(visibleAdapters, ObjectAdapter.Functions.getObject()));

        final ObjectSpecification parameterSpec = getSpecification(parameterType);
        return CollectionUtils.getCollectionAsObjectArray(filteredObjects, parameterSpec, getAdapterManager());
    }

    @Override
    protected String toStringValues() {
        return "method=" + method + ",type=" + choicesType;
    }

    protected ObjectSpecification getSpecification(final Class<?> type) {
        return type != null ? getSpecificationLoader().loadSpecification(type) : null;
    }

    // ///////////////////////////////////////////////////////
    // Dependencies
    // ///////////////////////////////////////////////////////

    private SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    private AdapterManager getAdapterManager() {
        return adapterManager;
    }

}
