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

import org.apache.isis.tooling.model4adoc.AsciiDocFactory;

import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.block;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.doc;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.list;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.listItem;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.openBlock;

import lombok.val;

class OpenBlockTest extends AbstractAsciiDocWriterTest {

    private Document doc;

    @BeforeEach
    void setUp() throws Exception {
        doc = doc();
        super.adocSourceResourceLocation = "list-open-block-continuation.adoc";
        super.debugEnabled = false;
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
    @SuppressWarnings("unused")
    @Test
    void testOpenBlock() throws IOException {
        
        val list = list(doc);
        
        val item1 = listItem(list, "ListItem 1");
        val item2 = listItem(list, "ListItem 2");
        
        val openBlock1 = openBlock(item1);
        val openBlock2 = openBlock(item2);
        
        val block11 = block(openBlock1, "Here's an example of a document title:");
        val block12 = AsciiDocFactory.listingBlock(openBlock1, "= Document Title");
        val block13 = block(openBlock1, "NOTE: The header is optional.");
        
        val block21 = block(openBlock2, "paragr 1");
        val block22 = block(openBlock2, "paragr 2");
        
        assertDocumentIsCorrectlyWritten(doc);
    }
    

}
