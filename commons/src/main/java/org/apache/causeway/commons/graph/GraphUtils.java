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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.functional.IndexedConsumer;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel.GraphCharacteristic;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._PrimitiveCollections.IntList;
import org.apache.causeway.commons.internal.primitives._Longs;

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

            public static ImmutableEnumSet<GraphCharacteristic> undirected() {
                return ImmutableEnumSet.of(GraphCharacteristic.UNDIRECTED);
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
        public int neighborCount(final int nodeIndex) {
            return isWithinBounds(nodeIndex)
                    ? adjacencyList.get(nodeIndex).size()
                    : 0;
        }
        public void addEdge(final int u, final int v) {
            assertBounds(u);
            assertBounds(v);
            adjacencyList.get(u).addUnique(v);
            if(isUndirected()) {
                adjacencyList.get(v).addUnique(u);
            }
        }
        public IntStream streamNeighbors(final int nodeIndex) {
            assertBounds(nodeIndex);
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

        private void assertBounds(final int nodeIndex) {
            if(!isWithinBounds(nodeIndex)) {
                throw new IndexOutOfBoundsException(nodeIndex);
            }
        }

        private boolean isWithinBounds(final int nodeIndex) {
            return nodeIndex>=0
                    && nodeIndex<nodeCount;
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

    // -- FUNCTIONAL INTERFACES

    @FunctionalInterface
    public interface EdgeFilter {
        boolean test(int nodeIndexFrom, int nodeIndexTo);
        public static EdgeFilter includeAll() {
            return (i, j) -> true;
        }
        /**
         * Eg.g when undirected, we report edges only in one direction, that is,
         * when from-index is less or equal to to-index.
         */
        public static EdgeFilter excludeToLessThanFrom() {
            return (i, j) -> j>=i;
        }
    }

    @FunctionalInterface
    public interface EdgeConsumer<T> {
        void accept(int nodeIndexFrom, T nodeFrom, int nodeIndexTo, T nodeTo);
    }

    @FunctionalInterface
    public interface EdgeFunction<T, R> {
        R apply(int nodeIndexFrom, T nodeFrom, int nodeIndexTo, T nodeTo);
    }

    @FunctionalInterface
    public interface NodeFormatter<T> {
        String format(int nodeIndex, T node);
        public static <T> NodeFormatter<T> of(@Nullable final Function<T, String> toStringFunction) {
            return toStringFunction!=null
                ? (i, node)->toStringFunction.apply(node)
                : (i, node)->node.toString();
        }
    }

    @FunctionalInterface
    public interface EdgeFormatter<T> {
        String format(int nodeIndexFrom, T nodeFrom, int nodeIndexTo, T nodeTo, NodeFormatter<T> nodeFormatter);
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
        private final Map<Long, Object> edgeAttributeByPackedEdgeIndex;

        // -- TRAVERSAL

        public void visitNeighbors(final int nodeIndex,
                @Nullable final Consumer<T> nodeVisitor) {
            kernel()
                .streamNeighbors(nodeIndex)
                .forEach(neighborIndex->
                    nodeVisitor.accept(nodes.getElseFail(neighborIndex)));
        }

        public void visitNeighbors(final int nodeIndex,
                @Nullable final EdgeFilter edgeFilter,
                @Nullable final Consumer<T> nodeVisitor) {
            if(nodeVisitor==null) return;
            var stream = kernel()
                .streamNeighbors(nodeIndex);
            stream = edgeFilter==null
                    ? stream
                    : stream.filter(neighborIndex->
                        edgeFilter.test(nodeIndex, neighborIndex));
            stream.forEach(neighborIndex->
                    nodeVisitor.accept(nodes.getElseFail(neighborIndex)));
        }

        public void visitEdges(final int nodeIndex,
                @Nullable final EdgeFilter edgeFilter,
                @Nullable final EdgeConsumer<T> edgeConsumer) {
            if(edgeConsumer==null) return;
            var fromNode = nodes.getElseFail(nodeIndex);
            var stream = kernel()
                    .streamNeighbors(nodeIndex);
            stream = edgeFilter==null
                    ? stream
                    : stream.filter(neighborIndex->
                        edgeFilter.test(nodeIndex, neighborIndex));
            stream.forEach(neighborIndex->
                edgeConsumer.accept(
                        nodeIndex, fromNode,
                        neighborIndex, nodes.getElseFail(neighborIndex)));
        }

        public void visitNeighborsIndexed(final int nodeIndex,
                @Nullable final IndexedConsumer<T> nodeVisitor) {
            if(nodeVisitor==null) return;
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
            var graph = new Graph<R>(kernel, nodes.map(nodeMapper), edgeAttributeByPackedEdgeIndex());
            _Assert.assertEquals(kernel.nodeCount(), graph.nodes().size());
            return graph;
        }

        /**
         * Returns a sub-graph with any nodes removed from this graph, that do not pass the filter.
         */
        public Graph<T> filter(final Predicate<T> filter) {
            if(nodes.isEmpty()) return this;

            var nodeType = _Casts.<Class<T>>uncheckedCast(nodes.getFirst().get().getClass());
            var builder = new GraphBuilder<T>(nodeType, kernel().characteristics);
            var isUndirected = kernel().isUndirected();
            
            nodes.forEach(IndexedConsumer.zeroBased((nodeIndex, node)->{
                if(filter.test(node)) {
                    builder.addNode(node);
                    Graph.this.visitNeighborsIndexed(nodeIndex, (neighborIndex, neighbor)->{
                        if(isUndirected
                                && neighborIndex<nodeIndex) {
                            return;
                        }
                        if(filter.test(neighbor)) {
                            var edgeAttributes = edgeAttributeByPackedEdgeIndex()
                                    .get(_Longs.pack(nodeIndex, neighborIndex));
                            if(edgeAttributes!=null) {
                                builder.addEdge(node, neighbor, edgeAttributes);
                            } else {
                                builder.addEdge(node, neighbor);
                            }
                        }
                    });
                }
            }));

            return builder.build();
        }

        // -- EDGE ATTRIBUTE

        /**
         * For multi-graphs, edge attributes are shared.
         */
        public Optional<Object> getEdgeAttribute(final int fromIndex, final int toIndex) {
            final long packedEdgeIndex = kernel().isUndirected()
                    ? _Longs.pack(Math.min(fromIndex, toIndex), Math.max(fromIndex, toIndex))
                    : _Longs.pack(fromIndex, toIndex);
            return Optional.ofNullable(
                    edgeAttributeByPackedEdgeIndex.get(packedEdgeIndex));
        }

        // -- SORTING

        /**
         * Returns an isomorphic graph that has its node list sorted by given comparator.
         * <p>
         * Preserves graph characteristics and edge attributes.
         */
        public Graph<T> sorted(final Comparator<T> nodeComparator) {
            var sortedNodes = nodes.sorted(nodeComparator);
            _Assert.assertEquals(nodes.size(), sortedNodes.size(),
                    ()->"nodeComparator has reduced the number of nodes from the original node list");

            var builder = new GraphBuilder<T>((Class<T>) null, kernel.characteristics);
            sortedNodes.forEach(builder::addNode);

            for(int nodeIndex = 0; nodeIndex < kernel.nodeCount(); ++nodeIndex) {
                visitEdges(nodeIndex, EdgeFilter.includeAll(), (i, a, j, b)->{
                    builder.addEdge(a, b, getEdgeAttribute(i, j).orElse(null));
                });
            }
            return builder.build();
        }

        // -- FORMAT

        @Override
        public String toString() {
            return toString(null, null);
        }

        public String toString(
                @Nullable final Function<T, String> nodeFormatter) {
            return toString(NodeFormatter.of(nodeFormatter), null);
        }

        public String toString(
                @Nullable final NodeFormatter<T> nodeFormatter,
                @Nullable final Function<Object, String> edgeAttributeFormatter) {

            var isDirected = !kernel().isUndirected();
            var hasEdgeAttributes = !edgeAttributeByPackedEdgeIndex.isEmpty();

            final NodeFormatter<T> nodeFormat = nodeFormatter != null
                    ? nodeFormatter
                    : NodeFormatter.of(null);
            final Function<Object, String> edgeAttributeFormat = edgeAttributeFormatter != null
                    ? edgeAttributeFormatter
                    : edgeAttr->"(" + edgeAttr + ")";
            final EdgeFormatter<T> edgeFormat = hasEdgeAttributes
                    ? isDirected
                            ? (i, a, j, b, nf) -> String.format("%s -> %s%s", nf.format(i, a), nf.format(j, b),
                                    getEdgeAttribute(i, j).map(edgeAttributeFormat).map(s->" "+s).orElse(""))
                            : (i, a, j, b, nf) -> String.format("%s - %s%s", nf.format(i, a), nf.format(j, b),
                                    getEdgeAttribute(i, j).map(edgeAttributeFormat).map(s->" "+s).orElse(""))
                    : isDirected
                            ? (i, a, j, b, nf) -> String.format("%s -> %s", nf.format(i, a), nf.format(j, b))
                            : (i, a, j, b, nf) -> String.format("%s - %s", nf.format(i, a), nf.format(j, b));

            var filter = isDirected
                    ? EdgeFilter.includeAll()
                    : EdgeFilter.excludeToLessThanFrom();

            var sb = new StringBuilder();
            for(int nodeIndex = 0; nodeIndex < kernel.nodeCount(); ++nodeIndex) {
                visitEdges(nodeIndex, filter, (i, a, j, b)->{
                    sb
                        .append(edgeFormat.format(i, a, j, b, nodeFormat))
                        .append("\n");
                });
                if(kernel().neighborCount(nodeIndex)==0) {
                    sb
                        .append(String.format("%s", nodeFormat.format(nodeIndex, nodes.getElseFail(nodeIndex))))
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
        private final boolean isUndirected;
        private final Map<T, Integer> indexByNode;
        private final List<T> nodeList;
        private final IntList fromNode = new IntList(4); // best guess initial edge capacity
        private final IntList toNode = new IntList(4); // best guess initial edge capacity
        private final Map<Long, Object> edgeAttributeByPackedEdgeIndex;

        // -- FACTORIES

        public static <T> GraphBuilder<T> directed(final Class<T> nodeType) {
            return new GraphBuilder<T>(nodeType, GraphCharacteristic.directed());
        }

        public static <T> GraphBuilder<T> undirected(final Class<T> nodeType) {
            return new GraphBuilder<T>(nodeType, GraphCharacteristic.undirected());
        }

        /**
         * Adds a new node to the graph, respecting node equality, that is,
         * no duplicates are added.
         * @apiNote duplicates with respect to {@link Objects#equals} are not added
         */
        public GraphBuilder<T> addNode(final @NonNull T node) {
            addNodeHonoringIndexMap(node);
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
         * Variant of {@link #addEdge(int, int)}, that stores an arbitrary attribute with the edge.
         * @see #addEdge(int, int)
         */
        public GraphBuilder<T> addEdge(final int fromIndex, final int toIndex, @Nullable final Object edgeAttribute) {
            addEdge(fromIndex, toIndex);
            final long packedEdgeIndex = isUndirected
                    ? _Longs.pack(Math.min(fromIndex, toIndex), Math.max(fromIndex, toIndex))
                    : _Longs.pack(fromIndex, toIndex);
            if(edgeAttribute!=null) {
                edgeAttributeByPackedEdgeIndex.put(packedEdgeIndex, edgeAttribute);
            }
            return this;
        }

        /**
         * Adds a new edge to the graph.
         * Also adds any of the 2 given nodes to the node-list, if not already added before.
         */
        public GraphBuilder<T> addEdge(final T from, final T to) {
            addEdge(from, to, null);
            return this;
        }

        /**
         * Variant of {@link #addEdge(Object, Object)}, that stores an arbitrary attribute with the edge.
         * @see #addEdge(Object, Object)
         */
        public GraphBuilder<T> addEdge(final T from, final T to, @Nullable final Object edgeAttribute) {
            final int fromIndex = indexOfWithAdd(from);
            final int toIndex = indexOfWithAdd(to);
            addEdge(fromIndex, toIndex, edgeAttribute);
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
            this.isUndirected = characteristics.contains(GraphCharacteristic.UNDIRECTED);
            this.nodeList = new ArrayList<>();
            this.indexByNode = new HashMap<>();
            this.edgeAttributeByPackedEdgeIndex = new HashMap<>();
        }

        public Graph<T> build() {
            var kernel = new GraphKernel(nodeList.size(), characteristics);
            var edgeCount = edgeCount();
            for (int edgeIndex = 0; edgeIndex<edgeCount; edgeIndex++) {
                kernel.addEdge(fromNode.get(edgeIndex), toNode.get(edgeIndex));
            }
            var graph = new Graph<T>(kernel,
                    Can.ofCollection(nodeList),
                    Collections.unmodifiableMap(edgeAttributeByPackedEdgeIndex));
            return graph;
        }

        // -- HELPER

        /**
         * Created only if triggered by {@link #addEdge(Object, Object)}
         * or {@link #addEdge(Object, Object, Object)}.
         */
        private Map<T, Integer> nodeIndexByNode = null;

        private Map<T, Integer> snapshotNodeIndexByNode() {
            var nodeIndexByNode = new HashMap<T, Integer>();
            nodeList.forEach(IndexedConsumer.zeroBased((i, node)->{
                nodeIndexByNode.put(node, i);
            }));
            return nodeIndexByNode;
        }

        private int indexOfWithAdd(final T node) {
            if(nodeIndexByNode==null) {
                this.nodeIndexByNode = snapshotNodeIndexByNode();
            }
            var nodeIndex = nodeIndexByNode.get(node);
            if(nodeIndex!=null) {
                return nodeIndex;
            }
            return addNodeHonoringIndexMap(node);
        }

        private int addNodeHonoringIndexMap(final T node) {
            final Integer nodeIndex = indexByNode.get(node);
            // skip adding if the node is a duplicate
            if(nodeIndex!=null) return nodeIndex;

            final int nextIndex = nodeList.size();
            indexByNode.put(node, nextIndex);
            nodeList.add(node);
            if(nodeIndexByNode!=null) {
                nodeIndexByNode.put(node, nextIndex);
            }
            return nextIndex;
        }

    }

}
