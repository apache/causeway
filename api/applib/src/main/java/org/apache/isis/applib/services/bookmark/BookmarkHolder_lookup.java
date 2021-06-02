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
package org.apache.isis.applib.services.bookmark;

import javax.inject.Inject;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

/**
 * @since 1.x {@index}
 */
@Action(
        domainEvent = BookmarkHolder_lookup.ActionDomainEvent.class,
        semantics = SemanticsOf.SAFE
)
@ActionLayout(
        cssClassFa = "fa-bookmark"
)
@DomainObject(logicalTypeName = IsisModuleApplib.NAMESPACE + ".services.bookmark.BookmarkHolder_lookup")
@RequiredArgsConstructor
public class BookmarkHolder_lookup {

    private final BookmarkHolder bookmarkHolder;

    public static class ActionDomainEvent extends IsisModuleApplib.ActionDomainEvent<BookmarkHolder_lookup> {}

    public Object act() {
        return bookmarkService.lookup(bookmarkHolder).orElse(null);
    }

    // -- DEPENDENCIES

    @Inject private BookmarkService bookmarkService;

}
