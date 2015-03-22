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
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;

public class BookmarkTreeNode implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final List<BookmarkTreeNode> children = Lists.newArrayList();
    private final int depth;

    private final RootOid oidNoVer;
    private final String oidNoVerStr;
    private final PageType pageType;
    
    private String title;
    private PageParameters pageParameters;

    public static BookmarkTreeNode newRoot(
            BookmarkableModel<?> bookmarkableModel) {
        return new BookmarkTreeNode(bookmarkableModel, 0);
    }

    private BookmarkTreeNode(
            final BookmarkableModel<?> bookmarkableModel, 
            final int depth) {
        pageParameters = bookmarkableModel.getPageParameters();
        RootOid oid = oidFrom(pageParameters);
        this.oidNoVerStr = getOidMarshaller().marshalNoVersion(oid);
        this.oidNoVer = getOidMarshaller().unmarshal(oidNoVerStr, RootOid.class);
        
        // replace oid with the noVer equivalent.
        PageParameterNames.OBJECT_OID.removeFrom(pageParameters);
        PageParameterNames.OBJECT_OID.addStringTo(pageParameters, getOidNoVerStr());
        
        this.title = bookmarkableModel.getTitle();
        this.pageType = bookmarkableModel instanceof EntityModel ? PageType.ENTITY : PageType.ACTION_PROMPT;
        this.depth = depth;
        
    }

    public RootOid getOidNoVer() {
        return oidNoVer;
    }

    public String getOidNoVerStr() {
        return oidNoVerStr;
    }

    public String getTitle() {
        return title;
    }
    private void setTitle(String title) {
        this.title = title;
    }

    public PageType getPageType() {
        return pageType;
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
     * @return - whether the provided candidate is found or was added to this node's tree.
     */
    public boolean matches(BookmarkableModel<?> candidateBookmarkableModel) {
    	if(candidateBookmarkableModel instanceof EntityModel) {
    		if(this.pageType != PageType.ENTITY) { 
    			return false; 
			}
			return matchAndUpdateTitleFor((EntityModel) candidateBookmarkableModel);
    	} else if(candidateBookmarkableModel instanceof ActionModel) {
    		if(this.pageType != PageType.ACTION_PROMPT) { 
    			return false; 
			}
			return matchFor((ActionModel) candidateBookmarkableModel);
    	} else {
    		return false;
    	}
    }

    /**
     * Whether or not the provided {@link EntityModel} matches that contained
     * within this node, or any of its children.
     * 
     * <p>
     * If it does match, then the matched node's title is updated to that of the provided
     * {@link EntityModel}.
     * 
     * @return - whether the provided candidate is found or was added to this node's tree.
     */
	private boolean matchAndUpdateTitleFor(final EntityModel candidateEntityModel) {
		
		// match only on the oid string
		final String candidateOidStr = oidStrFrom(candidateEntityModel);
        boolean inGraph = Objects.equal(this.oidNoVerStr, candidateOidStr);
        if(inGraph) {
            this.setTitle(candidateEntityModel.getTitle());
        }

        // and also match recursively down to all children and grandchildren.
        if(candidateEntityModel.hasAsChildPolicy()) {
            for(BookmarkTreeNode childNode: this.getChildren()) {
                inGraph = childNode.matches(candidateEntityModel) || inGraph; // evaluate each
            }
            
            if(!inGraph) {
                inGraph = addToGraphIfParented(candidateEntityModel);
            }
        }
        return inGraph;
	}
	
    /**
     * Whether or not the provided {@link ActionModel} matches that contained
     * within this node (taking into account the action's arguments).
     * 
     * If it does match, then the matched node's title is updated to that of the provided
     * {@link ActionModel}.
     * <p>
     * 
     * @return - whether the provided candidate is found or was added to this node's tree.
     */
	private boolean matchFor(final ActionModel candidateActionModel) {
		
		// check if target object of the action is the same (the oid str)
		final String candidateOidStr = oidStrFrom(candidateActionModel);
		if(!Objects.equal(this.oidNoVerStr, candidateOidStr)) {
			return false;
		}
		
		// check if args same
        List<String> thisArgs = PageParameterNames.ACTION_ARGS.getListFrom(pageParameters);
        PageParameters candidatePageParameters = candidateActionModel.getPageParameters();
        List<String> candidateArgs = PageParameterNames.ACTION_ARGS.getListFrom(candidatePageParameters);
        if(!Objects.equal(thisArgs, candidateArgs)) {
        	return false;
        }

        // ok, a match
		return true;
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
                final String possibleParentOidStr = possibleParentOid.enStringNoVersion(getOidMarshaller());
                if(Objects.equal(this.oidNoVerStr, possibleParentOidStr)) {
                    this.addChild(candidateBookmarkableModel);
                    whetherAdded = true;
                }
            }
        }
        return whetherAdded;
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

    
    // //////////////////////////////////////

    public PageParameters getPageParameters() {
        return pageParameters;
    }
    
    // //////////////////////////////////////

    public static RootOid oidFrom(final PageParameters pageParameters) {
        String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
        if(oidStr == null) {
            return null;
        }
        try {
            return getOidMarshaller().unmarshal(oidStr, RootOid.class);
        } catch(Exception ex) {
            return null;
        }
    }

    public static String oidStrFrom(BookmarkableModel<?> candidateBookmarkableModel) {
        final RootOid oid = oidFrom(candidateBookmarkableModel.getPageParameters());
        return oid != null? getOidMarshaller().marshalNoVersion(oid): null;
    }


    // //////////////////////////////////////

    protected static OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }




}