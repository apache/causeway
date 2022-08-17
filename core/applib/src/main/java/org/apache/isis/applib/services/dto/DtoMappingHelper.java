/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.dto;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.schema.common.v1.BookmarkObjectState;
import org.apache.isis.schema.common.v1.OidDto;

import java.util.Optional;

@DomainService(nature = NatureOfService.DOMAIN)
public class DtoMappingHelper {

    @Programmatic
    public OidDto oidDtoFor(final Object object) {
        final Optional<Bookmark> bookmark = bookmarkService.bookmarkFor(object);
        if(!bookmark.isPresent())
            return null;
        return asOidDto(bookmark.get());
    }

    private static OidDto asOidDto(final Bookmark reference) {
        OidDto argValue;
        if (reference != null) {
            argValue = new OidDto();
            argValue.setObjectType(reference.getObjectType());
            argValue.setObjectState(bookmarkObjectStateOf(reference));
            argValue.setObjectIdentifier(reference.getIdentifier());
        } else {
            argValue = null;
        }
        return argValue;
    }

    private static BookmarkObjectState bookmarkObjectStateOf(final Bookmark bookmark) {
        switch (bookmark.getObjectState()) {
        case PERSISTENT:
            return BookmarkObjectState.PERSISTENT;
        case TRANSIENT:
            return BookmarkObjectState.TRANSIENT;
        case VIEW_MODEL:
            return BookmarkObjectState.VIEW_MODEL;
        }
        throw new IllegalArgumentException(
                String.format("objectState '%s' not recognized", bookmark.getObjectState()));
    }

    @Inject
    BookmarkService bookmarkService;
}
