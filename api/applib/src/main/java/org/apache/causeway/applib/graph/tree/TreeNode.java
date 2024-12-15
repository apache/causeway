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
package org.apache.causeway.applib.graph.tree;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.graph.Edge;
import org.apache.causeway.applib.graph.SimpleEdge;
import org.apache.causeway.applib.graph.Vertex;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.commons.internal.base._NullSafe;

import lombok.NonNull;

/**
 * Fundamental building block for tree structures.
 * <p>
 * Wraps a node value and holds references to related nodes.
 *
 * @since 2.0 {@index}
 *
 * @param <T> type constraint for values contained by this node
 */
@org.apache.causeway.applib.annotation.Value
public record TreeNode<T>(
    /**
     * Is required {@code null} iff this is a root node.
     * However, method {@link #rootNode()} never returns {@code null}.
     * @implNote records cannot self reference
     */
    @Nullable TreeNode<T> rootNode,
    /** position within the tree (as path) */
    @NonNull TreePath treePath,
    @NonNull T value,
    @NonNull TreeAdapter<T> treeAdapter,
    /**
     * this tree's shared state object, holding e.g. the collapse/expand state
     */
    @NonNull TreeState treeState)
implements Vertex<T> {

    // -- FACTORIES

    /**
     * Creates the root node of a tree structure as inferred from given treeAdapter.
     */
    public static <T> TreeNode<T> root(
            final @NonNull T rootValue,
            final @NonNull TreeAdapter<T> treeAdapter) {
        return TreeNode.root(rootValue, treeAdapter, TreeState.rootCollapsed());
    }

    /**
     * Creates the root node of a tree structure as inferred from given treeAdapter.
     */
    public static <T> TreeNode<T> root(
            final @NonNull T rootValue,
            final @NonNull Class<? extends TreeAdapter<T>> treeAdapterClass,
            final @NonNull FactoryService factoryService) {
        return root(rootValue, factoryService.getOrCreate(treeAdapterClass));
    }

    public static <T> TreeNode<T> root(
            final T rootValue,
            final Class<? extends TreeAdapter<T>> treeAdapterClass,
            final TreeState sharedState,
            final FactoryService factoryService) {
        return root(rootValue, factoryService.getOrCreate(treeAdapterClass));
    }

    public static <T> TreeNode<T> root(
            final T rootValue,
            final TreeAdapter<T> treeAdapter,
            final TreeState sharedState) {
        return new TreeNode<T>(null, TreePath.root(), rootValue, treeAdapter, sharedState);
    }

    // -- CONTRACT

    @Override
    public final String toString() {
        return "TreeNode[%s, value=%s]".formatted(treePath, value);
    }

    @Override
    public final boolean equals(final Object obj) {
        return obj instanceof TreeNode other
            ? Objects.equals(this.treePath, other.treePath)
                    && Objects.equals(this.value, other.value)
            : false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(treePath);
    }

    // --

    public TreeNode<T> rootNode() {
        return rootNode!=null
            ? rootNode
            : this;
    }

    public T rootValue() {
        return rootNode().value();
    }

    // -- VERTEX - IMPLEMENTATION

    @Override
    public int incomingCount() {
        return isRoot()
                ? 0
                : 1;
    }
    @Override
    public int outgoingCount() {
        return childCount();
    }
    @Override
    public Stream<Edge<T>> streamIncoming() {
        return lookupParent()
            .map(parentNode->SimpleEdge.<T>of(parentNode, this))
            .map(Stream::<Edge<T>>of)
            .orElseGet(Stream::empty);
    }
    @Override
    public Stream<Edge<T>> streamOutgoing() {
        return streamChildren()
                .map(to->SimpleEdge.<T>of(this, to));
    }

    // -- RESOLUTION

    /**
     * Resolves given path relative to the root of this tree.
     */
    public Optional<TreeNode<T>> resolve(final TreePath absolutePath) {
        /*
         * Optimize if absolutePath starts with this.treePath:
         *
         * If current path is
         *   /p0/p1/p2/p3
         * and we want to resolve
         *   /p0/p1/p2/p3/p4/p5
         * then instead of starting from root, we can start from here, resolving sub-node
         *   /p3/p4/p5
         * observe: the relative path /p3 would point to the sub-node itself
         */
        return absolutePath.startsWith(treePath)
                ? resolveRelative(absolutePath.subPath(treePath.size() - 1))
                : rootNode().resolveRelative(absolutePath);
    }

    /**
     * Resolves given path relative to this node.
     * <p>
     * E.g. starting from root, '/0' will return the root;<br>
     * starting from root, '/0/2' will return the 3rd child of root;<br>
     * starting from sub-node '/0/2', '/2/9' will resolve the 10th child ('/0/2/9') of this sub-node;<br>
     */
    private Optional<TreeNode<T>> resolveRelative(final TreePath relativePath) {
        if(Objects.equals(this.treePath, relativePath)) return Optional.of(this);

        final int childIndex = relativePath.childIndex().orElse(-1);
        if(childIndex<0) return Optional.empty();

        final Optional<TreeNode<T>> childNode = streamChildren().skip(childIndex).findFirst();
        if(!childNode.isPresent()) return Optional.empty();

        return relativePath.size()>2
                ? childNode.get().resolveRelative(relativePath.subPath(1))
                : childNode;
    }

    // -- PARENT

    public Optional<TreeNode<T>> lookupParent() {
        return treePath.parent()
                .flatMap(this::resolve);
    }

    // -- CHILDREN

    public int childCount() {
        return treeAdapter.childCountOf(value);
    }

    public Stream<TreeNode<T>> streamChildren() {
        if(isLeaf()) return Stream.empty();

        return treeAdapter.childrenOf(value)
                .map(IndexedFunction.zeroBased((siblingIndex, childPojo)->
                    toTreeNode(treePath.append(siblingIndex), childPojo)));
    }

    // -- BASIC PREDICATES

    public boolean isRoot() {
        return rootNode == null;
    }

    public boolean isLeaf() {
        return childCount() == 0;
    }

    // -- COLLAPSE/EXPAND

    public boolean isExpanded(final TreePath treePath) {
        final Set<TreePath> expandedPaths = treeState().expandedNodePaths();
        return expandedPaths.contains(treePath);
    }

    /**
     * Adds {@code treePaths} to the set of expanded nodes, as held by this tree's shared state object.
     * @param treePaths
     */
    public void expand(final TreePath ... treePaths) {
        final Set<TreePath> expandedPaths = treeState().expandedNodePaths();
        _NullSafe.stream(treePaths).forEach(expandedPaths::add);
    }

    /**
     * Expands this node and all its parents.
     */
    public void expand() {
        final Set<TreePath> expandedPaths = treeState().expandedNodePaths();
        streamHierarchyUp()
            .map(TreeNode::treePath)
            .forEach(expandedPaths::add);
    }

    /**
     * Removes {@code treePaths} from the set of expanded nodes, as held by this tree's shared state object.
     * @param treePaths
     */
    public void collapse(final TreePath ... treePaths) {
        final Set<TreePath> expandedPaths = treeState().expandedNodePaths();
        _NullSafe.stream(treePaths).forEach(expandedPaths::remove);
    }

    // -- SELECTION

    /**
     * Clears all selection markers.
     * @see #select(TreePath...)
     */
    public void clearSelection() {
        treeState().selectedNodePaths().clear();
    }

    /**
     * Whether node that corresponds to given {@link TreePath} has a selection marker.
     * @see #select(TreePath...)
     */
    public boolean isSelected(final TreePath treePath) {
        final Set<TreePath> selectedPaths = treeState().selectedNodePaths();
        return selectedPaths.contains(treePath);
    }

    /**
     * Select nodes by their corresponding {@link TreePath}, that is, activate their selection marker.
     * <p>
     * With the <i>Wicket Viewer</i> corresponds to expressing CSS class {@code tree-node-selected}
     * on the rendered tree node, which has default bg-color {@code lightgrey}. Color can be customized
     * by setting CSS var {@code--tree-node-selected-bg-color}
     * <pre>
     * .tree-theme-bootstrap .tree-node-selected {
     *     background-color: var(--tree-node-selected-bg-color, lightgrey);
     * }
     * </pre>
     */
    public void select(final TreePath ... treePaths) {
        final Set<TreePath> selectedPaths = treeState().selectedNodePaths();
        _NullSafe.stream(treePaths).forEach(selectedPaths::add);
    }

    // -- PARENT NODE ITERATION

    public Iterator<TreeNode<T>> iteratorHierarchyUp(){
        return new TreeNode_iteratorHierarchyUp<>(this);
    }

    // -- PARENT NODE STREAMING

    public Stream<TreeNode<T>> streamHierarchyUp(){
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iteratorHierarchyUp(), Spliterator.ORDERED),
                false); // not parallel
    }

    // -- CHILD NODE ITERATION

    public Iterator<TreeNode<T>> iteratorDepthFirst(){
        return new TreeNode_iteratorDepthFirst<>(this);
    }

    public Iterator<TreeNode<T>> iteratorBreadthFirst(){
        return new TreeNode_iteratorBreadthFirst<>(this);
    }

    // -- CHILD NODE STREAMING

    public Stream<TreeNode<T>> streamDepthFirst(){
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iteratorDepthFirst(), Spliterator.ORDERED),
                false); // not parallel
    }

    public Stream<TreeNode<T>> streamBreadthFirst(){
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iteratorBreadthFirst(), Spliterator.ORDERED),
                false); // not parallel
    }

    // -- HELPER

    private TreeNode<T> toTreeNode(final TreePath treePath, final T value){
        return new TreeNode<>(rootNode(), treePath, value, treeAdapter, treeState);
    }

}
