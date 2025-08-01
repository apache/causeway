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
package org.apache.causeway.viewer.wicket.model.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.context.MetaModelContext;

public record BookmarkedPagesModel(
    List<BookmarkTreeNode> rootNodes,
    /**
     * Meant to be transient, hence cleared on detach.
     */
    List<BookmarkTreeNode> depthFirstGraph
    ) implements IModel<List<BookmarkTreeNode>> {

    public BookmarkedPagesModel() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public void add(final BookmarkableModel bookmarkableModel) {

        var bookmark = bookmarkableModel.toBookmark().orElse(null);
        if(bookmark == null) return; // ignore

        var matchingRootNode = matchRootNode(bookmarkableModel).orElse(null);

        // MRU/LRU algorithm
        if(matchingRootNode != null) {
            rootNodes.remove(matchingRootNode);
            rootNodes.add(0, matchingRootNode);
        } else {
            if (bookmarkableModel.getBookmarkPolicy().isRoot()) {
                rootNodes.add(0, BookmarkTreeNode.newRoot(bookmark, bookmarkableModel));
            }
        }

        trim(rootNodes, getMaxSize());
    }

    @Override
    public List<BookmarkTreeNode> getObject() {
        if(rootNodes.isEmpty()) return List.of();
        if(!depthFirstGraph.isEmpty()) return depthFirstGraph;

        depthFirstGraph.clear();

        var sortedNodes = new ArrayList<>(rootNodes);
        Collections.sort(sortedNodes);

        for (BookmarkTreeNode rootNode : sortedNodes) {
            rootNode.appendGraphTo(depthFirstGraph);
        }
        return depthFirstGraph;
    }

    @Override
    public void detach() {
        depthFirstGraph.clear();
    }

    public void clear() {
        rootNodes.clear();
        depthFirstGraph.clear();
    }

    public boolean isEmpty() {
        return rootNodes.isEmpty();
    }

    public void remove(final BookmarkTreeNode rootNode) {
        rootNodes.remove(rootNode);
        depthFirstGraph.clear();
    }

    public void remove(final UiObjectWkt objectModel) {
        var bookmark = objectModel.getOwnerBookmark();
        rootNodes.removeIf(node->node.getBookmark().equals(bookmark));
        depthFirstGraph.clear();
    }

    // -- HELPER

    private Optional<BookmarkTreeNode> matchRootNode(final BookmarkableModel bookmarkableModel) {
        for (var rootNode : rootNodes) {
            if(rootNode.matches(bookmarkableModel)) {
                return Optional.of(rootNode);
            }
        }
        return Optional.empty();
    }

    private int getMaxSize() {
        return MetaModelContext.instanceElseFail().getWicketViewerSettings().bookmarkedPages().maxSize();
    }

    private static void trim(final List<?> list, final int requiredSize) {
        int numToRetain = Math.min(list.size(), requiredSize);
        list.retainAll(list.subList(0, numToRetain));
    }

}
