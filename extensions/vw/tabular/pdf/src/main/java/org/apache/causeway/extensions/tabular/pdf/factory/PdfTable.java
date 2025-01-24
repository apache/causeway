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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import lombok.Getter;
import lombok.Setter;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.Table;
import be.quodlibet.boxable.VerticalAlignment;
import be.quodlibet.boxable.image.Image;
import be.quodlibet.boxable.line.LineStyle;
import be.quodlibet.boxable.utils.FontUtils;

@SuppressWarnings("rawtypes")
class PdfTable {
    
    @Getter @Setter private Table table;
    private final List<String> headerTexts;
    @Getter @Setter private List<Float> colWidths;
    @Getter private final Cell headerCellTemplate;
    private final List<Cell> dataCellTemplateEvenList = new ArrayList<>();
    private final List<Cell> dataCellTemplateOddList = new ArrayList<>();
    private final Cell defaultCellTemplate;
    private boolean copyFirstColumnCellTemplateOddToEven = false;
    private boolean copyLastColumnCellTemplateOddToEven = false;

    PdfTable(Table table, PDPage page, List<String> headerTexts, List<Float> colWidths) throws IOException {
        this.table = table;
        this.headerTexts = headerTexts;
        this.colWidths = (colWidths.size() == 0) ? null : colWidths;
        // Create a dummy pdf document, page and table to create template cells
        PDDocument ddoc = new PDDocument();
        PDPage dpage = new PDPage();
        dpage.setMediaBox(page.getMediaBox());
        dpage.setRotation(page.getRotation());
        ddoc.addPage(dpage);
        BaseTable dummyTable = new BaseTable(10f, 10f, 10f, table.getWidth(), 10f, ddoc, dpage, false, false);
        Row dr = dummyTable.createRow(0f);
        headerCellTemplate = dr.createCell(10f, "A", HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
        if (this.colWidths == null) {
            dataCellTemplateEvenList.add(dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
            dataCellTemplateOddList.add(dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
            dataCellTemplateEvenList.add(dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
            dataCellTemplateOddList.add(dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
            dataCellTemplateEvenList.add(dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
            dataCellTemplateOddList.add(dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
        } else {
            for (int i = 0 ; i < this.colWidths.size(); i++) {
                dataCellTemplateEvenList.add(dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
                dataCellTemplateOddList.add(dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
            }
        }
        defaultCellTemplate = dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
        setDefaultStyles();
        ddoc.close();
    }

    @SuppressWarnings("unchecked")
    void appendRows(List<List<Object>> rows) throws IOException {
        boolean odd = true;
        Map<Integer, Float> colWidths = new HashMap<>();
        int numcols = 0;
        
        { // header
            List row = this.headerTexts;
            // calculate the width of the columns
            float totalWidth = 0.0f;
            if (this.colWidths == null) {
                
                for (int i = 0; i < row.size(); i++) {
                    String cellValue = (String)row.get(i);
                    float textWidth = FontUtils.getStringWidth(headerCellTemplate.getFont(), " " + cellValue + " ",
                            headerCellTemplate.getFontSize());
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
                    if (row.size() >= i) {
                        cellValue = (String)row.get(i);
                    }
                    float textWidth = FontUtils.getStringWidth(headerCellTemplate.getFont(), " " + cellValue + " ",
                            headerCellTemplate.getFontSize());
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
            updateTemplateList(row.size());
            
            // Add Header Row
            Row h = table.createRow(headerCellTemplate.getCellHeight());
            for (int i = 0; i <= numcols; i++) {
                String cellValue = (String)row.get(i);
                Cell c = h.createCell(colWidths.get(i), cellValue, headerCellTemplate.getAlign(),
                        headerCellTemplate.getValign());
                // Apply style of header cell to this cell
                c.copyCellStyle(headerCellTemplate);
                c.setText(cellValue);
            }
            table.addHeaderRow(h);
        }
        
        for (List row : rows) {
            Row r = table.createRow(dataCellTemplateEvenList.get(0).getCellHeight());
            for (int i = 0; i <= numcols; i++) {
                // Choose the correct template for the cell
                Cell template = dataCellTemplateEvenList.get(i);
                if (odd) {
                    template = dataCellTemplateOddList.get(i);;
                }
                
                Object cellValue = null;
                if (row.size() >= i) {
                    cellValue = row.get(i);
                    if (cellValue instanceof String s) {
                        cellValue = s.replaceAll("\n", "<br>");    
                    }
                }
                if(cellValue==null) cellValue = "";
                
                Cell c = switch(cellValue.getClass().getSimpleName()) {
                    case "String" -> r.createCell(colWidths.get(i), (String)cellValue, template.getAlign(), template.getValign());
                    case "BufferedImage" -> r.createImageCell(colWidths.get(i), new Image((BufferedImage)cellValue));
                    default -> throw new IllegalArgumentException("Unsupported value type: " + cellValue.getClass().getName());  
                };
                
                // Apply style of header cell to this cell
                c.copyCellStyle(template);
            }
            odd = !odd;
        }
    }

    // -- HELPER

    private void setDefaultStyles() {
        LineStyle thinline = new LineStyle(Color.BLACK, 0.75f);
        // Header style
        headerCellTemplate.setFillColor(new Color(137, 218, 245));
        headerCellTemplate.setTextColor(Color.BLACK);
        headerCellTemplate.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD));
        headerCellTemplate.setBorderStyle(thinline);

        // Normal cell style, all rows and columns are the same by default
        defaultCellTemplate.setFillColor(new Color(242, 242, 242));
        defaultCellTemplate.setTextColor(Color.BLACK);
        defaultCellTemplate.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA));
        defaultCellTemplate.setBorderStyle(thinline);
        Iterator<Cell> iterator = dataCellTemplateEvenList.iterator();
        while (iterator.hasNext()){
            iterator.next().copyCellStyle(defaultCellTemplate);
        }
        iterator = dataCellTemplateOddList.iterator();
        while (iterator.hasNext()){
            iterator.next().copyCellStyle(defaultCellTemplate);
        }
    }
    
    private void updateTemplateList(int size) {
        if (copyFirstColumnCellTemplateOddToEven)
            dataCellTemplateEvenList.set(0, dataCellTemplateOddList.get(0));
        if (copyLastColumnCellTemplateOddToEven)
            dataCellTemplateEvenList.set(dataCellTemplateEvenList.size() - 1,
                dataCellTemplateOddList.get(dataCellTemplateOddList.size() - 1));
        if (size <= 3)
            return; // Only in case of more than 3 columns there are first last and data template
        while (dataCellTemplateEvenList.size() < size)
            dataCellTemplateEvenList.add(1, dataCellTemplateEvenList.get(1));
        while (dataCellTemplateOddList.size() < size)
            dataCellTemplateOddList.add(1, dataCellTemplateOddList.get(1));
        while (dataCellTemplateEvenList.size() > size)
            dataCellTemplateEvenList.remove(dataCellTemplateEvenList.size() - 2);
        while (dataCellTemplateOddList.size() > size)
            dataCellTemplateOddList.remove(dataCellTemplateOddList.size() - 2);
        
    }
}
