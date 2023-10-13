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
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;

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
    @Getter private final @NonNull Comparator<DataColumn> columnOrder;
    @Getter private @NonNull Can<DataRow> dataRows;

    public DataTable(
            final @NonNull ObjectSpecification elementType,
            final @NonNull Comparator<DataColumn> columnOrder) {
        this(elementType, elementType.getSingularName(), columnOrder, Can.empty());
    }

    public DataTable(
            final @NonNull ObjectSpecification elementType,
            final @Nullable String tableFriendlyName,
            final @NonNull Comparator<DataColumn> columnOrder,
            final @NonNull Can<ManagedObject> dataElements) {

        this.tableFriendlyName = _Strings.nonEmpty(tableFriendlyName)
                .orElse("Collection"); // fallback to a generic name

        this.elementType = elementType;
        this.columnOrder = columnOrder;

        //TODO externalize filtering
        this.dataColumns = elementType
                .streamProperties(MixedIn.EXCLUDED)
                .filter(prop->prop.isIncludedWithSnapshots())
                .map(property->new DataColumn(this, property))
                //.sorted() // don't sort, use meta-model's order, to preserve order from schema
                .collect(Can.toCan());

        setDataElements(dataElements);
    }

    // -- POPULATE

    public DataTable withDataElements(final Can<ManagedObject> dataElements) {
        return new DataTable(elementType, tableFriendlyName, columnOrder, dataElements);
    }

    //XXX perhaps remove this mutability
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
