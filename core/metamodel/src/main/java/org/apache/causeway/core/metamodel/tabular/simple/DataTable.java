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

import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

import lombok.Getter;
import lombok.NonNull;

/**
 * Represents a collection of domain objects (typically entity instances).
 *
 * @since 2.0 {@index}
 */
public class DataTable {

    // -- CONSTRUCTION

    @Getter private final @NonNull String tableFriendlyName;
    @Getter private final @NonNull ObjectSpecification elementType;
    @Getter private final @NonNull Can<DataColumn> dataColumns;
    @Getter private @NonNull Can<DataRow> dataRows;

    public DataTable(
            final @NonNull ObjectSpecification elementType,
            final @NonNull Can<? extends ObjectAssociation> dataColumns) {
        this(elementType, elementType.getSingularName(), dataColumns, Can.empty());
    }

    public DataTable(
            final @NonNull ObjectSpecification elementType,
            final @Nullable String tableFriendlyName,
            final @NonNull Can<? extends ObjectAssociation> dataColumns,
            final @NonNull Can<ManagedObject> dataElements) {

        this.tableFriendlyName = _Strings.nonEmpty(tableFriendlyName)
                .orElse("Collection"); // fallback to a generic name

        this.elementType = elementType;

        this.dataColumns = dataColumns
                .map(assoc->new DataColumn(this, assoc));

        setDataElements(dataElements);
    }

    // -- POPULATE

    public void setDataElements(final Can<ManagedObject> dataElements) {
        this.dataRows = dataElements
                .map(domainObject->new DataRow(this, domainObject));
    }

    /**
     * Unique within application scope, can act as an id.
     */
    public String getLogicalName() {
        return getElementType().getLogicalTypeName();
    }

    /**
     * Count data rows.
     */
    public int getElementCount() {
        return dataRows.size();
    }

    public Stream<ManagedObject> streamDataElements() {
        return dataRows.stream()
            .map(DataRow::getRowElement);
    }

}
