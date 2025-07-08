/*
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
package org.apache.causeway.applib.jaxb;

import jakarta.inject.Inject;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.schema.common.v2.OidDto;

/**
 * @since 2.0 {@index}
 */
public class PersistentEntityAdapter extends XmlAdapter<OidDto, Object> {

    @Inject private BookmarkService bookmarkService;

    @Override
    public Object unmarshal(final OidDto oidDto) throws Exception {
        if(bookmarkService==null) return null;

        var bookmark = Bookmark.forOidDto(oidDto);
        return bookmarkService.lookup(bookmark).orElse(null);
    }

    @Override
    public OidDto marshal(final Object domainObject) throws Exception {
        if(domainObject == null) return null;
        if(bookmarkService==null) return null;

        var bookmark = bookmarkService.bookmarkForElseFail(domainObject);
        return bookmark.toOidDto();
    }

}
