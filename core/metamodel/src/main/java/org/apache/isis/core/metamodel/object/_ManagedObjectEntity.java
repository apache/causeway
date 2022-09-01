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

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;

/**
 * (package private) specialization corresponding to {@link Specialization#ENTITY}
 * @see ManagedObject.Specialization#ENTITY
 */
final class _ManagedObjectEntity
extends _ManagedObjectSpecified {

    private /*final*/ @Nullable Object pojo;
    private final @NonNull Bookmark bookmark;

    _ManagedObjectEntity(
            final ObjectSpecification spec,
            final Object pojo,
            final @NonNull Bookmark bookmark) {
        super(ManagedObject.Specialization.ENTITY, spec);
        this.pojo = assertCompliance(pojo);
        this.bookmark = bookmark;
    }

    @Override
    public Optional<Bookmark> getBookmark() {
        return Optional.of(bookmark);
    }

    @Override
    public Optional<Bookmark> getBookmarkRefreshed() {
        return getBookmark(); // no-op for entities
    }

    @Override
    public boolean isBookmarkMemoized() {
        return true;
    }

    @Override
    public void refreshViewmodel(final Supplier<Bookmark> bookmarkSupplier) {
        // no-op for entities
    }

    @Override
    public Object getPojo() {
        // TODO refetch if required
        return pojo;
    }

    // -- HELPER

//    private EntityFacet entityFacet() {
//        return getSpecification().entityFacet().orElseThrow();
//    }

}