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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.mixins.MixinConstants;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.core.commons.internal.base._Strings;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Mixin(method="prop") 
@RequiredArgsConstructor
public class Object_objectIdentifier {

    @Inject private BookmarkService bookmarkService;
    @Inject private MetaModelService mmService;
    
    private final Object holder;

    public static class ActionDomainEvent 
    extends org.apache.isis.applib.IsisModuleApplib.ActionDomainEvent<Object_objectIdentifier> {}

    @Action(
            domainEvent = ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(
            contributed = Contributed.AS_ASSOCIATION,
            hidden = Where.ALL_TABLES)
    @Property()
    @MemberOrder(name = MixinConstants.METADATA_LAYOUT_GROUPNAME, sequence = "700.2")
    public String prop() {
        val bookmark = bookmarkService.bookmarkForElseThrow(this.holder);
        val sort = mmService.sortOf(bookmark, MetaModelService.Mode.RELAXED);
        if(sort.isEntity()) {
            return bookmark.getIdentifier();    
        }
        return _Strings.ellipsifyAtStart(bookmark.getIdentifier(), 16, "…");
    }
    

}
