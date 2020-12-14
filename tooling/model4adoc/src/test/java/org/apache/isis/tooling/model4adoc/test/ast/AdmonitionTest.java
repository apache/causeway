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

import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.doc;

import lombok.val;

class AdmonitionTest extends AbstractAsciiDocWriterTest {

    private Document doc;

    @BeforeEach
    void setUp() throws Exception {
        doc = doc();
        super.adocSourceResourceLocation = "admonition.adoc";
        super.debugEnabled = false;
    }

    //[NOTE]
    //====
    //the note is multiple paragraphs, and can have all the usual styling
    //
    //also note
    //====
    //
    //TIP: Here's something worth knowing...
    @Test
    void testAdmonition() throws IOException {
        
        val note = AsciiDocFactory.note(doc);
        AsciiDocFactory.block(note, "the note is multiple paragraphs, and can have all the usual styling");
        AsciiDocFactory.block(note, "also note");
        
        AsciiDocFactory.tip(doc, "Here's something worth knowing...");
        
        assertDocumentIsCorrectlyWritten(doc);
    }
    

}
