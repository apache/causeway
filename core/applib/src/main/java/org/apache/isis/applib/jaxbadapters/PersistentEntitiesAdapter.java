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
package org.apache.isis.applib.jaxbadapters;

import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.OidsDto;

public class PersistentEntitiesAdapter extends XmlAdapter<OidsDto, List<Object>> {

    @Inject private BookmarkService bookmarkService;
    
    @Override
    public List<Object> unmarshal(final OidsDto oidsDto) {

        List<Object> domainObjects = _Lists.newArrayList();
        for (final OidDto oidDto : oidsDto.getOid()) {
            final Bookmark bookmark = Bookmark.from(oidDto);
            Object domainObject = bookmarkService.lookup(bookmark);
            domainObjects.add(domainObject);
        }
        return domainObjects;
    }

    @Override
    public OidsDto marshal(final List<Object> domainObjects) {
        if(domainObjects == null) {
            return null;
        }
        OidsDto oidsDto = new OidsDto();
        for (final Object domainObject : domainObjects) {
            final Bookmark bookmark = getBookmarkService().bookmarkFor(domainObject);
            oidsDto.getOid().add(bookmark.toOidDto());
        }
        return oidsDto;
    }

    protected BookmarkService getBookmarkService() {
        return bookmarkService;
    }
    
}
