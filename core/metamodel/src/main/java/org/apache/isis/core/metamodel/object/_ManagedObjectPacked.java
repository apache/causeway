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

import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * (package private) specialization corresponding to {@link Specialization#PACKED}
 * @see ManagedObject.Specialization#PACKED
 */
@RequiredArgsConstructor
@ToString
final class _ManagedObjectPacked
extends _ManagedObjectSpecified
implements PackedManagedObject {

    final @NonNull ObjectSpecification elementSpec;
    final @NonNull Can<ManagedObject> nonScalar;

    @Override
    public Specialization getSpecialization() {
        return Specialization.PACKED;
    }

    @Override
    public ObjectSpecification getSpecification() {
        return elementSpec;
    }

    @Override
    public Object getPojo() {
        // this algorithm preserves null pojos ...
        return Collections.unmodifiableList(
                nonScalar.stream()
                .map(ManagedObject::getPojo)
                .collect(Collectors.toList()));
    }

    private final _Lazy<Optional<Bookmark>> bookmarkLazy =
            _Lazy.threadSafe(()->{
                return Optional.of(getSpecification().getMetaModelContext().getObjectManager().bookmarkObject(this));
            });

    @Override
    public Optional<Bookmark> getBookmark() {
        return bookmarkLazy.get();
    }

    @Override
    public Optional<Bookmark> getBookmarkRefreshed() {
        return getBookmark(); // no-effect
    }

    @Override
    public boolean isBookmarkMemoized() {
        return bookmarkLazy.isMemoized();
    }

    @Override
    public Can<ManagedObject> unpack(){
        return nonScalar;
    }

    @Override
    public void refreshViewmodel(final @Nullable Supplier<Bookmark> bookmarkSupplier) {
    }

}
