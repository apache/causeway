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
package org.apache.causeway.extensions.d3.wkt.integration.res;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.apache.causeway.extensions.d3.applib.config.D3Config;

/**
 * Tests for {@link D3IntegrationReference}, in particular the escaping that makes
 * the (potentially attacker-influenced) graph JSON safe to embed verbatim inside an
 * inline {@code <script>} element.
 */
class D3IntegrationReferenceTest {

    private static String scriptFor(final D3Config config) {
        return D3IntegrationReference.domReadyScript(config).getJavaScript().toString();
    }

    @Test
    void domReadyScript_wrapsJsonInInitCall() {
        var script = scriptFor(D3Config.defaults().withCanvasId("canvas-1"));

        assertThat(script).startsWith("WicketStuff.D3.init(");
        assertThat(script).endsWith(")");
        assertThat(script).contains("\"canvasId\":\"canvas-1\"");
    }

    @Test
    void domReadyScript_escapesScriptBreakout() {
        // a graph value containing </script> must not be able to close the inline <script> element
        var script = scriptFor(D3Config.defaults()
                .withGraphData("</script><script>alert(1)</script>"));

        assertThat(script).doesNotContain("</script>");
        assertThat(script).doesNotContain("<script>");
        assertThat(script).contains("\\u003c").contains("\\u003e");
    }

    @Test
    void domReadyScript_escapesAmpersandAndAngleBrackets() {
        var script = scriptFor(D3Config.defaults().withGraphData("a & b < c > d"));

        assertThat(script).contains("\\u0026").contains("\\u003c").contains("\\u003e");
        assertThat(script).doesNotContain("&").doesNotContain("<").doesNotContain(">");
    }

    @Test
    void domReadyScript_escapesLineSeparators() {
        var script = scriptFor(D3Config.defaults().withGraphData("one\u2028two\u2029three"));

        assertThat(script).contains("\\u2028").contains("\\u2029");
    }

    @Test
    void asHeaderItem_isAvailable() {
        assertThat(D3IntegrationReference.asHeaderItem()).isNotNull();
    }
}
