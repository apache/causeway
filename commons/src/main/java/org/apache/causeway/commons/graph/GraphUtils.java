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
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel.GraphCharacteristic;
import org.apache.causeway.commons.internal.assertions._Assert;
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

}
