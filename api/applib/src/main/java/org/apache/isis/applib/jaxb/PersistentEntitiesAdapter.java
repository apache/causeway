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
package org.apache.isis.applib.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.schema.common.v2.OidsDto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

/**
 * @since 2.0 {@index}
 */
public class PersistentEntitiesAdapter extends XmlAdapter<OidsDto, Iterable<Object>> {

    @Inject @Getter(AccessLevel.PROTECTED)
    private BookmarkService bookmarkService;

    @Override
    public List<Object> unmarshal(final OidsDto oidsDto) {
        List<Object> domainObjects = new ArrayList<>();
        for (val oidDto : oidsDto.getOid()) {
            val bookmark = Bookmark.forOidDto(oidDto);
            bookmarkService.lookup(bookmark)
            .ifPresent(domainObjects::add);
        }
        return domainObjects;
    }

    @Override
    public OidsDto marshal(final Iterable<Object> domainObjects) {
        if(domainObjects == null) {
            return null;
        }
        val oidsDto = new OidsDto();
        for (val domainObject : domainObjects) {
            val bookmark = getBookmarkService().bookmarkForElseFail(domainObject);
            oidsDto.getOid().add(bookmark.toOidDto());
        }
        return oidsDto;
    }

}
