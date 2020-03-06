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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

import org.apache.isis.subdomains.excel.applib.dom.AggregationType;
import org.apache.isis.subdomains.excel.applib.dom.ExcelService;

import lombok.val;

public class SheetPivoter {

    private Sheet sourceSheet;
    private Sheet targetSheet;
    private AnnotationList annotations = new AnnotationList(new ArrayList<AnnotationTriplet>());

    // source sheet stuff
    private int numberOfColumnAnnotationsInSource;
    private int numberOfValueAnnotationsInSource;
    private int numberOfDecorationAnnotationsInSource;
    private int rowAnnotatedColumnNumber; // just one row annotation is supported

    // target sheet stuff
    private List<Row> columnHeaderValueRows = new ArrayList<>();
    private Row fieldNameRow;

    int columnLabelOffsetX;
    int valueRowOffsetY;
    int decoRowOffsetX;

    int valuesStartAtRownumber;

    // layout info source sheet
    private static class SourceLayOut {
        // conventions source sheet
        static final int ANNOTATION_ROW_NUMBER = 0;
        static final int ORDER_ROW_NUMBER = 1;
        static final int TYPE_ROW_NUMBER = 2;
        static final int FIELDNAME_ROW_NUMBER = 3;
        static final int VALUES_START_AT_ROWNUMBER = 4;
        static final int numberOfRowAnnotationsInSource = 1; // multiple row labels are not supported at the moment
    }

    // layout target sheet
    private static class TargetLayOut {

        static final int rowLabelColumnNumber = 0; // row labels in first colu

        //offsets
        static final int columnLabelOffsetY = 0; // top row used for pivot

        //some styling
        static final short fieldnameBgColorIndex = HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex();
        static final short columnHeaderValueBgColorIndex = HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex();
        static final short sumBgColorIndex = HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex();
        static CellStyle fieldNameStyle;
        static CellStyle columnHeaderValueStyle;
        static CellStyle rowSumStyle;
        static CellStyle columSumStyle;
        static CellStyle totalSumStyle;
        static Font boldFont;
    }


    /**
     * Takes the values of the source sheet and creates a pivot in the target sheet based on the information of
     * the first couple of rows. Only cells of type CELL_TYPE_NUMERIC will be handled and summed in the pivot.
     * <p>
     *     The source sheet has to honour the following conventions for pivot to be successful and meaningful:
     * </p>
     *
     *     <ul>
     *         <li>
     *             {@link Row}(0) contains at least a {@link Cell} of type CELL_TYPE_STRING with the value "row".
     *         </li>
     *         <li>
     *             Likewise {@link Row}(0) contains at least one {@link Cell} with value "column" and "value".
     *         </li>
     *         <li>
     *             {@link Row}(0) may contain one or more {@link Cell}'s with value "deco". The values in this column are expected (but not enforced) to
     *             have the same value for each distinct value found in the column marked "row".
     *         </li>
     *         <li>
     *             {@link Row}(1) may contain one or more {@link Cell}'s of type CELL_TYPE_NUMERIC that specifies an order for the value on top of it.
     *         </li>
     *          <li>
     *             {@link Row}(2) contains the aggregation types.
     *         </li>
     *         <li>
     *             {@link Row}(3) contains the field labels.
     *         </li>
     *         <li>
     *             All other {@link Row}'s, if present, contain the data for the pivot. Only numeric values in the column(s) annoted "value" will be summed.
     *         </li>
     *     </ul>
     *
     * <p>
     *     The pivot will use the distinct values of the first column marked "row" (left-to-right) as row labels. The distinct values of the columns marked as
     *     "column" as column labels (in the order of the specified order - if present.
     *     Otherwise in the order in which they appear in {@link Row}(0) left-to-right.)
     *     The values of the pivot are taken from the column(s) marked "value" (in the order of the specified order - if present.
     *     Otherwise in the order in which they appear in {@link Row}(0) left-to-right.) They have to be of type CELL_TYPE_NUMERIC and will me ignored otherwise.
     *     The values found in the column(s) marked "deco" are put in the column(s) 1 ..  following the row label and are meant as decoration.
     *     Since the assumption is that every distinct row label has the same decoration(s) only the first found value for each row will be added.
     * </p>
     */
    public void pivot(final Sheet pivotSourceSheet, final Sheet pivotTargetSheet) {

        sourceSheet = pivotSourceSheet;
        targetSheet = pivotTargetSheet;
        annotations = new AnnotationList(new ArrayList<AnnotationTriplet>());

        getAnnotationsFromSource();
        setOffsets();
        validateAndAdaptSourceDataIfNeeded();
        defineSomeCellStyles();
        createEmptyHeaderRowsAndCells();
        fillInHeaderRows();
        pivotSourceRows();
        addSummations();
    }

    private void getAnnotationsFromSource() {
        for (Cell cell : sourceSheet.getRow(SourceLayOut.ANNOTATION_ROW_NUMBER)){
            List<String> validAnnotations = Arrays.asList("row", "deco", "column", "value");
            if (validAnnotations.contains(cell.getStringCellValue())){
                annotations.list.add(
                        new AnnotationTriplet(
                                cell.getStringCellValue(),
                                cell.getColumnIndex(),
                                (int) sourceSheet.getRow(SourceLayOut.ORDER_ROW_NUMBER).getCell(cell.getColumnIndex()).getNumericCellValue())
                );
            }
        }

        rowAnnotatedColumnNumber = annotations.getByAnnotation_OrderBy_OrderAscending("row").get(0).getColnumber();
        numberOfColumnAnnotationsInSource = annotations.getByAnnotation_OrderBy_OrderAscending("column").size();
        numberOfValueAnnotationsInSource = annotations.getByAnnotation_OrderBy_OrderAscending("value").size();
        numberOfDecorationAnnotationsInSource = annotations.getByAnnotation_OrderBy_OrderAscending("deco").size();
    }

    private void setOffsets() {

        /*
                                    columnLabelOffsetX
                                    <---    (2)   --->
                              ^     |--------|--------|--------|--------|--------|--------| ^
                              |     |        |        |        |        |        |        | |
                              |     |        |        |        |        |        |        | |
                                    |--------|--------|--------|--------|--------|--------| v columnLabelOffsetY (1)
             valueRowOffsetY (3)    |        | fn of  |        |        |        |        |
                              |     |        |   col  |  c1    |        |  c2    |        |
                              |     |--------|--------|--------|--------|--------|--------|
                              |     |        |        |        |        |        |        |
                              |     |  fn1   |  fn2   |  fnv1  |   fnv2 |  fnv1  |   fnv2 |
                              v     |--------|--------|--------|--------|--------|--------|
                                    |        |        |        |        |        |        |
                                    |   l1   |  d1    |  ...   |  ..    |  ..    |  ..    |
                                    |--------|--------|--------|--------|--------|--------|

         */

        // offsets
        columnLabelOffsetX = SourceLayOut.numberOfRowAnnotationsInSource + numberOfDecorationAnnotationsInSource;
        valueRowOffsetY = TargetLayOut.columnLabelOffsetY + numberOfColumnAnnotationsInSource;
        decoRowOffsetX = SourceLayOut.numberOfRowAnnotationsInSource;
    }

    private void validateAndAdaptSourceDataIfNeeded() throws ExcelService.Exception {

        val sourceNotValid = sourceNotValid();
        if (sourceNotValid!=null){
            throw sourceNotValid;
        }

        for (Iterator<Row> iter = sourceSheet.rowIterator(); iter.hasNext();){
            Row r = iter.next();
            if (r.getRowNum()>=SourceLayOut.VALUES_START_AT_ROWNUMBER) {

                // check and adapt empty row values
                adaptOrCreateEmptySourceCell(r.getRowNum(), rowAnnotatedColumnNumber);

                // check and adapt empty column values
                for (AnnotationTriplet at : annotations.getByAnnotation_OrderBy_OrderAscending("column")) {
                    adaptOrCreateEmptySourceCell(r.getRowNum(), at.getColnumber());
                }

            }
        }

    }

    private Cell adaptOrCreateEmptySourceCell(int cellRow, int cellColumn){
        Cell cell = sourceSheet.getRow(cellRow).getCell(cellColumn);
        if (cell == null){
            cell = sourceSheet.getRow(cellRow).createCell(cellColumn);
            cell.setCellValue("(empty)");
            return cell;
        }
        if (cell.getCellType()== CellType.BLANK){
            cell.setCellValue("(empty)");
            return cell;
        }
        if (cell.getCellType()==CellType.STRING && cell.getStringCellValue().equals("")){
            cell.setCellValue("(empty)");
        }
        return cell;
    }

    private IllegalArgumentException sourceNotValid(){
        int srCnt;
        boolean badData = false;
        for (AnnotationTriplet at : annotations.getByAnnotation_OrderBy_OrderAscending("value")){
            for (srCnt = SourceLayOut.VALUES_START_AT_ROWNUMBER; srCnt <= sourceSheet.getLastRowNum(); srCnt++){
                Cell c = sourceSheet.getRow(srCnt).getCell(at.getColnumber());
                if (c != null && c.getCellType()!=CellType.BLANK){
                    if (c.getCellType()!=CellType.NUMERIC) {
                        badData = true;
                        continue;
                    }
                }
            }
        }
        if (badData) {
            return new IllegalArgumentException("Values other than CELL_TYPE_NUMERIC found.");
        } else {
            return null;
        }
    }

    private void createEmptyHeaderRowsAndCells() {

        int columnValueCounter = 1;
        for (AnnotationTriplet at : annotations.getByAnnotation_OrderBy_OrderAscending("column")){
            columnValueCounter = columnValueCounter * getDistinctValuesInSourceSheetColumn(sourceSheet ,at.getColnumber()).size();
        }
        int tableWidth = 1 + numberOfDecorationAnnotationsInSource + columnValueCounter * numberOfValueAnnotationsInSource;

        int trCnt;
        int tcCnt;
        trCnt = 0;
        for (@SuppressWarnings("unused") AnnotationTriplet at : annotations.getByAnnotation_OrderBy_OrderAscending("column")){
            Row workRow = targetSheet.createRow(TargetLayOut.columnLabelOffsetY + trCnt++);
            for (tcCnt=0; tcCnt<tableWidth; tcCnt++){
                workRow.createCell(tcCnt);
            }
            columnHeaderValueRows.add(workRow);
        }

        fieldNameRow = targetSheet.createRow(trCnt++);
        valuesStartAtRownumber = trCnt;
        for (tcCnt=0; tcCnt<tableWidth; tcCnt++){
            fieldNameRow.createCell(tcCnt);
        }

    }

    private void fillInHeaderRows() {

        int trCnt;

        // set fieldnames for every column
        trCnt = TargetLayOut.columnLabelOffsetY;
        for (AnnotationTriplet at : annotations.getByAnnotation_OrderBy_OrderAscending("column")){
            getFieldNameCell(columnLabelOffsetX-1 ,trCnt, at.getColnumber());
            trCnt++;
        }

        // set column labels
        trCnt = TargetLayOut.columnLabelOffsetY;
        int order = 0;
        for (AnnotationTriplet at : annotations.getByAnnotation_OrderBy_OrderAscending("column")){
            getColumnLabelCellsFor(columnLabelOffsetX, trCnt, order++, at.getColnumber());
            trCnt++;
        }

        fillInFieldNameRow();
    }

    private void getFieldNameCell(final int column, final int row, final int sourceCol) {
        Cell fieldNameSourceCell = sourceSheet.getRow(SourceLayOut.FIELDNAME_ROW_NUMBER).getCell(sourceCol);
        Cell newCell = targetSheet.getRow(row).createCell(column);
        PivotUtils.copyCell(fieldNameSourceCell, newCell);
        newCell.setCellStyle(TargetLayOut.fieldNameStyle);
    }

    private void getColumnLabelCellsFor(final int column, final int row, final int orderHeaderRow, final int sourceCol) {

        List<AnnotationTriplet> list = annotations.getByAnnotation_OrderBy_OrderAscending("column");

        int multiplier = 1;
        for (int i = 0; i < orderHeaderRow; i++){
            multiplier = multiplier * getDistinctValuesInSourceSheetColumn(sourceSheet, list.get(i).getColnumber()).size();
        }

        int emptyCells = 1;
        for (int i = orderHeaderRow+1; i < numberOfColumnAnnotationsInSource; i++ ){
            emptyCells = emptyCells * getDistinctValuesInSourceSheetColumn(sourceSheet, list.get(i).getColnumber()).size();
        }
        emptyCells = emptyCells * numberOfValueAnnotationsInSource;
        emptyCells = emptyCells-1;

        int tcCnt;
        tcCnt = column;
        for (int i = 0; i < multiplier; i++) {
            for (Cell c : getDistinctValuesInSourceSheetColumn(sourceSheet, sourceCol)) {
                // column header
                Cell newCell = targetSheet.getRow(row).createCell(tcCnt++);
                PivotUtils.copyCell(c, newCell);
                newCell.setCellStyle(TargetLayOut.columnHeaderValueStyle);
                // empty cells
                for (int ii = 0; ii < emptyCells; ii++){
                    newCell = targetSheet.getRow(row).createCell(tcCnt++);
                    newCell.setCellStyle(TargetLayOut.columnHeaderValueStyle);
                }
            }
        }

    }

    private void fillInFieldNameRow() {

        int tcCnt;
        getFieldNameCell(TargetLayOut.rowLabelColumnNumber, fieldNameRow.getRowNum(), rowAnnotatedColumnNumber);
        tcCnt = decoRowOffsetX;
        for (AnnotationTriplet at : annotations.getByAnnotation_OrderBy_OrderAscending("deco")) {
            getFieldNameCell(tcCnt++, fieldNameRow.getRowNum(), at.getColnumber());
        }
        // set repeating field names (for every column value in the row above)
        Row rowAbove = columnHeaderValueRows.get(numberOfColumnAnnotationsInSource-1);
        while (tcCnt < rowAbove.getLastCellNum()){
            for (AnnotationTriplet at : annotations.getByAnnotation_OrderBy_OrderAscending("value")) {
                getFieldNameCell(tcCnt++, fieldNameRow.getRowNum(), at.getColnumber());
            }
        }
    }

    private void pivotSourceRows() {

        for (Iterator<Row> iter = sourceSheet.rowIterator(); iter.hasNext();){

            Row r = iter.next();
            if (r.getRowNum()>=SourceLayOut.VALUES_START_AT_ROWNUMBER){

                // create label
                Cell sourceLabelCell = r.getCell(rowAnnotatedColumnNumber);
                Integer rowPosTarget = getRowPosCellInTarget(sourceLabelCell, TargetLayOut.rowLabelColumnNumber);
                if (rowPosTarget==null){
                    rowPosTarget = targetSheet.getLastRowNum()+1;
                    Row workRow = targetSheet.createRow(rowPosTarget);
                    Cell newCell = workRow.createCell(TargetLayOut.rowLabelColumnNumber);
                    PivotUtils.copyCell(sourceLabelCell, newCell);
                    // create decoration(s)
                    for (int i = 0; i < numberOfDecorationAnnotationsInSource; i++){
                        Cell sourceCell = findDecorationFor(newCell, i);
                        Cell target = workRow.createCell(decoRowOffsetX + i);
                        PivotUtils.copyCell(sourceCell, target);

                    }
                }
                for (AnnotationTriplet at : annotations.getByAnnotation_OrderBy_OrderAscending("value")) {
                    addValuesToPivot(rowPosTarget, r.getRowNum(), at.getColnumber(), getAggregationTypeFromSource(at.getColnumber()));
                }

            }

        }

        // adapt fieldnames for column values to reflect aggregation type
        for (AnnotationTriplet at : annotations.getByAnnotation_OrderBy_OrderAscending("value")) {
            for (Cell cell : fieldNameRow) {
                if (cell.getStringCellValue().equals(sourceSheet.getRow(SourceLayOut.FIELDNAME_ROW_NUMBER).getCell(at.getColnumber()).getStringCellValue())) {
                    reWriteFieldNamesToReflectAggregationType(getAggregationTypeFromSource(at.getColnumber()), cell);
                }
            }
        }

    }

    private Cell findDecorationFor(Cell cell, Integer order){

        AnnotationTriplet at = annotations.getByAnnotation_OrderBy_OrderAscending("deco").get(order);
        int decoX = at.getColnumber();

        int srCnt;
        for (srCnt = SourceLayOut.VALUES_START_AT_ROWNUMBER; srCnt <= sourceSheet.getLastRowNum(); srCnt++) {
            if (
                    PivotUtils.cellValueEquals(
                            sourceSheet.getRow(srCnt).getCell(rowAnnotatedColumnNumber),
                            cell
                    )
                    ){
                return sourceSheet.getRow(srCnt).getCell(decoX);
            }
        }
        return null;
    }

    private void addValuesToPivot(final int rowInTarget, final int rowInSource, final int colInSource, final AggregationType type) {

        // find position in target sheet
        int colPosTarget;

        // search header rows of target sheet for matching column(s) row-by-row, left-to-right
        int rowSearchStart = TargetLayOut.columnLabelOffsetY; // row start of search
        int colSearchStart = columnLabelOffsetX; // col start of search
        int colSearchEnd = targetSheet.getRow(rowSearchStart).getLastCellNum();
        for (AnnotationTriplet at : annotations.getByAnnotation_OrderBy_OrderAscending("column")){
            Cell sourceColumnCell = sourceSheet.getRow(rowInSource).getCell(at.getColnumber());
            colSearchStart = getColPosCellInTarget(sourceColumnCell,rowSearchStart++, colSearchStart, colSearchEnd);
        }
        // search fieldname row of target sheet for matching fieldname for value
        Cell valueFieldNameSourceCell = sourceSheet.getRow(SourceLayOut.FIELDNAME_ROW_NUMBER).getCell(colInSource);
        colPosTarget = getColPosCellInTarget(valueFieldNameSourceCell, fieldNameRow.getRowNum(), colSearchStart, colSearchEnd);

        // copy or add value to target cell
        Cell valueCellSource = sourceSheet.getRow(rowInSource).getCell(colInSource);
        Cell valueCellTarget = targetSheet.getRow(rowInTarget).getCell(colPosTarget);
        if (valueCellSource!=null && (valueCellTarget== null || valueCellTarget.getCellType()==CellType.BLANK)){
            valueCellTarget = targetSheet.getRow(rowInTarget).createCell(colPosTarget);
            switch (type) {
            case SUM:
                PivotUtils.copyCell(valueCellSource, valueCellTarget);
                break;

            case COUNT:
                valueCellTarget.setCellValue(1);
                break;
            }
        } else {
            switch (type) {
            case SUM:
                PivotUtils.addCellValueTo(valueCellSource, valueCellTarget);
                break;

            case COUNT:
                if (valueCellTarget==null){
                    valueCellTarget = targetSheet.getRow(rowInTarget).createCell(colPosTarget);
                    valueCellTarget.setCellValue(1);
                } else {
                    double counter = valueCellTarget.getNumericCellValue();
                    valueCellTarget.setCellValue(counter + 1);
                }
                break;
            }
        }

    }

    private Integer getColPosCellInTarget(Cell cell, int rowNumber, int colStart, int colEnd){
        for (int i = colStart; i < colEnd; i++){
            if (PivotUtils.cellValueEquals(targetSheet.getRow(rowNumber).getCell(i),cell)){
                return i;
            }
        }
        return null;
    }

    private Integer getRowPosCellInTarget(Cell cell, int colNumber){
        for (int i = valueRowOffsetY; i <= targetSheet.getLastRowNum(); i++){
            if (PivotUtils.cellValueEquals(targetSheet.getRow(i).getCell(colNumber), cell)){
                return i;
            }
        }
        return null;
    }

    private void addSummations(){

        int tableWidth = targetSheet.getRow(valueRowOffsetY).getLastCellNum();
        int tableHeight = targetSheet.getLastRowNum() + 1;

        if (numberOfValueAnnotationsInSource < 2) {

            // summations by row (only makes sense with one value annotation)
            for (Iterator<Row> iter = targetSheet.rowIterator(); iter.hasNext(); ) {
                Row r = iter.next();
                Cell c = targetSheet.getRow(r.getRowNum()).createCell(tableWidth);
                if (r.getRowNum() > valueRowOffsetY) {
                    String start = CellReference.convertNumToColString(columnLabelOffsetX);
                    String end = CellReference.convertNumToColString(tableWidth - 1);
                    c.setCellFormula("SUM(" + start + String.valueOf(r.getRowNum() + 1) + ":" + end + String.valueOf(r.getRowNum() + 1) + ")");
                    c.setCellStyle(TargetLayOut.rowSumStyle);
                } else {
                    // just set cell style
                    c.setCellStyle(TargetLayOut.rowSumStyle);
                }
            }

        }

        // summations for column
        Row r = targetSheet.createRow(tableHeight);
        int tcCnt;
        for (tcCnt = 0; tcCnt < tableWidth; tcCnt++){
            Cell c = r.createCell(tcCnt);
            if (tcCnt >= columnLabelOffsetX) {
                String columnStr = CellReference.convertNumToColString(tcCnt);
                c.setCellFormula("SUM(" + columnStr + String.valueOf(valueRowOffsetY + 2) + ":" + columnStr + String.valueOf(tableHeight) + ")");
                c.setCellStyle(TargetLayOut.columSumStyle);
            } else {
                // just set cell style
                c.setCellStyle(TargetLayOut.columSumStyle);
            }
        }
        if (numberOfValueAnnotationsInSource < 2) {
            // add total for sums (only makes sense with one value annotation)
            Cell c = r.createCell(tableWidth);
            String columnStr = CellReference.convertNumToColString(tableWidth);
            c.setCellFormula("SUM(" + columnStr + String.valueOf(valueRowOffsetY + 2) + ":" + columnStr + String.valueOf(tableHeight) + ")");
            c.setCellStyle(TargetLayOut.totalSumStyle);
        }
    }

    private void reWriteFieldNamesToReflectAggregationType(AggregationType type, Cell cell) {
        if (type!=null){
            String fieldName = cell.getStringCellValue();

            switch (type) {
            case SUM:
                fieldName = fieldName + (" (sum)");
                break;
            case COUNT:
                fieldName = fieldName + (" (count)");
                break;
            }

            cell.setCellValue(fieldName);
        }
    }

    private AggregationType getAggregationTypeFromSource(final int colNumber){
        AggregationType type = AggregationType.SUM;
        Cell typeSourceCell = sourceSheet.getRow(SourceLayOut.TYPE_ROW_NUMBER).getCell(colNumber);
        if (typeSourceCell!=null && typeSourceCell.getStringCellValue().equals("COUNT")){
            type = AggregationType.COUNT;
        }
        return type;
    }

    static List<Cell> getDistinctValuesInSourceSheetColumn(Sheet sourceSheet, Integer columnNumber){

        List<Cell> result = new ArrayList<>();

        for (int i = SourceLayOut.VALUES_START_AT_ROWNUMBER; i <= sourceSheet.getLastRowNum(); i++){
            Row r = sourceSheet.getRow(i);
            Cell c = r.getCell(columnNumber);
            boolean found = false;
            for (Cell cell : result){
                if (PivotUtils.cellValueEquals(c, cell)){
                    found = true;
                    continue;
                }
            }
            if (!found){
                result.add(c);
            }
        }
        return result;
    }

    private void defineSomeCellStyles() {
        TargetLayOut.boldFont= targetSheet.getWorkbook().createFont();
        TargetLayOut.boldFont.setBold(true);

        TargetLayOut.fieldNameStyle = targetSheet.getWorkbook().createCellStyle();
        TargetLayOut.fieldNameStyle.setFillForegroundColor(TargetLayOut.fieldnameBgColorIndex);
        TargetLayOut.fieldNameStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        TargetLayOut.fieldNameStyle.setFont(TargetLayOut.boldFont);

        TargetLayOut.columnHeaderValueStyle = targetSheet.getWorkbook().createCellStyle();
        TargetLayOut.columnHeaderValueStyle.setFillForegroundColor(TargetLayOut.columnHeaderValueBgColorIndex);
        TargetLayOut.columnHeaderValueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        TargetLayOut.columnHeaderValueStyle.setFont(TargetLayOut.boldFont);

        TargetLayOut.rowSumStyle = targetSheet.getWorkbook().createCellStyle();
        TargetLayOut.rowSumStyle.setBorderLeft(BorderStyle.THIN);
        TargetLayOut.rowSumStyle.setFillForegroundColor(TargetLayOut.sumBgColorIndex);
        TargetLayOut.rowSumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        TargetLayOut.columSumStyle = targetSheet.getWorkbook().createCellStyle();
        TargetLayOut.columSumStyle.setBorderTop(BorderStyle.THIN);
        TargetLayOut.columSumStyle.setFillForegroundColor(TargetLayOut.sumBgColorIndex);
        TargetLayOut.columSumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        TargetLayOut.totalSumStyle = targetSheet.getWorkbook().createCellStyle();
        TargetLayOut.totalSumStyle.setBorderTop(BorderStyle.THIN);
        TargetLayOut.totalSumStyle.setBorderLeft(BorderStyle.THIN);
        TargetLayOut.totalSumStyle.setFillForegroundColor(TargetLayOut.sumBgColorIndex);
        TargetLayOut.totalSumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

}
