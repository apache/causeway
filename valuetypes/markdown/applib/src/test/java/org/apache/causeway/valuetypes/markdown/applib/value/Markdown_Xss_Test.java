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
package org.apache.causeway.valuetypes.markdown.applib.value;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Proves the XSS issue of CAUSEWAY-4079 for the {@link Markdown} value type:
 * Markdown raw HTML passes through the flexmark renderer and - via the Wicket
 * {@code MarkupComponent} - reaches the browser as raw, unescaped HTML.
 *
 * <p>These tests are expected to FAIL until raw HTML is suppressed/sanitized.
 */
class Markdown_Xss_Test {

    @Test
    void raw_script_is_not_rendered() {
        var html = Markdown.valueOf("<script>alert('XSS')</script>").asHtml();

        assertFalse(html.contains("<script>"),
                () -> "expected <script> to be stripped, but HTML was:\n" + html);
    }

    @Test
    void raw_html_block_with_event_handler_is_not_rendered() {
        // a block-level HTML block is passed through by flexmark (defaults)
        var html = Markdown.valueOf("<div onclick=\"alert('XSS')\">click</div>").asHtml();

        assertFalse(html.contains("onclick"),
                () -> "expected onclick to be stripped, but HTML was:\n" + html);
    }

    @Test
    void benign_formatting_is_preserved() {
        var html = Markdown.valueOf("Hello **world**").asHtml();

        assertTrue(html.contains("<strong>world</strong>"),
                () -> "expected basic formatting to survive, but HTML was:\n" + html);
    }

}
