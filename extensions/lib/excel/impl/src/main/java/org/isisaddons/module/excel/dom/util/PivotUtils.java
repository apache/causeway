package org.isisaddons.module.excel.dom.util;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import org.isisaddons.module.excel.dom.AggregationType;

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
                case Cell.CELL_TYPE_BLANK:
                    if (other.getCellType()==Cell.CELL_TYPE_BLANK){
                        return true;
                    }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    if (cell.getBooleanCellValue() == other.getBooleanCellValue()){
                        return true;
                    }
                    break;
                case Cell.CELL_TYPE_ERROR:
                    if (cell.getErrorCellValue() == other.getErrorCellValue()){
                        return true;
                    }
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if (cell.getNumericCellValue() == other.getNumericCellValue()){
                        return true;
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
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

        if (target.getCellType()==Cell.CELL_TYPE_NUMERIC && source.getCellType()==Cell.CELL_TYPE_NUMERIC ){
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

        // Set the cell data type
        target.setCellType(source.getCellType());

        // Set the cell data value
        switch (source.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                target.setCellValue(source.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                target.setCellErrorValue(source.getErrorCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                target.setCellFormula(source.getCellFormula());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                target.setCellValue(source.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                target.setCellValue(source.getRichStringCellValue());
                break;
        }

    }

}
