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
import java.util.Map;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.CollectionUtils;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.MinLengthUtil;
import org.apache.isis.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacetAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public class PropertyAutoCompleteFacetMethod extends PropertyAutoCompleteFacetAbstract implements ImperativeFacet {

    private final Method method;
    private final Class<?> choicesClass;
    private final int minLength;

    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final ObjectAdapterProvider adapterProvider;
    private SpecificationLoader specificationLoader;

    public PropertyAutoCompleteFacetMethod(
            final Method method,
            final Class<?> choicesClass,
            final FacetHolder holder,
            final SpecificationLoader specificationLoader,
            final AuthenticationSessionProvider authenticationSessionProvider,
            final ObjectAdapterProvider adapterProvider) {
        super(holder);
        this.method = method;
        this.choicesClass = choicesClass;
        this.specificationLoader = specificationLoader;
        this.authenticationSessionProvider = authenticationSessionProvider;
        this.adapterProvider = adapterProvider;
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
    public Object[] autoComplete(
            final ObjectAdapter owningAdapter,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final Object collectionOrArray = ObjectAdapter.InvokeUtils.invoke(method, owningAdapter, searchArg);
        if (collectionOrArray == null) {
            return null;
        }

        final ObjectAdapter collectionAdapter = getObjectAdapterProvider().adapterFor(collectionOrArray);

        final FacetedMethod facetedMethod = (FacetedMethod) getFacetHolder();
        final Class<?> propertyType = facetedMethod.getType();

        final List<ObjectAdapter> visibleAdapters =
                ObjectAdapter.Util.visibleAdapters(
                        collectionAdapter,
                        interactionInitiatedBy);
        final List<Object> filteredObjects =
                _Lists.map(visibleAdapters, ObjectAdapter.Util::unwrapPojo);

        final ObjectSpecification propertySpec = getSpecification(propertyType);
        return CollectionUtils.getCollectionAsObjectArray(filteredObjects, propertySpec, getObjectAdapterProvider());
    }

    @Override
    protected String toStringValues() {
        return "method=" + method + ",class=" + choicesClass;
    }




    protected ObjectSpecification getSpecification(final Class<?> type) {
        return type != null ? getSpecificationLoader().loadSpecification(type) : null;
    }

    // ////////////////////////////////////////////
    // Dependencies
    // ////////////////////////////////////////////

    protected ObjectAdapterProvider getObjectAdapterProvider() {
        return adapterProvider;
    }

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    protected AuthenticationSession getAuthenticationSession() {
        return authenticationSessionProvider.getAuthenticationSession();
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        ImperativeFacet.Util.appendAttributesTo(this, attributeMap);
        attributeMap.put("choicesType", choicesClass);
        attributeMap.put("minLength", minLength);
    }

}
