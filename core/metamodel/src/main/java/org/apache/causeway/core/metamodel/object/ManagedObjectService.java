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

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.object.ManagedObject.Specialization;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import org.jspecify.annotations.NonNull;

/**
 * (package private) specialization corresponding to {@link Specialization#SERVICE}
 * @see ManagedObject.Specialization#SERVICE
 */
record ManagedObjectService(
    @NonNull ObjectSpecification objSpec,
    @NonNull Object pojo,
    @NonNull Bookmark bookmark)
implements ManagedObject {

    ManagedObjectService(
        final ObjectSpecification objSpec,
        final Object pojo) {
        this(objSpec, pojo, Bookmark.forLogicalTypeAndIdentifier(objSpec.logicalType(), "1"));
        assertInjectable(objSpec);
        assertCompliance(pojo);
    }

    @Override
    public Optional<Bookmark> getBookmark() {
        return Optional.of(bookmark);
    }

    @Override
    public boolean isBookmarkMemoized() {
        return true;
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
        return ManagedObject.Specialization.SERVICE;
    }

    @Override
    public Object getPojo() {
        return pojo;
    }

    @Override
    public final boolean equals(final Object obj) {
        return obj instanceof ManagedObjectService other
            ? Objects.equals(bookmark, other.bookmark)
            : false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(bookmark);
    }

    @Override
    public final String toString() {
        return "ManagedObjectService[%s]".formatted(objSpec.logicalTypeName());
    }

    // -- HELPER

    private void assertInjectable(final ObjectSpecification spec) {
        _Assert.assertTrue(spec.isInjectable(),
                ()->"type %s must be injectable to be considered a service; bean-sort: %s"
                        .formatted(pojo.getClass(), spec.getBeanSort()));
    }

    @Override
    public <T> T assertCompliance(@NonNull final T pojo) {
        return _Compliance.assertCompliance(objSpec, specialization(), pojo);
    }

}