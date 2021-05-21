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
package org.apache.isis.applib.graph.tree;

import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.graph.Edge;
import org.apache.isis.applib.graph.SimpleEdge;
import org.apache.isis.applib.graph.Vertex;
import org.apache.isis.commons.internal.base._NullSafe;

/**
 * Fundamental building block of Tree structures. 
 * 
 * @since 2.0 {@index}
 *
 * @param <T> type constraint for values contained by this node
 */
public interface TreeNode<T> extends Vertex<T> {

    // -- VERTEX - IMPLEMENTATION
    
    default int getIncomingCount() {
        return isRoot() ? 0 : 1;
    }
    default int getOutgoingCount() {
        return getChildCount();
    }
    default Stream<Edge<T>> streamIncoming() {
        return isRoot() ? Stream.empty() : Stream.of(SimpleEdge.<T>of(getParentIfAny(), this));
    }
    default Stream<Edge<T>> streamOutgoing() {
        return streamChildren()
                .map(to->SimpleEdge.<T>of(this, to));
    }
    
    // -- PARENT

    public @Nullable TreeNode<T> getParentIfAny();

    // -- CHILDREN

    public int getChildCount();

    public Stream<TreeNode<T>> streamChildren();

    // -- BASIC PREDICATES

    public default boolean isRoot() {
        return getParentIfAny() == null;
    }

    public default boolean isLeaf() {
        return getChildCount() == 0;
    }

    // -- PATH INFO

    public TreePath getPositionAsPath();

    // -- COLLAPSE/EXPAND

    /**
     * @return this tree's shared state object, holding e.g. the collapse/expand state
     */
    public TreeState getTreeState();

    public default boolean isExpanded(TreePath treePath) {
        final Set<TreePath> expandedPaths = getTreeState().getExpandedNodePaths();
        return expandedPaths.contains(treePath);
    }

    /**
     * Adds {@code treePaths} to the set of expanded nodes, as held by this tree's shared state object.
     * @param treePaths
     */
    @Programmatic
    public default void expand(TreePath ... treePaths) {
        final Set<TreePath> expandedPaths = getTreeState().getExpandedNodePaths();
        _NullSafe.stream(treePaths).forEach(expandedPaths::add);
    }

    /**
     * Expands this node and all its parents.
     */
    @Programmatic
    public default void expand() {
        final Set<TreePath> expandedPaths = getTreeState().getExpandedNodePaths();
        streamHierarchyUp()
            .map(TreeNode::getPositionAsPath)
            .forEach(expandedPaths::add);
    }

    /**
     * Removes {@code treePaths} from the set of expanded nodes, as held by this tree's shared state object.
     * @param treePaths
     */
    @Programmatic
    public default void collapse(TreePath ... treePaths) {
        final Set<TreePath> expandedPaths = getTreeState().getExpandedNodePaths();
        _NullSafe.stream(treePaths).forEach(expandedPaths::remove);
    }

    // -- CONSTRUCTION

    /**
     * Convenient shortcut.
     * @param node
     * @param treeAdapterClass
     * @return new LazyTreeNode
     */
    public static <T> TreeNode<T> lazy(T node, Class<? extends TreeAdapter<T>> treeAdapterClass) {
        return LazyTreeNode.of(node, treeAdapterClass, TreeState.rootCollapsed());
    }

    // -- PARENT NODE ITERATION

    public default Iterator<TreeNode<T>> iteratorHierarchyUp(){
        return new TreeNode_iteratorHierarchyUp<>(this);
    }

    // -- PARENT NODE STREAMING

    public default Stream<TreeNode<T>> streamHierarchyUp(){
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iteratorHierarchyUp(), Spliterator.ORDERED),
                false); // not parallel
    }

    // -- CHILD NODE ITERATION

    public default Iterator<TreeNode<T>> iteratorDepthFirst(){
        return new TreeNode_iteratorDepthFirst<>(this);
    }

    public default Iterator<TreeNode<T>> iteratorBreadthFirst(){
        return new TreeNode_iteratorBreadthFirst<>(this);
    }

    // -- CHILD NODE STREAMING

    public default Stream<TreeNode<T>> streamDepthFirst(){
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iteratorDepthFirst(), Spliterator.ORDERED),
                false); // not parallel
    }

    public default Stream<TreeNode<T>> streamBreadthFirst(){
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iteratorBreadthFirst(), Spliterator.ORDERED),
                false); // not parallel
    }

    // -- LAZY NODE ADAPTING

    /**
     * @apiNote a class rather than an instance, because otherwise
     * the adapter would need to be serializable for Wicket's trees to work correctly.
     */
    public Class<? extends TreeAdapter<T>> getTreeAdapterClass();


}
