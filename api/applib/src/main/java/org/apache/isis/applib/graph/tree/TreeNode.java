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
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.graph.Edge;
import org.apache.isis.applib.graph.SimpleEdge;
import org.apache.isis.applib.graph.Vertex;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.SneakyThrows;

/**
 * Fundamental building block of Tree structures.
 *
 * @since 2.0 {@index}
 *
 * @param <T> type constraint for values contained by this node
 */
@Value(logicalTypeName = IsisModuleApplib.NAMESPACE + ".graph.tree.TreeNode")
public class TreeNode<T> implements Vertex<T> {

    private final TreeState sharedState;
    private final T value;
    private final Class<? extends TreeAdapter<T>> treeAdapterClass;
    private final _Lazy<TreeAdapter<T>> treeAdapter = _Lazy.of(this::newTreeAdapter);
    private final _Lazy<TreePath> treePath = _Lazy.of(this::resolveTreePath);

    public static <T> TreeNode<T> of(final T value, final Class<? extends TreeAdapter<T>> treeAdapterClass, final TreeState sharedState) {
        return new TreeNode<T>(value, treeAdapterClass, sharedState);
    }

    protected TreeNode(final T value, final Class<? extends TreeAdapter<T>> treeAdapterClass, final TreeState sharedState) {
        this.value = Objects.requireNonNull(value);
        this.treeAdapterClass = Objects.requireNonNull(treeAdapterClass);
        this.sharedState = sharedState;
    }

    @Override
    public T getValue() {
        return value;
    }


    // -- VERTEX - IMPLEMENTATION

    @Override
    public int getIncomingCount() {
        return isRoot() ? 0 : 1;
    }
    @Override
    public int getOutgoingCount() {
        return getChildCount();
    }
    @Override
    public Stream<Edge<T>> streamIncoming() {
        return isRoot() ? Stream.empty() : Stream.of(SimpleEdge.<T>of(getParentIfAny(), this));
    }
    @Override
    public Stream<Edge<T>> streamOutgoing() {
        return streamChildren()
                .map(to->SimpleEdge.<T>of(this, to));
    }

    // -- PARENT

    public @Nullable TreeNode<T> getParentIfAny() {
        return treeAdapter().parentOf(getValue())
                .map(this::toTreeNode)
                .orElse(null);
    }



    // -- CHILDREN

    public int getChildCount() {
        return treeAdapter().childCountOf(value);
    }

    public Stream<TreeNode<T>> streamChildren() {
        if(isLeaf()) {
            return Stream.empty();
        }
        return treeAdapter().childrenOf(value)
                .map(this::toTreeNode);
    }




    // -- BASIC PREDICATES

    public boolean isRoot() {
        return getParentIfAny() == null;
    }

    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    // -- PATH INFO


    public TreePath getPositionAsPath() {
        return treePath.get();
    }



    // -- COLLAPSE/EXPAND

    /**
     * @return this tree's shared state object, holding e.g. the collapse/expand state
     */
    public TreeState getTreeState() {
        return sharedState;
    }


    public boolean isExpanded(final TreePath treePath) {
        final Set<TreePath> expandedPaths = getTreeState().getExpandedNodePaths();
        return expandedPaths.contains(treePath);
    }

    /**
     * Adds {@code treePaths} to the set of expanded nodes, as held by this tree's shared state object.
     * @param treePaths
     */
    @Programmatic
    public void expand(final TreePath ... treePaths) {
        final Set<TreePath> expandedPaths = getTreeState().getExpandedNodePaths();
        _NullSafe.stream(treePaths).forEach(expandedPaths::add);
    }

    /**
     * Expands this node and all its parents.
     */
    @Programmatic
    public void expand() {
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
    public void collapse(final TreePath ... treePaths) {
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
    public static <T> TreeNode<T> lazy(final T node, final Class<? extends TreeAdapter<T>> treeAdapterClass) {
        return TreeNode.of(node, treeAdapterClass, TreeState.rootCollapsed());
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

    // -- LAZY NODE ADAPTING

    /**
     * @apiNote a class rather than an instance, because otherwise
     * the adapter would need to be serializable for Wicket's trees to work correctly.
     */
    public Class<? extends TreeAdapter<T>> getTreeAdapterClass() {
        return treeAdapterClass;
    }

    // -- HELPER

    @SneakyThrows
    private TreeAdapter<T> newTreeAdapter() {
        try {
            return treeAdapterClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(
                    String.format("failed to instantiate TreeAdapter '%s'", treeAdapterClass.getName()), e);
        }
    }

    private TreeAdapter<T> treeAdapter() {
        return treeAdapter.get();
    }

    private TreeNode<T> toTreeNode(final T value){
        return of(value, getTreeAdapterClass(), sharedState);
    }

    private TreePath resolveTreePath() {
        final TreeNode<T> parent = getParentIfAny();
        if(parent==null) {
            return TreePath.root();
        }
        return parent.getPositionAsPath().append(indexWithinSiblings(parent));
    }

    /*
     * @return zero based index
     */
    private int indexWithinSiblings(final TreeNode<T> parent) {
        final LongAdder indexOneBased = new LongAdder();

        boolean found = parent.streamChildren()
                .peek(__->indexOneBased.increment())
                .anyMatch(this::isEqualTo);

        if(!found) {
            throw _Exceptions.unexpectedCodeReach();
        }

        return indexOneBased.intValue()-1;
    }

    private boolean isEqualTo(final TreeNode<T> other) {
        if(other==null) {
            return false;
        }
        return Objects.equals(this.getValue(), other.getValue());
    }

}
