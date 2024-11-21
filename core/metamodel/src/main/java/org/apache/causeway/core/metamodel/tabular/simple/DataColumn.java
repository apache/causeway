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
package org.apache.causeway.core.metamodel.tabular.simple;

import java.util.Comparator;
import java.util.Optional;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

import lombok.NonNull;

/**
 * Represents a single association of a domain object type (typically an entity type).
 *
 * @since 2.0 {@index}
 */
public record DataColumn(
        @NonNull ObjectAssociation metamodel,
        @NonNull String columnId,
        @NonNull String columnFriendlyName,
        @NonNull Optional<String> columnDescription,
        boolean isMultivalued
    ) {

    public DataColumn(final ObjectAssociation metamodel) {
        this(metamodel, metamodel.getId(),
            metamodel.getCanonicalFriendlyName(),
            metamodel.getCanonicalDescription(),
            metamodel.isCollection());
    }

    // -- UTILITY

    /**
     * Alphabetical column order using column's underlying (member) id.
     */
    public static Comparator<DataColumn> orderByColumnId() {
        return (o1, o2) -> _Strings.compareNullsFirst(
                o1.columnId(),
                o2.columnId());
    }

}
