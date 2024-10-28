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
            final TabularCell cellValue,
            final Cell cell,
            final CellStyleProvider cellStyleProvider) {

        final var cellElements = cellValue.pojos();

        if(cellElements.isEmpty()) {
            cell.setBlank();
            return 1;
        }

        if(cellElements.isCardinalityMultiple()) {
            String joinedElementsLiteral = cellValue.labels()
                .limit(maxCellElements)
                .collect(Collectors.joining(POI_LINE_DELIMITER));

            // if cardinality exceeds threshold, truncate with 'has more' label at the end
            final int overflow = cellElements.size() - maxCellElements;
            if(overflow>0) {
                joinedElementsLiteral += POI_LINE_DELIMITER + String.format("(has %d more)", overflow);
            }
            cell.setCellValue(joinedElementsLiteral);
            cell.setCellStyle(cellStyleProvider.multilineStyle());
            return overflow>0
                    ? maxCellElements + 1
                    : cellElements.size();
        }

        var valueAsObj = cellElements.getFirstElseFail();

        // event though filtered for null by caller, keep this as a guard
        // null
        if(valueAsObj == null) {
            cell.setBlank();
            return 1;
        }

        // boolean
        if(valueAsObj instanceof Boolean) {
            boolean value = (Boolean) valueAsObj;
            cell.setCellValue(value);
            return 1;
        }

        // date
        if(valueAsObj instanceof Date) {
            Date value = (Date) valueAsObj;
            setCellValueForDate(cell, value, cellStyleProvider);
            return 1;
        }
        if(valueAsObj instanceof LocalDate) {
            LocalDate value = (LocalDate) valueAsObj;
            Date date = _TimeConversion.toDate(value);
            setCellValueForDate(cell, date, cellStyleProvider);
            return 1;
        }
        if(valueAsObj instanceof LocalDateTime) {
            LocalDateTime value = (LocalDateTime) valueAsObj;
            Date date = _TimeConversion.toDate(value);
            setCellValueForDate(cell, date, cellStyleProvider);
            return 1;
        }
        if(valueAsObj instanceof OffsetDateTime) {
            OffsetDateTime value = (OffsetDateTime) valueAsObj;
            Date date = _TimeConversion.toDate(value);
            setCellValueForDate(cell, date, cellStyleProvider);
            return 1;
        }

        // number
        if(valueAsObj instanceof Double) {
            Double value = (Double) valueAsObj;
            setCellValueForDouble(cell, value);
            return 1;
        }
        if(valueAsObj instanceof Float) {
            Float value = (Float) valueAsObj;
            setCellValueForDouble(cell, value);
            return 1;
        }
        if(valueAsObj instanceof BigDecimal) {
            BigDecimal value = (BigDecimal) valueAsObj;
            setCellValueForDouble(cell, value.doubleValue());
            return 1;
        }
        if(valueAsObj instanceof BigInteger) {
            BigInteger value = (BigInteger) valueAsObj;
            setCellValueForDouble(cell, value.doubleValue());
            return 1;
        }
        if(valueAsObj instanceof Long) {
            Long value = (Long) valueAsObj;
            setCellValueForDouble(cell, value);
            return 1;
        }
        if(valueAsObj instanceof Integer) {
            Integer value = (Integer) valueAsObj;
            setCellValueForDouble(cell, value);
            return 1;
        }
        if(valueAsObj instanceof Short) {
            Short value = (Short) valueAsObj;
            setCellValueForDouble(cell, value);
            return 1;
        }
        if(valueAsObj instanceof Byte) {
            Byte value = (Byte) valueAsObj;
            setCellValueForDouble(cell, value);
            return 1;
        }

        final String objectAsStr = cellValue.labels().findFirst().orElseGet(valueAsObj::toString);
        cell.setCellValue(objectAsStr);
        return 1;
    }

    private static void setCellValueForDouble(final Cell cell, final double value) {
        cell.setCellValue(value);
    }

    private static void setCellValueForDate(final Cell cell, final Date date, final CellStyleProvider cellStyleProvider) {
        cell.setCellValue(date);
        cell.setCellStyle(cellStyleProvider.dateStyle());
    }

}
