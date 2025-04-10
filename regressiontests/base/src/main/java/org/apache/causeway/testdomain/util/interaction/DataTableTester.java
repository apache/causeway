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
package org.apache.causeway.testdomain.util.interaction;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.binding.Observable;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.tabular.DataColumn;
import org.apache.causeway.core.metamodel.tabular.DataRow;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class DataTableTester {

    @Getter final @NonNull DataTableInteractive dataTable;

    public void assertUnfilteredDataElements(final List<Object> expectedPojoElements) {
        assertEquals(expectedPojoElements,
                dataTable.dataElementsObservable().getValue()
                .map(MmUnwrapUtils::single).toList());
    }

    public void assertFilteredDataElements(final List<Object> expectedPojoElements) {
        assertEquals(expectedPojoElements,
                dataTable.dataRowsFilteredAndSortedObservable().getValue()
                .map(DataRow::rowElement)
                .map(MmUnwrapUtils::single).toList());
    }

    public void assertSelectedDataElements(final List<Object> expectedPojoElements) {
        assertEquals(expectedPojoElements,
                dataTable.dataRowsSelectedObservable().getValue()
                .map(DataRow::rowElement)
                .map(MmUnwrapUtils::single).toList());
    }

    public void assertDataRowSelectionWhenToggledOn(
            final List<Integer> toggleOnIndices,
            final List<Object> expectedPojoElements) {
        toggleOnIndices.forEach(index->
            dataTable.dataRowsFilteredAndSortedObservable().getValue().getElseFail(index).selectToggleBindable().setValue(true));
        assertSelectedDataElements(expectedPojoElements);
    }

    public void assertDataRowSelectionWhenToggledOff(
            final List<Integer> toggleOffIndices,
            final List<Object> expectedPojoElements) {
        toggleOffIndices.forEach(index->
            dataTable.dataRowsFilteredAndSortedObservable().getValue().getElseFail(index).selectToggleBindable().setValue(false));
        assertSelectedDataElements(expectedPojoElements);
    }

    public void assertDataRowSelectionIsAll() {
        assertEquals(
                dataTable.dataRowsFilteredAndSortedObservable().getValue()
                .map(DataRow::rowElement)
                .map(MmUnwrapUtils::single).toList(),
                dataTable.dataRowsSelectedObservable().getValue()
                .map(DataRow::rowElement)
                .map(MmUnwrapUtils::single).toList());
    }

    public void assertDataRowSelectionIsEmpty() {
        assertEquals(0, dataTable.dataRowsSelectedObservable().getValue().size());
    }

    public void assertColumnNames(final List<String> expectedColumnNames) {
        assertEquals(expectedColumnNames,
                dataTable.dataColumnsObservable().getValue().stream()
                .map(DataColumn::columnFriendlyNameObservable)
                .map(Observable::getValue)
                .collect(Collectors.toList()));
    }

}
