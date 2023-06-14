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

import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;

/**
 * (package private) specialization corresponding to {@link Specialization#VIEWMODEL}
 * @see ManagedObject.Specialization#VIEWMODEL
 */
final class _ManagedObjectViewmodel
extends _ManagedObjectSpecified
implements
    Bookmarkable.BookmarkRefreshable {

    @Getter(onMethod_ = {@Override})
    @Nullable private final Object pojo;

    protected final _Lazy<Optional<Bookmark>> bookmarkLazy =
            _Lazy.threadSafe(()->createBookmark());

    _ManagedObjectViewmodel(
            final ObjectSpecification spec,
            final Object pojo,
            final Optional<Bookmark> bookmarkIfKnown) {
        super(ManagedObject.Specialization.VIEWMODEL, spec);
        _Assert.assertTrue(spec.isViewModel());
        this.pojo = assertCompliance(pojo);
        if(bookmarkIfKnown.isPresent()) {
            this.bookmarkLazy.set(bookmarkIfKnown);
        }
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
    public void invalidateBookmark() {
        bookmarkLazy.clear();
    }

    private Optional<Bookmark> createBookmark() {
        return Optional.ofNullable(getSpecification().viewmodelFacetElseFail().serializeToBookmark(this));
    }

}