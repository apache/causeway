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
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Reduction;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.tabular.TabularModel;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

public record ExcelFileWriter() {

    @SneakyThrows
    void write(final TabularModel tabular, final File tempFile) {
        try(final Workbook wb = new XSSFWorkbook()) {
            tabular.sheets().forEach(sheet->writeSheet(wb, sheet));
            try(var fos = new FileOutputStream(tempFile)) {
                wb.write(fos);
            }
        }
    }

    // -- HELPER

    @RequiredArgsConstructor
    private static class RowFactory {
        private final Sheet sheet;
        private int rowNum;
        public Row newRow() {
            return sheet.createRow((short) rowNum++);
        }
    }

    private void writeSheet(final Workbook wb, final TabularModel.TabularSheet tabularSheet) {

            var sheetName = tabularSheet.sheetName();

            Row row;

            var cellWriter = new ExcelCellWriter(5);
            var sheet = wb.createSheet(sheetName);
            var cellStyleProvider = new CellStyleProvider(wb);
            var rowFactory = new RowFactory(sheet);

            var dataColumns = tabularSheet.columns();

            // primary header row
            row = rowFactory.newRow();
            int i=0;
            for(var column : dataColumns) {
                final Cell cell = row.createCell((short) i++);
                cell.setCellValue(column.columnName());
                cell.setCellStyle(cellStyleProvider.primaryHeaderStyle());
            }

            // secondary header row
            row = rowFactory.newRow();
            i=0;
            var maxLinesInRow = _Reduction.of(1, Math::max); // row auto-size calculation
            for(var column : dataColumns) {
                final Cell cell = row.createCell((short) i++);
                final String columnDescription = column.columnDescription();
                cell.setCellValue(columnDescription);
                maxLinesInRow.accept((int)
                        _Strings.splitThenStream(columnDescription, "\n").count());
                cell.setCellStyle(cellStyleProvider.secondaryHeaderStyle());
            }
            autoSizeRow(row, maxLinesInRow.getResult().orElse(1),
                    wb.getFontAt(cellStyleProvider.secondaryHeaderStyle().getFontIndex()));

            var dataRows = tabularSheet.rows();

            // detail rows
            for (var dataRow : dataRows) {
                row = rowFactory.newRow();
                i=0;
                maxLinesInRow = _Reduction.of(1, Math::max); // row auto-size calculation
                for(var column : dataColumns) {
                    final Cell cell = row.createCell((short) i++);
                    final int linesWritten = cellWriter.setCellValue(
                            column,
                            dataRow.getCell(column),
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
    }

    private void autoSizeRow(final Row row, final int numberOfLines, final @Nullable Font fontHint) {
        if(numberOfLines<2) return; // ignore
        final int defaultHeight = fontHint!=null
                ? fontHint.getFontHeight()
                : row.getSheet().getDefaultRowHeight();
        int height = numberOfLines * defaultHeight;
        height = Math.min(height, Short.MAX_VALUE); // upper bound to 32767 'twips' or 1/20th of a point
        row.setHeight((short) height);
    }

    private void autoSizeColumns(final Sheet sheet, final int columnCount) {
        IntStream.range(0, columnCount)
            .forEach(sheet::autoSizeColumn);
    }

}
