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

import java.awt.Color;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.tabular.TabularModel.TabularRow;
import org.apache.causeway.extensions.tabular.excel.exporter.ExcelFileWriter.Options.CustomCellStyle;

import lombok.NonNull;

record CellStyleProvider(
        @NonNull Workbook workbook,
        @NonNull CellStyle primaryHeaderStyle,
        @NonNull CellStyle secondaryHeaderStyle,
        @NonNull CellStyle dateStyle,
        @NonNull CellStyle multilineStyle,
        @NonNull CellStyle[] blueStyles,
        @NonNull CellStyle[] greenStyles,
        @NonNull CellStyle[] indigoStyles,
        @NonNull CellStyle[] warningStyles,
        @NonNull CellStyle[] dangerStyles,
        @NonNull ExcelFileWriter.Options options) {

    static CellStyleProvider create(final Workbook wb, @Nullable final ExcelFileWriter.Options options) {
        var styleIndexList = new StyleIndexList();
        return new CellStyleProvider(wb,
            createPrimaryHeaderRowStyle(wb),
            createSecondaryHeaderRowStyle(wb),
            createDateFormatCellStyle(wb, styleIndexList, "yyyy-mm-dd"),
            createMultilineCellStyle(wb, styleIndexList),
            createColoredStyles(wb, styleIndexList, new Color(0x6ea8fe)), // blue-300 (6ea8fe)
            createColoredStyles(wb, styleIndexList, new Color(0x75b798)), // green-300 (75b798)
            createColoredStyles(wb, styleIndexList, new Color(0xa370f7)), // indigo-300 (a370f7)
            createColoredStyles(wb, styleIndexList, new Color(0xffcd39)), // warning yellow-400 (ffcd39)
            createColoredStyles(wb, styleIndexList, new Color(0xe35d6a)), // danger red-400 (e35d6a)
            options!=null
                ? options
                : ExcelFileWriter.Options.builder().build());
    }

    /**
     * Applies a custom cell style to given {@link Cell}
     * based on whether any of the cellStyleFunctions is available
     * and if so, whether any of the cellStyleFunctions returns a non-null CellStyle.
     * @implNote we are doing this distinction of cases for performance reasons,
     *      that is, minimize the number of style function calls;
     *      otherwise code could be simplified
     */
    void applyCustomStyle(final TabularRow tabularRow, final Iterable<Cell> cells) {
        switch (StylePolicy.get(options)) {
            case NONE: return;
            case ROW_ONLY: {
                var customRowStyle = CustomCellStyle.nullToDefault(
                        options.rowStyleFunction().apply(tabularRow));
                if(customRowStyle.isDefault()) return;
                for(var cell : cells) {
                    applyCustomCellStyle(cell, customRowStyle);
                }
                return;
            }
            case CELL_ONLY: {
                int cellIndex = 0;
                for(var cell : cells) {
                    var customCellStyle = CustomCellStyle.nullToDefault(
                            options.cellStyleFunction().apply(tabularRow.getCell(cellIndex++)));
                    applyCustomCellStyle(cell, customCellStyle);
                }
                return;
            }
            case CELL_AND_ROW: {
                CustomCellStyle customRowStyle = null; // only calculate if needed
                int cellIndex = 0;
                for(var cell : cells) {
                    var customCellStyle = CustomCellStyle.nullToDefault(
                            options.cellStyleFunction().apply(tabularRow.getCell(cellIndex++)));
                    if(!customCellStyle.isDefault()) {
                        applyCustomCellStyle(cell, customCellStyle);
                    } else {
                        if(customRowStyle==null) {
                            customRowStyle = CustomCellStyle.nullToDefault(
                                    options.rowStyleFunction().apply(tabularRow));
                        }
                        applyCustomCellStyle(cell, customRowStyle);
                    }
                }
                return;
            }
        }
    }

    Cell applyDateStyle(final Cell cell) {
        cell.setCellStyle(dateStyle());
        return cell;
    }

    Cell applyMultilineStyle(final Cell cell) {
        cell.setCellStyle(multilineStyle());
        return cell;
    }

    private void applyCustomCellStyle(final Cell cell, final CustomCellStyle customCellStyle) {
        if(customCellStyle==null
                || customCellStyle.isDefault()) return;

        final int ordinal = valueKindOf(cell).ordinal();

        if(ordinal>0)
            System.err.printf("%d%n", ordinal);

        cell.setCellStyle(
            switch (customCellStyle) {
                case BLUE -> blueStyles()[ordinal];
                case GREEN -> greenStyles()[ordinal];
                case INDIGO -> indigoStyles()[ordinal];
                case WARNING -> warningStyles()[ordinal];
                case DANGER -> dangerStyles()[ordinal];
                case DEFAULT -> throw new UnsupportedOperationException("unexpected code reach");
            });
    }

    private enum ValueKind {
        DEFAULT,
        DATE,
        MULTILINE;
    }
    private ValueKind valueKindOf(final Cell cell) {
        if(cell.getCellStyle().getIndex() == dateStyle.getIndex()) return ValueKind.DATE;
        if(cell.getCellStyle().getIndex() == multilineStyle.getIndex()) return ValueKind.MULTILINE;
        return ValueKind.DEFAULT;
    }

    /**
     * Creates a colored variant for each {@link ValueKind}.
     * @param styleIndexList
     */
    private static CellStyle[] createColoredStyles(final Workbook wb, final StyleIndexList styleIndexList, final Color color) {
        var valueKinds = ValueKind.values();
        var cellStyles = new CellStyle[valueKinds.length];
        for(var valueKind : valueKinds) {

            var cellStyle = cellStyles[valueKind.ordinal()] = wb.createCellStyle();

            switch (valueKind) {
                case DEFAULT -> {}
                case DATE -> cellStyle.cloneStyleFrom(wb.getCellStyleAt(styleIndexList.dateStyleIndex));
                case MULTILINE -> cellStyle.cloneStyleFrom(wb.getCellStyleAt(styleIndexList.multilineStyleIndex));
            }

            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle.setFillForegroundColor(createColor(color));
        }
        return cellStyles;
    }

    private static CellStyle createPrimaryHeaderRowStyle(final Workbook wb) {
        var font = wb.createFont();
        font.setBold(true);
        var cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(createColor(new Color(0xc0c0c0)));
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        return cellStyle;
    }

    private static CellStyle createSecondaryHeaderRowStyle(final Workbook wb) {
        var font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        var cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(createColor(new Color(0xeeeeee)));
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.NONE);
        cellStyle.setBorderBottom(BorderStyle.MEDIUM);
        return cellStyle;
    }

    private static CellStyle createDateFormatCellStyle(final Workbook wb, final StyleIndexList styleIndexList, final String dateFormat) {
        var cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(dateFormat));
        styleIndexList.dateStyleIndex = cellStyle.getIndex();
        return cellStyle;
    }

    private static CellStyle createMultilineCellStyle(final Workbook wb, final StyleIndexList styleIndexList) {
        var cellStyle = wb.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyle.setWrapText(true);
        styleIndexList.multilineStyleIndex = cellStyle.getIndex();
        return cellStyle;
    }

    private static XSSFColor createColor(final Color color) {
        return new XSSFColor(color, new DefaultIndexedColorMap());
    }

    private static class StyleIndexList {
        short dateStyleIndex = -1;
        short multilineStyleIndex = -1;
    }

    private enum StylePolicy {
        NONE,
        CELL_ONLY,
        ROW_ONLY,
        CELL_AND_ROW;
        static StylePolicy get(final ExcelFileWriter.Options options) {
            return options.rowStyleFunction()!=null
                    ? options.cellStyleFunction()!=null
                        ? StylePolicy.CELL_AND_ROW
                        : StylePolicy.ROW_ONLY
                    : options.cellStyleFunction()!=null
                        ? StylePolicy.CELL_ONLY
                        : StylePolicy.NONE;
        }
    }

}
