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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract non-sealed class _ManagedObjectEntityAbstract
implements ManagedObject {

    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true, makeFinal = true)
    private final @NonNull Specialization specialization;

    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true, makeFinal = true)
    private final @NonNull ObjectSpecification objSpec;

    final <T> T assertCompliance(final @NonNull T pojo) {
        return _Compliance.assertCompliance(objSpec, specialization, pojo);
    }

    @Override
    public String getTitle() {
        return _InternalTitleUtil.titleString(
                TitleRenderRequest.forObject(this));
    }

    @Override
    public Optional<ObjectMemento> getMemento() {
        return Optional.ofNullable(mementoForScalar(this));
    }

    private ObjectMemento mementoForScalar(final @Nullable ManagedObject adapter) {
        MmAssertionUtils.assertPojoIsScalar(adapter);
        return ObjectMemento.singular(adapter)
                .orElseGet(()->
                    ManagedObjects.isSpecified(adapter)
                        ? ObjectMemento.empty(adapter.logicalType())
                        : null);
    }

    @Override
    public final boolean equals(final Object obj) {
        return _Compliance.equals(this, obj);
    }

    @Override
    public final int hashCode() {
        return _Compliance.hashCode(this);
    }

    @Override
    public final String toString() {
        return _Compliance.toString(this);
    }

}
