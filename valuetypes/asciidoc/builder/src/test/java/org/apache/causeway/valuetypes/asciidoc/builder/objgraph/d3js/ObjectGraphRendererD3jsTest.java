package org.apache.causeway.valuetypes.asciidoc.builder.objgraph.d3js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.valuetypes.asciidoc.builder.objgraph.d3js.ObjectGraphRendererD3js.GraphRenderOptions;

class ObjectGraphRendererD3jsTest {

    @Test
    void canReadAssets() {
        final var sb = new StringBuilder();

        new ObjectGraphRendererD3js(GraphRenderOptions.builder().build())
            .renderProlog(sb);

        assertTrue(sb.toString().length()>100);
    }

}
