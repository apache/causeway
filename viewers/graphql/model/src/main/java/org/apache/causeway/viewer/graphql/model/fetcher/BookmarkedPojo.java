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
package org.apache.causeway.viewer.graphql.model.fetcher;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.viewer.graphql.model.context.Context;

public class BookmarkedPojo {

    private final Bookmark bookmark;
    private final BookmarkService bookmarkService;

    public BookmarkedPojo(
            final Bookmark bookmark,
            final BookmarkService bookmarkService) {

        this.bookmark = bookmark;
        this.bookmarkService = bookmarkService;
    }

    public static Object sourceFrom(DataFetchingEnvironment dataFetchingEnvironment) {
        var source = dataFetchingEnvironment.getSource();
        return source instanceof BookmarkedPojo
                ? ((BookmarkedPojo) source).getTargetPojo()
                : source;
    }

    public static BookmarkedPojo sourceFrom(DataFetchingEnvironment dataFetchingEnvironment, Context context) {
        var sourcePojo = sourceFrom(dataFetchingEnvironment);
        return context.bookmarkService.bookmarkFor(sourcePojo)
                .map(bookmark -> new BookmarkedPojo(bookmark, context.bookmarkService))
                .orElseThrow();
    }

    public Object getTargetPojo() {
        return bookmarkService.lookup(bookmark).orElseThrow();
    }
}
