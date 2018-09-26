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

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.BookmarkService.FieldResetPolicy;

@Mixin(method = "act")
public class BookmarkHolder_lookup {

    private final BookmarkHolder bookmarkHolder;

    public BookmarkHolder_lookup(final BookmarkHolder bookmarkHolder) {
        this.bookmarkHolder = bookmarkHolder;
    }

    public static class ActionDomainEvent extends IsisApplibModule.ActionDomainEvent<BookmarkHolder_lookup> { private static final long serialVersionUID = 1L; }

    @Action(
            domainEvent = ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(
            contributed = Contributed.AS_ACTION,
            cssClassFa = "fa-bookmark"
            )
    public Object act() {
        return bookmarkService.lookup(bookmarkHolder, FieldResetPolicy.RESET);
    }



    @javax.inject.Inject
    private BookmarkService bookmarkService;

}
