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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

import lombok.val;

public class BookmarkedPagesModel extends ModelAbstract<List<BookmarkTreeNode>> {

    private static final long serialVersionUID = 1L;

    private final List<BookmarkTreeNode> rootNodes = _Lists.newArrayList();

    public BookmarkedPagesModel(final MetaModelContext commonContext) {
        super(commonContext);
    }

    public void bookmarkPage(final BookmarkableModel bookmarkableModel) {

        // hack: remove any garbage that might've got stored in 'rootNodes'
        cleanUpGarbage(rootNodes);

        val bookmark = bookmarkableModel.toBookmark().orElse(null);
        if(bookmark == null) {
            // ignore
            return;
        }

        BookmarkTreeNode rootNode = null;
        for (BookmarkTreeNode treeNode : rootNodes) {
            if(treeNode.matches(bookmarkableModel)) {
                rootNode = treeNode;
            }
        }

        // MRU/LRU algorithm
        if(rootNode != null) {
            rootNodes.remove(rootNode);
            rootNodes.add(0, rootNode);
        } else {
            if (bookmarkableModel.getBookmarkPolicy().isRoot()) {
                rootNode = BookmarkTreeNode.newRoot(bookmark, bookmarkableModel);
                rootNodes.add(0, rootNode);
            }
        }

        trim(rootNodes, getMaxSize());
    }

    private int getMaxSize() {
        return getWicketViewerSettings().getBookmarkedPages().getMaxSize();
    }

    private static void trim(final List<?> list, final int requiredSize) {
        int numToRetain = Math.min(list.size(), requiredSize);
        list.retainAll(list.subList(0, numToRetain));
    }

    @Override
    protected List<BookmarkTreeNode> load() {
        List<BookmarkTreeNode> depthFirstGraph = _Lists.newArrayList();

        List<BookmarkTreeNode> sortedNodes = _Lists.newArrayList(rootNodes);
        Collections.sort(sortedNodes);

        for (BookmarkTreeNode rootNode : sortedNodes) {
            rootNode.appendGraphTo(depthFirstGraph);
        }
        return depthFirstGraph;
    }

    private static void cleanUpGarbage(final List<BookmarkTreeNode> rootNodes) {
        final Iterator<BookmarkTreeNode> iter = rootNodes.iterator();
        while(iter.hasNext()) {
            BookmarkTreeNode node = iter.next();
            // think this is redundant...
            if(node.getBookmark() == null) {
                iter.remove();
            }
        }
    }

    public void clear() {
        rootNodes.clear();
    }

    public boolean isEmpty() {
        return rootNodes.isEmpty();
    }

    public void remove(final BookmarkTreeNode rootNode) {
        rootNodes.remove(rootNode);
    }

    public void remove(final UiObjectWkt entityModel) {
        val bookmark = entityModel.getOwnerBookmark();
        rootNodes.removeIf(node->Objects.equals(node.getBookmark(), bookmark));
    }


}
