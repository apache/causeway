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

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;

@Getter
final class _ManagedObjectEmpty
extends _ManagedObjectSpecified {

    _ManagedObjectEmpty(
            final ObjectSpecification spec) {
        super(ManagedObject.Specialization.EMPTY, spec);
    }

    @Override
    public Object getPojo() {
        return null;
    }

    @Override
    public Optional<Bookmark> getBookmark() {
        return Optional.empty();
    }

    @Override
    public Optional<Bookmark> getBookmarkRefreshed() {
        return Optional.empty();
    }

    @Override
    public void refreshViewmodel(final Supplier<Bookmark> bookmarkSupplier) {
    }

    @Override
    public boolean isBookmarkMemoized() {
        return false;
    }

}