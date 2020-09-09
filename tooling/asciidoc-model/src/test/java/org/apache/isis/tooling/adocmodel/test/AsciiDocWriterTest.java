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
package org.apache.isis.tooling.adocmodel.test;

import java.io.IOException;
import java.util.HashMap;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.tooling._infra._Strings;
import org.apache.isis.tooling.adocmodel.AsciiDocWriter;

import static org.apache.isis.tooling.adocmodel.AsciiDocFactory.*;

import lombok.val;

class AsciiDocWriterTest {

    private Document doc;

    @BeforeEach
    void setUp() throws Exception {
        doc = doc();
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testDocTitle() throws IOException {
        
        doc.setTitle("Hello World");
        
        String actualAdoc = AsciiDocWriter.toString(doc); 
        String expectedAdoc = "= Hello World\n";
        
        System.out.println(actualAdoc);
        
        assertEquals(expectedAdoc, actualAdoc);
    }
    
    @Test
    void testSimpleTable() throws IOException {
        
        val adocRef = _Strings.readResource(this, "simple-table.adoc");
        
        val table = table(doc);
        table.setTitle("Table");
        
        headCell(table, 0, 0, "Col-1");
        headCell(table, 0, 1, "Col-2");
        headCell(table, 0, 2, "Col-3");
        
        cell(table, 0, 0, "1-1");
        cell(table, 0, 1, "1-2");
        cell(table, 0, 2, "1-3");
        
        cell(table, 1, 0, "2-1");
        cell(table, 1, 1, "2-2");
        cell(table, 1, 2, "2-3");
        
        String actualAdoc = AsciiDocWriter.toString(doc); 
        
        System.out.println(actualAdoc);
        
        assertEquals(adocRef, actualAdoc);
        
    }
    
    @Test
    void testAttributedTable() throws IOException {
        
        val adocRef = _Strings.readResource(this, "attributed-table.adoc");
        
        val table = table(doc);
        table.setTitle("Some table");
        table.setAttribute("cols", "3m,2a", true);
        table.setAttribute("header-option", "", true);
        
        headCell(table, 0, 0, "Col-1");
        headCell(table, 0, 1, "Col-2");
        
        cell(table, 0, 0, "1-1");
        cell(table, 0, 1, "1-2");
        
        String actualAdoc = AsciiDocWriter.toString(doc); 
        
        System.out.println(actualAdoc);
        
        assertEquals(adocRef, actualAdoc);
        
    }
    
    @Test @Disabled
    void testSimpleTableModel() throws IOException {
    
        val adocRef = _Strings.readResource(this, "simple-table.adoc");
        val asciidoctor = Asciidoctor.Factory.create();
        val refDoc = asciidoctor.load(adocRef, new HashMap<String, Object>());
        
        String actualAdoc = AsciiDocWriter.toString(refDoc);
        
        debug(refDoc);
        
        System.out.println(actualAdoc);
        
        assertEquals(adocRef, actualAdoc);
    }
    
    
    @Test
    void testAttributedTableModel() throws IOException {
    
        val adocRef = _Strings.readResource(this, "attributed-table.adoc");
        val asciidoctor = Asciidoctor.Factory.create();
        val refDoc = asciidoctor.load(adocRef, new HashMap<String, Object>());
        
        String actualAdoc = AsciiDocWriter.toString(refDoc);
        
        debug(refDoc);
        
        System.out.println(actualAdoc);
        
        assertEquals(adocRef, actualAdoc);
    }
    
    private static void debug(Document doc) {
        val refTable = (Table)doc.getBlocks().get(0);
        val refCol = refTable.getColumns().get(0);
        val refRow = refTable.getBody().get(0);
        val refCell = refRow.getCells().get(0);
        
        val refHead = refTable.getHeader().get(0);
        
        
        System.out.println("tab attrib: " + refTable.getAttributes());
        System.out.println("tab caption: " + refTable.getCaption());
        System.out.println("tab title: " + refTable.getTitle());
        
        System.out.println("col attrib: " + refCol.getAttributes());
        System.out.println("col context: " + refCol.getContext());
        System.out.println("col id: " + refCol.getId());
        System.out.println("col reftex: " + refCol.getReftext());
        System.out.println("col nodeName: " + refCol.getNodeName());
        System.out.println("col role: " + refCol.getRole());
        
        
        System.out.println("cell source: " + refCell.getSource());
        
        System.out.println("head source: " + refHead.getCells().get(0).getSource());
    }
    

}
