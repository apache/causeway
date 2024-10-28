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
package org.apache.causeway.commons.tabular;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.causeway.commons.collections.Can;

/**
 * General purpose tabular data structure,
 * that maps well onto excel files,
 * but can also be used for other tabular file formats.
 *
 * @since 3.2
 */
public record TabularModel(
        Can<TabularSheet> sheets) {

    public record TabularSheet(
            String sheetName,
            Can<TabularColumn> columns,
            Can<TabularRow> rows) {
    }

    public record TabularColumn(
            int columnIndex,
            String columnName,
            String columnDescription) {
    }

    //TODO[CAUSEWAY-3825] this is nothing more than a stub yet
    public record TabularCell(
            Can<Object> pojos,
            Supplier<Stream<String>> labelSupplier) {

        public Stream<String> labels() {
            return labelSupplier.get();
        }
    }

    public record TabularRow(
            Can<TabularCell> cells) {

        public TabularCell getCell(final TabularColumn column) {
            return getCell(column.columnIndex());
        }

        public TabularCell getCell(final int columnIndex) {
            return cells.getElseFail(columnIndex);
        }
    }

}
