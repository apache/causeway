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

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.causeway.viewer.commons.model.mixin.HasIcon;
import org.apache.causeway.viewer.commons.model.mixin.HasTitle;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;

public interface BookmarkableModel
extends HasTitle, HasIcon {

    /**
     * So can be bookmarked / added to <tt>BookmarkedPagesModel</tt>.
     */
    PageParameters getPageParameters();

    PageParameters getPageParametersWithoutUiHints();

    /** governs how to populate the BookmarkPanel in the UI */
    BookmarkPolicy getBookmarkPolicy();

    default Optional<Bookmark> toBookmark() {
        return PageParameterUtils.toBookmark(getPageParametersWithoutUiHints());
    }

    /**
     * Stream bookmarks of all non mixed in properties of the underlying domain object.
     * (empty for action bookmarks)
     * <p>
     * Introduced to discover parent/child relations for the BookmarkPanel.
     */
    default Stream<Bookmark> streamPropertyBookmarks() {
        return Stream.empty();
    }

    /**
     * XXX refactoring hint
     * @apiNote There should be a unified common model for Object and Action icons,
     * as both could utilize the same resource lookup and caching mechanics in the background.
     * While objects do support font-awesome or image file icons,
     * I believe actions only support the former. Hence the asymmetry here.
     */
    @Override
    default Either<ObjectIcon, FontAwesomeLayers> getIcon() {
        return Either.left(null); // overwritten for domain objects
    }

}
