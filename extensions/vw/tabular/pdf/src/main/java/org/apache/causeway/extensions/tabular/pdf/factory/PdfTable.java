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
package org.apache.causeway.extensions.tabular.pdf.factory;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.BaseTable;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.Cell;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.HorizontalAlignment;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.Row;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.Table;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.VerticalAlignment;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.line.LineStyle;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.utils.FontUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@SuppressWarnings("rawtypes")
class PdfTable {

    @Getter @Setter private Table table;
    private final List<String> primaryHeaderTexts;
    private final List<String> secondaryHeaderTexts;
    @Getter @Setter private List<Float> colWidths;

    private final Cell primaryHeaderTemplate;
    private final Cell secondaryHeaderTemplate;
    private final Cell evenTemplate;
    private final Cell oddTemplate;

    @SneakyThrows
    PdfTable(final Table table, final PDPage page, final List<Float> colWidths, final List<String> primaryHeaderTexts, final List<String> secondaryHeaderTexts) {
        this.table = table;
        this.primaryHeaderTexts = primaryHeaderTexts;
        this.secondaryHeaderTexts = secondaryHeaderTexts;
        this.colWidths = (colWidths.size() == 0) ? null : colWidths;
        // Create a dummy pdf document, page and table to create template cells
        PDDocument ddoc = new PDDocument();
        PDPage dpage = new PDPage();
        dpage.setMediaBox(page.getMediaBox());
        dpage.setRotation(page.getRotation());
        ddoc.addPage(dpage);
        BaseTable dummyTable = new BaseTable(10f, 10f, 10f, table.getWidth(), 10f, ddoc, dpage, false, false);
        Row dr = dummyTable.createRow(0f);
        this.primaryHeaderTemplate = dr.createCell(10f, "A", HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
        this.secondaryHeaderTemplate = dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
        this.evenTemplate = dr.createCell(10f, "A", HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
        this.oddTemplate = dr.createCell(10f, "A", HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);



        setDefaultStyles();
        ddoc.close();
    }

    @SuppressWarnings("unchecked")
    void appendRows(final List<List<Object>> rows) throws IOException {
        Map<Integer, Float> colWidths = new HashMap<>();
        int numcols = 0;

        { // header
            // calculate the width of the columns
            float totalWidth = 0.0f;
            if (this.colWidths == null) {
                List<String> rowData = this.primaryHeaderTexts;

                for (int i = 0; i < rowData.size(); i++) {
                    String cellValue = rowData.get(i);
                    float textWidth = FontUtils.getStringWidth(primaryHeaderTemplate.getFont(), " " + cellValue + " ",
                        primaryHeaderTemplate.getFontSize());
                    totalWidth += textWidth;
                    numcols = i;
                }
                // totalWidth has the total width we need to have all
                // columns
                // full sized.
                // calculate a factor to reduce/increase size by to make it
                // fit
                // in our table
                float sizefactor = table.getWidth() / totalWidth;
                for (int i = 0; i <= numcols; i++) {
                    String cellValue = "";
                    if (rowData.size() >= i) {
                        cellValue = rowData.get(i);
                    }
                    float textWidth = FontUtils.getStringWidth(primaryHeaderTemplate.getFont(), " " + cellValue + " ",
                        primaryHeaderTemplate.getFontSize());
                    float widthPct = textWidth * 100 / table.getWidth();
                    // apply width factor
                    widthPct = widthPct * sizefactor;
                    colWidths.put(i, widthPct);
                }
            } else {
                for (Float width : this.colWidths){
                    totalWidth += width;
                }
                for (int i = 0; i < this.colWidths.size(); i++) {
                    // to
                    // percent
                    colWidths.put(i,this.colWidths.get(i) / (totalWidth / 100));
                    numcols = i;
                }
            }

            // add primary header row
            {
                List<String> rowData = this.primaryHeaderTexts;
                Row h = table.createRow(primaryHeaderTemplate.getCellHeight());
                for (int i = 0; i <= numcols; i++) {
                    String cellValue = rowData.get(i);
                    Cell c = h.createCell(colWidths.get(i), cellValue);
                    // Apply style of header cell to this cell
                    c.copyCellStyle(primaryHeaderTemplate);
                    c.setText(cellValue);
                }
                table.addHeaderRow(h);
            }

            // add secondary header row
            if(!_NullSafe.isEmpty(this.secondaryHeaderTexts)) {
                List<String> rowData = this.secondaryHeaderTexts;
                Row h = table.createRow(secondaryHeaderTemplate.getCellHeight());
                for (int i = 0; i <= numcols; i++) {
                    String cellValue = rowData.get(i);
                    Cell c = h.createCell(colWidths.get(i), cellValue);
                    // Apply style of header cell to this cell
                    c.copyCellStyle(secondaryHeaderTemplate);
                    c.setText(cellValue);
                }
                table.addHeaderRow(h);
            }
        }

        int rowIndex = 0;
        for (List<Object> rowData : rows) {

            final Cell template = rowIndex%2 == 0
                ? evenTemplate
                : oddTemplate;

            var row = table.createRow(template.getCellHeight());

            var cellFactory = new CellFactory(row, template);

            for (int i = 0; i <= numcols; i++) {
                cellFactory.createCell(i, colWidths.get(i), rowData);
            }
            ++rowIndex;
        }
    }

    // -- HELPER

    private void setDefaultStyles() {
        LineStyle thinline = new LineStyle(Color.BLACK, 0.75f);

        primaryHeaderTemplate.setFillColor(new Color(137, 218, 245));
        primaryHeaderTemplate.setTextColor(Color.BLACK);
        primaryHeaderTemplate.setFont(FontFactory.helveticaBold());
        primaryHeaderTemplate.setBorderStyle(thinline);

        secondaryHeaderTemplate.setFillColor(Color.LIGHT_GRAY);
        secondaryHeaderTemplate.setTextColor(Color.BLACK);
        secondaryHeaderTemplate.setFont(FontFactory.helvetica());
        secondaryHeaderTemplate.setFontSize(7);
        secondaryHeaderTemplate.setBorderStyle(thinline);

        evenTemplate.setFillColor(new Color(242, 242, 242));
        evenTemplate.setTextColor(Color.BLACK);
        evenTemplate.setFont(FontFactory.helvetica());
        evenTemplate.setBorderStyle(thinline);

        oddTemplate.setFillColor(new Color(230, 230, 230));
        oddTemplate.setTextColor(Color.BLACK);
        oddTemplate.setFont(FontFactory.helvetica());
        oddTemplate.setBorderStyle(thinline);
    }

}
