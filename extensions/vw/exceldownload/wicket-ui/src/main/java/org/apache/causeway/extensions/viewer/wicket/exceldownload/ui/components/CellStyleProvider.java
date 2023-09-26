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
package org.apache.causeway.extensions.viewer.wicket.exceldownload.ui.components;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

import lombok.Getter;
import lombok.val;
import lombok.experimental.Accessors;

//record candidate
@Getter @Accessors(fluent=true)
class CellStyleProvider {

    public CellStyleProvider(final Workbook wb) {
        this.workbook = wb;
        this.headerStyle = createHeaderRowStyle(wb);
        this.dateStyle = createDateFormatCellStyle(wb, "yyyy-mm-dd");
        this.multilineStyle = createMultilineCellStyle(wb);
    }

    final Workbook workbook;
    final CellStyle headerStyle;
    final CellStyle dateStyle;
    final CellStyle multilineStyle;

    protected CellStyle createHeaderRowStyle(final Workbook wb) {
        val font = wb.createFont();
        font.setBold(true);
        val cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        return cellStyle;
    }

    protected CellStyle createDateFormatCellStyle(final Workbook wb, final String dateFormat) {
        val cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(dateFormat));
        return cellStyle;
    }

    protected CellStyle createMultilineCellStyle(final Workbook wb) {
        val cellStyle = wb.createCellStyle();
        cellStyle.setWrapText(true);
        return cellStyle;
    }

}
