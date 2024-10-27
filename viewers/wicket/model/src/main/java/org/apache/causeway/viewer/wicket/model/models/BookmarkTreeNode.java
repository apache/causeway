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
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.functions._Functions;

import lombok.Getter;
import lombok.NonNull;

public class BookmarkTreeNode
implements
    Comparable<BookmarkTreeNode>, Serializable {

    private static final long serialVersionUID = 1L;

    @Getter private final List<BookmarkTreeNode> children = _Lists.newArrayList();
    @Getter private final int depth; // starting at root with depth = 0
    @Getter private final @NonNull Bookmark bookmark;
    @Getter private final @NonNull PageParameters pageParameters;

    @Getter private String title;

    /** its either a iconResourceReference or a iconFaClass or neither (decomposed for easy serialization) */
    private ResourceReference iconResourceReference;
    /** its either a iconResourceReference or a FontAwesomeLayers or neither (decomposed for easy serialization) */
    private FontAwesomeLayers faLayers;

    //private final Set<Bookmark> propertyBookmarks; ... in support of parents referencing their child

    // -- FACTORIES

    public static BookmarkTreeNode newRoot(
            final @NonNull Bookmark bookmark,
            final @NonNull BookmarkableModel bookmarkableModel) {
        return new BookmarkTreeNode(bookmark, bookmarkableModel, 0);
    }

    // -- CONSTRUCTION

    private BookmarkTreeNode(
            final @NonNull Bookmark bookmark,
            final @NonNull BookmarkableModel bookmarkableModel,
            final int depth) {

        this.pageParameters = bookmarkableModel.getPageParametersWithoutUiHints();
        this.bookmark = bookmark;
//        this.propertyBookmarks = bookmarkableModel.streamPropertyBookmarks()
//                .collect(Collectors.toCollection(HashSet::new));

        this.title = bookmarkableModel.getTitle();

        _Casts.castTo(UiObjectWkt.class, bookmarkableModel)
        .map(UiObjectWkt::getIconAsResourceReference)
        .ifPresent(either->either.accept(
                iconResourceReference->
                    this.iconResourceReference = iconResourceReference,
                faLayers->
                    this.faLayers = faLayers
                )
        );

        this.depth = depth;
    }

    // -- ICON

    public Either<ResourceReference, FontAwesomeLayers> eitherIconOrFaClass() {
        return faLayers==null
                ? Either.left(iconResourceReference)
                : Either.right(faLayers);
    }

    // -- COMPARATOR

    @Override
    public int compareTo(final BookmarkTreeNode o2) {

        var o1 = this;

        // sort by entity type
        var typeName1 = o1.getBookmark().getLogicalTypeName();
        var typeName2 = o2.getBookmark().getLogicalTypeName();

        final int typeNameComparison = typeName1.compareTo(typeName2);
        if(typeNameComparison != 0) {
            return typeNameComparison;
        }

        return o1.getTitle().compareTo(o2.getTitle());
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

    public void appendGraphTo(final List<BookmarkTreeNode> list) {
        list.add(this);
        for (BookmarkTreeNode childNode : children) {
            childNode.appendGraphTo(list);
        }
    }

    // -- HELPER

    private Optional<BookmarkTreeNode> addChild(final BookmarkableModel childModel) {
        return childModel.toBookmark()
                .map(bookmark->new BookmarkTreeNode(bookmark, childModel, depth+1))
                .map(_Functions.peek(children::add));
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
        var candidateBookmark = candidateEntityModel.toBookmark().orElse(null);
        boolean inGraph = getBookmark().equals(candidateBookmark);
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

//    /**
//     * Whether or not the provided {@link ActionModelImpl} matches that contained
//     * within this node (taking into account the action's arguments).
//     *
//     * If it does match, then the matched node's title is updated to that of the provided
//     * {@link ActionModelImpl}.
//     * <p>
//     *
//     * @return - whether the provided candidate is found or was added to this node's tree.
//     */
//    private boolean matchFor(final ActionModelImpl candidateActionModel) {
//
//        var candidateBookmark = candidateActionModel.toBookmark().orElse(null);
//
//        // check if target object of the action is the same
//        if(!Objects.equals(getBookmark(), candidateBookmark)) {
//            return false;
//        }
//
//        // check if args same
//        List<String> thisArgs = PageParameterNames.ACTION_ARGS.getListFrom(pageParameters);
//        PageParameters candidatePageParameters = candidateActionModel.getPageParameters();
//        List<String> candidateArgs = PageParameterNames.ACTION_ARGS.getListFrom(candidatePageParameters);
//        if(!Objects.equals(thisArgs, candidateArgs)) {
//            return false;
//        }
//
//        // ok, a match
//        return true;
//    }

    /**
     * For given candidate model look into its properties and see whether one matches this node's bookmark.
     * If so, we found a parent/child relation for the tree to populate
     */
    private boolean addToGraphIfParented(final BookmarkableModel candidateBookmarkableModel) {

        var addedCount = new LongAdder();

        candidateBookmarkableModel.streamPropertyBookmarks()
        .filter(getBookmark()::equals)
        .forEach(propBookmark->{
            if(this.addChild(candidateBookmarkableModel).isPresent()) {
                addedCount.increment();
            }
        });

        if(addedCount.longValue()>0L) {
            return true;
        }

//        /* also check the other way around, that is,
//         * whether the child is referenced from one of the parent's properties
//         */
//        if(candidateBookmarkableModel.toBookmark()
//                .map(propertyBookmarks::contains)
//                .orElse(false)) {
//            return this.addChild(candidateBookmarkableModel).isPresent();
//        }
        return false;
    }

}
