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
package org.apache.isis.applib.mixins.metamodel;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.mixins.layout.LayoutMixinConstants;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.commons.internal.base._Strings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * @since 1.x {@index}
 */
@Property
@PropertyLayout(hidden = Where.ALL_TABLES)
@RequiredArgsConstructor
public class Object_objectIdentifier {

    @Inject private BookmarkService bookmarkService;
    @Inject private MetaModelService mmService;

    private final Object holder;

    public static class ActionDomainEvent
    extends org.apache.isis.applib.IsisModuleApplib.ActionDomainEvent<Object_objectIdentifier> {}

    @MemberOrder(name = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME, sequence = "700.2")
    public String prop() {
        val bookmark = bookmarkService.bookmarkForElseThrow(this.holder);
        val sort = mmService.sortOf(bookmark, MetaModelService.Mode.RELAXED);
        if(!sort.isEntity()) {
            return shortend(bookmark.getIdentifier());
        }
        return bookmark.getIdentifier();
    }

    // -- HELPER

    private String shortend(@NonNull String identifier) {

        val hashHexed = Integer.toHexString(identifier.hashCode());
        val hashPadded = _Strings.padStart(hashHexed, 8, '0');
        return "»" + hashPadded;
    }


}
