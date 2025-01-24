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

import org.apache.causeway.commons.tabular.TabularModel;
import org.apache.causeway.extensions.tabular.pdf.factory.PdfFactory;

record PdfFileWriter(PdfFactory pdfFactory) {

    public void write(TabularModel tabularModel, File tempFile) {

        //TODO[causeway-extensions-tabular-pdf-CAUSEWAY-3854] yet just a hello world (WIP)
        
        try(PdfFactory pdf = new PdfFactory(PdfFactory.Options.a4Portrait().build())) {
            
            var rows = new ArrayList<List<Object>>();
                
            rows.add(List.of("row1-col1", "row1-col2", "row1-col3"));
            rows.add(List.of("row2-col1", "row2-col2", "row2-col3"));
            
            pdf.drawDataTable(List.of("Img", "Id", "Name"), List.of(), rows);
    
            pdf.writeToFile(tempFile);
        }
        
    }
    
}
