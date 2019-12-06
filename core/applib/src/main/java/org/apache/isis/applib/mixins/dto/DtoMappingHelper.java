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
package org.apache.isis.applib.mixins.dto;

import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.schema.common.v1.BookmarkObjectState;
import org.apache.isis.schema.common.v1.OidDto;
import org.springframework.stereotype.Service;

@Service
@Named("isisApplib.DtoMappingHelper")
@Log4j2
public class DtoMappingHelper {

    public OidDto oidDtoFor(final Object object) {
        final Bookmark bookmark = bookmarkService.bookmarkFor(object);
        return asOidDto(bookmark);
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
        default:
            throw _Exceptions.illegalArgument("objectState '%s' not recognized", bookmark.getObjectState());
        }

    }

    @Inject BookmarkService bookmarkService;
}
