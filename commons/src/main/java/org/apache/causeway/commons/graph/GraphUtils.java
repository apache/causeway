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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.functional.IndexedConsumer;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel.GraphCharacteristic;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.collections._PrimitiveCollections.IntList;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

/**
 * Early draft.
 */
@UtilityClass
public class GraphUtils {

    // -- FACTORIES

    public <T> GraphKernel kernelForAdjacency(final Can<T> nodes, final BiPredicate<T, T> adjaciency) {
        var kernel = new GraphKernel(nodes.size(), ImmutableEnumSet.noneOf(GraphCharacteristic.class));
        final int m = nodes.size()-1;
        final int n = nodes.size();
        for (int i = 0; i < m; i++) {
            var a = nodes.getElseFail(i);
            for (int j = i; j < n; j++) {
                var b = nodes.getElseFail(j);
                if(adjaciency.test(a, b)) {
                    kernel.addEdge(i, j);
                }
                if(adjaciency.test(b, a)) {
                    kernel.addEdge(j, i);
                }
            }
        }
        return kernel;
    }

    // -- GRAPH KERNEL

    /** no multi edge support */
    public final static class GraphKernel {

        public enum GraphCharacteristic {
            UNDIRECTED
            //NO_LOOPS,
            //NO_MULTI_EDGES,
            //WEIGHTED,
            ;

            // -- FACTORIES

            public static ImmutableEnumSet<GraphCharacteristic> directed() {
                return ImmutableEnumSet.noneOf(GraphCharacteristic.class);
            }
        }

        private final ImmutableEnumSet<GraphCharacteristic> characteristics;
        @Getter @Accessors(fluent=true)
        private final int nodeCount;
        private final List<IntList> adjacencyList;
        public GraphKernel(
                final int nodeCount,
                final @NonNull ImmutableEnumSet<GraphCharacteristic> characteristics) {
            this.characteristics = characteristics;
            this.nodeCount = nodeCount;
            this.adjacencyList = new ArrayList<>(nodeCount);
            for (int i = 0; i < nodeCount; i++) {
                adjacencyList.add(new IntList());
            }
        }
        public boolean isUndirected() {
            return characteristics.contains(GraphCharacteristic.UNDIRECTED);
        }
        public int edgeCount() {
            return adjacencyList.stream().mapToInt(IntList::size).sum();
        }
        public void addEdge(final int u, final int v) {
            boundsCheck(u);
            boundsCheck(v);
            adjacencyList.get(u).addUnique(v);
            if(isUndirected()) {
                adjacencyList.get(v).addUnique(u);
            }
        }
        public IntStream streamNeighbors(final int nodeIndex) {
            boundsCheck(nodeIndex);
            return adjacencyList.get(nodeIndex).stream();
        }
        public GraphKernel copy() {
            var copy = new GraphKernel(nodeCount, characteristics);
            for (int u = 0; u < nodeCount; u++) {
                for (int v : adjacencyList.get(u)) {
                    copy.addEdge(u, v);
                }
            }
            return copy;
        }
        public GraphKernel toUndirected() {
            var copy = new GraphKernel(nodeCount, characteristics.add(GraphCharacteristic.UNDIRECTED));
            for (int u = 0; u < nodeCount; u++) {
                for (int v : adjacencyList.get(u)) {
                    copy.addEdge(u, v);
                    copy.addEdge(v, u);
                }
            }
            return copy;
        }
        /**
         * Returns a sub-graph comprised only of nodes as picked per (zero based) indexes {@code int[]}.
         * @apiNote assumes nodeIndexes are unique and valid
         */
        public GraphKernel subGraph(final @Nullable int[] nodeIndexes) {
            var pickedSubset = new IntList(nodeIndexes);
            final int subsize = pickedSubset.size();
            var sub = new GraphKernel(subsize, characteristics);
            for (int i = 0; i < subsize; i++) {
                final int fromIndex = i;
                for (int v : adjacencyList.get(nodeIndexes[i])) {
                    pickedSubset.indexOf(v)
                        .ifPresent(toIndex->sub.addEdge(fromIndex, toIndex));
                }
            }
            return sub;
        }
        /**
         * Returns a list of {@code int[]},
         * where each list entry contains the zero based indexes of weakly connected graph nodes.
         */
        public List<int[]> findWeaklyConnectedNodes() {
            var undirectedGraph = this.isUndirected()
                    ? this
                    : this.toUndirected();
            var adjacencyList = new WeaklyConnectedNodesFinder().find(undirectedGraph);
            return adjacencyList.stream()
                .filter(IntList::isNotEmpty)
                .map(IntList::toArray)
                .collect(Collectors.toList());
        }

        // -- HELPER

        private void boundsCheck(final int nodeIndex) {
            if(nodeIndex<0
                    || nodeIndex>=nodeCount) {
                throw new IndexOutOfBoundsException(nodeIndex);
            }
        }

        private class WeaklyConnectedNodesFinder {
            List<IntList> find(final GraphKernel undirectedGraph) {
                _Assert.assertTrue(undirectedGraph.isUndirected());
                var connectedComponents = new ArrayList<IntList>();
                final boolean[] isVisited = new boolean[undirectedGraph.nodeCount()];
                for (int i = 0; i < undirectedGraph.nodeCount(); i++) {
                    if (!isVisited[i]) {
                        var component = new IntList();
                        findConnectedComponent(i, isVisited, component, undirectedGraph);
                        connectedComponents.add(component);
                    }
                }
                return connectedComponents;
            }
            // finds a connected component starting from source using DFS
            private void findConnectedComponent(
                    final int src,
                    final boolean[] isVisited,
                    final IntList component,
                    final GraphKernel undirectedGraph) {
                isVisited[src] = true;
                component.add(src);
                for (int v : undirectedGraph.adjacencyList.get(src)) {
                    if (!isVisited[v]) {
                        findConnectedComponent(v, isVisited, component, undirectedGraph);
                    }
                }
            }
        }

    }

    // -- GRAPH

    /**
     * Represents a graph, that is, nodes and edges between nodes,
     * as reflected by the {@link Graph}'s kernel ({@link GraphKernel}).
     *
     * @param <T> graph's node type
     * @since 2.1, 3.1
     */
    @lombok.Value @Accessors(fluent=true)
    public class Graph<T> {
        private final GraphKernel kernel;
        private final Can<T> nodes;

        // -- TRAVERSAL

        public void visitNeighbors(final int nodeIndex, final Consumer<T> nodeVisitor) {
            kernel()
                .streamNeighbors(nodeIndex)
                .forEach(neighborIndex->
                    nodeVisitor.accept(nodes.getElseFail(neighborIndex)));
        }

        public void visitNeighborsIndexed(final int nodeIndex, final IndexedConsumer<T> nodeVisitor) {
            kernel()
                .streamNeighbors(nodeIndex)
                .forEach(neighborIndex->
                    nodeVisitor.accept(neighborIndex, nodes.getElseFail(neighborIndex)));
        }

//        public Stream<T> streamNeighbors(final int nodeIndex) {
//            return kernel()
//                .streamNeighbors(nodeIndex)
//                .mapToObj(neighborIndex->nodes.getElseFail(neighborIndex));
//        }
//
//        public record IndexedNode<T>(
//                int index,
//                T node){
//        }
//
//        public Stream<IndexedNode<T>> streamNeighborsIndexed(final int nodeIndex) {
//            return kernel()
//                .streamNeighbors(nodeIndex)
//                .mapToObj(neighborIndex->new IndexedNode<>(neighborIndex, nodes.getElseFail(neighborIndex)));
//        }

        // -- TRANSFORMATION

        /**
         * Returns an isomorphic graph with this graph's nodes replaced by given mapping function.
         */
        public <R> Graph<R> map(final Function<T, R> nodeMapper) {
            return new Graph<R>(kernel, nodes.map(nodeMapper));
        }

        @Override
        public String toString() {
            return toString(Object::toString);
        }

        public String toString(final Function<T, String> nodeFormatter) {
            var sb = new StringBuilder();
            var neighbourCount = _Refs.intRef(0);
            for(int nodeIndex = 0; nodeIndex < kernel.nodeCount(); ++nodeIndex) {
                var a = nodes().getElseFail(nodeIndex);
                neighbourCount.setValue(0);
                visitNeighbors(nodeIndex, b->{
                    sb
                        .append(String.format("%s -> %s", nodeFormatter.apply(a), nodeFormatter.apply(b)))
                        .append("\n");
                    neighbourCount.incAndGet();
                });
                if(neighbourCount.getValue()==0) {
                    sb
                        .append(String.format("%s", nodeFormatter.apply(a)))
                        .append("\n");
                }
            }
            return sb.toString();
        }

    }

    /**
     * Builder for a {@link Graph}.
     * @param <T> graph's node type
     * @since 2.1, 3.1
     * @implNote not thread-safe, in other words: should to be used by a single thread only
     */
    public class GraphBuilder<T> {

        @SuppressWarnings("unused")
        private final Class<T> nodeType;
        private final ImmutableEnumSet<GraphCharacteristic> characteristics;
        private final List<T> nodeList;
        private final IntList fromNode = new IntList(4); // best guess initial edge capacity
        private final IntList toNode = new IntList(4); // best guess initial edge capacity

        // -- FACTORIES

        public static <T> GraphBuilder<T> directed(final Class<T> nodeType) {
            return new GraphBuilder<T>(nodeType, GraphCharacteristic.directed());
        }

        /**
         * Adds a new node to the graph.
         * @apiNote nodes are not required to be unique with respect to {@link Objects#equals}.
         */
        public GraphBuilder<T> addNode(final @NonNull T node) {
            nodeList.add(node);
            return this;
        }

        /**
         * Adds a new edge to the graph. Indices are zero-based references to the node list.
         * @apiNote Indices are not bound checked until the {@link #build()} method is called.
         */
        public GraphBuilder<T> addEdge(final int fromIndex, final int toIndex) {
            // no bound check here, but later when the kernel is built
            fromNode.add(fromIndex);
            toNode.add(toIndex);
            return this;
        }

        /**
         * Current node count. It increments with each node added.
         */
        public int nodeCount() {
            return nodeList.size();
        }

        /**
         * Current edge count. It increments with each edge added.
         */
        public int edgeCount() {
            return fromNode.size();
        }

        // -- CONSTRUCTION

        private GraphBuilder(final Class<T> nodeType, final ImmutableEnumSet<GraphCharacteristic> characteristics) {
            this.nodeType = nodeType;
            this.characteristics = characteristics;
            this.nodeList = new ArrayList<>();
        }

        public Graph<T> build() {
            var kernel = new GraphKernel(nodeList.size(), characteristics);
            var edgeCount = edgeCount();
            for (int edgeIndex = 0; edgeIndex<edgeCount; edgeIndex++) {
                kernel.addEdge(fromNode.get(edgeIndex), toNode.get(edgeIndex));
            }
            var graph = new Graph<T>(kernel, Can.ofCollection(nodeList));
            return graph;
        }

    }

}
