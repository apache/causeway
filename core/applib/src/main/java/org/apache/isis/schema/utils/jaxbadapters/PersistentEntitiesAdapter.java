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
package org.apache.isis.schema.utils.jaxbadapters;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.Lists;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.OidsDto;

public class PersistentEntitiesAdapter extends XmlAdapter<OidsDto, List<Object>> {

    @Override
    public List<Object> unmarshal(final OidsDto oidsDto) throws Exception {

        List<Object> domainObjects = Lists.newArrayList();
        for (final OidDto oidDto : oidsDto.getOid()) {
            final Bookmark bookmark = Bookmark.from(oidDto);
            Object domainObject = bookmarkService.lookup(bookmark, BookmarkService2.FieldResetPolicy.DONT_RESET);
            domainObjects.add(domainObject);
        }
        return domainObjects;
    }

    @Override
    public OidsDto marshal(final List<Object> domainObjects) throws Exception {
        if(domainObjects == null) {
            return null;
        }
        OidsDto oidsDto = new OidsDto();
        for (final Object domainObject : domainObjects) {
            final Optional<Bookmark> bookmark = getBookmarkService().bookmarkFor(domainObject);
            if(!bookmark.isPresent())
                return null;
            oidsDto.getOid().add(bookmark.get().toOidDto());
        }
        return oidsDto;
    }

    private static String coalesce(final String first, final String second) {
        return first != null? first: second;
    }


    protected BookmarkService getBookmarkService() {
        return bookmarkService;
    }

    @Inject
    BookmarkService2 bookmarkService;
}
