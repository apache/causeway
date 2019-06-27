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

package org.apache.isis.metamodel.facets.object.recreatable;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.core.commons.lang.MethodExtensions;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.MarkerFacetAbstract;
import org.apache.isis.metamodel.facets.PostConstructMethodCache;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;

public abstract class RecreatableObjectFacetAbstract extends MarkerFacetAbstract implements ViewModelFacet {

    private final PostConstructMethodCache postConstructMethodCache;
    private final ViewModelFacet.RecreationMechanism recreationMechanism;

    public static Class<? extends Facet> type() {
        return ViewModelFacet.class;
    }

    public RecreatableObjectFacetAbstract(final FacetHolder holder, final RecreationMechanism recreationMechanism,
            final PostConstructMethodCache postConstructMethodCache) {
        super(type(), holder);
        this.postConstructMethodCache = postConstructMethodCache;
        this.recreationMechanism = recreationMechanism;
    }

    @Override
    public boolean isCloneable(Object pojo) {
        return pojo != null && pojo instanceof ViewModel.Cloneable;
    }

    @Override
    public boolean isImplicitlyImmutable() {
        final FacetHolder facetHolder = getFacetHolder();
        if (facetHolder instanceof ObjectSpecificationDefault) {
            final ObjectSpecificationDefault objectSpec = (ObjectSpecificationDefault) facetHolder;
            final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
            if (ViewModel.Cloneable.class.isAssignableFrom(correspondingClass)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object clone(Object pojo) {
        ViewModel.Cloneable viewModelCloneable = (ViewModel.Cloneable) pojo;
        return viewModelCloneable.clone();
    }

    @Override
    public RecreationMechanism getRecreationMechanism() {
        return recreationMechanism;
    }

    @Override
    public final Object instantiate(final Class<?> viewModelClass, final String mementoStr) {
        if (getRecreationMechanism() == RecreationMechanism.INITIALIZES) {
            throw new IllegalStateException("This view model instantiates rather than initializes");
        }
        final Object viewModelPojo = doInstantiate(viewModelClass, mementoStr);
        getServiceInjector().injectServicesInto(viewModelPojo);
        invokePostConstructMethod(viewModelPojo);
        return viewModelPojo;
    }

    /**
     * Hook for subclass; must be overridden if {@link #getRecreationMechanism()} is
     * {@link RecreationMechanism#INSTANTIATES} (ignored otherwise).
     */
    protected Object doInstantiate(final Class<?> viewModelClass, final String mementoStr) {
        throw new IllegalStateException("doInstantiate() must be overridden if RecreationMechanism is INSTANTIATES");
    }

    @Override
    public final void initialize(final Object viewModelPojo, final String mementoStr) {
        if (getRecreationMechanism() == RecreationMechanism.INSTANTIATES) {
            throw new IllegalStateException("This view model instantiates rather than initializes");
        }
        doInitialize(viewModelPojo, mementoStr);
        getServiceInjector().injectServicesInto(viewModelPojo);
        invokePostConstructMethod(viewModelPojo);
    }

    /**
     * Hook for subclass; must be overridden if {@link #getRecreationMechanism()} is
     * {@link RecreationMechanism#INITIALIZES} (ignored otherwise).
     */
    protected void doInitialize(final Object viewModelPojo, final String mementoStr) {
        throw new IllegalStateException("doInitialize() must be overridden if RecreationMechanism is INITIALIZE");
    }

    private void invokePostConstructMethod(final Object viewModel) {
        final Method postConstructMethod = postConstructMethodCache.postConstructMethodFor(viewModel);
        if (postConstructMethod != null) {
            MethodExtensions.invoke(postConstructMethod, viewModel);
        }
    }

    @Override
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("recreationMechanism", recreationMechanism);
    }
    
}
