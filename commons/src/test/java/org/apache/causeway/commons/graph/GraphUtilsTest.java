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

import java.util.Comparator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import org.springframework.util.StringUtils;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel.GraphCharacteristic;
import org.apache.causeway.commons.graph.GraphUtils.NodeFormatter;
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
    void nodeEqualityDirected() {
        var graph = GraphUtils.GraphBuilder.directed(Customer.class)
            .addNode(new Customer("A"))
            .addNode(new Customer("B"))
            .addNode(new Customer("A"))
            .addEdge(0, 1)
            .build();

        //debug
        //System.err.println(graph.toString(Customer::getName));

        assertEquals(2,
                graph.nodes().size());
    }

    @Test
    void nodeEqualityUndirected() {
        var graph = GraphUtils.GraphBuilder.undirected(Customer.class)
            .addNode(new Customer("A"))
            .addNode(new Customer("B"))
            .addNode(new Customer("A"))
            .addEdge(0, 1)
            .build();

        //debug
        //System.err.println(graph.toString(Customer::getName));

        assertEquals(2,
                graph.nodes().size());
    }

    @Test
    void builderWithEdgeAttributes() {
        var gBuilder = GraphUtils.GraphBuilder.undirected(Customer.class);
        gBuilder
            .addNode(new Customer("A"))
            .addNode(new Customer("B"))
            .addNode(new Customer("C"))
            .addNode(new Customer("D"))
            .addEdge(0, 1, 0.1)
            .addEdge(1, 2, 0.7)
            .addEdge(2, 0);

        var graph = gBuilder.build();
        var textForm = graph.toString(
                NodeFormatter.of(Customer::getName),
                edgeAttr->String.format("(weight=%.1f)", (double)edgeAttr));
        //debug
        //System.err.println(textForm);

        assertEquals(
                Can.of("A - B (weight=0.1)", "A - C", "B - C (weight=0.7)", "D"),
                TextUtils.readLines(textForm).filter(StringUtils::hasLength));
    }

    @Test
    void builderWithAdvancedEdgeAdding() {
        var a = new Customer("A");
        var b = new Customer("B");
        var c = new Customer("C");
        var d = new Customer("D");

        var gBuilder = GraphUtils.GraphBuilder.undirected(Customer.class);
        gBuilder
            .addNode(d)
            .addEdge(a, b, 0.1)
            .addEdge(c, a)
            .addEdge(c, b, 0.7);

        var graph = gBuilder
                .build()
                .sorted(Comparator.comparing(Customer::getName)); // test sorting
        var textForm = graph.toString(
                NodeFormatter.of(Customer::getName),
                edgeAttr->String.format("(weight=%.1f)", (double)edgeAttr));
        //debug
        //System.err.println(textForm);

        assertEquals(
                Can.of("A - B (weight=0.1)", "A - C", "B - C (weight=0.7)", "D"),
                TextUtils.readLines(textForm).filter(StringUtils::hasLength));
    }

    @Test
    void kernelSubgraph() {
        var kernel = new GraphKernel(4, ImmutableEnumSet.noneOf(GraphCharacteristic.class));
        kernel.addEdge(0, 1);
        kernel.addEdge(1, 2);
        kernel.addEdge(2, 3);

        assertFalse(kernel.isUndirected());
        assertEquals(4, kernel.nodeCount());
        assertEquals(3, kernel.edgeCount());

        // identity
        var subgraphId = kernel.subGraph(new int[] {0, 1, 2, 3});
        assertFalse(subgraphId.isUndirected());
        assertEquals(4, subgraphId.nodeCount());
        assertEquals(3, subgraphId.edgeCount());

        // disjoint
        var subgraphDisjointNoEdges = kernel.subGraph(new int[] {0, 3});
        assertFalse(subgraphDisjointNoEdges.isUndirected());
        assertEquals(2, subgraphDisjointNoEdges.nodeCount());
        assertEquals(0, subgraphDisjointNoEdges.edgeCount());

        // subgraph w/ 3 nodes, reordered
        var subgraph3 = kernel.subGraph(new int[] {3, 1, 2});
        assertFalse(subgraph3.isUndirected());
        assertEquals(3, subgraph3.nodeCount());
        assertEquals(2, subgraph3.edgeCount());
    }

    @Test
    void filterDirectedGraph() {
        var graph = GraphUtils.GraphBuilder.directed(Customer.class)
            .addNode(new Customer("A"))
            .addNode(new Customer("B"))
            .addNode(new Customer("C"))
            .addNode(new Customer("D"))
            .addEdge(0, 1, 0.1) // A -> B (weight=0.1)
            .addEdge(1, 2)      // B -> C
            .addEdge(2, 0, 0.7) // C -> A (weight=0.7)
            .build()
            .filter(node->!node.getName().equals("C")); // now remove C

        var textForm = graph.toString(Customer::getName);

        //debug
        //System.err.println(textForm);

        assertLinesMatch(
                Can.of("A -> B (0.1)", "B", "D").toList(),
                TextUtils.readLines(textForm).filter(StringUtils::hasLength).toList());
    }

    @Test
    void filterUndirectedGraph() {
        var a = new Customer("A");
        var b = new Customer("B");
        var c = new Customer("C");
        var d = new Customer("D");

        var graph = GraphUtils.GraphBuilder.undirected(Customer.class)
            .addEdge(a, b, 0.1) // A - B (weight=0.1)
            .addEdge(c, a)      // A - C
            .addEdge(c, b, 0.7) // B - C (weight=0.7)
            .addNode(d)
            .build()
            .filter(node->!node.getName().equals("C")) // now remove C
            ;

        var textForm = graph.toString(
                NodeFormatter.of(Customer::getName),
                edgeAttr->String.format("(weight=%.1f)", (double)edgeAttr));
        //debug
        //System.err.println(textForm);

        assertLinesMatch(
                Can.of("A - B (weight=0.1)", "D").toList(),
                TextUtils.readLines(textForm).filter(StringUtils::hasLength).toList());
    }

}
