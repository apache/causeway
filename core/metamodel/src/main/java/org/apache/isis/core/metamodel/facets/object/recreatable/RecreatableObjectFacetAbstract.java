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

package org.apache.isis.core.metamodel.facets.object.recreatable;

import java.lang.reflect.Method;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.core.commons.lang.MethodExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.MarkerFacetAbstract;
import org.apache.isis.core.metamodel.facets.PostConstructMethodCache;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;

public abstract class RecreatableObjectFacetAbstract extends MarkerFacetAbstract implements ViewModelFacet {

    private final ArchitecturalLayer architecturalLayer;
    private final PostConstructMethodCache postConstructMethodCache;

    public static Class<? extends Facet> type() {
        return ViewModelFacet.class;
    }

    public RecreatableObjectFacetAbstract(
            final FacetHolder holder,
            final ArchitecturalLayer architecturalLayer,
            final PostConstructMethodCache postConstructMethodCache) {
        super(type(), holder);
        this.architecturalLayer = architecturalLayer;
        this.postConstructMethodCache = postConstructMethodCache;
    }

    @Override
    public boolean isCloneable(Object pojo) {
        return pojo != null && pojo instanceof ViewModel.Cloneable;
    }

    @Override
    public Object clone(Object pojo) {
        ViewModel.Cloneable viewModelCloneable = (ViewModel.Cloneable) pojo;
        return viewModelCloneable.clone();
    }

    @Override
    public ArchitecturalLayer getArchitecturalLayer() {
        return architecturalLayer;
    }

    @Override
    public final void initialize(
            final Object viewModelPojo,
            final String mementoStr) {
        doInitialize(viewModelPojo, mementoStr);
        invokePostConstructMethod(viewModelPojo);
    }

    /**
     * Mandatory hook for subclasses.
     */
    protected abstract void doInitialize(
            final Object viewModelPojo,
            final String mementoStr);

    private void invokePostConstructMethod(final Object viewModel) {
        final Method postConstructMethod = postConstructMethodCache.postConstructMethodFor(viewModel);
        if(postConstructMethod != null) {
            MethodExtensions.invoke(postConstructMethod, viewModel);
        }
    }

}
