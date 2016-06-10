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
package org.apache.isis.viewer.wicket.viewer.services;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.hint.HintStore;

@Mixin
public class Object_clearHints {

    public static class ActionDomainEvent
            extends org.apache.isis.applib.services.eventbus.ActionDomainEvent<Object> {
    }

    private final Object object;

    public Object_clearHints(Object object) {
        this.object = object;
    }

    public static class ClearHintsDomainEvent extends ActionDomainEvent { }

    @Action(
            domainEvent = ClearHintsDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            cssClassFa = "fa-trash",
            position = ActionLayout.Position.PANEL_DROPDOWN
    )
    @MemberOrder(name = "Metadata", sequence = "400.1")
    public Object $$() {
        if (getHintStoreUsingWicketSession() != null) {
            final Bookmark bookmark = bookmarkService.bookmarkFor(object);
            getHintStoreUsingWicketSession().removeAll(bookmark);
        }
        return object;
    }

    public boolean hide$$() {
        return getHintStoreUsingWicketSession() == null;
    }

    private HintStoreUsingWicketSession getHintStoreUsingWicketSession() {
        return hintStore instanceof HintStoreUsingWicketSession
                ? (HintStoreUsingWicketSession) hintStore
                : null;
    }

    @javax.inject.Inject
    HintStore hintStore;

    @javax.inject.Inject
    BookmarkService bookmarkService;

}
