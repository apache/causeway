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

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Reduction;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.tabular.simple.CollectionContentsExporter;
import org.apache.causeway.core.metamodel.tabular.simple.DataTable;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
class ExcelExporter implements BiConsumer<DataTable, File> {

    /**
     * If a cell's cardinality exceeds this threshold, truncate with '... has more' label at the end.
     */
    private static final int MAX_CELL_ELEMENTS = 5;
    private static final String POI_LINE_DELIMITER = "\n";

    @RequiredArgsConstructor
    static class RowFactory {
        private final Sheet sheet;
        private int rowNum;
        public Row newRow() {
            return sheet.createRow((short) rowNum++);
        }
    }

    final InteractionInitiatedBy interactionInitiatedBy;

    static ExcelExporter of(final CollectionContentsExporter.AccessMode accessMode) {
        switch(accessMode) {
            case USER: return new ExcelExporter(InteractionInitiatedBy.USER);
            case PASS_THROUGH: return new ExcelExporter(InteractionInitiatedBy.PASS_THROUGH);
            default:
                throw new IllegalArgumentException("Unexpected value: " + accessMode);
        }
    }

    @Override @SneakyThrows
    public void accept(final DataTable table, final File tempFile) {
        try(final Workbook wb = new XSSFWorkbook()) {
            final String sheetName = table.getTableFriendlyName();

            Row row;

            var sheet = wb.createSheet(sheetName);

            var cellStyleProvider = new CellStyleProvider(wb);

            final ExcelExporter.RowFactory rowFactory = new RowFactory(sheet);

            var dataColumns = table.getDataColumns();

            // primary header row
            row = rowFactory.newRow();
            int i=0;
            for(var column : dataColumns) {
                final Cell cell = row.createCell((short) i++);
                cell.setCellValue(column.getColumnFriendlyName());
                cell.setCellStyle(cellStyleProvider.primaryHeaderStyle());
            }

            // secondary header row
            row = rowFactory.newRow();
            i=0;
            var maxLinesInRow = _Reduction.of(1, Math::max); // row auto-size calculation
            for(var column : dataColumns) {
                final Cell cell = row.createCell((short) i++);
                final String columnDescription = column.getColumnDescription().orElse("");
                cell.setCellValue(columnDescription);
                maxLinesInRow.accept((int)
                        _Strings.splitThenStream(columnDescription, "\n").count());
                cell.setCellStyle(cellStyleProvider.secondaryHeaderStyle());
            }
            autoSizeRow(row, maxLinesInRow.getResult().orElse(1),
                    wb.getFontAt(cellStyleProvider.secondaryHeaderStyle().getFontIndex()));

            var dataRows = table.getDataRows();

            // detail rows
            for (var dataRow : dataRows) {
                row = rowFactory.newRow();
                i=0;
                maxLinesInRow = _Reduction.of(1, Math::max); // row auto-size calculation
                for(var column : dataColumns) {
                    final Cell cell = row.createCell((short) i++);
                    var cellElements = dataRow.getCellElements(column, interactionInitiatedBy)
                            .filter(managedObject->managedObject.getPojo()!=null);
                    final int linesWritten = setCellValue(cellElements,
                            cell,
                            cellStyleProvider);
                    maxLinesInRow.accept(linesWritten);
                }
                autoSizeRow(row, maxLinesInRow.getResult().orElse(1), null);
            }

            // column auto-size
            autoSizeColumns(sheet, dataColumns.size());

            // freeze panes
            sheet.createFreezePane(0, 2);

            try(var fos = new FileOutputStream(tempFile)) {
                wb.write(fos);
            }
        }
    }

    protected void autoSizeRow(final Row row, final int numberOfLines, final @Nullable Font fontHint) {
        if(numberOfLines<2) return; // ignore
        final int defaultHeight = fontHint!=null
                ? fontHint.getFontHeight()
                : row.getSheet().getDefaultRowHeight();
        int height = numberOfLines * defaultHeight;
        height = Math.min(height, Short.MAX_VALUE); // upper bound to 32767 'twips' or 1/20th of a point
        row.setHeight((short) height);
    }

    protected void autoSizeColumns(final Sheet sheet, final int columnCount) {
        IntStream.range(0, columnCount)
            .forEach(sheet::autoSizeColumn);
    }

    /**
     * @return lines actually written to the cell (1 or more)
     */
    private int setCellValue(
            final Can<ManagedObject> cellElements, // pre-filtered, so contains only non-null pojos
            final Cell cell,
            final CellStyleProvider cellStyleProvider) {

        if(cellElements.isEmpty()) {
            cell.setBlank();
            return 1;
        }

        if(cellElements.isCardinalityMultiple()) {
            String joinedElementsLiteral = cellElements.stream()
                .limit(MAX_CELL_ELEMENTS)
                .map(ManagedObject::getTitle)
                .collect(Collectors.joining(POI_LINE_DELIMITER));

            // if cardinality exceeds threshold, truncate with 'has more' label at the end
            final int overflow = cellElements.size()-MAX_CELL_ELEMENTS;
            if(overflow>0) {
                joinedElementsLiteral += POI_LINE_DELIMITER + String.format("(has %d more)", overflow);
            }
            cell.setCellValue(joinedElementsLiteral);
            cell.setCellStyle(cellStyleProvider.multilineStyle());
            return overflow>0
                    ? MAX_CELL_ELEMENTS + 1
                    : cellElements.size();
        }

        var singleton = cellElements.getFirstElseFail();
        var valueAsObj = singleton.getPojo();

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

        final String objectAsStr = singleton.getTitle();
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
