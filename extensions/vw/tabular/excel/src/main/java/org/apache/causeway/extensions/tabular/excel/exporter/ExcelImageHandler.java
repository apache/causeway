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
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * @see <a href="https://www.baeldung.com/java-add-image-excel">baeldung</a>
 */
record ExcelImageHandler(
    XSSFWorkbook workbook,
    XSSFDrawing drawing) {
    
    ExcelImageHandler(Sheet sheet) {
        this((XSSFWorkbook) sheet.getWorkbook(), (XSSFDrawing) sheet.createDrawingPatriarch());
    }

    void addImage(BufferedImage value, Cell cell) {
        addImage(value, cell.getRowIndex(), cell.getColumnIndex());

        // set the row height, based on the image height
        // 1) don't make less high than already is
        // 2) don't exceed 300px
        var currentRowHeight = cell.getRow().getHeightInPoints();
        var requiredRowHeight = Math.min(300, value.getHeight());
        var newRowHeight = Math.max(currentRowHeight, requiredRowHeight);
        cell.getRow().setHeightInPoints(newRowHeight);
    }
    
    // -- HELPER
    
    private void addImage(BufferedImage image, int rowIndex, int colIndex) {
        var imgId = workbook.addPicture(toBytes(image), Workbook.PICTURE_TYPE_PNG);
        var anchor = new XSSFClientAnchor();
        
        anchor.setCol1(colIndex);
        anchor.setCol2(colIndex + 1);
        anchor.setRow1(rowIndex);
        anchor.setRow2(rowIndex + 1);
        
        drawing.createPicture(anchor, imgId);
    }
    
    @SneakyThrows
    private static byte[] toBytes(final @NonNull BufferedImage image){
        try(var bos = new ByteArrayOutputStream(8 * 1024)) {
            ImageIO.write(image, "png", bos); // png is lossless
            return bos.toByteArray();
        }
    }
    
}
