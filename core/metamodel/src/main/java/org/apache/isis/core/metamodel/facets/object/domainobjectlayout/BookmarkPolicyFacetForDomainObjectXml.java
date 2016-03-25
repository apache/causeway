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

package org.apache.isis.core.metamodel.facets.object.domainobjectlayout;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacetAbstract;

public class BookmarkPolicyFacetForDomainObjectXml extends BookmarkPolicyFacetAbstract {

    public static BookmarkPolicyFacet create(final DomainObjectLayoutData domainObjectLayout, final FacetHolder holder) {
        if (domainObjectLayout == null) {
            return null;
        }
        final BookmarkPolicy bookmarkPolicy = domainObjectLayout.getBookmarking();
        return bookmarkPolicy != null && bookmarkPolicy != BookmarkPolicy.NEVER ? new BookmarkPolicyFacetForDomainObjectXml(bookmarkPolicy, holder) : null;
    }

    private BookmarkPolicyFacetForDomainObjectXml(final BookmarkPolicy bookmarkPolicy, final FacetHolder holder) {
        super(bookmarkPolicy, holder);
    }

}
