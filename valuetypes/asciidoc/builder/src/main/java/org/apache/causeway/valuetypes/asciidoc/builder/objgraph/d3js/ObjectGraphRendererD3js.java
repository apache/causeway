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
package org.apache.causeway.valuetypes.asciidoc.builder.objgraph.d3js;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.causeway.applib.services.metamodel.ObjectGraph;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.commons.io.JsonUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ObjectGraphRendererD3js implements ObjectGraph.Renderer {

    @Builder
    public static class GraphRenderOptions {
        @Builder.Default @Getter private final double scale = 1.0;
        @Builder.Default @Getter private final int node_radius = 5;
        @Builder.Default @Getter private final int arrow_length = 8;
        @Builder.Default @Getter private final int arrow_breadth = 5;
        @Builder.Default @Getter private final Boolean enable_nodelabels = true;
        @Builder.Default @Getter private final Boolean enable_edgelabels = true;
        @Builder.Default @Getter private final Boolean enable_edgearrows = true;
        @Builder.Default @Getter private final Boolean enable_stickyNodeDrag = true;
        String toJavaScript() {
            return JsonUtils.toStringUtf8(this).replace('"', ' ');
        }
    }

    /**
     * Graph model used as input to accompanied force-directed-graph JavaScript.
     */
    @lombok.Value
    private static class D3jsGraph {
        @lombok.Value
        public static class Node {
            int id;
            String label;
            String group; // future use
            String description;
        }
        @lombok.Value
        public static class Link {
            int source;
            int target;
            String label;
            float weight = 1.f;
        }
        private final List<Node> nodes;
        private final List<Link> links;
    }

    private final GraphRenderOptions graphRenderOptions;

    @Override
    public void render(final StringBuilder sb, final ObjectGraph objGraph) {

        renderProlog(sb);

        val d3jsGraph = new D3jsGraph(new ArrayList<>(), new ArrayList<>());

//debug
//        d3jsGraph.nodes.add(new D3jsGraph.Node(1, "A", "an A"));
//        d3jsGraph.nodes.add(new D3jsGraph.Node(2, "B", "a B"));
//        d3jsGraph.nodes.add(new D3jsGraph.Node(3, "C", "a C"));
//
//        d3jsGraph.links.add(new D3jsGraph.Link(1, 2, "E1"));
//        d3jsGraph.links.add(new D3jsGraph.Link(2, 3, "E2"));
//        d3jsGraph.links.add(new D3jsGraph.Link(3, 1, "E3"));


        val objectLookup = new HashMap<ObjectGraph.Object, Integer>();

        objGraph.objects().forEach(obj->{
            val counter = objectLookup.size();
            d3jsGraph.nodes.add(new D3jsGraph.Node(counter, obj.name(), obj.packageName(), obj.packageName()));
            objectLookup.put(obj, counter);
        });

        objGraph.relations().forEach(rel->{
            val source = objectLookup.get(rel.from());
            val target = objectLookup.get(rel.to());
            d3jsGraph.links.add(new D3jsGraph.Link(source, target, rel.labelFormatted()));
        });

        renderSvg(sb, d3jsGraph);
    }

    protected void renderSvg(final StringBuilder sb, final D3jsGraph d3jsGraph) {
        sb.append("<div class=\"svg-container\">\n");
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" class=\"force-directed-graph\">\n");
        sb.append("<script>\n");
        sb.append("var d3jsGraph =\n");
        sb.append(JsonUtils.toStringUtf8(d3jsGraph)).append("\n");
        sb.append("renderForceDirectedGraph(d3jsGraph);\n");
        sb.append("</script>\n");
        sb.append("</svg>\n");
        sb.append("</div>\n");
    }

    protected void renderProlog(final StringBuilder sb) {
        sb.append("<script src=\"https://d3js.org/d3.v4.min.js\"></script>\n");

        // -- CSS

        sb.append("<style>\n");
        sb.append(DataSource.ofResource(ObjectGraphRendererD3js.class, "g-style.css")
                .tryReadAsStringUtf8()
                .valueAsNonNullElseFail());
        sb.append("\n</style>\n");

        // -- JS

        sb.append("<script>\n");
        sb.append("var ropts = ")
            .append(graphRenderOptions.toJavaScript())
            .append(";\n");
        sb.append(DataSource.ofResource(ObjectGraphRendererD3js.class, "svgCheckBox.js")
                .tryReadAsStringUtf8()
                .valueAsNonNullElseFail());
        sb.append(DataSource.ofResource(ObjectGraphRendererD3js.class, "force-directed-graph-1.0.js")
                .tryReadAsStringUtf8()
                .valueAsNonNullElseFail());
        sb.append("\n</script>\n");
    }

}


