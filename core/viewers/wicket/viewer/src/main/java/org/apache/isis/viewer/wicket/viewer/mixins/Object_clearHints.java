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
package org.apache.isis.viewer.wicket.viewer.mixins;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.CommandPersistence;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.hint.HintStore;
import org.apache.isis.viewer.wicket.viewer.services.HintStoreUsingWicketSession;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Mixin(method="act")
@RequiredArgsConstructor
public class Object_clearHints {

    @Inject private HintStore hintStore;
    @Inject private BookmarkService bookmarkService;
    
    private final Object holder;

    public static class ActionDomainEvent 
    extends org.apache.isis.applib.events.domain.ActionDomainEvent<Object> {}

    @Action(
            domainEvent = ActionDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            commandPersistence = CommandPersistence.NOT_PERSISTED
            )
    @ActionLayout(
            contributed = Contributed.AS_ACTION,
            cssClassFa = "fa-circle-o",
            position = ActionLayout.Position.PANEL_DROPDOWN
            )
    @MemberOrder(name = "datanucleusIdLong", sequence = "400.1")
    public Object act() {
        if (getHintStoreUsingWicketSession() != null) {
            val bookmark = bookmarkService.bookmarkForElseThrow(holder);
            getHintStoreUsingWicketSession().removeAll(bookmark);
        }
        return holder;
    }

    public boolean hideAct() {
        return getHintStoreUsingWicketSession() == null;
    }

    private HintStoreUsingWicketSession getHintStoreUsingWicketSession() {
        return hintStore instanceof HintStoreUsingWicketSession
                ? (HintStoreUsingWicketSession) hintStore
                        : null;
    }

}
