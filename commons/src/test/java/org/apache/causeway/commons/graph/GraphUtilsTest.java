package org.apache.causeway.commons.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel.GraphCharacteristic;

class GraphUtilsTest {

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
