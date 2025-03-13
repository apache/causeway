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
package org.apache.causeway.core.metamodel.object;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.xactn.TransactionId;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.debug._XrayEvent;
import org.apache.causeway.commons.internal.ref.TransientObjectRef;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/**
 * (package private) specialization corresponding to {@link Specialization#VIEWMODEL}
 * @see ManagedObject.Specialization#VIEWMODEL
 */
record ManagedObjectViewmodel(
    @NonNull ObjectSpecification objSpec,
    @NonNull TransientObjectRef<Object> pojoRef,
    @NonNull TransientObjectRef<TransactionId> txIdDuringWhichRefreshed,
    @NonNull _Lazy<Bookmark> bookmarkLazy)
implements
    ManagedObject,
    Bookmarkable.BookmarkRefreshable,
    _RefreshableViewmodel {

    ManagedObjectViewmodel(
            final ObjectSpecification objSpec,
            final Object pojo,
            final Optional<Bookmark> bookmarkIfKnown) {

        this(
            objSpec,
            new TransientObjectRef<>(pojo),
            new TransientObjectRef<TransactionId>(null),
            null);
        bookmarkIfKnown.ifPresent(bookmarkLazy::set);
    }

    // canonical constructor
    ManagedObjectViewmodel(
        final ObjectSpecification objSpec,
        final TransientObjectRef<Object> pojoRef,
        final TransientObjectRef<TransactionId> txIdDuringWhichRefreshed,
        final _Lazy<Bookmark> bookmarkLazy) {
        _Assert.assertTrue(objSpec.isViewModel());
        specialization().assertCompliance(objSpec, pojoRef.getObject());
        this.objSpec = objSpec;
        this.pojoRef = pojoRef;
        this.txIdDuringWhichRefreshed = txIdDuringWhichRefreshed;
        this.bookmarkLazy = _Lazy.threadSafe(()->objSpec.viewmodelFacetElseFail().serializeToBookmark(this));
    }

    @Override
    public Optional<ObjectMemento> getMemento() {
        return ObjectMemento.singular(this);
    }

    @Override
    public String getTitle() {
        return _InternalTitleUtil.titleString(
            TitleRenderRequest.forObject(this));
    }

    @Override
    public Specialization specialization() {
        return ManagedObject.Specialization.VIEWMODEL;
    }

    @Override
    public Object getPojo() {
        return pojoRef.getObject();
    }

    @Override
    public final Optional<Bookmark> getBookmark() {
        return Optional.of(bookmarkLazy.get());
    }

    @Override
    public final boolean isBookmarkMemoized() {
        return bookmarkLazy.isMemoized();
    }

    @Override
    public void invalidateBookmark() {
        bookmarkLazy.clear();
    }

    // -- REFRESH OPTIMIZATION

    @Override
    public final void refreshViewmodel(final @Nullable Supplier<Bookmark> bookmarkSupplier) {
        var shouldRefresh = getTransactionService().currentTransactionId()
            .map(this::shouldRefresh)
            .orElse(true); // if there is no current transaction, refresh regardless; unexpected state, might fail later

        if(!shouldRefresh) return;

        if(isBookmarkMemoized()) {
            reloadViewmodelFromMemoizedBookmark();
        } else {
            var bookmark = bookmarkSupplier!=null
                    ? bookmarkSupplier.get()
                    : null;
            if(bookmark!=null) {
                reloadViewmodelFromBookmark(bookmark);
            }
        }
    }

    // -- OBJECT CONTRACT

    @Override
    public final boolean equals(final Object obj) {
        return obj instanceof ManagedObjectViewmodel other
            ? Objects.equals(this.objSpec().logicalTypeName(), other.objSpec().logicalTypeName())
                && Objects.equals(this.getPojo(), other.getPojo())
            : false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(objSpec().logicalTypeName(), getPojo());
    }

    @Override
    public final String toString() {
        return "ManagedObjectViewmodel[logicalTypeName=%s]".formatted(objSpec().logicalTypeName());
    }

    // -- HELPER

    private void replaceBookmark(final UnaryOperator<Bookmark> replacer) {
        final Bookmark old = bookmarkLazy.isMemoized()
                ? bookmarkLazy.get()
                : null;
        bookmarkLazy.clear();
        bookmarkLazy.set(replacer.apply(old));
    }

    private boolean shouldRefresh(final @NonNull TransactionId transactionId) {
        // if already refreshed within current transaction, skip
        if(Objects.equals(this.txIdDuringWhichRefreshed.getObject(), transactionId)) return false;
        this.txIdDuringWhichRefreshed.update(__->transactionId);
        return true;
    }

    /**
     * Reload current viewmodel object from memoized bookmark, otherwise does nothing.
     */
    private void reloadViewmodelFromMemoizedBookmark() {
        var bookmark = getBookmark().get();
        var viewModelClass = getCorrespondingClass();

        var recreatedViewmodel =
                getFactoryService().viewModel(viewModelClass, bookmark);

        _XrayEvent.event("Viewmodel '%s' recreated from memoized bookmark.", viewModelClass.getName());

        replacePojo(old->recreatedViewmodel);
    }

    private void reloadViewmodelFromBookmark(final @NonNull Bookmark bookmark) {
        var viewModelClass = getCorrespondingClass();
        var recreatedViewmodel =
                getFactoryService().viewModel(viewModelClass, bookmark);

        _XrayEvent.event("Viewmodel '%s' recreated from provided bookmark.", viewModelClass.getName());

        replacePojo(old->recreatedViewmodel);
        replaceBookmark(old->bookmark);
    }

    private void replacePojo(final UnaryOperator<Object> replacer) {
        pojoRef.update(pojo->specialization().assertCompliance(objSpec, replacer.apply(pojo)));
    }

}