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

class CalloutTest extends AbstractAsciiDocWriterTest {

    private Document doc;

    @BeforeEach
    void setUp() throws Exception {
        doc = doc();
        super.adocSourceResourceLocation = "callout.adoc";
        super.debugEnabled = false;
    }


    //<.> fn-1
    //+
    //--
    //[WARNING]
    //====
    //warn-1
    //
    //warn-2
    //====
    //
    //para-1
    //
    //para-2
    //
    //* li-1
    //* li-2
    //
    //para-3
    //--
    @Test
    void testFootnote() throws IOException {

        val callouts = AsciiDocFactory.callouts(doc);
        val calloutLI = AsciiDocFactory.callout(callouts, "fn-1");
        val callout = AsciiDocFactory.openBlock(calloutLI);


        val note = AsciiDocFactory.warning(callout);
        AsciiDocFactory.block(note, "warn-1");
        AsciiDocFactory.block(note, "warn-2");

        AsciiDocFactory.block(callout, "para-1");
        AsciiDocFactory.block(callout, "para-2");

        val nestedList = AsciiDocFactory.list(callout);
        AsciiDocFactory.listItem(nestedList, "li-1");
        AsciiDocFactory.listItem(nestedList, "li-2");

        AsciiDocFactory.block(callout, "para-3");

        AsciiDocFactory.tip(doc, "Here's something worth knowing...");

        assertDocumentIsCorrectlyWritten(doc);
    }



}

