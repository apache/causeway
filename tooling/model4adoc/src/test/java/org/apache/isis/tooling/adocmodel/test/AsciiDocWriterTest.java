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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;
import org.apache.isis.tooling.model4adoc.AsciiDocWriter;

import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.attrNotice;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.block;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.cell;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.doc;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.headCell;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.list;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.listItem;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.table;

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
        
        // System.out.println(actualAdoc); // debug
        
        assertEquals(expectedAdoc, actualAdoc);
    }
    
    //= Sample
    //:Notice: my special license
    //
    //hi
    @Test
    void testDocHeader() throws IOException {
        
        doc.setTitle("Sample");
        
        attrNotice(doc, "my special license");
        
        block(doc).getLines().add("hi");
        
        String actualAdoc = AsciiDocWriter.toString(doc);
        
        _Text.assertTextEquals(
                _Text.readLinesFromResource(this.getClass(), "document-header.adoc", StandardCharsets.UTF_8), 
                actualAdoc);
    }
    
    @Test @Disabled
    void reverseTestDocHeader() throws IOException {
    
        val adocRef = _Strings.readFromResource(this.getClass(), "document-header.adoc", StandardCharsets.UTF_8);
        val asciidoctor = Asciidoctor.Factory.create();
        val refDoc = asciidoctor.load(adocRef, new HashMap<String, Object>());
        
        Debug.debug(refDoc);
        
        String actualAdoc = AsciiDocWriter.toString(refDoc);
        System.out.println(actualAdoc); //debug
        
        _Text.assertTextEquals(adocRef, actualAdoc);
    }
    
    @Test
    void testSimpleTable() throws IOException {
        
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
        
        //System.out.println(actualAdoc); //debug
        
        _Text.assertTextEquals(
                _Text.readLinesFromResource(this.getClass(), "table-simple.adoc", StandardCharsets.UTF_8), 
                actualAdoc);
    }
    
    @Test
    void testSimpleList() throws IOException {
        
        val list = list(doc);
        list.setTitle("SimpleList");
        
        listItem(list, "Item-1");
        listItem(list, "Item-2");
        
        String actualAdoc = AsciiDocWriter.toString(doc); 
        
        System.out.println(actualAdoc); //debug
        
        _Text.assertTextEquals(
                _Text.readLinesFromResource(this.getClass(), "list-simple.adoc", StandardCharsets.UTF_8), 
                actualAdoc);
    }
    
    @SuppressWarnings("unused")
    @Test
    void testNestedList() throws IOException {
        
        val list = list(doc);
        list.setTitle("NestedList");
        
        val item1 = listItem(list, "Item-1");
        val item2 = listItem(list, "Item-2");
        
        val list1 = list(item1);
        
        val item11 = listItem(list1, "Item-1-1");
        val item12 = listItem(list1, "Item-1-2");
        
        val list12 = list(item12);
        
        val item121 = listItem(list12, "Item-1-2-1");
        
        String actualAdoc = AsciiDocWriter.toString(doc); 
        
        System.out.println(actualAdoc); //debug
        
        _Text.assertTextEquals(
                _Text.readLinesFromResource(this.getClass(), "list-nested.adoc", StandardCharsets.UTF_8), 
                actualAdoc);
    }
    
    @Test @Disabled
    void reverseTestNestedList() throws IOException {
    
        val adocRef = _Strings.readFromResource(this.getClass(), "list-nested.adoc", StandardCharsets.UTF_8);
        val asciidoctor = Asciidoctor.Factory.create();
        val refDoc = asciidoctor.load(adocRef, new HashMap<String, Object>());
        
        String actualAdoc = AsciiDocWriter.toString(refDoc);
        
        debug((org.asciidoctor.ast.List)refDoc.getBlocks().get(0));
        
        System.out.println(actualAdoc); //debug
        
        _Text.assertTextEquals(adocRef, actualAdoc);
    }

    
    //* ListItem 1
    //+
    //--
    //Here's an example of a document title:
    //
    //----
    //= Document Title
    //----
    //
    //NOTE: The header is optional.
    //--
    //* ListItem 2
    //+
    //--
    //paragr 1 
    //
    //paragr 2
    //--
    @Test
    void testListWithOpenBlockContinuation() throws IOException {
        
        val list = list(doc);
        
        val item1 = listItem(list, "ListItem 1");
        val item2 = listItem(list, "ListItem 2");
        
        val openBlock1 = AsciiDocFactory.openBlock(item1);
        val openBlock2 = AsciiDocFactory.openBlock(item2);
        
        val block11 = AsciiDocFactory.block(openBlock1);
        val block12 = AsciiDocFactory.block(openBlock1);
        val block13 = AsciiDocFactory.block(openBlock1);
        
        block11.setSource("Here's an example of a document title:");
        block12.setSource("----\n= Document Title\n----");
        block13.setSource("NOTE: The header is optional.");
        
        val block21 = AsciiDocFactory.block(openBlock2);
        val block22 = AsciiDocFactory.block(openBlock2);
        
        block21.setSource("paragr 1");
        block22.setSource("paragr 2");
        
        String actualAdoc = AsciiDocWriter.toString(doc); 
        
        //System.out.println(actualAdoc); //debug
        
        _Text.assertTextEquals(
                _Text.readLinesFromResource(this.getClass(), "list-open-block-continuation.adoc", StandardCharsets.UTF_8), 
                actualAdoc);
    }
    
    
    
    @Test @Disabled
    void reverseTestListWithOpenBlockContinuation() throws IOException {
    
        val adocRef = _Strings.readFromResource(this.getClass(), "list-open-block-continuation.adoc", StandardCharsets.UTF_8);
        val asciidoctor = Asciidoctor.Factory.create();
        val refDoc = asciidoctor.load(adocRef, new HashMap<String, Object>());
        
        String actualAdoc = AsciiDocWriter.toString(refDoc);
        
        Debug.debug(refDoc);
        
        System.out.println(actualAdoc); //debug
        
        _Text.assertTextEquals(adocRef, actualAdoc);
    }


    @Test
    void testAttributedTable() throws IOException {
        
        val table = table(doc);
        table.setTitle("Some table");
        table.setAttribute("cols", "3m,2a", true);
        table.setAttribute("header-option", "", true);
        
        headCell(table, 0, 0, "Col-1");
        headCell(table, 0, 1, "Col-2");
        
        cell(table, 0, 0, "1-1");
        cell(table, 0, 1, "1-2");
        
        String actualAdoc = AsciiDocWriter.toString(doc); 
        
        System.out.println(actualAdoc); // debug
        
        _Text.assertTextEquals(
                _Text.readLinesFromResource(this.getClass(), "table-attributed.adoc", StandardCharsets.UTF_8), 
                actualAdoc);
        
    }
    
    @Test @Disabled
    void reverseTestSimpleTableModel() throws IOException {
    
        val adocRef = _Strings.readFromResource(this.getClass(), "table-simple.adoc", StandardCharsets.UTF_8);
        val asciidoctor = Asciidoctor.Factory.create();
        val refDoc = asciidoctor.load(adocRef, new HashMap<String, Object>());
        
        String actualAdoc = AsciiDocWriter.toString(refDoc);
        
        //debug((Table)refDoc.getBlocks().get(0));
        
        //System.out.println(actualAdoc); //debug
        
        _Text.assertTextEquals(adocRef, actualAdoc);
    }
    
    @Test
    void testAttributedTableModel() throws IOException {
    
        val adocRef = _Strings.readFromResource(this.getClass(), "table-attributed.adoc", StandardCharsets.UTF_8);
        val asciidoctor = Asciidoctor.Factory.create();
        val refDoc = asciidoctor.load(adocRef, new HashMap<String, Object>());
        
        String actualAdoc = AsciiDocWriter.toString(refDoc);
        
        //debug(refDoc);
        
        //System.out.println(actualAdoc); // debug
        
        _Text.assertTextEquals(adocRef, actualAdoc);
    }
    
    @SuppressWarnings("unused")
    private static void debug(Table refTable) {
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
    
    @SuppressWarnings("unused")
    private static void debug(org.asciidoctor.ast.List refList) {
        System.out.println("list blocks: " + refList.getBlocks());
        System.out.println("list items: " + refList.getItems());
        System.out.println("list level: " + refList.getLevel());
        for(val item0 : refList.getItems()) {
            val item = (ListItem) item0;
            System.out.println("\t *");
            System.out.println("\t item level: " + item.getLevel());
            System.out.println("\t item class: " + item.getClass());
            System.out.println("\t item caption: " + item.getCaption());
            System.out.println("\t item nodename: " + item.getNodeName());
            System.out.println("\t item source: " + item.getSource());
            System.out.println("\t item blocks: " + item.getBlocks());
            _NullSafe.stream(item.getBlocks())
            .forEach(block->debug((org.asciidoctor.ast.List)block));
        }
        
    }
    

}
