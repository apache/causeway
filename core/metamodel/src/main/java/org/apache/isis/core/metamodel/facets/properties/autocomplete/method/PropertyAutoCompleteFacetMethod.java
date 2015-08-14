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

package org.apache.isis.core.metamodel.facets.properties.autocomplete.method;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.CollectionUtils;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.MinLengthUtil;
import org.apache.isis.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacetAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;

public class PropertyAutoCompleteFacetMethod extends PropertyAutoCompleteFacetAbstract implements ImperativeFacet {

    private final Method method;
    private final Class<?> choicesClass;
    private final int minLength;

    private final AdapterManager adapterManager;
    private SpecificationLoader specificationLookup;

    public PropertyAutoCompleteFacetMethod(final Method method, final Class<?> choicesClass, final FacetHolder holder, final SpecificationLoader specificationLookup, final AdapterManager adapterManager) {
        super(holder);
        this.method = method;
        this.choicesClass = choicesClass;
        this.specificationLookup = specificationLookup;
        this.adapterManager = adapterManager;
        this.minLength = MinLengthUtil.determineMinLength(method);
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
    public int getMinLength() {
        return minLength;
    }

    @Override
    public boolean impliesResolve() {
        return true;
    }

    @Override
    public boolean impliesObjectChanged() {
        return false;
    }

    @Override
    public Object[] autoComplete(
            final ObjectAdapter owningAdapter,
            final String searchArg,
            final AuthenticationSession authenticationSession,
            final DeploymentCategory deploymentCategory) {

        final Object collectionOrArray = ObjectAdapter.InvokeUtils.invoke(method, owningAdapter, searchArg);
        if (collectionOrArray == null) {
            return null;
        }

        final ObjectAdapter collectionAdapter = getAdapterManager().adapterFor(collectionOrArray);

        final FacetedMethod facetedMethod = (FacetedMethod) getFacetHolder();
        final Class<?> propertyType = facetedMethod.getType();

        final List<ObjectAdapter> visibleAdapters =
                ObjectAdapter.Util.visibleAdapters(
                        collectionAdapter, propertyType,
                        authenticationSession, deploymentCategory,
                        getSpecificationLookup());
        final List<Object> filteredObjects = Lists.newArrayList(
                Iterables.transform(visibleAdapters, ObjectAdapter.Functions.getObject()));

        final ObjectSpecification propertySpec = getSpecification(propertyType);
        return CollectionUtils.getCollectionAsObjectArray(filteredObjects, propertySpec, getAdapterManager());
    }

    @Override
    protected String toStringValues() {
        return "method=" + method + ",class=" + choicesClass;
    }




    protected ObjectSpecification getSpecification(final Class<?> type) {
        return type != null ? getSpecificationLookup().loadSpecification(type) : null;
    }

    // ////////////////////////////////////////////
    // Dependencies
    // ////////////////////////////////////////////

    protected AdapterManager getAdapterManager() {
        return adapterManager;
    }

    protected SpecificationLoader getSpecificationLookup() {
        return specificationLookup;
    }



}
