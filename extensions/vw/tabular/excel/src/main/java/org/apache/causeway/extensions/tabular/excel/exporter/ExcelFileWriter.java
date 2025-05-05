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
import java.nio.file.Files;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal.base._Reduction;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.commons.tabular.TabularModel;
import org.apache.causeway.commons.tabular.TabularModel.TabularCell;
import org.apache.causeway.commons.tabular.TabularModel.TabularRow;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Utility to write a {@link TabularModel} to file.
 */
public record ExcelFileWriter(@Nullable Options options) {

    @Builder
    public record Options(
            /**
             * Custom style for individual cell based on cell value. Overrules rowStyleFunction if provided.
             * <p>
             * {@link Function} may return {@code null}.
             */
            @Nullable Function<TabularCell, CustomCellStyle> cellStyleFunction,
            /**
             * Custom style for entire row based on row data. Overruled by cellStyleFunction if provided.
             * {@link Function} may return {@code null}.<br>
             */
            @Nullable Function<TabularRow, CustomCellStyle> rowStyleFunction) {

        public enum CustomCellStyle {
            DEFAULT,
            BLUE,
            GREEN,
            INDIGO,
            WARNING,
            DANGER;
            public boolean isDefault() { return this == CustomCellStyle.DEFAULT; }
            static CustomCellStyle nullToDefault(final @Nullable CustomCellStyle customCellStyle) {
                return customCellStyle!=null
                        ? customCellStyle
                        : DEFAULT;
            }
        }

    }

    @SneakyThrows
    public void write(final TabularModel tabular, final File tempFile) {
        try(final Workbook wb = new SXSSFWorkbook()) {
            tabular.sheets().forEach(sheet->writeSheet(wb, sheet));
            try(var fos = new FileOutputStream(tempFile)) {
                wb.write(fos);
            }
        }
    }

    /**
     * Writes given tabular data to a {@link Blob}, using given name as blob name.
     */
    @SneakyThrows
    public Blob writeToBlob(final String name, final TabularModel tabular) {
        var tempFile = File.createTempFile(this.getClass().getCanonicalName(), name);
        try {
            write(tabular, tempFile);
            return Blob.of(name, CommonMimeType.XLSX, DataSource.ofFile(tempFile).bytes());
        } finally {
            Files.deleteIfExists(tempFile.toPath()); // cleanup
        }
    }

    // -- HELPER

    @RequiredArgsConstructor
    private static class RowFactory {
        private final Sheet sheet;
        private int rowNum;
        public Row newRow() {
            return sheet.createRow(rowNum++);
        }
    }

    private void writeSheet(final Workbook wb, final TabularModel.TabularSheet tabularSheet) {

        var sheetName = tabularSheet.sheetName();

        Row row;
        
        var sheet = wb.createSheet(sheetName);
        if(sheet instanceof SXSSFSheet sxssfSheet) {
            sxssfSheet.trackAllColumnsForAutoSizing();
        }
        var cellWriter = new ExcelCellWriter(5, new ExcelImageHandler(sheet));
        
        var cellStyleProvider = CellStyleProvider.create(wb, options);
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
                final TabularCell tabularCell = dataRow.getCell(column);
                final int linesWritten = cellWriter.setCellValue(
                    column,
                    tabularCell,
                    cell,
                    cellStyleProvider);
                maxLinesInRow.accept(linesWritten);
            }
            cellStyleProvider.applyCustomStyle(dataRow, row);
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
