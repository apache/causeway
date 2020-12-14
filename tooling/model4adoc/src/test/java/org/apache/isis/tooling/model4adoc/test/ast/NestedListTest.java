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
package org.apache.isis.tooling.model4adoc.test.ast;

import java.io.IOException;

import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.doc;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.list;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.listItem;

import lombok.val;

class NestedListTest extends AbstractAsciiDocWriterTest {

    private Document doc;

    @BeforeEach
    void setUp() throws Exception {
        doc = doc();
        super.adocSourceResourceLocation = "list-nested.adoc";
        super.debugEnabled = false;
    }

    @SuppressWarnings("unused")
    @Test
    void testList() throws IOException {
        
        val list = list(doc);
        list.setTitle("NestedList");
        
        val item1 = listItem(list, "Item-1");
        val item2 = listItem(list, "Item-2");
        
        val list1 = list(item1);
        
        val item11 = listItem(list1, "Item-1-1");
        val item12 = listItem(list1, "Item-1-2");
        
        val list12 = list(item12);
        
        val item121 = listItem(list12, "Item-1-2-1");
        
        assertDocumentIsCorrectlyWritten(doc);
    }
    

}
