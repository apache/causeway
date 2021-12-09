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
import java.util.Optional;
import java.util.function.BiConsumer;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.commons.MethodExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.HasPostConstructMethodCache;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;
import lombok.val;

public abstract class RecreatableObjectFacetAbstract
extends FacetAbstract
implements ViewModelFacet {

    private final HasPostConstructMethodCache postConstructMethodCache;
    private final ViewModelFacet.RecreationMechanism recreationMechanism;

    private static final Class<? extends Facet> type() {
        return ViewModelFacet.class;
    }

    protected RecreatableObjectFacetAbstract(
            final FacetHolder holder,
            final RecreationMechanism recreationMechanism,
            final HasPostConstructMethodCache postConstructMethodCache) {
        super(type(), holder);
        this.postConstructMethodCache = postConstructMethodCache;
        this.recreationMechanism = recreationMechanism;
    }

    protected RecreatableObjectFacetAbstract(
            final FacetHolder holder,
            final RecreationMechanism recreationMechanism,
            final HasPostConstructMethodCache postConstructMethodCache,
            final Facet.Precedence precedence) {
        super(type(), holder, precedence);
        this.postConstructMethodCache = postConstructMethodCache;
        this.recreationMechanism = recreationMechanism;
    }

    @Override
    public RecreationMechanism getRecreationMechanism() {
        return recreationMechanism;
    }

    @Override
    public final Object instantiate(final Class<?> viewModelClass, final Bookmark bookmark) {
        if (getRecreationMechanism() == RecreationMechanism.INITIALIZES) {
            throw new IllegalStateException("This view model instantiates rather than initializes");
        }

        val viewModelPojo = bookmark==null
                    || _Strings.isNullOrEmpty(bookmark.getIdentifier())
                ? ClassExtensions.newInstance(viewModelClass)
                : doInstantiate(viewModelClass, Optional.ofNullable(bookmark));

        getServiceInjector().injectServicesInto(viewModelPojo);
        invokePostConstructMethod(viewModelPojo);
        return viewModelPojo;
    }

    /**
     * Hook for subclass; must be overridden if {@link #getRecreationMechanism()} is
     * {@link org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet.RecreationMechanism#INSTANTIATES}
     * (ignored otherwise).
     */
    protected Object doInstantiate(final Class<?> viewModelClass, final Optional<Bookmark> bookmark) {
        throw new IllegalStateException("doInstantiate() must be overridden if RecreationMechanism is INSTANTIATES");
    }

    @Override
    public final void initialize(final Object viewModelPojo, final @Nullable Bookmark bookmark) {
        if (getRecreationMechanism() == RecreationMechanism.INSTANTIATES) {
            throw new IllegalStateException("This view model instantiates rather than initializes");
        }
        doInitialize(viewModelPojo, Optional.ofNullable(bookmark));
        getServiceInjector().injectServicesInto(viewModelPojo);
        invokePostConstructMethod(viewModelPojo);
    }

    /**
     * Hook for subclass; must be overridden if {@link #getRecreationMechanism()} is
     * {@link org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet.RecreationMechanism#INITIALIZES}
     * (ignored otherwise).
     */
    protected void doInitialize(final Object viewModelPojo, final Optional<Bookmark> bookmark) {
        throw new IllegalStateException("doInitialize() must be overridden if RecreationMechanism is INITIALIZE");
    }

    private void invokePostConstructMethod(final Object viewModel) {
        final Method postConstructMethod = postConstructMethodCache.postConstructMethodFor(viewModel);
        if (postConstructMethod != null) {
            MethodExtensions.invoke(postConstructMethod, viewModel);
        }
    }

    @Override
    public final Bookmark serializeToBookmark(@NonNull final ManagedObject managedObject) {
        return Bookmark.forLogicalTypeAndIdentifier(
                managedObject.getSpecification().getLogicalType(),
                serialize(managedObject));
    }

    protected abstract @NonNull String serialize(@NonNull ManagedObject managedObject);

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("recreationMechanism", recreationMechanism);
    }

}
