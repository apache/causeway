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
package org.apache.causeway.extensions.tabular.excel.exporter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;

import org.apache.causeway.commons.tabular.TabularModel.TabularCell;
import org.apache.causeway.commons.tabular.TabularModel.TabularColumn;

record ExcelCellWriter(
        /**
         * If a cell's cardinality exceeds this threshold, truncate with '... has more' label at the end.
         */
        int maxCellElements) {

    private static final String POI_LINE_DELIMITER = "\n";

    /**
     * @return lines actually written to the cell (1 or more)
     */
    int setCellValue(
            final TabularColumn column,
            final TabularCell tabularCell,
            final Cell cell,
            final CellStyleProvider cellStyleProvider) {

        if(tabularCell.cardinality() == 0) {
            cell.setBlank();
            return 1;
        }

        if(tabularCell.eitherValueOrLabelSupplier().isRight()) {
            String joinedElementsLiteral = tabularCell.labels()
                .limit(maxCellElements)
                .collect(Collectors.joining(POI_LINE_DELIMITER));

            // if cardinality exceeds threshold, truncate with 'has more' label at the end
            final int overflow = tabularCell.cardinality() - maxCellElements;
            if(overflow>0) {
                joinedElementsLiteral += POI_LINE_DELIMITER + String.format("(has %d more)", overflow);
            }
            cell.setCellValue(joinedElementsLiteral);
            cellStyleProvider.applyMultilineStyle(cell);
            return overflow>0
                    ? maxCellElements + 1
                    : tabularCell.cardinality();
        }

        final var valueAsObj = tabularCell.eitherValueOrLabelSupplier().leftIfAny();

        // null guard
        if(valueAsObj == null) {
            cell.setBlank();
            return 1;
        }

        // String
        if(valueAsObj instanceof CharSequence value) {
            cell.setCellValue(value.toString());
            return 1;
        }

        // boolean
        if(valueAsObj instanceof Boolean value) {
            cell.setCellValue(value);
            return 1;
        }

        // date
        if(valueAsObj instanceof Date value) {
            setCellValueForDate(cell, value, cellStyleProvider);
            return 1;
        }
        if(valueAsObj instanceof LocalDate value) {
            Date date = _TimeConversion.toDate(value);
            setCellValueForDate(cell, date, cellStyleProvider);
            return 1;
        }
        if(valueAsObj instanceof LocalDateTime value) {
            Date date = _TimeConversion.toDate(value);
            setCellValueForDate(cell, date, cellStyleProvider);
            return 1;
        }
        if(valueAsObj instanceof OffsetDateTime value) {
            Date date = _TimeConversion.toDate(value);
            setCellValueForDate(cell, date, cellStyleProvider);
            return 1;
        }

        // number
        if(valueAsObj instanceof Double value) {
            setCellValueForDouble(cell, value);
            return 1;
        }
        if(valueAsObj instanceof Float value) {
            setCellValueForDouble(cell, value);
            return 1;
        }
        if(valueAsObj instanceof BigDecimal value) {
            setCellValueForDouble(cell, value.doubleValue());
            return 1;
        }
        if(valueAsObj instanceof BigInteger value) {
            setCellValueForDouble(cell, value.doubleValue());
            return 1;
        }
        if(valueAsObj instanceof Long value) {
            setCellValueForDouble(cell, value);
            return 1;
        }
        if(valueAsObj instanceof Integer value) {
            setCellValueForDouble(cell, value);
            return 1;
        }
        if(valueAsObj instanceof Short value) {
            setCellValueForDouble(cell, value);
            return 1;
        }
        if(valueAsObj instanceof Byte value) {
            setCellValueForDouble(cell, value);
            return 1;
        }

        // if all else fails fallback to value's toString method
        final String objectAsStr = tabularCell.labels().findFirst().orElseGet(valueAsObj::toString);
        cell.setCellValue(objectAsStr);
        return 1;
    }

    private static void setCellValueForDouble(final Cell cell, final double value) {
        cell.setCellValue(value);
    }

    private static void setCellValueForDate(final Cell cell, final Date date, final CellStyleProvider cellStyleProvider) {
        cell.setCellValue(date);
        cellStyleProvider.applyDateStyle(cell);
    }

}
