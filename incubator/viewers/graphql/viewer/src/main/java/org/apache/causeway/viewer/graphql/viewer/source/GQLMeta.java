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
package org.apache.causeway.viewer.graphql.viewer.source;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;

import lombok.Data;

@Data
public class GQLMeta {

    private final Bookmark bookmark;
    private final BookmarkService bookmarkService;

    public String logicalTypeName(){
        return bookmark.getLogicalTypeName();
    }

    public String id(){
        return bookmark.getIdentifier();
    }

    public String version(){
        Object domainObject = bookmarkService.lookup(bookmark).orElse(null);
        if (domainObject == null) return null;

        // TODO: implement; we would like to be this independent of the persistence mechanism
        return null;
    }

}
