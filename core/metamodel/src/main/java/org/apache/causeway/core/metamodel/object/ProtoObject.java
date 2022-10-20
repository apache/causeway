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
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.NonNull;

/**
 * Pair of {@link ObjectSpecification} and {@link Bookmark}.
 */
@lombok.Value(staticConstructor = "of")
public class ProtoObject {

    private final @NonNull ObjectSpecification objectSpecification;
    private final @NonNull Bookmark bookmark;

    public static Optional<ProtoObject> resolve(
            final @NonNull SpecificationLoader specificationLoader,
            final @Nullable Bookmark bookmark) {
        if(bookmark==null) {
            return Optional.empty();
        }
        return specificationLoader
                .specForLogicalTypeName(bookmark.getLogicalTypeName())
                .map(spec->of(spec, bookmark));
    }

    public static ProtoObject resolveElseFail(
            final @NonNull SpecificationLoader specificationLoader,
            final @NonNull Bookmark bookmark) {
        return of(specificationLoader
                    .specForLogicalTypeNameElseFail(bookmark.getLogicalTypeName()),
                bookmark);
    }

}
