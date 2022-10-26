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
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataColumn;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class DataTableTester {

    @Getter final @NonNull DataTableModel dataTable;

    public void assertUnfilteredDataElements(final List<Object> expectedPojoElements) {
        assertEquals(expectedPojoElements,
                dataTable.getDataElements().getValue()
                .map(MmUnwrapUtil::single).toList());
    }

    public void assertFilteredDataElements(final List<Object> expectedPojoElements) {
        assertEquals(expectedPojoElements,
                dataTable.getDataRowsFiltered().getValue()
                .map(DataRow::getRowElement)
                .map(MmUnwrapUtil::single).toList());
    }

    public void assertSelectedDataElements(final List<Object> expectedPojoElements) {
        assertEquals(expectedPojoElements,
                dataTable.getDataRowsSelected().getValue()
                .map(DataRow::getRowElement)
                .map(MmUnwrapUtil::single).toList());
    }

    public void assertDataRowSelectionWhenToggledOn(
            final List<Integer> toggleOnIndices,
            final List<Object> expectedPojoElements) {
        toggleOnIndices.forEach(index->
            dataTable.getDataRowsFiltered().getValue().getElseFail(index).getSelectToggle().setValue(true));
        assertSelectedDataElements(expectedPojoElements);
    }

    public void assertDataRowSelectionWhenToggledOff(
            final List<Integer> toggleOffIndices,
            final List<Object> expectedPojoElements) {
        toggleOffIndices.forEach(index->
            dataTable.getDataRowsFiltered().getValue().getElseFail(index).getSelectToggle().setValue(false));
        assertSelectedDataElements(expectedPojoElements);
    }

    public void assertDataRowSelectionIsAll() {
        assertEquals(
                dataTable.getDataRowsFiltered().getValue()
                .map(DataRow::getRowElement)
                .map(MmUnwrapUtil::single).toList(),
                dataTable.getDataRowsSelected().getValue()
                .map(DataRow::getRowElement)
                .map(MmUnwrapUtil::single).toList());
    }

    public void assertDataRowSelectionIsEmpty() {
        assertEquals(0, dataTable.getDataRowsSelected().getValue().size());
    }

    public void assertColumnNames(final List<String> expectedColumnNames) {
        assertEquals(expectedColumnNames,
                dataTable.getDataColumns().getValue().stream()
                .map(DataColumn::getColumnFriendlyName)
                .map(Observable::getValue)
                .collect(Collectors.toList()));
    }

}
