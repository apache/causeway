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

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.extensions.d3.applib.config.D3Config;

/**
 * Tests for {@link D3IntegrationReference}, in particular the escaping that makes
 * the (potentially attacker-influenced) graph JSON safe to embed verbatim inside an
 * inline {@code <script>} element.
 */
@DisplayName("D3IntegrationReference — inline-script generation & escaping")
class D3IntegrationReferenceTest {

    private static final String INIT_PREFIX = "WicketStuff.D3.init(";

    private static String scriptFor(final D3Config config) {
        return D3IntegrationReference.domReadyScript(config).getJavaScript().toString();
    }

    /** Extracts (and sanity-checks the wrapper of) the JSON argument of the init(...) call. */
    private static String jsonOf(final D3Config config) {
        var script = scriptFor(config);
        assertThat(script).startsWith(INIT_PREFIX).endsWith(")");
        return script.substring(INIT_PREFIX.length(), script.length() - 1);
    }

    @Test
    @DisplayName("wraps the config JSON in a WicketStuff.D3.init(...) call")
    void domReadyScript_wrapsJsonInInitCall() {
        var script = scriptFor(D3Config.defaults().withCanvasId("canvas-1"));

        assertThat(script).startsWith(INIT_PREFIX);
        assertThat(script).endsWith(")");
        assertThat(script).contains("\"canvasId\":\"canvas-1\"");
    }

    @Test
    @DisplayName("neutralises a </script> break-out attempt in the graph data")
    void domReadyScript_escapesScriptBreakout() {
        var script = scriptFor(D3Config.defaults()
                .withGraphData("</script><script>alert(1)</script>"));

        assertThat(script).doesNotContain("</script>");
        assertThat(script).doesNotContain("<script>");
        assertThat(script).contains("\\u003c").contains("\\u003e");
    }

    @ParameterizedTest(name = "escapes U+{1} to {2}")
    @MethodSource("escapingCases")
    @DisplayName("escapes characters that are unsafe inside an inline <script>")
    void domReadyScript_escapesUnsafeCharacters(final char raw, final String codePoint, final String escaped) {
        var script = scriptFor(D3Config.defaults().withGraphData("x" + raw + "y"));

        assertThat(script).contains(escaped);
        assertThat(script).doesNotContain(String.valueOf(raw));
    }

    static Stream<Arguments> escapingCases() {
        return Stream.of(
                Arguments.of('&', "0026", "\\u0026"),
                Arguments.of('<', "003C", "\\u003c"),
                Arguments.of('>', "003E", "\\u003e"),
                Arguments.of('\u2028', "2028", "\\u2028"),
                Arguments.of('\u2029', "2029", "\\u2029"));
    }

    @Test
    @DisplayName("escaping is semantically transparent (parsed graph data is unchanged)")
    void domReadyScript_escapingIsSemanticallyTransparent() {
        var graphData = "</script>&<>\u2028\u2029";

        var restored = JsonUtils.tryRead(D3Config.class, jsonOf(D3Config.defaults().withGraphData(graphData)))
                .getValue()
                .orElseThrow();

        assertThat(restored.graphData()).isEqualTo(graphData);
    }

    @Test
    @DisplayName("asHeaderItem() returns a header item")
    void asHeaderItem_isAvailable() {
        assertThat(D3IntegrationReference.asHeaderItem()).isNotNull();
    }
}
