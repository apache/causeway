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

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;

public class BookmarkTreeNode implements Serializable {
    
        private static final long serialVersionUID = 1L;
        
        public static final Function<? super BookmarkTreeNode, ? extends PageParameters> AS_PAGE_PARAMETERS = new Function<BookmarkTreeNode, PageParameters>() {
            public PageParameters apply(BookmarkTreeNode node) {
                return node.pageParameters;
            }
        };
        final PageParameters pageParameters;
        private final List<BookmarkTreeNode> children = Lists.newArrayList();

        private final int depth;
        
        public static BookmarkTreeNode newRoot(BookmarkableModel<?> bookmarkableModel) {
            return new BookmarkTreeNode(bookmarkableModel, 0);
        }

        private BookmarkTreeNode(BookmarkableModel<?> node, int depth) {
            this.depth = depth;
            this.pageParameters = node.getPageParameters();
        }
        public PageParameters getPageParameters() {
            return pageParameters;
        }
        public List<BookmarkTreeNode> getChildren() {
            return children;
        }
        public BookmarkTreeNode addChild(BookmarkableModel<?> childModel) {
            final BookmarkTreeNode childNode = new BookmarkTreeNode(childModel, depth+1);
            children.add(childNode);
            return childNode;
        }
        
        /**
         * Whether or not the provided {@link BookmarkableModel} matches that contained
         * within this node, or any of its children.
         * 
         * <p>
         * If it does, then the matched node's title is updated to that of the provided
         * {@link BookmarkableModel}.
         * 
         * <p>
         * The {@link PageParameters} (used for matching) is 
         * {@link BookmarkableModel#getPageParameters() obtained} from the {@link BookmarkableModel}.
         * 
         * <p>
         * The convoluted logic in this method is because we temporarily remove the title 
         * in order to do the check.
         * 
         * @return - whether the provided candidate is found or was added to this node's tree.
         */
        public boolean matchAndUpdateTitle(BookmarkableModel<?> candidateBookmarkableModel) {

            boolean inGraph = false;

            // attempt to match this node.
            final PageParameters candidatePP = candidateBookmarkableModel.getPageParameters();
            inGraph = matchAndUpdateTitle(candidatePP, this.pageParameters);

            // and also match recursively down to all children and grandchildren.
            if(candidateBookmarkableModel.hasAsChildPolicy()) {
                for(BookmarkTreeNode childNode: this.getChildren()) {
                    inGraph = childNode.matchAndUpdateTitle(candidateBookmarkableModel) || inGraph; // evaluate each
                }
                
                if(!inGraph) {
                    inGraph = addToGraphIfParented(candidateBookmarkableModel);
                }
            }
            return inGraph;
        }

        private boolean addToGraphIfParented(BookmarkableModel<?> candidateBookmarkableModel) {
            
            boolean whetherAdded = false;
            // TODO: this ought to be move into a responsibility of BookmarkableModel, perhaps, rather than downcasting
            if(candidateBookmarkableModel instanceof EntityModel) {
                EntityModel entityModel = (EntityModel) candidateBookmarkableModel;
                final ObjectAdapter candidateAdapter = entityModel.getObject();
                final List<ObjectAssociation> properties = candidateAdapter.getSpecification().getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.REFERENCE_PROPERTIES);
                for (ObjectAssociation objectAssoc : properties) {
                    final ObjectAdapter possibleParentAdapter = objectAssoc.get(candidateAdapter);
                    if(possibleParentAdapter == null) {
                        continue;
                    } 
                    final Oid possibleParentOid = possibleParentAdapter.getOid();
                    if(possibleParentOid == null) {
                        continue;
                    } 
                    final String possibleParentOidStr = possibleParentOid.enStringNoVersion(IsisContext.getOidMarshaller());
                    final String thisOidStr = PageParameterNames.OBJECT_OID.getStringFrom(this.pageParameters);
                    if(Objects.equal(thisOidStr, possibleParentOidStr)) {
                        this.addChild(candidateBookmarkableModel);
                        whetherAdded = true;
                    }
                }
            }
            return whetherAdded;
        }

        protected boolean matchAndUpdateTitle(final PageParameters candidatePP, PageParameters pageParameters) {

            final String candidateTitle = PageParameterNames.PAGE_TITLE.getStringFrom(candidatePP);
            String pageTitle = PageParameterNames.PAGE_TITLE.getStringFrom(pageParameters);

            final String pageTitleKey = PageParameterNames.PAGE_TITLE.toString();
            try {
                // temporarily remove to do comparison
                candidatePP.remove(pageTitleKey);

                pageParameters.remove(pageTitleKey);
                if(pageParameters.equals(candidatePP)) {
                    // update the existing
                    pageTitle = candidateTitle;
                    return true;
                }

            } finally {
                // restore
                PageParameterNames.PAGE_TITLE.addStringTo(pageParameters, pageTitle);
                PageParameterNames.PAGE_TITLE.addStringTo(candidatePP, candidateTitle);
            }

            // did not match, title not updated.
            return false;
        }

        public void appendGraphTo(List<BookmarkTreeNode> list) {
            list.add(this);
            for (BookmarkTreeNode childNode : children) {
                childNode.appendGraphTo(list);
            }
        }

        public int getDepth() {
            return depth;
        }
    }