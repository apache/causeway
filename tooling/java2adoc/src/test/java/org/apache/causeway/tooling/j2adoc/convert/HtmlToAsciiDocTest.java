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
package org.apache.causeway.tooling.j2adoc.convert;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.tooling.model4adoc.AsciiDocFactory;
import org.apache.causeway.tooling.model4adoc.AsciiDocWriter;

import lombok.val;

class HtmlToAsciiDocTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void simplifiedParagraphSyntax() {

        val doc = AsciiDocFactory.doc();

        // when
        val descriptionAsHtml = Jsoup.parse("Hello<p>second paragraph<p>third paragraph");
        val descriptionAdoc = HtmlToAsciiDoc.body(descriptionAsHtml.selectFirst("body"));

        val blocks = descriptionAdoc.getBlocks();
        doc.getBlocks().addAll(blocks);

        // then we expect 3 paragraphs delimited by an empty line
        final String adocAsString = AsciiDocWriter.toString(doc);
        val lines = _Text.getLines(adocAsString).map(String::trim);

        assertEquals(Can.of("Hello", "", "second paragraph", "", "third paragraph", ""), lines);
    }

}
