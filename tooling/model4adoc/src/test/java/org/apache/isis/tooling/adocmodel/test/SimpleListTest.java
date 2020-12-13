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

import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.doc;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.list;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.listItem;

import lombok.val;

class SimpleListTest extends AbstractAsciiDocWriterTest {

    private Document doc;

    @BeforeEach
    void setUp() throws Exception {
        doc = doc();
        super.adocSourceResourceLocation = "list-simple.adoc";
        super.debugEnabled = false;
    }

    @Test
    void testList() throws IOException {
        
        val list = list(doc);
        list.setTitle("SimpleList");
        
        listItem(list, "Item-1");
        listItem(list, "Item-2");
        
        assertDocumentIsCorrectlyWritten(doc);
    }
    

}
