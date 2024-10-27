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

import static org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory.doc;

class SectionsTest extends AbstractAsciiDocWriterTest {

    private Document doc;

    @BeforeEach
    void setUp() throws Exception {
        doc = doc();
        super.adocSourceResourceLocation = "sections.adoc";
        super.debugEnabled = false;
    }

    /*
    = Example Document

    Text of the first block.

    Text of the second block.

    == Section 1

    Text of the first block under section 1.

    == Section 2

    Text of the first block under section 2.

    === Sub Section 2.1

    Text of the first block under section 2.1.

    == Section 3

    Text of the first block under section 3.
     */
    @Test
    void testSimpleExample() throws IOException {

        doc.setTitle("Example Document");

        AsciiDocFactory.block(doc, "Text of the first block.");
        AsciiDocFactory.block(doc, "Text of the second block.");

        var section1 = AsciiDocFactory.section(doc, "Section 1");
        AsciiDocFactory.block(section1, "Text of the first block under section 1.");

        var section2 = AsciiDocFactory.section(doc, "Section 2");
        AsciiDocFactory.block(section2, "Text of the first block under section 2.");

        var section21 = AsciiDocFactory.section(section2, "Sub Section 2.1");
        AsciiDocFactory.block(section21, "Text of the first block under section 2.1.");

        var section3 = AsciiDocFactory.section(doc, "Section 3");
        AsciiDocFactory.block(section3, "Text of the first block under section 3.");

        assertDocumentIsCorrectlyWritten(doc);
    }

}
