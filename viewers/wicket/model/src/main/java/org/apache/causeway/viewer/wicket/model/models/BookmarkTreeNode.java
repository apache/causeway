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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.functions._Functions;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class BookmarkTreeNode
implements
    Comparable<BookmarkTreeNode>, Serializable {

    private static final long serialVersionUID = 1L;

    @Getter private final List<BookmarkTreeNode> children = _Lists.newArrayList();
    @Getter private final int depth; // starting at root with depth = 0
    @Getter private final @NonNull Bookmark bookmark;
    @Getter private final @NonNull PageParameters pageParameters;

    @Getter private String title;

    public static BookmarkTreeNode newRoot(
            final @NonNull Bookmark bookmark,
            final @NonNull BookmarkableModel bookmarkableModel) {
        return new BookmarkTreeNode(bookmark, bookmarkableModel, 0);
    }

    private BookmarkTreeNode(
            final @NonNull Bookmark bookmark,
            final @NonNull BookmarkableModel bookmarkableModel,
            final int depth) {

        this.pageParameters = bookmarkableModel.getPageParametersWithoutUiHints();
        this.bookmark = bookmark;

        // replace oid with the noVer equivalent.
        PageParameterNames.OBJECT_OID.removeFrom(pageParameters);
        PageParameterNames.OBJECT_OID.addStringTo(pageParameters, bookmark.stringify());

        this.title = bookmarkableModel.getTitle();
        this.depth = depth;
    }

    private Optional<BookmarkTreeNode> addChild(final BookmarkableModel childModel) {
        return PageParameterUtils.toBookmark(childModel.getPageParametersWithoutUiHints())
                .map(bookmark->new BookmarkTreeNode(bookmark, childModel, depth+1))
                .map(_Functions.peek(children::add));
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
    public boolean matches(final BookmarkableModel candidateBookmarkableModel) {
        if(candidateBookmarkableModel instanceof UiObjectWkt) {
            return matchAndUpdateTitleFor((UiObjectWkt) candidateBookmarkableModel);
        }
        return false;
    }

    /**
     * Whether or not the provided {@link UiObjectWkt} matches that contained
     * within this node, or any of its children.
     *
     * <p>
     * If it does match, then the matched node's title is updated to that of the provided
     * {@link UiObjectWkt}.
     *
     * @return - whether the provided candidate is found or was added to this node's tree.
     */
    private boolean matchAndUpdateTitleFor(final UiObjectWkt candidateEntityModel) {
        val candidateBookmark = candidateEntityModel.toBookmark().orElse(null);
        boolean inGraph = Objects.equals(getBookmark(), candidateBookmark);
        if(inGraph) {
            this.title = candidateEntityModel.getTitle();
        }

        // and also match recursively down to all children and grand-children.
        if(candidateEntityModel.getBookmarkPolicy().isChild()) {
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
     * Whether or not the provided {@link ActionModelImpl} matches that contained
     * within this node (taking into account the action's arguments).
     *
     * If it does match, then the matched node's title is updated to that of the provided
     * {@link ActionModelImpl}.
     * <p>
     *
     * @return - whether the provided candidate is found or was added to this node's tree.
     */
    private boolean matchFor(final ActionModelImpl candidateActionModel) {

        val candidateBookmark = candidateActionModel.toBookmark().orElse(null);

        // check if target object of the action is the same
        if(!Objects.equals(getBookmark(), candidateBookmark)) {
            return false;
        }

        // check if args same
        List<String> thisArgs = PageParameterNames.ACTION_ARGS.getListFrom(pageParameters);
        PageParameters candidatePageParameters = candidateActionModel.getPageParameters();
        List<String> candidateArgs = PageParameterNames.ACTION_ARGS.getListFrom(candidatePageParameters);
        if(!Objects.equals(thisArgs, candidateArgs)) {
            return false;
        }

        // ok, a match
        return true;
    }

    private boolean addToGraphIfParented(final BookmarkableModel candidateBookmarkableModel) {

        val whetherAdded = _Refs.booleanRef(false);

        // TODO: this ought to be move into a responsibility of BookmarkableModel, perhaps, rather than downcasting
        if(candidateBookmarkableModel instanceof UiObjectWkt) {
            val entityModel = (UiObjectWkt) candidateBookmarkableModel;
            val candidateAdapter = entityModel.getObject();

            candidateAdapter.getSpecification()
            .streamAssociations(MixedIn.EXCLUDED)
            .filter(ObjectAssociation.Predicates.REFERENCE_PROPERTIES) // properties only
            .map(objectAssoc->{
                val parentAdapter =
                        objectAssoc.get(candidateAdapter, InteractionInitiatedBy.USER);
                return parentAdapter;
            })
            .filter(_NullSafe::isPresent)
            .map(parentAdapter->ManagedObjects.bookmark(parentAdapter).orElse(null))
            .filter(_NullSafe::isPresent)
            .forEach(parentBookmark->{
                if(Objects.equals(getBookmark(), parentBookmark)) {
                    whetherAdded.setValue(this.addChild(candidateBookmarkableModel).isPresent());
                }
            });
        }
        return whetherAdded.isTrue();
    }

    public void appendGraphTo(final List<BookmarkTreeNode> list) {
        list.add(this);
        for (BookmarkTreeNode childNode : children) {
            childNode.appendGraphTo(list);
        }
    }

    // -- COMPARATOR

    @Override
    public int compareTo(final BookmarkTreeNode o2) {

        val o1 = this;

        // sort by entity type
        val typeName1 = o1.getBookmark().getLogicalTypeName();
        val typeName2 = o2.getBookmark().getLogicalTypeName();

        final int typeNameComparison = typeName1.compareTo(typeName2);
        if(typeNameComparison != 0) {
            return typeNameComparison;
        }

        return o1.getTitle().compareTo(o2.getTitle());
    }

}
