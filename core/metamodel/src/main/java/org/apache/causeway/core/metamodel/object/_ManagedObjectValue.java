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

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.facets.object.value.ValueSerializer.Format;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * (package private) specialization corresponding to {@link Specialization#VALUE}
 * @see ManagedObject.Specialization#VALUE
 */
final class _ManagedObjectValue
extends _ManagedObjectSpecified {

    @Getter(onMethod_ = {@Override}) @Accessors(makeFinal = true)
    private final @NonNull Object pojo;

    protected final _Lazy<Optional<Bookmark>> bookmarkLazy =
            _Lazy.threadSafe(()->Optional.of(createBookmark()));

    _ManagedObjectValue(
            final ObjectSpecification spec,
            final Object pojo) {
        super(ManagedObject.Specialization.VALUE, spec);
        _Assert.assertTrue(spec.isValue());
        this.pojo = assertCompliance(pojo);
    }

    @Override
    public Optional<Bookmark> getBookmark() {
        return bookmarkLazy.get();
    }

    @Override
    public boolean isBookmarkMemoized() {
        return bookmarkLazy.isMemoized();
    }

    // -- HELPER

    private ValueFacet<?> valueFacet() {
        return getSpecification().valueFacetElseFail();
    }

    private Bookmark createBookmark() {
        return createBookmark(
                valueFacet().enstring(Format.URL_SAFE, _Casts.uncheckedCast(getPojo())));
    }

}