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

import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;

public class SheetPivoterTest {

    XSSFWorkbook workbook;
    XSSFSheet sourceSheet;
    XSSFSheet targetSheet;
    SheetPivoter p;

    @Before
    public void setup() {
        workbook = new XSSFWorkbook();
        sourceSheet = workbook.createSheet();
        targetSheet = workbook.createSheet();
        p = new SheetPivoter();
    }

    @Test
    public void poi_method_lastCellNumber_yields_1_higher_than_expected(){

        // when
        Cell c = targetSheet.createRow(0).createCell(0);
        c.setBlank();

        // then
        // ************** NOTE ***************************************************************************
        Assertions.assertThat(targetSheet.getRow(0).getLastCellNum()).isEqualTo((short)1); // NOTE !!!!!!!
        Assertions.assertThat(targetSheet.getRow(0).getCell(1)).isEqualTo(null);
        // ************** NOTE ***************************************************************************

        Assertions.assertThat(c).isNotNull();
        Assertions.assertThat(targetSheet.getLastRowNum()).isEqualTo(0); // Expected

        // and when
        targetSheet.getRow(0).createCell(0).setCellValue("a");

        // then still
        Assertions.assertThat(targetSheet.getRow(0).getLastCellNum()).isEqualTo((short)1); // NOTE !!!!!!!
        Assertions.assertThat(c.getCellType()).isEqualTo(CellType.STRING);

        // and when
        targetSheet.getRow(0).createCell(1).setCellValue("b");
        Assertions.assertThat(targetSheet.getRow(0).getLastCellNum()).isEqualTo((short)2); // NOTE !!!!!!!

    }

    @Test
    public void empty_value_rows_works(){

        // given
        List<String> annotations = Arrays.asList("row", "value", "column");
        List<Integer> orderNumbers = Arrays.asList(0, 0, 0);
        List<String> typeList = Arrays.asList(null, "SUM", null);
        List<String> fieldNameList = Arrays.asList("fn1", "fn2", "fn3");
        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList);

        // when
        p.pivot(sourceSheet, targetSheet);

        // then
        Assertions.assertThat(p.valueRowOffsetY).isEqualTo(1);
        Assertions.assertThat(p.columnLabelOffsetX).isEqualTo(1);
        Assertions.assertThat(p.decoRowOffsetX).isEqualTo(1);
        Assertions.assertThat(p.valuesStartAtRownumber).isEqualTo(2); // even though there are no values

        Assertions.assertThat(targetSheet.getLastRowNum()).isEqualTo(2);
        Assertions.assertThat(targetSheet.getRow(1).getLastCellNum()).isEqualTo((short)2);
        testRow(0, "fn3", null);
        testRow(1, "fn1", null);
        // last row is for summing
        Assertions.assertThat(targetSheet.getRow(2).getCell(0).getCellType()).isEqualTo(CellType.BLANK);
        Assertions.assertThat(targetSheet.getRow(2).getCell(1).getCellType()).isEqualTo(CellType.FORMULA);
        Assertions.assertThat(targetSheet.getRow(2).getCell(1).getCellFormula()).isEqualTo("SUM(B3:B2)");

    }

    @Test
    public void setOffsets_works(){
        // given
        List<String> annotations = Arrays.asList("row", "value", "value", "column", "deco");
        List<Integer> orderNumbers = Arrays.asList(0, 0, 1, 1, 1);
        List<String> typeList = Arrays.asList(null, "SUM", "SUM", null, null);
        List<String> fieldNameList = Arrays.asList("fn1", "fn2", "fn2a", "fn3", "fn4");
        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList);

        // when
        p.pivot(sourceSheet, targetSheet);

        // then
        Assertions.assertThat(p.valueRowOffsetY).isEqualTo(1);
        Assertions.assertThat(p.columnLabelOffsetX).isEqualTo(2);
        Assertions.assertThat(p.decoRowOffsetX).isEqualTo(1);
        Assertions.assertThat(p.valuesStartAtRownumber).isEqualTo(2);

        // given
        annotations = Arrays.asList("row", "value", "value", "column", "deco", "column");
        orderNumbers = Arrays.asList(0, 0, 1, 1, 1, 2);
        typeList = Arrays.asList(null, "SUM", "SUM", null, null, null);
        fieldNameList = Arrays.asList("fn1", "fn2", "fn2a", "fn3", "fn4", "fn3a");
        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList);

        // when
        p.pivot(sourceSheet, targetSheet);

        // then
        Assertions.assertThat(p.valueRowOffsetY).isEqualTo(2);
        Assertions.assertThat(p.columnLabelOffsetX).isEqualTo(2);
        Assertions.assertThat(p.decoRowOffsetX).isEqualTo(1);
        Assertions.assertThat(p.valuesStartAtRownumber).isEqualTo(3);

    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void validateSourceData_works(){

        // given
        List<String> annotations = Arrays.asList("row", "value", "value", "column");
        List<Integer> orderNumbers = Arrays.asList(0, 0, 1, 1);
        List<String> typeList = Arrays.asList(null, "SUM", "SUM", null);
        List<String> fieldNameList = Arrays.asList("fn1", "fn2", "fn2a", "fn3");
        List<?> v1 = Arrays.asList("r1", "a", 1, "c1");
        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList, v1);

        // then
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Values other than CELL_TYPE_NUMERIC found");

        // when
        p.pivot(sourceSheet, targetSheet);

    }

    @Test
    public void replaceEmptyRowAndColumns_when_validating_sourcedata_works(){

        // given
        List<String> annotations = Arrays.asList("row", "value", "column");
        List<Integer> orderNumbers = Arrays.asList(0, 0, 1);
        List<String> typeList = Arrays.asList(null, "SUM", null);
        List<String> fieldNameList = Arrays.asList("fn1", "fn2", "fn3");
        List<?> v1 = Arrays.asList(null, 1, null);
        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList, v1);

        // when
        p.pivot(sourceSheet, targetSheet);

        // then
        Assertions.assertThat(sourceSheet.getRow(4).getCell(0).getStringCellValue()).isEqualTo("(empty)");
        Assertions.assertThat(sourceSheet.getRow(4).getCell(2).getStringCellValue()).isEqualTo("(empty)");
    }

    @Test
    public void headerRow_Fill_works() {
        // given case for 3 col, 2 val
        List<String> annotations = Arrays.asList("row", "column", "column", "column", "value", "value", "deco", "deco");
        List<Integer> orderNumbers = Arrays.asList(0, 1, 2, 3, 1, 2, 1, 2);
        List<String> typeList = Arrays.asList(null, null, null, null, "SUM", "SUM", null, null);
        List<String> fieldNameList = Arrays.asList("fn-r1", "fn-c", "fn-c1", "fn-c2", "fn-v1", "fn-v2", "fn-d1", "fn-d2");
        List<?> v1 = Arrays.asList("r1", "a", "c1-1", "c2-1", null, null, "r1d1", null);
        List<?> v2 = Arrays.asList("r2", "a", "c1-2", "c2-1", null, null, null, "r2d2");
        List<?> v3 = Arrays.asList("r3", "b", "c1-3", "c2-2", null, null, "r3d1", "r3d2");
        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList, v1, v2, v3);

        // when
        p.pivot(sourceSheet, targetSheet);

        // then
        Assertions.assertThat(targetSheet.getLastRowNum()).isEqualTo(7);
        testRow(0, null, null, "fn-c", "a", null, null, null, null, null, null, null, null, null, null, null, "b"); //etc
        testRow(1, null, null, "fn-c1", "c1-1", null, null, null, "c1-2", null, null, null, "c1-3", null, null, null, "c1-1"); //etc
        testRow(2, null, null, "fn-c2", "c2-1", null, "c2-2", null, "c2-1", null, "c2-2", null, "c2-1", null, "c2-2", null, "c2-1"); //etc
        testRow(3, "fn-r1", "fn-d1", "fn-d2", "fn-v1 (sum)", "fn-v2 (sum)", "fn-v1 (sum)", "fn-v2 (sum)", "fn-v1 (sum)", "fn-v2 (sum)"); //etc
        testRow(4, "r1", "r1d1", null); //etc
        testRow(5, "r2", null, "r2d2");
        testRow(6, "r3", "r3d1", "r3d2");
        // last row is for summing

    }

    @Test
    public void testPivoting_works() throws Exception {

        // given
        List<String> annotations = Arrays.asList("row", "value", "column", "deco", "deco");
        List<Integer> orderNumbers = Arrays.asList(0, 1, 1, 2, 1);
        List<String> typeList = Arrays.asList(null, "SUM", null, null, null);
        List<String> fieldNameList = Arrays.asList("fn1", "fn2", "fn3", "fn4", "fn5");
        List<?> v1 = Arrays.asList("l1", 1, "c1", "d1-f-l1", "d2-f-l1");
        List<?> v2 = Arrays.asList("l2", 2, "c2", "deco for l2", null);
        List<?> v3 = Arrays.asList("l1", 3, "c1", "other deco not used", null);
        List<?> v4 = Arrays.asList("l3", 33, "c1", null, null);

        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList, v1, v2, v3, v4);

        // when
        p.pivot(sourceSheet, targetSheet);

        // then
        Assertions.assertThat(targetSheet.getLastRowNum()).isEqualTo(5);
        testRow(0, null, null, "fn3", "c1", "c2");
        testRow(1, "fn1", "fn5", "fn4", "fn2 (sum)", "fn2 (sum)");
        testRow(2, "l1", "d2-f-l1", "d1-f-l1", 4, null);
        Assertions.assertThat(targetSheet.getRow(2).getCell(5).getCellFormula()).isEqualTo("SUM(D3:E3)");
        testRow(3, "l2", null, "deco for l2", null, 2);
        Assertions.assertThat(targetSheet.getRow(3).getCell(5).getCellFormula()).isEqualTo("SUM(D4:E4)");
        testRow(4, "l3", null, null, 33, null);
        Assertions.assertThat(targetSheet.getRow(4).getCell(5).getCellFormula()).isEqualTo("SUM(D5:E5)");

        Assertions.assertThat(targetSheet.getRow(5).getCell(3).getCellFormula()).isEqualTo("SUM(D3:D5)");
        Assertions.assertThat(targetSheet.getRow(5).getCell(4).getCellFormula()).isEqualTo("SUM(E3:E5)");
        Assertions.assertThat(targetSheet.getRow(5).getCell(5).getCellFormula()).isEqualTo("SUM(F3:F5)");

    }

    @Test
    public void testPivoting_WithTwoValues_works() throws Exception {

        // given
        List<String> annotations = Arrays.asList("row", "value", "column", "deco", "deco", "value");
        List<Integer> orderNumbers = Arrays.asList(0, 1, 1, 2, 1, 2);
        List<String> typeList = Arrays.asList(null, "SUM", null, null, null, "COUNT");
        List<String> fieldNameList = Arrays.asList("fn1", "fn2", "fn3", "fn4", "fn5", "fn2a");
        List<?> v1 = Arrays.asList("l1", 1, "c1", "d1-f-l1", "d2-f-l1", 2);
        List<?> v2 = Arrays.asList("l2", 2, "c2", "deco for l2", null, null);
        List<?> v3 = Arrays.asList("l1", 3, "c1", "other deco not used", null, 2);
        List<?> v4 = Arrays.asList("l3", 33, "c1", null, null, 2);

        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList, v1, v2, v3, v4);

        // when
        p.pivot(sourceSheet, targetSheet);

        // then
        Assertions.assertThat(targetSheet.getLastRowNum()).isEqualTo(5);
        testRow(0, null, null, "fn3", "c1", null, "c2", null);
        testRow(1, "fn1", "fn5", "fn4", "fn2 (sum)", "fn2a (count)", "fn2 (sum)", "fn2a (count)");
        testRow(2, "l1", "d2-f-l1", "d1-f-l1", 4, 2, null, null);
        testRow(3, "l2", null, "deco for l2", null, null, 2, 1);
        testRow(4, "l3", null, null, 33, 1, null, null);

        Assertions.assertThat(targetSheet.getRow(5).getCell(3).getCellFormula()).isEqualTo("SUM(D3:D5)");
        Assertions.assertThat(targetSheet.getRow(5).getCell(4).getCellFormula()).isEqualTo("SUM(E3:E5)");
        Assertions.assertThat(targetSheet.getRow(5).getCell(5).getCellFormula()).isEqualTo("SUM(F3:F5)");
        Assertions.assertThat(targetSheet.getRow(5).getCell(6).getCellFormula()).isEqualTo("SUM(G3:G5)");

    }

    @Test
    public void testPivoting_decovalues() throws Exception {

        // given
        List<String> annotations = Arrays.asList("deco", "row", "value", "column");
        List<Integer> orderNumbers = Arrays.asList(1, 0, 1, 1);
        List<String> typeList = Arrays.asList(null, null, "SUM", null);
        List<String> fieldNameList = Arrays.asList("fn1", "fn2", "fn3", "fn4");
        List<?> v1 = Arrays.asList("deco used", "l1", 1, "c1");
        List<?> v2 = Arrays.asList(null, "l1", 1, "c2");
        List<?> v3 = Arrays.asList("deco not used", "l1", 1, "c3");

        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList, v1, v2, v3);

        // when
        p.pivot(sourceSheet, targetSheet);

        // then
        Assertions.assertThat(targetSheet.getLastRowNum()).isEqualTo(3);
        testRow(0, null, "fn4", "c1", "c2", "c3");
        testRow(1, "fn2", "fn1", "fn3 (sum)", "fn3 (sum)", "fn3 (sum)");
        testRow(2, "l1", "deco used", 1, 1, 1);
    }

    @Test
    public void testPivoting_NotSupportedValueType_Boolean() throws Exception {

        // given
        List<String> annotations = Arrays.asList("row", "value", "column");
        List<Integer> orderNumbers = Arrays.asList(0, 1, 1);
        List<String> typeList = Arrays.asList(null, "SUM", null);
        List<String> fieldNameList = Arrays.asList("fn1", "fn2", "fn3");
        List<?> v1 = Arrays.asList("l1", true, "c1");
        List<?> v2 = Arrays.asList("l2", false, "c2");
        List<?> v3 = Arrays.asList("l1", true, "c1");

        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList, v1, v2, v3);

        // when
        p.pivot(sourceSheet, targetSheet);

        // then
        Assertions.assertThat(targetSheet.getLastRowNum()).isEqualTo(4);
        testRow(0, "fn3", "c1", "c2");
        testRow(1, "fn1", "fn2 (sum)", "fn2 (sum)");
        testRow(2, "l1", null, null);
        testRow(3, "l2", null, null);

    }

    @Test
    public void testPivoting_Numeric_As_Rowlabel() throws Exception {

        // given
        List<String> annotations = Arrays.asList("row", "value", "column");
        List<Integer> orderNumbers = Arrays.asList(0, 1, 1);
        List<String> typeList = Arrays.asList(null, "SUM", null);
        List<String> fieldNameList = Arrays.asList("fn1", "fn2", "fn3");
        List<?> v1 = Arrays.asList(1, 1, "c1");
        List<?> v2 = Arrays.asList(2, 1, "c2");
        List<?> v3 = Arrays.asList(1, 1, "c1");

        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList, v1, v2, v3);

        // when
        p.pivot(sourceSheet, targetSheet);

        // then
        Assertions.assertThat(targetSheet.getLastRowNum()).isEqualTo(4);
        testRow(0, "fn3", "c1", "c2");
        testRow(1, "fn1", "fn2 (sum)", "fn2 (sum)");
        testRow(2, 1, 2, null);
        testRow(3, 2, null, 1);
    }

    @Test
    public void emptyRowValuesAreSupported() {

        // given
        List<String> annotations = Arrays.asList("row", "value", "column");
        List<Integer> orderNumbers = Arrays.asList(0, 1, 1);
        List<String> typeList = Arrays.asList(null, "SUM", null);
        List<String> fieldNameList = Arrays.asList("fn1", "fn2", "fn3");
        List<?> v1 = Arrays.asList(null, 1, "c1"); // null
        List<?> v2 = Arrays.asList("", 1, "c1"); // empty string

        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList, v1, v2);

        // when
        p.pivot(sourceSheet, targetSheet);

        // then
        Assertions.assertThat(targetSheet.getLastRowNum()).isEqualTo(3);
        testRow(0, "fn3", "c1");
        testRow(1, "fn1", "fn2 (sum)");
        testRow(2, "(empty)", 2);

    }

    @Test
    public void emptyColumnValuesAreSupported() {

        // given
        List<String> annotations = Arrays.asList("row", "value", "column");
        List<Integer> orderNumbers = Arrays.asList(0, 1, 1);
        List<String> typeList = Arrays.asList(null, "SUM", null);
        List<String> fieldNameList = Arrays.asList("fn1", "fn2", "fn3");
        List<?> v1 = Arrays.asList("l1", 1, null); // null
        List<?> v2 = Arrays.asList("l1", 1, ""); // empty string

        sourceSheetBuilder(annotations, orderNumbers, typeList, fieldNameList, v1, v2);

        // when
        p.pivot(sourceSheet, targetSheet);

        // then
        Assertions.assertThat(targetSheet.getLastRowNum()).isEqualTo(3);
        testRow(0, "fn3", "(empty)");
        testRow(1, "fn1", "fn2 (sum)");
        testRow(2, "l1", 2);

    }

    @Test
    public void getDistinctValuesInSourceSheetColumnTest() {

        // given (only headers in source)
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        sheet.createRow(0).createCell(0).setCellValue("row");
        sheet.createRow(1).createCell(0).setCellValue(1);
        sheet.createRow(2).createCell(0).setCellValue("fieldname");

        // when
        List<Cell> l = p.getDistinctValuesInSourceSheetColumn(sheet, 0);

        // then
        Assertions.assertThat(l.size()).isEqualTo(0);

        // and when (values are added)
        sheet.createRow(3).createCell(0).setCellValue("a");
        sheet.createRow(4).createCell(0).setCellValue("a");
        sheet.createRow(5).createCell(0).setCellValue("b");
        sheet.createRow(6).createCell(0).setCellValue(1);
        sheet.createRow(7).createCell(0).setCellValue(""); // empty string differs from empty cell
        sheet.createRow(8).createCell(0);  // empty string differs from empty cell

        l = p.getDistinctValuesInSourceSheetColumn(sheet, 0);

        // then
        Assertions.assertThat(l.size()).isEqualTo(5);

    }

    void testRow(int rowNumber, Object... vals){

        int i = 0;
        for (Object val : vals){
            testCell(rowNumber, i++, val);
        }

    }

    private void testCell(int x, int y, Object expectedValue){

        if (expectedValue==null){
            Row r = targetSheet.getRow(x);
            if (r == null){
                // skip
            } else {
                Cell c = r.getCell(y);
                if (c == null) {
                    // OK skip
                } else {
                    if (c.getCellType() == CellType.NUMERIC) {
                        assertTrue(Double.isFinite(c.getNumericCellValue()));
                    } else {
                        Assertions.assertThat(c.getStringCellValue()).isEqualTo("");
                    }
                }
            }
        } else {
            if (expectedValue.getClass()==String.class) {
                Assertions.assertThat(targetSheet.getRow(x).getCell(y).getStringCellValue()).isEqualTo(expectedValue);
            } else {
                if (expectedValue.getClass() == Integer.class) {
                    val cellValue = targetSheet.getRow(x).getCell(y);
                    if (cellValue != null){
                        val expectedDouble = Double.valueOf(expectedValue.toString());  
                        Assertions.assertThat(cellValue.getNumericCellValue()).isEqualTo(expectedDouble);
                    }
                } else {
                    //fail!
                    throw new AssertionError("no sensible test possible");
                }
            }

        }

    }

    void sourceSheetBuilder(List<String> annotationList, List<Integer> orderList, List<String> typeList, List<String> fieldNameList, List<?>... values){

        Row r0 = sourceSheet.createRow(0);
        int i = 0;
        for (String a : annotationList){
            r0.createCell(i++).setCellValue(a);
        }

        Row r1 = sourceSheet.createRow(1);
        if (orderList!=null) {
            i = 0;
            for (int o : orderList) {
                r1.createCell(i++).setCellValue(o);
            }
        }

        Row r2 = sourceSheet.createRow(2);
        if (typeList!=null) {
            i = 0;
            for (String f : typeList) {
                r2.createCell(i++).setCellValue(f);
            }
        }

        Row r3 = sourceSheet.createRow(3);
        if (fieldNameList!=null) {
            i = 0;
            for (String f : fieldNameList) {
                r3.createCell(i++).setCellValue(f);
            }
        }

        if (values!=null) {
            int t = 4;
            for (List<?> v : values) {
                Row r = sourceSheet.createRow(t++);
                i = 0;
                for (Object o : v) {
                    if (o == null){
                        i++;
                        continue;
                    }
                    if (o.getClass() == Integer.class){
                        r.createCell(i++).setCellValue((Integer) o);
                        continue;
                    }
                    if (o.getClass() == String.class){
                        r.createCell(i++).setCellValue((String) o);
                        continue;
                    } else {
                        // skip
                        i++;
                    }
                }
            }
        }

    }

} 
