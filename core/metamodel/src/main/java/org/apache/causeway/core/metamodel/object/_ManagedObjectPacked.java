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

import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;

/**
 * (package private) specialization corresponding to {@link Specialization#PACKED}
 * @see ManagedObject.Specialization#PACKED
 */
final class _ManagedObjectPacked
extends _ManagedObjectSpecified
implements
    Bookmarkable.NoBookmark,
    PackedManagedObject {

    private final @NonNull Can<ManagedObject> nonScalar;

    _ManagedObjectPacked(
            final ObjectSpecification elementSpec,
            final @Nullable Can<ManagedObject> nonScalar) {
        super(Specialization.PACKED, elementSpec);
        this.nonScalar = nonScalar!=null
                ? nonScalar
                : Can.empty();
    }

    @Override
    public Object getPojo() {
        // this algorithm preserves null pojos ...
        return Collections.unmodifiableList(
                nonScalar.stream()
                .map(ManagedObject::getPojo)
                .collect(Collectors.toList()));
    }

    @Override
    public Can<ManagedObject> unpack(){
        return nonScalar;
    }

}
