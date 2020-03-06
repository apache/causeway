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
package org.apache.isis.subdomains.excel.applib.dom.util;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import org.apache.isis.subdomains.excel.applib.dom.AggregationType;

public class PivotUtils {

    public static void createAnnotationRow(final Row annotationRow, final List<String> annotationList) {

        int i = 0;
        for (final String a : annotationList) {
            final Cell cell = annotationRow.createCell((short) i++);
            cell.setCellValue(a);
        }

    }

    public static void createOrderRow(final Row orderRow, final List<Integer> orderList) {

        int i = 0;
        for (final Integer order : orderList) {
            final Cell cell = orderRow.createCell((short) i++);
            cell.setCellValue(order);
        }

    }

    public static void createTypeRow(final Row typeRow, final List<AggregationType> typeList) {

        int i = 0;
        for (final AggregationType type : typeList) {
            final Cell cell = typeRow.createCell((short) i++);
            if (type != null) {
                cell.setCellValue(type.name());
            }
        }

    }

    static boolean cellValueEquals(Cell cell, Cell other) {

        if (cell != null && other != null && cell.getCellType() == other.getCellType()) {

            switch (cell.getCellType()) {
                case BLANK:
                    if (other.getCellType()==CellType.BLANK) {
                        return true;
                    }
                    break;
                case BOOLEAN:
                    if (cell.getBooleanCellValue() == other.getBooleanCellValue()){
                        return true;
                    }
                    break;
                case ERROR:
                    if (cell.getErrorCellValue() == other.getErrorCellValue()){
                        return true;
                    }
                    break;
                case FORMULA:
                    break;
                case NUMERIC:
                    if (cell.getNumericCellValue() == other.getNumericCellValue()){
                        return true;
                    }
                    break;
                case STRING:
                    if (cell.getStringCellValue().equals(other.getStringCellValue())){
                        return true;
                    }
            }
        }

        return  false;
    }

    static Cell addCellValueTo(Cell source, Cell target){

        if (source == null) {
            return target;
        }

        if (target.getCellType()==CellType.NUMERIC && source.getCellType()==CellType.NUMERIC ){
            double val1 = target.getNumericCellValue();
            double val2 = source.getNumericCellValue();
            target.setCellValue(val1+val2);
        }

        return target;
    }

    static void copyCell(Cell source, Cell target) {

        // If the source cell is null, return
        if (source == null) {
            return;
        }

        // Use source cell style
        target.setCellStyle(source.getCellStyle());

        // If there is a cell comment, copy
        if (target.getCellComment() != null) {
            target.setCellComment(source.getCellComment());
        }

        // If there is a cell hyperlink, copy
        if (source.getHyperlink() != null) {
            target.setHyperlink(source.getHyperlink());
        }

        // Set the cell data value
        switch (source.getCellType()) {
            case BLANK:
                target.setBlank();
                break;
            case BOOLEAN:
                target.setCellValue(source.getBooleanCellValue());
                break;
            case ERROR:
                target.setCellErrorValue(source.getErrorCellValue());
                break;
            case FORMULA:
                target.setCellFormula(source.getCellFormula());
                break;
            case NUMERIC:
                target.setCellValue(source.getNumericCellValue());
                break;
            case STRING:
                target.setCellValue(source.getRichStringCellValue());
                break;
        }

    }

}
