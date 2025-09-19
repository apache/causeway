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
import java.util.function.Consumer;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.render.ObjectIconEmbedded;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.functions._Functions;

import lombok.Getter;

public class BookmarkTreeNode
implements
    Comparable<BookmarkTreeNode>, Serializable {

    private static final long serialVersionUID = 1L;

    @Getter private final List<BookmarkTreeNode> children = _Lists.newArrayList();
    @Getter private final int depth; // starting at root with depth = 0
    @Getter private final @NonNull Bookmark bookmark;
    @Getter private final @NonNull PageParameters pageParameters;

    @Getter private String title;

    private ResourceReference iconResourceReference;
    private FontAwesomeLayers faLayers;
    private ObjectIconEmbedded embedded;

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
        this.title = bookmarkableModel.getTitle();

        _Casts.castTo(UiObjectWkt.class, bookmarkableModel)
        .ifPresent(x->x.visitIconVariantOrElse(
                IconSize.MEDIUM,
                rref->{this.iconResourceReference = rref;},
                embedded->{this.embedded = embedded;},
                faLayers->{this.faLayers = faLayers;},
                ()->{}));

        this.depth = depth;
    }

    public void visitIconVariantOrElse(
            Consumer<ResourceReference> a,
            Consumer<ObjectIconEmbedded> b,
            Consumer<FontAwesomeLayers> c,
            Runnable onNoMatch) {
        if(this.iconResourceReference!=null) a.accept(iconResourceReference);
        else if(this.embedded!=null) b.accept(embedded);
        else if(this.faLayers!=null) c.accept(faLayers);
        else onNoMatch.run();
    }

    // -- COMPARATOR

    @Override
    public int compareTo(final BookmarkTreeNode o2) {

        var o1 = this;

        // sort by entity type
        var typeName1 = o1.getBookmark().logicalTypeName();
        var typeName2 = o2.getBookmark().logicalTypeName();

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

        return addedCount.longValue()>0L;
    }

}
