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
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.applib.graph.Edge;
import org.apache.causeway.applib.graph.SimpleEdge;
import org.apache.causeway.applib.graph.Vertex;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.context._Context;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * Fundamental building block of Tree structures.
 *
 * @since 2.0 {@index}
 *
 * @param <T> type constraint for values contained by this node
 */
@Named(CausewayModuleApplib.NAMESPACE + ".graph.tree.TreeNode")
@Value
public class TreeNode<T> implements Vertex<T> {

    @Getter
    private final TreeNode<T> rootNode;
    private final TreePath treePath;
    private final T value;
    private final TreeState sharedState;
    private final Class<? extends TreeAdapter<T>> treeAdapterClass;
    private final _Lazy<TreeAdapter<T>> treeAdapter = _Lazy.of(this::newTreeAdapter);

    public static <T> TreeNode<T> root(
            final T value, 
            final Class<? extends TreeAdapter<T>> treeAdapterClass, 
            final TreeState sharedState) {
        return new TreeNode<T>(value, treeAdapterClass, sharedState);
    }

    // generic node constructor, with reference to root
    protected TreeNode(
            final @NonNull TreeNode<T> rootNode,
            final @NonNull TreePath treePath,
            final @NonNull T value, 
            final @NonNull Class<? extends TreeAdapter<T>> treeAdapterClass, 
            final @NonNull TreeState sharedState) {
        this.rootNode = rootNode;
        this.treePath = treePath;
        this.value = value;
        this.treeAdapterClass = treeAdapterClass;
        this.sharedState = sharedState;
    }

    // root-node constructor
    private TreeNode(
            final @NonNull T value, 
            final @NonNull Class<? extends TreeAdapter<T>> treeAdapterClass, 
            final @NonNull TreeState sharedState) {
        this.rootNode = this;
        this.treePath = TreePath.root();
        this.value = value;
        this.treeAdapterClass = treeAdapterClass;
        this.sharedState = sharedState;
    }
    
    public T getRootValue() {
        return getRootNode().getValue();
    }
    
    @Override
    public T getValue() {
        return value;
    }

    // -- VERTEX - IMPLEMENTATION

    @Override
    public int getIncomingCount() {
        return getPositionAsPath().isRoot()
                ? 0 
                : 1;
    }
    @Override
    public int getOutgoingCount() {
        return getChildCount();
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
    public Optional<TreeNode<T>> resolve(TreePath absolutePath) {
        // optimize if absolutePath starts with this.treePath
        return absolutePath.startsWith(treePath)
                ? resolveRelative(absolutePath.subPath(treePath.size()))
                : rootNode.resolveRelative(absolutePath);
    }
    
    /**
     * Resolves given path relative to this node.
     */
    private Optional<TreeNode<T>> resolveRelative(TreePath realtivePath) {
        final int childIndex = realtivePath.childIndex().orElse(-1);
        if(childIndex<0) return Optional.empty();
        
        final Optional<TreeNode<T>> childNode = streamChildren().skip(childIndex-1).findFirst();
        if(!childNode.isPresent()) return Optional.empty();
        
        return realtivePath.size()>2
                ? childNode.get().resolveRelative(realtivePath.subPath(1))
                : childNode;
    }
    
    // -- PARENT
    
    public Optional<TreeNode<T>> lookupParent() {
        return Optional.ofNullable(treePath.getParentIfAny())
                .flatMap(this::resolve);
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
                .map(IndexedFunction.zeroBased((siblingIndex, childPojo)->
                    toTreeNode(treePath.append(siblingIndex), childPojo)));
    }

    // -- BASIC PREDICATES

    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    // -- PATH INFO

    public TreePath getPositionAsPath() {
        return treePath;
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

    // -- SELECTION

    /**
     * Clears all selection markers.
     * @see #select(TreePath...)  
     */
    @Programmatic
    public void clearSelection() {
        getTreeState().getSelectedNodePaths().clear();
    }

    /**
     * Whether node that corresponds to given {@link TreePath} has a selection marker.
     * @see #select(TreePath...)
     */
    @Programmatic
    public boolean isSelected(final TreePath treePath) {
        final Set<TreePath> selectedPaths = getTreeState().getSelectedNodePaths();
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
    @Programmatic
    public void select(final TreePath ... treePaths) {
        final Set<TreePath> selectedPaths = getTreeState().getSelectedNodePaths();
        _NullSafe.stream(treePaths).forEach(selectedPaths::add);
    }

    // -- CONSTRUCTION

    /**
     * Convenient shortcut.
     * @param rootNode
     * @param treeAdapterClass
     * @return new LazyTreeNode
     */
    public static <T> TreeNode<T> root(
            final T rootNode, 
            final Class<? extends TreeAdapter<T>> treeAdapterClass) {
        return TreeNode.root(rootNode, treeAdapterClass, TreeState.rootCollapsed());
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

    // -- TREE NODE ADAPTING

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
            var adapter = treeAdapterClass.getDeclaredConstructor().newInstance();
            return _Context.lookup(ServiceInjector.class) //TODO[CAUSEWAY-3711] requires some cooperator that provides it
                .map(serviceInjector->serviceInjector.injectServicesInto(adapter))
                .orElse(adapter);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(
                    String.format("failed to instantiate TreeAdapter '%s'", treeAdapterClass.getName()), e);
        }
    }

    private TreeAdapter<T> treeAdapter() {
        return treeAdapter.get();
    }

    private TreeNode<T> toTreeNode(final TreePath treePath, final T value){
        return new TreeNode<>(rootNode, treePath, value, getTreeAdapterClass(), sharedState);
    }

}
