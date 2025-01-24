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

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.io.DataSink;

import lombok.Builder;
import lombok.SneakyThrows;

import be.quodlibet.boxable.BaseTable;

public class PdfFactory implements AutoCloseable {
    
    @Builder
    public record Options(
        PDRectangle pdRectangle) {
        public static OptionsBuilder a4Portrait() {
            return Options.builder()
                .pdRectangle(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
        }
        public static OptionsBuilder a4Landscape() {
            return Options.builder()
                .pdRectangle(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
        }
    }
    
    final Options options;
    final PDDocument document;
    final PDPage page;
    final float margin;
    final float bottomMargin;
    final float tablesmargin;
    final float tableWidth;
    final float yStartNewPage;
    
    float yStart;
    
    public PdfFactory(Options options) {
        this.options = options;
        this.document = new PDDocument();
        this.page = new PDPage();
        this.margin = 10;
        this.bottomMargin = 20;
        this.tablesmargin = 50;
        page.setMediaBox(options.pdRectangle());
        document.addPage(page);
        this.tableWidth = page.getMediaBox().getWidth() - (2 * margin);
        this.yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
        this.yStart = yStartNewPage;
    }
    
    public PDDocument document() { return document; } 
    
    @SneakyThrows
    @Override
    public void close() {
        document.close();
    }
    
    @SneakyThrows
    public void drawDataTable(
            List<String> headerTexts,
            List<Float> colWidths, 
            List<List<Object>> rowData) {
        var baseTable = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, 
            document, page, true, true);
        var pdfTable = new PdfTable(baseTable, page, headerTexts, colWidths);
        pdfTable.appendRows(rowData);
        yStart = baseTable.draw() - tablesmargin;
    }
    
    @SneakyThrows
    public void writeToFile(File file) {
        document.save(file);
    }
    
    public Blob toBlob(String name) {
        var result = new AtomicReference<Blob>();
        
        DataSink.ofByteArrayConsumer(bytes->
                result.set(Blob.of(name, CommonMimeType.PDF, bytes)))
            .writeAll(document::save);
        
        return result.get();
    }
}