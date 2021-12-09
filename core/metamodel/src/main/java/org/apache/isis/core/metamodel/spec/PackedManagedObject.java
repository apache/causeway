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
package org.apache.isis.core.metamodel.spec;

import java.util.Collections;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PackedManagedObject implements ManagedObject {

    final ObjectSpecification elementSpec;
    final Can<ManagedObject> nonScalar;

    public static ManagedObject pack(
            final ObjectSpecification elementSpec,
            final Can<ManagedObject> nonScalar) {
        return new PackedManagedObject(elementSpec, nonScalar);
    }

    @Override
    public ObjectSpecification getSpecification() {
        return elementSpec;
    }

    @Override
    public Object getPojo() {
        return Collections.unmodifiableList(
                nonScalar.stream()
                .map(ManagedObject::getPojo)
                .collect(Collectors.toList()));
    }

    @Override
    public void replacePojo(final UnaryOperator<Object> replacer) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public void replaceBookmark(final UnaryOperator<Bookmark> replacer) {
        throw _Exceptions.unsupportedOperation();
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

    public Can<ManagedObject> unpack(){
        return nonScalar;
    }

}
