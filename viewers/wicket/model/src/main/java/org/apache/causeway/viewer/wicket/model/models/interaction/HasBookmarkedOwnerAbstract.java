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
package org.apache.causeway.viewer.wicket.model.models.interaction;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.wicket.model.models.ModelAbstract;

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
        return bookmarkedObject.asManagedObject();
    }

    public final BookmarkedObjectWkt bookmarkedObjectModel() {
        return bookmarkedObject;
    }

    // -- SHORTCUTS

    public final boolean hasAsRootPolicy() {
        return Facets.bookmarkPolicyMatches(BookmarkPolicy.AS_ROOT::equals)
                .test(getTypeOfSpecification());
    }

    public final boolean hasAsChildPolicy() {
        return Facets.bookmarkPolicyMatches(BookmarkPolicy.AS_CHILD::equals)
                .test(getTypeOfSpecification());
    }

    public final ObjectSpecification getTypeOfSpecification() {
        //return getBookmarkedOwner().getSpecification();
        return bookmarkedObject.getObject().getSpecification(); // serving this from an unattached entity seems safe
    }

}
