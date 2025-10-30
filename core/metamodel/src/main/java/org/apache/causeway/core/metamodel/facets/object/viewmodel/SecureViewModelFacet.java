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

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

import org.apache.causeway.applib.exceptions.unrecoverable.DigitalVerificationException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.commons.ClassExtensions;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.util.hmac.HmacUrlCodec;

sealed abstract class SecureViewModelFacet
extends FacetAbstract implements ViewModelFacet
permits
    ViewModelFacetForDomainObjectAnnotation,
    ViewModelFacetForJavaRecord,
    ViewModelFacetForSerializableInterface,
    ViewModelFacetForViewModelInterface,
    ViewModelFacetForXmlRootElementAnnotation {

    private static final Class<? extends Facet> type() {
        return ViewModelFacet.class;
    }

    private final HmacUrlCodec hmacUrlCodec;

    protected SecureViewModelFacet(
            final HmacUrlCodec hmacUrlCodec,
            final FacetHolder holder) {
        super(type(), holder);
        this.hmacUrlCodec = Objects.requireNonNull(hmacUrlCodec);
    }

    protected SecureViewModelFacet(
            final HmacUrlCodec hmacUrlCodec,
            final FacetHolder holder,
            final Facet.Precedence precedence) {
        super(type(), holder, precedence);
        this.hmacUrlCodec = Objects.requireNonNull(hmacUrlCodec);
    }

    @Override
    public final ManagedObject instantiate(
            final ObjectSpecification viewmodelSpec,
            final Optional<Bookmark> untrustedBookmarkOpt) {

        Objects.requireNonNull(viewmodelSpec);

        if(untrustedBookmarkOpt==null || untrustedBookmarkOpt.isEmpty()) {
            // this code path needs NO bookmark checking

            var viewModel = createViewmodel(viewmodelSpec);
            initialize(viewModel.getPojo());

            viewModel.getBookmark(); // trigger bookmark memoization, if not memoized already

            _Assert.assertTrue(viewModel.isBookmarkMemoized(),
                    ()->"Framework Bug: Viewmodel should have its bookmark memoized once initialized.");
            return viewModel;
        }

        // this code path needs untrusted bookmark checking ..

        var untrustedBookmark = untrustedBookmarkOpt.orElseThrow();

        byte[] trustedBookmarkIdAsBytes = Optional.ofNullable(untrustedBookmark.identifier())
            .filter(StringUtils::hasText)
            .map(untrustedBookmarkId->hmacUrlCodec.decodeFromUrl(untrustedBookmarkId).orElse(null))
            .orElse(null);

        if(trustedBookmarkIdAsBytes==null) {
            // verification failed
            throw new DigitalVerificationException("invalid request for " + viewmodelSpec.logicalTypeName());
        }

        var viewModel = ManagedObject.bookmarked(
            viewmodelSpec,
            createViewmodelPojo(viewmodelSpec, trustedBookmarkIdAsBytes),
            untrustedBookmark /* now trusted */);

        initialize(viewModel.getPojo());

        viewModel.getBookmark(); // trigger bookmark memoization, if not memoized already

        _Assert.assertTrue(viewModel.isBookmarkMemoized(),
                ()->"Framework Bug: Viewmodel should have its bookmark memoized once initialized.");
        return viewModel;
    }

    @Override
    public final void initialize(final @Nullable Object pojo) {
        if(pojo==null) return;
        getServiceInjector().injectServicesInto(pojo);
        invokePostConstructMethod(pojo);
    }

    @Override
    public final Bookmark serializeToBookmark(final @NonNull ManagedObject managedObject) {
        var digitallySignedBookmarId = hmacUrlCodec.encodeForUrl(encodeState(managedObject));
        return managedObject.createBookmark(digitallySignedBookmarId);
    }

    // -- ABSTRACT

    /**
     * Create viewmodel instance from given validated viewmodel state data.
     */
    protected abstract Object createViewmodelPojo(
            @NonNull ObjectSpecification viewmodelSpec,
            @NonNull byte[] trustedViewmodelState);

    /**
     * Encodes given viewmodel's state into a byte array for further processing
     * (digital signing and url-safe encoding).
     *
     * <p> The resulting byte array is eventually fed into {@link #createViewmodelPojo(ObjectSpecification, byte[])}
     * for re-creaton of the viewmodel instance.
     */
    protected abstract @NonNull byte[] encodeState(
            @NonNull ManagedObject viewmodel);

    /**
     * Create default viewmodel instance (without any {@link Bookmark} available).
     */
    protected ManagedObject createViewmodel(final @NonNull ObjectSpecification spec) {
        return ManagedObject.viewmodel(spec, ClassExtensions.newInstance(spec.getCorrespondingClass()),
            Optional.empty());
    }

    // -- HELPER

    private void invokePostConstructMethod(final Object viewModel) {
        _ClassCache.getInstance().streamPostConstructMethods(viewModel.getClass())
        .forEach(postConstructMethod->
        CanonicalInvoker.invoke(postConstructMethod, viewModel));
    }
}
