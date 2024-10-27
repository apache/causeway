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
package org.apache.causeway.valuetypes.asciidoc.builder.test.ast;

import java.io.IOException;

import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory;

import static org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory.block;
import static org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory.doc;
import static org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory.list;
import static org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory.listItem;
import static org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory.openBlock;

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
        
        var list = list(doc);
        
        var item1 = listItem(list, "ListItem 1");
        var item2 = listItem(list, "ListItem 2");
        
        var openBlock1 = openBlock(item1);
        var openBlock2 = openBlock(item2);
        
        var block11 = block(openBlock1, "Here's an example of a document title:");
        var block12 = AsciiDocFactory.listingBlock(openBlock1, "= Document Title");
        var block13 = block(openBlock1, "NOTE: The header is optional.");
        
        var block21 = block(openBlock2, "paragr 1");
        var block22 = block(openBlock2, "paragr 2");
        
        assertDocumentIsCorrectlyWritten(doc);
    }
    
}
