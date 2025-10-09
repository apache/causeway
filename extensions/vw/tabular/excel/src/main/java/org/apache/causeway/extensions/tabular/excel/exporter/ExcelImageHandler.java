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

import java.awt.image.BufferedImage;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFDrawing;
import org.apache.poi.xssf.streaming.SXSSFPicture;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;

import org.apache.causeway.commons.internal.image._Images;

/**
 * @see <a href="https://www.baeldung.com/java-add-image-excel">baeldung</a>
 */
record ExcelImageHandler(
    SXSSFWorkbook workbook,
    SXSSFDrawing drawing) {
    
    ExcelImageHandler(Sheet sheet) {
        this((SXSSFWorkbook) sheet.getWorkbook(), (SXSSFDrawing) sheet.createDrawingPatriarch());
    }

    void addImage(BufferedImage value, Cell cell) {

        // set the row height, based on the image height
        // 1) don't make less high than already is
        // 2) don't exceed height of 120 points
        var currentRowHeight = cell.getRow().getHeightInPoints();
        var requiredRowHeight = Math.min(120, value.getHeight());
        var newRowHeight = Math.max(currentRowHeight, requiredRowHeight);
        cell.getRow().setHeightInPoints(newRowHeight);
        
        // var picture =
        addImage(value, cell.getRowIndex(), cell.getColumnIndex());
        //picture.resize();
    }
    
    // -- HELPER
    
    private SXSSFPicture addImage(BufferedImage image, int rowIndex, int colIndex) {
        var imgId = workbook.addPicture(_Images.toBytes(_Images.resizeToMaxHeight(image, 120)), Workbook.PICTURE_TYPE_PNG);
        var anchor = new XSSFClientAnchor();
        
        anchor.setCol1(colIndex);
        anchor.setCol2(colIndex + 1);
        anchor.setRow1(rowIndex);
        anchor.setRow2(rowIndex + 1);
        
        return drawing.createPicture(anchor, imgId);
    }
    
}
