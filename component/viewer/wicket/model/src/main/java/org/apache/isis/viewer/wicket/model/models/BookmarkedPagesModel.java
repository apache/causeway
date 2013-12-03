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

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;


public class BookmarkedPagesModel extends ModelAbstract<List<? extends BookmarkTreeNode>> {

    private static final BookmarkTreeNodeComparator COMPARATOR = new BookmarkTreeNodeComparator();

    private static final long serialVersionUID = 1L;
    
    private List<BookmarkTreeNode> rootNodes = Lists.newArrayList();
    private transient PageParameters current;
    
    public void bookmarkPage(final BookmarkableModel<?> bookmarkableModel) {

        // hack: remove any garbage that might've got stored in 'rootNodes'
        cleanUpGarbage(rootNodes);
        
        final PageParameters candidatePP = bookmarkableModel.getPageParameters();
        RootOid oid = BookmarkTreeNode.oidFrom(candidatePP);
        if(oid == null) {
            // ignore
            return;
        }

        boolean foundInGraph = false;
        for (BookmarkTreeNode eachNode : rootNodes) {
            if(eachNode.matchAndUpdateTitle(bookmarkableModel)) {
                current = candidatePP;
                foundInGraph = true;
            }
        }

        if(!foundInGraph && bookmarkableModel.hasAsRootPolicy()) {
            BookmarkTreeNode rootNode = BookmarkTreeNode.newRoot(bookmarkableModel);
            rootNodes.add(rootNode);
            Collections.sort(rootNodes, COMPARATOR);
            current = candidatePP;
        }
        return;
    }

    @Override
    protected List<BookmarkTreeNode> load() {
        List<BookmarkTreeNode> depthFirstGraph = Lists.newArrayList();
        for (BookmarkTreeNode rootNode : rootNodes) {
            rootNode.appendGraphTo(depthFirstGraph);
        }
        return depthFirstGraph;
    }

    public boolean isCurrent(PageParameters pageParameters) {
        return Objects.equal(current, pageParameters);
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

    public void clear() {
        rootNodes.clear();
    }

    public boolean isEmpty() {
        return rootNodes.isEmpty();
    }


    public void remove(BookmarkTreeNode rootNode) {
        this.rootNodes.remove(rootNode);
    }



}