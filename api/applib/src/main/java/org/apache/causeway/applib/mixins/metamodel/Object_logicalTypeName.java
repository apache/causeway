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
package org.apache.causeway.applib.mixins.metamodel;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.MetaModelService;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Contributes a property exposing the logical object type of the domain
 * object, typically as specified by {@link javax.inject.Named}.
 *
 * <p>
 *     The logical type name is also accessible from the
 *     {@link org.apache.causeway.applib.services.bookmark.Bookmark} of the
 *     object.
 * </p>
 *
 * @see DomainObject
 * @see org.apache.causeway.applib.mixins.metamodel.Object_objectIdentifier
 * @see org.apache.causeway.applib.services.bookmark.Bookmark
 * @see org.apache.causeway.applib.services.bookmark.BookmarkService
 *
 * @since 1.x {@index}
 */
@Property
@PropertyLayout(
        describedAs = "The logical name of this domain class (as used in persistence, URLs etc).  Intended to be stable/unchanging across time",
        hidden = Where.ALL_TABLES,
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        sequence = "400.1"
)
//mixin's don't need a logicalTypeName
@RequiredArgsConstructor
public class Object_logicalTypeName {

    @Inject BookmarkService bookmarkService;
    @Inject MetaModelService metaModelService;

    private final Object holder;

    public static class ActionDomainEvent
    extends org.apache.causeway.applib.CausewayModuleApplib.ActionDomainEvent<Object_logicalTypeName> {}

    @Action(
            domainEvent = ActionDomainEvent.class   // this is a workaround to allow the mixin to be subscribed to (CAUSEWAY-2650)
    )
    @MemberSupport public String prop() {
        val bookmark = bookmarkService.bookmarkForElseFail(this.holder);
        return bookmark.getLogicalTypeName();
    }

    @MemberSupport public boolean hideProp() {
        val bookmark = bookmarkService.bookmarkForElseFail(this.holder);
        val beanSort = metaModelService.sortOf(bookmark, MetaModelService.Mode.RELAXED);
        return !beanSort.isEntity() && !beanSort.isViewModel();
    }

}
