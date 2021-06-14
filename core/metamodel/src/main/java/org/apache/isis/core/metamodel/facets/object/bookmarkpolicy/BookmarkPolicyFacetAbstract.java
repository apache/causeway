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
package org.apache.isis.core.metamodel.facets.object.bookmarkpolicy;

import java.util.function.BiConsumer;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public abstract class BookmarkPolicyFacetAbstract
extends FacetAbstract
implements BookmarkPolicyFacet {

    public static Class<? extends Facet> type() {
        return BookmarkPolicyFacet.class;
    }

    private final BookmarkPolicy bookmarkPolicy;

    protected BookmarkPolicyFacetAbstract(
            final BookmarkPolicy bookmarkPolicy,
            final FacetHolder facetHolder) {
        super(BookmarkPolicyFacetAbstract.type(), facetHolder);
        this.bookmarkPolicy = bookmarkPolicy;
    }

    protected BookmarkPolicyFacetAbstract(
            final BookmarkPolicy bookmarkPolicy,
            final FacetHolder facetHolder,
            final Facet.Precedence precedence) {
        super(BookmarkPolicyFacetAbstract.type(), facetHolder, precedence);
        this.bookmarkPolicy = bookmarkPolicy;
    }

    @Override
    public BookmarkPolicy value() {
        return bookmarkPolicy;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("bookmarkPolicy", bookmarkPolicy);
    }
}
