package org.apache.isis.extensions.excel.dom.util;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.extensions.excel.dom.util.PivotUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class PivotUtilsTest {

    @Test
    public void createAnnotationRowTest() {

        // given
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        Row r = sheet.createRow(0);

        List<String> l = Arrays.asList("a", "b", "c");

        // when
        PivotUtils.createAnnotationRow(r,l);

        // then
        Assertions.assertThat(r.getCell(0).getStringCellValue()).isEqualTo("a");
        Assertions.assertThat(r.getCell(1).getStringCellValue()).isEqualTo("b");
        Assertions.assertThat(r.getCell(2).getStringCellValue()).isEqualTo("c");

    }

    @Test
    public void createOrderRowTest() {

        // given
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        Row r = sheet.createRow(1);

        List<Integer> l = Arrays.asList(1, 0, 2);

        // when
        PivotUtils.createOrderRow(r,l);

        // then
        Assertions.assertThat(r.getCell(0).getNumericCellValue()).isEqualTo(1);
        Assertions.assertThat(r.getCell(1).getNumericCellValue()).isEqualTo(0);
        Assertions.assertThat(r.getCell(2).getNumericCellValue()).isEqualTo(2);

    }

    @Test
    public void cellValueEqualsTest() {

        // given
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        Row r = sheet.createRow(0);
        Cell c1 = r.createCell(0);
        Cell c2 = r.createCell(1);

        // when numeric (double)
        c1.setCellValue(1.000000000000001);
        c2.setCellValue(1);

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(false);

        // when (limits of double comparison)
        c1.setCellValue(1.0000000000000001);
        c2.setCellValue(1);

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(true);

        // when
        c1.setCellValue(1);
        c2.setCellValue(0.9999999999999999);

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(false);

        // when (limits of double comparison)
        c1.setCellValue(1);
        c2.setCellValue(0.99999999999999999);

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(true);

        // when (empty cells or cells with values not set, can't compare)
        c1 = r.createCell(0);
        c2 = r.createCell(1);

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(true);

        // when (type not equal)
        c1 = r.createCell(0);
        c1.setCellType(CellType.BLANK);
        c2 = r.createCell(1);
        c2.setCellType(CellType.BOOLEAN);

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(false);

        // when string
        c1 = r.createCell(0);
        c2 = r.createCell(1);
        c1.setCellValue("a");
        c2.setCellValue("a");

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(true);

        // when case sensitive
        c1.setCellValue("a");
        c2.setCellValue("A");

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(false);

        // when empty string
        c1.setCellValue("");
        c2.setCellValue("");

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(true);

        // when empty string and empty cell
        c1 = r.createCell(0);
        c2 = r.createCell(1);
        c1.setCellValue("");

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(false);

        // when string and numeric
        c1 = r.createCell(0);
        c2 = r.createCell(1);
        c1.setCellValue("a");
        c2.setCellType(CellType.NUMERIC);

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(false);

        // when string and numeric
        c1 = r.createCell(0);
        c2 = r.createCell(1);
        c1.setCellValue("a");
        c2.setCellValue(0);

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(false);

        // when boolean
        c1.setCellValue(true);
        c2.setCellValue(true);

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(true);

        // when
        c1.setCellValue(false);
        c2.setCellValue(true);

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(false);

        // when 0 (false) 1 (true) value in boolean cell supported
        c1.setCellValue(true);
        c2.setCellValue(1);
        c2.setCellType(CellType.BOOLEAN);

        // then
        Assertions.assertThat(PivotUtils.cellValueEquals(c1,c2)).isEqualTo(true);

    }

    @Test
    public void addCellValueToTest(){

        // given
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        Row r = sheet.createRow(0);

        // when source null
        Cell cSource = null;
        Cell cTarget = r.createCell(1);
        cTarget.setCellValue(1);

        PivotUtils.addCellValueTo(cSource, cTarget);

        // then
        Assertions.assertThat(cTarget.getNumericCellValue()).isEqualTo(1);

        // when numeric
        cSource = r.createCell(0);
        cTarget = r.createCell(1);
        cSource.setCellValue(1);
        cTarget.setCellValue(2);

        PivotUtils.addCellValueTo(cSource, cTarget);

        // then
        Assertions.assertThat(cTarget.getNumericCellValue()).isEqualTo(3);

        // when mixed: adding ignored
        cSource.setCellValue(1);
        cTarget.setCellValue("b");

        PivotUtils.addCellValueTo(cSource, cTarget);

        // then
        Assertions.assertThat(cTarget.getStringCellValue()).isEqualTo("b");

        // when boolean: adding ignored
        cSource.setCellValue(true);
        cTarget.setCellValue(false);

        PivotUtils.addCellValueTo(cSource, cTarget);

        // then
        Assertions.assertThat(cTarget.getBooleanCellValue()).isEqualTo(false);

    }

    public void copyCellTest() {

        // given
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        Row r = sheet.createRow(0);
        Cell cTarget = r.createCell(0);
        cTarget.setCellValue(1);

        // given source null, do nothing
        Cell cSource = null;

        // when
        PivotUtils.copyCell(cSource, cTarget);

        // then
        Assertions.assertThat(cTarget.getNumericCellValue()).isEqualTo(1);

        // given source has no value
        cSource = r.createCell(1);

        // when
        PivotUtils.copyCell(cSource, cTarget);

        // then
        Assertions.assertThat(cTarget).isEqualTo(cSource);
        Assertions.assertThat(cTarget.getCellType()).isEqualTo(CellType.BLANK);

        // given source has a value
        cSource = r.createCell(1);
        cSource.setCellValue("a");

        // when
        PivotUtils.copyCell(cSource, cTarget);

        // then
        Assertions.assertThat(cTarget).isEqualTo(cSource);
        Assertions.assertThat(cTarget.getStringCellValue()).isEqualTo("a");

        // given styles
        CellStyle styleSource = workbook.createCellStyle();
        cSource.setCellStyle(styleSource);
        CellStyle styleTarget = workbook.createCellStyle();
        cTarget.setCellStyle(styleTarget);

        // when
        PivotUtils.copyCell(cSource, cTarget);

        // then style is written over
        Assertions.assertThat(cTarget.getCellStyle()).isEqualTo(cSource.getCellStyle());
        Assertions.assertThat(cTarget.getCellStyle()).isEqualTo(styleSource);

    }

}
