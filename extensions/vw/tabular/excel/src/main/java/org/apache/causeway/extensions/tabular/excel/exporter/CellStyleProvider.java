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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;

import lombok.Getter;

import lombok.experimental.Accessors;

//record candidate
@Getter @Accessors(fluent=true)
class CellStyleProvider {

    public CellStyleProvider(final Workbook wb) {
        this.workbook = wb;
        this.primaryHeaderStyle = createPrimaryHeaderRowStyle(wb);
        this.secondaryHeaderStyle = createSecondaryHeaderRowStyle(wb);
        this.dateStyle = createDateFormatCellStyle(wb, "yyyy-mm-dd");
        this.multilineStyle = createMultilineCellStyle(wb);
    }

    final Workbook workbook;
    final CellStyle primaryHeaderStyle;
    final CellStyle secondaryHeaderStyle;
    final CellStyle dateStyle;
    final CellStyle multilineStyle;

    protected CellStyle createPrimaryHeaderRowStyle(final Workbook wb) {
        var font = wb.createFont();
        font.setBold(true);
        var cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(createColor(new Color(0xc0c0c0)));
        //cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        return cellStyle;
    }

    protected CellStyle createSecondaryHeaderRowStyle(final Workbook wb) {
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
        //cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        return cellStyle;
    }

    protected CellStyle createDateFormatCellStyle(final Workbook wb, final String dateFormat) {
        var cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(dateFormat));
        return cellStyle;
    }

    protected CellStyle createMultilineCellStyle(final Workbook wb) {
        var cellStyle = wb.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyle.setWrapText(true);
        return cellStyle;
    }

    static XSSFColor createColor(final Color color) {
        return new XSSFColor(color, new DefaultIndexedColorMap());
    }

}
