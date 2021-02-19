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

package org.apache.isis.viewer.wicket.model.models;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;

import lombok.val;


public class BookmarkedPagesModel extends ModelAbstract<List<BookmarkTreeNode>> {

    private static final long serialVersionUID = 1L;

    private final List<BookmarkTreeNode> rootNodes = _Lists.newArrayList();

    private transient PageParameters current;

    private int numPinned;
    
    public BookmarkedPagesModel(IsisAppCommonContext commonContext) {
        super(commonContext);
        this.numPinned = 0;
    }

    public void bookmarkPage(final BookmarkableModel bookmarkableModel) {

        // hack: remove any garbage that might've got stored in 'rootNodes'
        cleanUpGarbage(rootNodes);

        final PageParameters candidatePP = bookmarkableModel.getPageParametersWithoutUiHints();
        RootOid oid = BookmarkTreeNode.oidFrom(candidatePP);
        if(oid == null) {
            // ignore
            return;
        }

        BookmarkTreeNode rootNode = null;
        for (BookmarkTreeNode eachNode : rootNodes) {
            if(eachNode.matches(bookmarkableModel)) {
                rootNode = eachNode;
            }
        }
        // MRU/LRU algorithm
        if(rootNode != null) {
            rootNodes.remove(rootNode);
            rootNodes.add(0, rootNode);
            current = candidatePP;
        } else {
            if (bookmarkableModel.hasAsRootPolicy()) {
                rootNode = BookmarkTreeNode.newRoot(bookmarkableModel);
                rootNodes.add(0, rootNode);
                current = candidatePP;
            }
        }

        trim(rootNodes, getMaxSize());
    }

    private int getMaxSize() {
        return getConfiguration().getViewer().getWicket().getBookmarkedPages().getMaxSize() + this.numPinned;
    }

    private static void trim(List<?> list, int requiredSize) {
        int numToRetain = Math.min(list.size(), requiredSize);
        list.retainAll(list.subList(0, numToRetain));
    }

    @Override
    protected List<BookmarkTreeNode> load() {
        List<BookmarkTreeNode> depthFirstGraph = _Lists.newArrayList();

        List<BookmarkTreeNode> sortedNodes = _Lists.newArrayList(rootNodes);
        Collections.sort(sortedNodes, new BookmarkTreeNodeComparator(getSpecificationLoader()));

        for (BookmarkTreeNode rootNode : sortedNodes) {
            rootNode.appendGraphTo(depthFirstGraph);
        }
        return depthFirstGraph;
    }

    public boolean isCurrent(PageParameters pageParameters) {
        return Objects.equals(current, pageParameters);
    }

    private static void cleanUpGarbage(List<BookmarkTreeNode> rootNodes) {
        final Iterator<BookmarkTreeNode> iter = rootNodes.iterator();
        while(iter.hasNext()) {
            BookmarkTreeNode node = iter.next();
            // think this is redundant...
            if(node.getOidNoVer() == null) {
                iter.remove();
            }
        }
    }

    public void pin(BookmarkTreeNode rootNode) {
        this.rootNodes.remove(rootNode);
        this.rootNodes.add(0, rootNode);
        rootNode.pin();
        this.numPinned++;
    }

    public void unpin(BookmarkTreeNode rootNode) {
        this.rootNodes.remove(rootNode);
        this.rootNodes.add(this.numPinned, rootNode);
        rootNode.unpin();
        this.numPinned--;
    }

    public void clear() {
        rootNodes.clear();
        current = null;
    }

    public boolean isEmpty() {
        return rootNodes.isEmpty();
    }

    public void remove(BookmarkTreeNode rootNode) {
        this.rootNodes.remove(rootNode);
    }

    public void remove(EntityModel entityModel) {
        val str = entityModel.oidStringIfSupported();
        rootNodes.removeIf(node->node.getOidNoVerStr().equals(str));
    }


}
