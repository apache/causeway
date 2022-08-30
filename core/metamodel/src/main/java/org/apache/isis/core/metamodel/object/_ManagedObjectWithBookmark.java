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
package org.apache.isis.core.metamodel.object;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.debug._XrayEvent;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.val;

abstract class _ManagedObjectWithBookmark
extends _ManagedObjectSpecifiedLegacy {

    protected final _Lazy<Optional<Bookmark>> bookmarkLazy =
            _Lazy.threadSafe(()->bookmark(this));

    protected _ManagedObjectWithBookmark(final Specialization specialization) {
        super(specialization);
    }

    @Override
    public final Optional<Bookmark> getBookmark() {
        return bookmarkLazy.get();
    }

    @Override
    public final boolean isBookmarkMemoized() {
        return bookmarkLazy.isMemoized();
    }

    @Override
    public final Optional<Bookmark> getBookmarkRefreshed() {
        // silently ignore invalidation, when the pojo is an entity
        if(!getSpecification().isEntity()) {
            bookmarkLazy.clear();
        }
        return getBookmark();
    }

    private void replaceBookmark(final UnaryOperator<Bookmark> replacer) {
        final Bookmark old = bookmarkLazy.isMemoized()
                ? bookmarkLazy.get().orElse(null)
                : null;
        bookmarkLazy.clear();
        bookmarkLazy.set(Optional.ofNullable(replacer.apply(old)));
    }

    // guards against non-identifiable objects;
    // historically, we allowed non-identifiable to be handled by the objectManager,
    // which as a fallback creates 'random' UUIDs
    private Optional<Bookmark> bookmark(final @Nullable ManagedObject adapter) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)
                || adapter.getSpecification().isValue()
                || !ManagedObjects.isIdentifiable(adapter)) {
            return Optional.empty();
        }

        return ManagedObjects.spec(adapter)
                .map(ObjectSpecification::getMetaModelContext)
                .map(MetaModelContext::getObjectManager)
                .map(objectManager->objectManager.bookmarkObject(adapter));
    }

    // -- REFRESH OPTIMIZATION

    private UUID interactionIdDuringWhichRefreshed = null;

    @Override
    public final void refreshViewmodel(final @Nullable Supplier<Bookmark> bookmarkSupplier) {
        val spec = getSpecification();
        if(spec.isViewModel()) {
            val viewModelFacet = spec.getFacet(ViewModelFacet.class);
            if(viewModelFacet.containsEntities()) {

                val shouldRefresh = spec.getMetaModelContext().getInteractionProvider().getInteractionId()
                .map(this::shouldRefresh)
                .orElse(true); // if there is no current interaction, refresh regardless; unexpected state, might fail later

                if(!shouldRefresh) {
                    return;
                }

                if(isBookmarkMemoized()) {
                    reloadViewmodelFromMemoizedBookmark();
                } else {
                    val bookmark = bookmarkSupplier!=null
                            ? bookmarkSupplier.get()
                            : null;
                    if(bookmark!=null) {
                        reloadViewmodelFromBookmark(bookmark);
                    }
                }
            }
        }
    }

    private boolean shouldRefresh(final @NonNull UUID interactionId) {
        if(Objects.equals(this.interactionIdDuringWhichRefreshed, interactionId)) {
            return false; // already refreshed within current interaction
        }
        this.interactionIdDuringWhichRefreshed = interactionId;
        return true;
    }

    /**
     * Reload current viewmodel object from memoized bookmark, otherwise does nothing.
     */
    private void reloadViewmodelFromMemoizedBookmark() {
        val spec = getSpecification();
        if(isBookmarkMemoized()
                && spec.isViewModel()) {

            val bookmark = getBookmark().get();
            val viewModelClass = spec.getCorrespondingClass();

            val recreatedViewmodel =
                    getMetaModelContext().getFactoryService().viewModel(viewModelClass, bookmark);

            _XrayEvent.event("Viewmodel '%s' recreated from memoized bookmark.", viewModelClass.getName());

            replacePojo(old->recreatedViewmodel);
        }
    }

    private void reloadViewmodelFromBookmark(final @NonNull Bookmark bookmark) {
        val spec = getSpecification();
        if(spec.isViewModel()) {
            val viewModelClass = spec.getCorrespondingClass();

            val recreatedViewmodel =
                    getMetaModelContext().getFactoryService().viewModel(viewModelClass, bookmark);

            _XrayEvent.event("Viewmodel '%s' recreated from provided bookmark.", viewModelClass.getName());

            replacePojo(old->recreatedViewmodel);
            replaceBookmark(old->bookmark);
        }
    }

    /**
     * Introduced, so we can re-fetch detached entity pojos in place.
     */
    abstract void replacePojo(UnaryOperator<Object> replacer);

}