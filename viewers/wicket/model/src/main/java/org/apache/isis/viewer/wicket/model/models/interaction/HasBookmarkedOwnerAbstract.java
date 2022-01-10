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
package org.apache.isis.viewer.wicket.model.models.interaction;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.ModelAbstract;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class HasBookmarkedOwnerAbstract<T>
extends ModelAbstract<T>
implements
    HasBookmarkedOwner {

    private static final long serialVersionUID = 1L;

    final BookmarkedObjectWkt bookmarkedObject;

    @Override
    public final Bookmark getOwnerBookmark() {
        return bookmarkedObject.getBookmark();
    }

    @Override
    public final ManagedObject getBookmarkedOwner() {
        return bookmarkedObject.getObjectAndRefetch();
    }

    public final BookmarkedObjectWkt bookmarkedObjectModel() {
        return bookmarkedObject;
    }

    // -- SHORTCUTS

    public final boolean hasAsRootPolicy() {
        return hasBookmarkPolicy(BookmarkPolicy.AS_ROOT);
    }

    public final boolean hasAsChildPolicy() {
        return hasBookmarkPolicy(BookmarkPolicy.AS_CHILD);
    }

    public final ObjectSpecification getTypeOfSpecification() {
        //return getBookmarkedOwner().getSpecification();
        return bookmarkedObject.getObject().getSpecification(); // serving this from an unattached entity seems safe
    }

    // -- HELPER

    private boolean hasBookmarkPolicy(final BookmarkPolicy policy) {
        return getTypeOfSpecification().lookupFacet(BookmarkPolicyFacet.class)
                .map(facet->facet.value() == policy)
                .orElse(false);
    }

}
