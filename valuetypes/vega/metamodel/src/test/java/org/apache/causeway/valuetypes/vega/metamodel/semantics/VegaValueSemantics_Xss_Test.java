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
package org.apache.causeway.valuetypes.vega.metamodel.semantics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.causeway.valuetypes.vega.applib.value.Vega;

/**
 * Proves the XSS issue of CAUSEWAY-4079 for the {@link Vega} value type:
 * the Vega JSON payload is inlined verbatim into a {@code <script>} element,
 * so a JSON string containing {@code </script>} breaks out of the script
 * context (a JavaScript-context injection, not an HTML-context one).
 *
 * <p>These tests are expected to FAIL until the JSON is hardened for the
 * JavaScript context.
 */
class VegaValueSemantics_Xss_Test {

    private final VegaValueSemantics semantics = new VegaValueSemantics();

    @Test
    void script_end_tag_in_json_does_not_break_out_of_script_element() {
        var json = "{\"$schema\":\"https://vega.github.io/schema/vega/v6.json\","
                + "\"data\":[{\"values\":[{\"x\":\"</script><script>alert('XSS')</script>\"}]}]}";
        var vega = new Vega(Vega.Schema.VEGA, json);

        var html = semantics.htmlPresentation(null, vega);

        assertFalse(html.contains("</script><script>"),
                () -> "expected no </script><script> breakout, but HTML was:\n" + html);
    }

    @Test
    void vega_lite_script_end_tag_does_not_break_out_of_script_element() {
        var json = "{\"$schema\":\"https://vega.github.io/schema/vega-lite/v6.json\","
                + "\"data\":{\"values\":[{\"x\":\"</script><script>alert('XSS')</script>\"}]}}";
        var vega = new Vega(Vega.Schema.VEGA_LITE, json);

        var html = semantics.htmlPresentation(null, vega);

        assertFalse(html.contains("</script><script>"),
                () -> "expected no </script><script> breakout, but HTML was:\n" + html);
    }

    @Test
    void chart_is_still_produced() {
        var json = "{\"$schema\":\"https://vega.github.io/schema/vega/v6.json\","
                + "\"data\":[{\"values\":[]}]}";
        var vega = new Vega(Vega.Schema.VEGA, json);

        var html = semantics.htmlPresentation(null, vega);

        assertTrue(html.contains("var spec ="),
                () -> "expected a vega spec to be embedded, but HTML was:\n" + html);
    }

}
