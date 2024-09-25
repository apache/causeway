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
package org.apache.causeway.commons.graph;

import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.springframework.util.StringUtils;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel.GraphCharacteristic;
import org.apache.causeway.commons.graph.GraphUtils.NodeFormatter;
import org.apache.causeway.commons.internal.primitives._Longs;
import org.apache.causeway.commons.io.TextUtils;

import lombok.Value;

class GraphUtilsTest {

    @Value
    static class Customer {
       String name;
    }

    @Test
    void builderDirected() {
        var gBuilder = GraphUtils.GraphBuilder.directed(Customer.class);
        gBuilder
            .addNode(new Customer("A"))
            .addNode(new Customer("B"))
            .addNode(new Customer("C"))
            .addNode(new Customer("D"))
            .addEdge(0, 1)
            .addEdge(1, 2)
            .addEdge(2, 0);

        var graph = gBuilder.build();
        var textForm = graph.toString(Customer::getName);

        //debug
        //System.err.println(textForm);

        assertEquals(
                Can.of("A -> B", "B -> C", "C -> A", "D"),
                TextUtils.readLines(textForm).filter(StringUtils::hasLength));
    }

    @Test
    void builderUndirected() {
        var gBuilder = GraphUtils.GraphBuilder.undirected(Customer.class);
        gBuilder
            .addNode(new Customer("A"))
            .addNode(new Customer("B"))
            .addNode(new Customer("C"))
            .addNode(new Customer("D"))
            .addEdge(0, 1)
            .addEdge(1, 2)
            .addEdge(2, 0);

        var graph = gBuilder.build();
        var textForm = graph.toString(Customer::getName);

        //debug
        //System.err.println(textForm);

        assertEquals(
                Can.of("A - B", "A - C", "B - C", "D"),
                TextUtils.readLines(textForm).filter(StringUtils::hasLength));
    }

    @Test
    void builderWeighted() {
        var gBuilder = GraphUtils.GraphBuilder.undirected(Customer.class);
        gBuilder
            .addNode(new Customer("A"))
            .addNode(new Customer("B"))
            .addNode(new Customer("C"))
            .addNode(new Customer("D"))
            .addEdge(0, 1)
            .addEdge(1, 2)
            .addEdge(2, 0);

        var weights = new TreeMap<Long, Double>();

        weights.put(_Longs.pack(0, 1), 0.1);
        weights.put(_Longs.pack(1, 2), 0.7);
        weights.put(_Longs.pack(0, 2), 0.3);

        var graph = gBuilder.build();
        var textForm = graph.toString(NodeFormatter.of(Customer::getName), (i, a, j, b, nf)->
            String.format("%s - %s (weight=%.1f)", nf.format(i, a), nf.format(j, b), weights.get(_Longs.pack(i, j))));

        //debug
        System.err.println(textForm);

        assertEquals(
                Can.of("A - B (weight=0.1)", "A - C (weight=0.3)", "B - C (weight=0.7)", "D"),
                TextUtils.readLines(textForm).filter(StringUtils::hasLength));
    }

    @Test
    void subgraph() {
        var graph = new GraphKernel(4, ImmutableEnumSet.noneOf(GraphCharacteristic.class));
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);

        assertFalse(graph.isUndirected());
        assertEquals(4, graph.nodeCount());
        assertEquals(3, graph.edgeCount());

        // identity
        var subgraphId = graph.subGraph(new int[] {0, 1, 2, 3});
        assertFalse(subgraphId.isUndirected());
        assertEquals(4, subgraphId.nodeCount());
        assertEquals(3, subgraphId.edgeCount());

        // disjoint
        var subgraphDisjointNoEdges = graph.subGraph(new int[] {0, 3});
        assertFalse(subgraphDisjointNoEdges.isUndirected());
        assertEquals(2, subgraphDisjointNoEdges.nodeCount());
        assertEquals(0, subgraphDisjointNoEdges.edgeCount());

        // subgraph w/ 3 nodes, reordered
        var subgraph3 = graph.subGraph(new int[] {3, 1, 2});
        assertFalse(subgraph3.isUndirected());
        assertEquals(3, subgraph3.nodeCount());
        assertEquals(2, subgraph3.edgeCount());
    }

}
