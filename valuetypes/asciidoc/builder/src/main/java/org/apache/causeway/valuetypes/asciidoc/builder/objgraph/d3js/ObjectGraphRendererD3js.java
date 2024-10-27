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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.applib.services.metamodel.objgraph.ObjectGraph;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.commons.io.TextUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

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
            /** Governs node color. */
            String group;
            /** Shown as tooltip. */
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

        var d3jsGraph = new D3jsGraph(new ArrayList<>(), new ArrayList<>());

        var objectLookup = new HashMap<ObjectGraph.Object, Integer>();

        objGraph.objects().forEach(obj->{
            var counter = objectLookup.size();
            d3jsGraph.nodes.add(new D3jsGraph.Node(counter,
                    obj.name(),
                    obj.packageName(), // node group -> auto color (max 20 colors)
                    asTooltip(obj)));
            objectLookup.put(obj, counter);
        });

        objGraph.relations().forEach(rel->{
            var source = objectLookup.get(rel.from());
            var target = objectLookup.get(rel.to());
            d3jsGraph.links.add(new D3jsGraph.Link(source, target, rel.descriptionFormatted()));
        });

        renderSvg(sb, d3jsGraph);
    }

    private String asTooltip(final ObjectGraph.Object obj) {
        var sb = new StringBuilder();
        sb.append(obj.packageName()).append('.').append(obj.name());
        obj.fields().forEach(field->{
            sb.append("\n").append(" * ").append(field.name()).append(": ");
            sb.append(field.isPlural()
                    ? String.format("[%s]", field.elementTypeShortName())
                    : field.elementTypeShortName());
        });
        return sb.toString();
    }

    protected void renderSvg(final StringBuilder sb, final D3jsGraph d3jsGraph) {

        var noteText = "Note: Dragging nodes leaves them sticky. "
                + "Double-click releases them. "
                + "Single-click toggles node highlight (incoming edges are emphasized). ";

        sb.append("<div class=\"svg-container\">\n");
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" class=\"force-directed-graph\">\n");
        sb.append("<script>\n");
        sb.append("var d3jsGraph =\n");
        sb.append(JsonUtils.toStringUtf8(d3jsGraph)).append("\n");
        sb.append(String.format("renderForceDirectedGraph(d3jsGraph, \"%s\");\n", noteText));
        sb.append("</script>\n");
        sb.append("</svg>\n");
        sb.append("</div>\n");
    }

    protected void renderProlog(final StringBuilder sb) {
        sb.append("<script src=\"https://d3js.org/d3.v4.min.js\"></script>\n");

        // -- CSS

        sb.append("<style>\n");
        sb.append(readAsset("force-directed-graph.css"));
        sb.append("\n</style>\n");

        // -- JS

        sb.append("<script>\n");
        sb.append("var ropts = ")
            .append(graphRenderOptions.toJavaScript())
            .append(";\n");
        sb.append(readAsset("svg-checkbox.js"));
        sb.append(readAsset("force-directed-graph.js"));
        sb.append("\n</script>\n");
    }

    /** skips 18 license header lines and any single line comments as well as empty lines */
    @SneakyThrows
    private String readAsset(
            final @NonNull String resourceName) {
        return TextUtils.readLinesFromResource(
                ObjectGraphRendererD3js.class, "assets/" + resourceName, StandardCharsets.UTF_8)
                .filter(_Strings::isNotEmpty)
                .filter(line->!line.trim().startsWith("//"))
                .stream()
                .skip(18) // skip license header
                .collect(Collectors.joining("\n"));
    }

}