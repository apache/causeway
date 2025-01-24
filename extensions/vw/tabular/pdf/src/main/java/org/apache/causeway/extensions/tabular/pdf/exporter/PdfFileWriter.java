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
package org.apache.causeway.extensions.tabular.pdf.exporter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.commons.tabular.TabularModel;
import org.apache.causeway.commons.tabular.TabularModel.TabularColumn;
import org.apache.causeway.commons.tabular.TabularModel.TabularSheet;
import org.apache.causeway.extensions.tabular.pdf.factory.PdfFactory;

record PdfFileWriter(PdfFactory pdfFactory) {

    public void write(TabularModel tabular, File tempFile) {
        try(PdfFactory pdf = new PdfFactory(PdfFactory.Options.a4Portrait().build())) {
            tabular.sheets().forEach(sheet->writeSheet(pdf, sheet));
            pdf.writeToFile(tempFile);
        }
    }
    
    // -- HELPER

    private void writeSheet(PdfFactory pdf, TabularSheet tabularSheet) {

        var rows = new ArrayList<List<Object>>();
        
        var sheetName = tabularSheet.sheetName();
        pdf.drawHeader(sheetName);
        
        var dataColumns = tabularSheet.columns();

        // primary header row
        var primaryHeadRow = dataColumns.stream()
            .map(TabularColumn::columnName)
            .toList();
        var secondaryHeadRow = dataColumns.stream()
            .map(TabularColumn::columnDescription)
            .toList();
        
        // detail rows
        for (var dataRow : tabularSheet.rows()) {
            var row = new ArrayList<Object>(dataColumns.size());
            for(var column : dataColumns) {
                var tabularCell = dataRow.getCell(column);
                tabularCell.eitherValueOrLabelSupplier().accept(row::add, labels->{
                    row.add(labels.get().collect(Collectors.joining("\n")));
                });
            }
            rows.add(row);
        }
        
        pdf.drawTable(List.of(), primaryHeadRow, secondaryHeadRow, rows);
    }
    
}
