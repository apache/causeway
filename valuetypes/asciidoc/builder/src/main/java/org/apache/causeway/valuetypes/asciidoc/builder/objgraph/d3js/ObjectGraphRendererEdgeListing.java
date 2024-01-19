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

import java.util.stream.Collectors;

import org.apache.causeway.applib.services.metamodel.objgraph.ObjectGraph;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.functional.IndexedConsumer;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ObjectGraphRendererEdgeListing implements ObjectGraph.Renderer {

    @Override
    public void render(final StringBuilder sb, final ObjectGraph objGraph) {

        /*
         * List of {@code int[]},
         * where each array contains to the zero based indexes of weakly connected graph nodes (objects).
         */
        var listOfWeaklyConnectedNodes = objGraph.kernel(ImmutableEnumSet.of(GraphKernel.GraphCharacteristic.UNDIRECTED))
            .findWeaklyConnectedNodes();

        var listOfWeaklyConnectedEdges = listOfWeaklyConnectedNodes.stream()
            .map(connectedObjectIndexes->renderConnectedSubGraph(objGraph.subGraph(connectedObjectIndexes)))
            .filter(_Strings::isNotEmpty)
            .collect(Collectors.joining(",\n"));
        sb.append(listOfWeaklyConnectedEdges);
    }

    private String renderConnectedSubGraph(final ObjectGraph subGraph) {
        final int maxRelIndex = subGraph.relations().size() - 1;
        if(maxRelIndex<0) return null;

        var sb = new StringBuilder();
        sb.append("edges = {");

        subGraph.relations().forEach(IndexedConsumer.zeroBased((i, rel)->{
            var fromId = rel.from().name();
            var toId = rel.to().name();
            sb.append(String.format("%s->%s", fromId, toId));
            if(i<maxRelIndex) sb.append(",");
            if(i%8==7) sb.append("\n");
        }));

        sb.append("}");

        return sb.toString();
    }

}