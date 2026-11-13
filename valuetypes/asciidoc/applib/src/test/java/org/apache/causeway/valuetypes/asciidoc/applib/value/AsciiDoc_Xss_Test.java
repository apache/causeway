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
package org.apache.causeway.valuetypes.asciidoc.applib.value;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.causeway.valuetypes.asciidoc.applib.CausewayModuleValAsciidocApplib;

/**
 * Proves the XSS issue of CAUSEWAY-4079 for the {@link AsciiDoc} value type:
 * AsciiDoc passthrough content is converted to HTML and - via the Wicket
 * {@code MarkupComponent} - reaches the browser as raw, unescaped HTML.
 *
 * <p>These tests are expected to FAIL until the rendered AsciiDoc HTML is sanitized.
 */
class AsciiDoc_Xss_Test {

    @BeforeAll
    static void initConverter() {
        // mirrors CausewayModuleValAsciidocApplib.createAdocToHtmlConverter(...)
        var asciidoctor = Asciidoctor.Factory.create();
        new CausewayModuleValAsciidocApplib.AdocToHtmlConverter(asciidoctor,
                Options.builder()
                        .safe(SafeMode.UNSAFE)
                        .toFile(false)
                        .build());
    }

    @Test
    void passthrough_block_containing_script_is_not_rendered_raw() {
        var adoc = """
                ++++
                <script>alert('XSS')</script>
                ++++""";

        var html = AsciiDoc.valueOf(adoc).asHtml();

        assertFalse(html.contains("<script>"),
                () -> "expected <script> to be stripped, but HTML was:\n" + html);
    }

    @Test
    void inline_passthrough_containing_onerror_is_not_rendered_raw() {
        var adoc = "pass:[<img src=x onerror=alert('XSS')>]";

        var html = AsciiDoc.valueOf(adoc).asHtml();

        assertFalse(html.contains("onerror"),
                () -> "expected onerror to be stripped, but HTML was:\n" + html);
    }

    @Test
    void benign_markup_is_preserved() {
        var html = AsciiDoc.valueOf("Hello *world*").asHtml();

        assertTrue(html.contains("<strong>world</strong>"),
                () -> "expected basic formatting to survive sanitization, but HTML was:\n" + html);
    }

}
