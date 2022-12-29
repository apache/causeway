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
package org.apache.causeway.core.metamodel.facets.object.viewmodel;

import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.commons.ClassExtensions;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.val;

public abstract class ViewModelFacetAbstract
extends FacetAbstract
implements ViewModelFacet {

    private static final Class<? extends Facet> type() {
        return ViewModelFacet.class;
    }

    protected ViewModelFacetAbstract(
            final FacetHolder holder) {
        super(type(), holder);
    }

    protected ViewModelFacetAbstract(
            final FacetHolder holder,
            final Facet.Precedence precedence) {
        super(type(), holder, precedence);
    }

    @Override
    public final ManagedObject instantiate(
            final ObjectSpecification spec,
            final Optional<Bookmark> bookmarkIfAny) {

        val bookmark = bookmarkIfAny.orElse(null);
        val isBookmarkAvailable = bookmarkIfAny.map(Bookmark::getIdentifier)
                .map(_Strings::isNotEmpty)
                .orElse(false);

        val viewModel = !isBookmarkAvailable
                ? createViewmodel(spec)
                : createViewmodel(spec, bookmark);

        initialize(viewModel.getPojo());
        return viewModel;
    }

    @Override
    public final void initialize(final @Nullable Object pojo) {
        if(pojo==null) return;
        getServiceInjector().injectServicesInto(pojo);
        invokePostConstructMethod(pojo);
    }

    /**
     * Create default viewmodel instance (without any {@link Bookmark} available).
     */
    protected ManagedObject createViewmodel(final @NonNull ObjectSpecification spec) {
        return ManagedObject.viewmodel(spec, ClassExtensions.newInstance(spec.getCorrespondingClass()),
                Optional.empty());
    }

    /**
     * Create viewmodel instance from a given valid {@link Bookmark}.
     */
    protected abstract ManagedObject createViewmodel(
            @NonNull ObjectSpecification spec,
            @NonNull Bookmark bookmark);

    private void invokePostConstructMethod(final Object viewModel) {
        _ClassCache.getInstance().streamPostConstructMethods(viewModel.getClass())
        .forEach(postConstructMethod->
            CanonicalInvoker.invoke(postConstructMethod, viewModel));
    }

    @Override
    public final Bookmark serializeToBookmark(final @NonNull ManagedObject managedObject) {
        return managedObject.createBookmark(serialize(managedObject));
    }

    protected abstract @NonNull String serialize(@NonNull ManagedObject managedObject);

}
