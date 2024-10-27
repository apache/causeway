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

class SourceTest extends AbstractAsciiDocWriterTest {

    private Document doc;

    @BeforeEach
    void setUp() throws Exception {
        doc = doc();
        super.adocSourceResourceLocation = "source.adoc";
        //super.debugEnabled = true;
        super.skipAsciidocjComplianceTest = true;
    }

    // = Source
    //
    // [source, java]
    // .Java Example
    // ----
    // class Hello {
    // }
    // ----
    @Test
    void testSourceBlock() throws IOException {
        
        doc.setTitle("Source");
        
        var sourceBlock = AsciiDocFactory.sourceBlock(doc, "java", "class Hello {\n}");
        
        sourceBlock.setTitle("Java Example");
        
        assertDocumentIsCorrectlyWritten(doc);
    }
    
    @Test
    void testSourceFactory() throws IOException {
        
        doc.setTitle("Source");
        
        AsciiDocFactory.SourceFactory
                .java(doc, "class Hello {\n}", "Java Example");
        
        assertDocumentIsCorrectlyWritten(doc);
    }
   

}
