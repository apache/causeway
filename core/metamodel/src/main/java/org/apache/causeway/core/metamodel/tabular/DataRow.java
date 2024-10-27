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
package org.apache.causeway.core.metamodel.tabular;

import java.util.Optional;

import org.apache.causeway.applib.services.filter.CollectionFilterService.Tokens;
import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.NonNull;

public interface DataRow {

    /**
     * The underlying domain object or value object this {@link DataRow} is associated with.
     */
    ManagedObject getRowElement();

    /**
     * If the table supports row selection, represents the bindable row selection status for this {@link DataRow}.
     */
    Bindable<Boolean> getSelectToggle();

    /**
     * Absolute zero-based index of this row, invariant with respect to sorting.
     */
    int getRowIndex();

    /**
     * Table model this {@link DataRow} is a part of.
     */
    DataTableInteractive getParentTable();

    /**
     * Lookup the {@link DataColumn} given its id.
     */
    Optional<DataColumn> lookupColumnById(@NonNull String columnId);

    /**
     * Can be none, one or many per table cell.
     */
    Can<ManagedObject> getCellElementsForColumn(@NonNull DataColumn column);

    /**
     * Can be none, one or many per table cell. (returns empty Can if column not found)
     */
    Can<ManagedObject> getCellElementsForColumn(@NonNull String columnId);
    Optional<Tokens> getFilterTokens();

}
