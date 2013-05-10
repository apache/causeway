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
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;


public class BookmarkedPagesModel extends ModelAbstract<List<? extends BookmarkTreeNode>> {

    private static final BookmarkTreeNodeComparator COMPARATOR = new BookmarkTreeNodeComparator();

    private static final long serialVersionUID = 1L;
    
    private List<BookmarkTreeNode> rootNodes = Lists.newArrayList();
    private transient PageParameters current;
    
    public void bookmarkPage(final BookmarkableModel<?> bookmarkableModel) {

        final PageParameters candidatePP = bookmarkableModel.getPageParameters();
        if(!isValidParameters(candidatePP)) {
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
            rootNodes.add(BookmarkTreeNode.newRoot(bookmarkableModel));
            Collections.sort(rootNodes, COMPARATOR);
            current = candidatePP;
        }

        return;
    }


    /**
     * @return whether the {@link PageParameters} contain all required fields.
     */
    private static boolean isValidParameters(final PageParameters candidatePP) {

        // ignore if doesn't provide a page type for subsequent disambiguation
        PageType pageType = PageParameterNames.PAGE_TYPE.getEnumFrom(candidatePP, PageType.class);
        if(pageType==null) {
            return false;
        }
        
        // ignore if doesn't provide a title for rendering
        String candidateTitle = PageParameterNames.PAGE_TITLE.getStringFrom(candidatePP);
        if(candidateTitle==null) {
            return false;
        }
        
        return true;
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

    public static String titleFrom(final PageParameters pageParameters) {
        return PageParameterNames.PAGE_TITLE.getStringFrom(pageParameters);
    }

    public static RootOid oidFrom(final PageParameters pageParameters) {
        String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
        if(oidStr == null) {
            return null;
        }
        return IsisContext.getOidMarshaller().unmarshal(oidStr, RootOid.class);
    }

    public void clear() {
        rootNodes.clear();
    }

    public boolean isEmpty() {
        return rootNodes.isEmpty();
    }


}