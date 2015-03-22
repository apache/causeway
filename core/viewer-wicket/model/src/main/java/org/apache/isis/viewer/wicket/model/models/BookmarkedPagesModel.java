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
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.system.context.IsisContext;


public class BookmarkedPagesModel extends ModelAbstract<List<? extends BookmarkTreeNode>> {


    private static final long serialVersionUID = 1L;

    private static final BookmarkTreeNodeComparator COMPARATOR = new BookmarkTreeNodeComparator();

    private static final String MAX_SIZE_KEY = "isis.viewer.wicket.bookmarkedPages.maxSize";
    private static final int MAX_SIZE_DEFAULT_VALUE = 15;

    private final List<BookmarkTreeNode> rootNodes = Lists.newArrayList();
    
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
        return getConfiguration().getInteger(MAX_SIZE_KEY, MAX_SIZE_DEFAULT_VALUE);
    }

    private static void trim(List<?> list, int requiredSize) {
        int numToRetain = Math.min(list.size(), requiredSize);
        list.retainAll(list.subList(0, numToRetain));
    }

    @Override
    protected List<BookmarkTreeNode> load() {
        List<BookmarkTreeNode> depthFirstGraph = Lists.newArrayList();

        List<BookmarkTreeNode> sortedNodes = Lists.newArrayList(rootNodes);
        Collections.sort(sortedNodes, COMPARATOR);

        for (BookmarkTreeNode rootNode : sortedNodes) {
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

    public void remove(EntityModel entityModel) {
        BookmarkTreeNode rootNode = null;
        for (BookmarkTreeNode eachNode : rootNodes) {
            if(eachNode.getOidNoVerStr().equals((entityModel).getObjectAdapterMemento().toString())) {
                rootNode = eachNode;
            }
        }
        if(rootNode != null) {
            rootNodes.remove(rootNode);
        }
    }
    
    // //////////////////////////////////////

    
    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }


    
}
